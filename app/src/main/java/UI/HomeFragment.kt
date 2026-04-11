package UI

import Model.Add.Expense
import Model.Vehicle.Expense as VehicleExpense
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.spendly.R
import com.example.spendly.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import viewModel.Add.ExpenseViewModel
import viewModel.Add.ExpenseViewModelFact
import viewModel.Profile.ProfileViewModel
import viewModel.Vehicle.VehicleViewModel
import viewModel.Vehicle.VehicleViewModelFactory
import Repository.Add.ExpenseRepo
import Repository.Profile.ProfileRepository
import Repository.Vehicle.VehicleRepository
import Util.Home.CategoryAdapter
import Util.Home.CategoryUIModel
import Util.Home.TransactionAdapter
import Util.Home.TransactionUIModel
import viewModel.Profile.ProfileViewModelFact

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding

    private lateinit var expenseViewModel: ExpenseViewModel
    private lateinit var vehicleViewModel: VehicleViewModel
    private lateinit var profileViewModel: ProfileViewModel

    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var transactionAdapter: TransactionAdapter

    private var dailyTotal = 0.0
    private var vehicleTotal = 0.0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        setupViewModels()
        setupRecycler()

        observeProfile()
        observeTotal()
        observeCategory()

        loadRecent()

        // API calls
        expenseViewModel.getStats()
        expenseViewModel.getChart(false)
        vehicleViewModel.getVehicles()
        profileViewModel.loadProfile()
    }

    // ---------------- SETUP ----------------

    private fun setupViewModels() {
        val expenseRepo = ExpenseRepo()
        val vehicleRepo = VehicleRepository()
        val profileRepo = ProfileRepository()

        expenseViewModel = ViewModelProvider(this, ExpenseViewModelFact(expenseRepo))[ExpenseViewModel::class.java]

        vehicleViewModel = ViewModelProvider(
            this,
            VehicleViewModelFactory(vehicleRepo)
        )[VehicleViewModel::class.java]

        profileViewModel = ViewModelProvider(
            this,
            ProfileViewModelFact(profileRepo)
        )[ProfileViewModel::class.java]
    }

    private fun setupRecycler() {
        categoryAdapter = CategoryAdapter()
        transactionAdapter = TransactionAdapter()

        binding.rvCategories.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        binding.rvTransactions.layoutManager = LinearLayoutManager(requireContext())

        binding.rvCategories.adapter = categoryAdapter
        binding.rvTransactions.adapter = transactionAdapter
    }

    // ---------------- PROFILE ----------------

    private fun observeProfile() {
        profileViewModel.profileState.observe(viewLifecycleOwner) { result ->
            result.onSuccess { profile ->

                binding.headerLayout.tvGreetingName.text = profile.name
                binding.headerLayout.tvGreetingSub.text=getGreeting()

                Glide.with(requireContext())
                    .load(profile.profileImage)
                    .placeholder(R.drawable.ic_profile)
                    .circleCrop()
                    .into(binding.headerLayout.ivProfile)
            }
        }
    }

    // ---------------- TOTAL ----------------

    private fun observeTotal() {

        expenseViewModel.statsState.observe(viewLifecycleOwner) {
            it.onSuccess { stats ->
                dailyTotal = stats.total
                updateTotal()
            }
        }

        vehicleViewModel.vehicleListState.observe(viewLifecycleOwner) {
            it.onSuccess { list ->
                vehicleTotal = list.sumOf { v -> v.total_this_month }
                updateTotal()
            }
        }
    }

    private fun updateTotal() {
        val total = dailyTotal + vehicleTotal

        binding.totalLayout.tvTotalAmount
            .text = "₹${total.toInt()}"
    }

    // ---------------- CATEGORY ----------------

    private fun observeCategory() {

        expenseViewModel.chartState.observe(viewLifecycleOwner) { result ->
            result.onSuccess { dailyMap ->

                loadVehicleCategory { vehicleMap ->

                    val mergedMap = mutableMapOf<String, Double>()

                    // daily
                    for ((key, value) in dailyMap) {
                        mergedMap[key] = (mergedMap[key] ?: 0.0) + value
                    }

                    // vehicle
                    for ((key, value) in vehicleMap) {
                        mergedMap[key] = (mergedMap[key] ?: 0.0) + value
                    }

                    val list = mergedMap.map {
                        CategoryUIModel(it.key, it.value)
                    }.sortedByDescending { it.amount }

                    categoryAdapter.submitList(list)
                }
            }
        }
    }

    // ---------------- RECENT (MERGED) ----------------

    private fun loadRecent() {

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance()
            .collection("Users")
            .document(uid)
            .collection("Expenses")
            .get()
            .addOnSuccessListener { dailySnap ->

                val dailyList = dailySnap.toObjects(Expense::class.java)

                loadVehicleExpenses { vehicleList ->

                    val finalList = mergeTransactions(dailyList, vehicleList)

                    transactionAdapter.submitList(finalList.take(10))
                }
            }
    }
    private fun loadVehicleExpenses(onResult: (List<VehicleExpense>) -> Unit) {

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance()
            .collection("Users")
            .document(uid)
            .collection("VehicleExpenses")
            .get()
            .addOnSuccessListener { snapshot ->
                val list = snapshot.toObjects(VehicleExpense::class.java)
                onResult(list)
            }
    }
    private fun getGreeting(): String {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)

        return when (hour) {
            in 0..11 -> "Good morning 👋"
            in 12..16 -> "Good afternoon ☀️"
            in 17..20 -> "Good evening 🌇"
            else -> "Good night 🌙"
        }
    }

    private fun mergeTransactions(
        daily: List<Expense>,
        vehicle: List<VehicleExpense>
    ): List<TransactionUIModel> {

        val dailyUI = daily.map {
            TransactionUIModel(
                id = it.id,
                title = it.note.ifEmpty { it.type },
                type = it.type,
                amount = it.amount,
                date = it.date
            )
        }

        val vehicleUI = vehicle.map {
            TransactionUIModel(
                id = it.id,
                title = it.note.ifEmpty { it.type },
                type = it.type,
                amount = it.amount,
                date = it.date
            )
        }

        return (dailyUI + vehicleUI)
            .sortedByDescending { it.date }
    }
    private fun loadVehicleCategory(onResult: (Map<String, Double>) -> Unit) {

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance()
            .collection("Users")
            .document(uid)
            .collection("VehicleExpenses")
            .get()
            .addOnSuccessListener { snapshot ->

                val map = mutableMapOf<String, Double>()

                for (doc in snapshot.documents) {
                    val type = doc.getString("type")?.lowercase()?.trim() ?: continue
                    val amount = doc.getDouble("amount") ?: 0.0

                    map[type] = (map[type] ?: 0.0) + amount
                }

                onResult(map)
            }
    }
}