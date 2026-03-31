package UI

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import Model.Vehicle.VehicleExpApiInstance
import Model.Vehicle.VehicleExpenseRequest
import Repository.Vehicle.VehicleExpRepo
import android.content.Context
import android.util.Log
import android.widget.TextView
import com.example.spendly.R
import com.example.spendly.databinding.FragmentVehicleBinding
import util.vehicleAdapter.addVehicleAdapter
import viewModel.Vehicle.VehicleExpViewModel
import viewModel.Vehicle.VehicleExpViewModelFact

class VehicleFragment : Fragment() {

    private var _binding: FragmentVehicleBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: VehicleExpViewModel
    private lateinit var adapter: addVehicleAdapter

    private var selectedVehicleId: String? = null

    private var isDataLoaded = false
    private lateinit var token: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentVehicleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val prefs = requireContext().getSharedPreferences("app", Context.MODE_PRIVATE)
        Log.d("TOKEN_DEBUG", prefs.getString("token", "NULL") ?: "NULL")
        token = "Bearer ${prefs.getString("token", "")}"

        setupViewModel()
        setupRecyclerView()
        setupClicks()
        observeViewModel()

        viewModel.getVehicles(token)
    }

    private fun setupViewModel() {
        val api = VehicleExpApiInstance.api
        val repo = VehicleExpRepo(api)

        viewModel = ViewModelProvider(
            this,
            VehicleExpViewModelFact(repo)
        )[VehicleExpViewModel::class.java]
    }

    private fun setupRecyclerView() {

        adapter = addVehicleAdapter { vehicle ->
            selectedVehicleId = vehicle.id
            Log.d("DEBUG", "VehicleId: $selectedVehicleId")
            viewModel.getStats(token, vehicle.id)
        }

        binding.rvVehicles.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

       _binding?.let { binding ->
           binding.rvVehicles.adapter = adapter
       }
    }

    private fun observeViewModel() {
        viewModel.vehicleState.observe(viewLifecycleOwner) { result ->
            if (_binding == null) return@observe

            result.onSuccess { vehicles ->

                if (vehicles.isEmpty()) {
                    Toast.makeText(context, "Add a vehicle first", Toast.LENGTH_SHORT).show()
                    return@onSuccess
                }
                adapter.updateData(vehicles)

                // auto select first
                selectedVehicleId = vehicles[0].id
                viewModel.getStats(token, selectedVehicleId!!)
            }

            result.onFailure {
                Toast.makeText(context, "Failed to load vehicles", Toast.LENGTH_SHORT).show()
            }
        }
        viewModel.statsState.observe(viewLifecycleOwner) { result ->

            result.onSuccess { stats ->

                // ================= TOTAL =================
                val totalCard = binding.cardTotalSpend

                totalCard.findViewById<TextView>(R.id.tvStatValue)
                    .text = "₹${stats.total.toInt()}"

                val totalTrend = totalCard.findViewById<TextView>(R.id.tvStatTrend)

                if (stats.total > stats.totalLastMonth) {
                    totalTrend.text = "↑ vs last month"
                } else {
                    totalTrend.text = "↓ vs last month"
                }


                // ================= FUEL =================
                val fuelCard = binding.cardFuelSpend

                fuelCard.findViewById<TextView>(R.id.tvStatValue)
                    .text = "₹${stats.fuel.toInt()}"

                val fuelTrend = fuelCard.findViewById<TextView>(R.id.tvStatMonth)

                if (stats.fuelToday > 0) {
                    fuelTrend.text = "↑ Today ₹${stats.fuelToday.toInt()}"
                } else {
                    fuelTrend.text = "No expense today"
                }


                // ================= SERVICE =================
                val serviceCard = binding.cardServiceSpend

                serviceCard.findViewById<TextView>(R.id.tvStatValue)
                    .text = "₹${stats.service.toInt()}"

                val serviceTrend = serviceCard.findViewById<TextView>(R.id.tvStatLastDate)

                if (stats.service > stats.serviceLastMonth) {
                    serviceTrend.text = "↑ vs last month"
                } else {
                    serviceTrend.text = "↓ vs last month"
                }


                // ================= AVG =================
                val avgCard = binding.cardAvgSpend

                avgCard.findViewById<TextView>(R.id.tvStatValue)
                    .text = "₹${stats.avgPerDay.toInt()}"

                val avgTrend = avgCard.findViewById<TextView>(R.id.tvStatTrend)

                if (stats.avgPerDay > 500) {
                    avgTrend.text = "↑ High spending"
                } else {
                    avgTrend.text = "↓ Under control"
                }
            }

            result.onFailure {
                Toast.makeText(context, "Stats failed", Toast.LENGTH_SHORT).show()
            }
        }

        // 🔥 EXPENSE RESPONSE
        viewModel.expenseState.observe(viewLifecycleOwner) { result ->

            result.onSuccess {
                Log.d("SUCCESS", it.toString())

                Toast.makeText(context, "Expense Added ✅", Toast.LENGTH_SHORT).show()

                if (!isDataLoaded) {
                    Log.d("DEBUG", "Token: $token")
                    viewModel.getVehicles(token)
                    isDataLoaded = true
                }
            }

            result.onFailure {
                Log.e("ERROR", it.message ?: "Unknown error")
                Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupClicks() {

        val map = mapOf(
            binding.cardFuel to "fuel",
            binding.cardOil to "oil",
            binding.cardService to "service",
            binding.cardBrake to "brake",
            binding.cardGear to "gear",
            binding.cardAccessories to "accessories",
            binding.cardTyre to "tyre",
            binding.cardInsurance to "insurance",
            binding.cardOther to "other"
        )

        map.forEach { (view, type) ->
            view.setOnClickListener {
                openExpenseDialog(type)
            }
        }
    }

    private fun openExpenseDialog(type: String) {

        val input = EditText(requireContext())
        input.hint = "Enter amount"

        AlertDialog.Builder(requireContext())
            .setTitle("Add $type expense")
            .setView(input)
            .setPositiveButton("Add") { _, _ ->

                val amount = input.text.toString().toDoubleOrNull()


                if (amount != null && selectedVehicleId != null) {

                    val request = VehicleExpenseRequest(
                        vehicle_id = selectedVehicleId!!,
                        type = type,
                        amount = amount
                    )

                    viewModel.VaddExpense(token, request)

                } else {
                    Toast.makeText(
                        context,
                        "Select vehicle & valid amount",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
        viewModel.vehicleState.removeObservers(viewLifecycleOwner)
        viewModel.expenseState.removeObservers(viewLifecycleOwner)
    }
}