package UI

import Model.Add.ExpenseRequest
import Repository.Add.ExpenseRepo
import android.app.DatePickerDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.spendly.R
import com.example.spendly.databinding.FragmentAddBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import viewModel.Add.ExpenseViewModel
import viewModel.Add.ExpenseViewModelFact
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class AddFragment : Fragment() {

    private var _binding: FragmentAddBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ExpenseViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupViewModel()
        setupClicks()
        observeVM()

        viewModel.getStats()
    }

    private fun setupViewModel() {
        val repo = ExpenseRepo()
        viewModel = ViewModelProvider(
            this,
            ExpenseViewModelFact(repo)
        )[ExpenseViewModel::class.java]
    }
    private fun setupClicks() {

        val map = mapOf(
            binding.cardFood to "food",
            binding.cardGrocery to "grocery",
            binding.cardShopping to "shopping",
            binding.cardMedical to "medical",
            binding.cardTrip to "trip",
            binding.cardSubscription to "subscription",
            binding.cardStocks to "stocks",
            binding.cardOther to "other"
        )

        map.forEach { (view, type) ->
            view.setOnClickListener {
                openExpenseBottomSheet(type)
            }
        }
    }
    private fun openExpenseBottomSheet(type: String) {

        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.dialog_addexpense, null)

        dialog.setContentView(view)
        dialog.show()

        // -------- VIEWS -------- //
        val etAmount = view.findViewById<EditText>(R.id.etAmount)
        val etNote = view.findViewById<EditText>(R.id.etNote)
        val tvDate = view.findViewById<TextView>(R.id.tvSelectedDate)
        val btnSave = view.findViewById<TextView>(R.id.btnSaveExpense)
        val btnClose = view.findViewById<View>(R.id.btnClose)

        val tvCategory = view.findViewById<TextView>(R.id.tvCategoryName)
        val imgCategory = view.findViewById<ImageView>(R.id.imgCategoryIcon)
        val iconBg = view.findViewById<View>(R.id.ivCategoryIcon)

        // -------- CATEGORY UI -------- //
        tvCategory.text = type.replaceFirstChar { it.uppercase() }

        when (type) {
            "food" -> {
                imgCategory.setImageResource(R.drawable.ic_food)
                iconBg.setBackgroundResource(R.drawable.bg_expense_icon_fuel)
            }
            "grocery" -> {
                imgCategory.setImageResource(R.drawable.ic_grocery)
                iconBg.setBackgroundResource(R.drawable.bg_oil)
            }
            "shopping" -> {
                imgCategory.setImageResource(R.drawable.ic_shopping)
                iconBg.setBackgroundResource(R.drawable.bg_expense_gear)
            }
            else -> {
                imgCategory.setImageResource(R.drawable.ic_add)
                iconBg.setBackgroundResource(R.drawable.bg_glass_card)
            }
        }

        // -------- DATE -------- //
        var selectedDate = System.currentTimeMillis()
        tvDate.text = formatDate(selectedDate)

        view.findViewById<View>(R.id.layoutDatePicker).setOnClickListener {
            val cal = Calendar.getInstance()

            DatePickerDialog(
                requireContext(),
                { _, y, m, d ->
                    val c = Calendar.getInstance()
                    c.set(y, m, d)
                    selectedDate = c.timeInMillis
                    tvDate.text = formatDate(selectedDate)
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // -------- CLOSE -------- //
        btnClose.setOnClickListener { dialog.dismiss() }

        // -------- SAVE -------- //
        btnSave.setOnClickListener {

            val amount = etAmount.text.toString().toDoubleOrNull()
            val note = etNote.text.toString()

            if (amount != null) {

                val request = ExpenseRequest(
                    type = type,
                    amount = amount,
                    note = note,
                    date = selectedDate
                )

                viewModel.addExpense(request)
                dialog.dismiss()

            } else {
                Toast.makeText(context, "Enter valid amount", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun observeVM() {

        viewModel.expenseState.observe(viewLifecycleOwner) { result ->
            result.onSuccess {
                Toast.makeText(context, "Expense Added", Toast.LENGTH_SHORT).show()
                viewModel.getStats()
            }

            result.onFailure {
                Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.statsState.observe(viewLifecycleOwner) { result ->

            result.onSuccess { stats ->

                // TOTAL
                binding.cardTotalSpend
                    .findViewById<TextView>(R.id.tvStatValue)
                    .text = "₹${stats.total.toInt()}"

                // GROCERY
                binding.cardGrocerySpend
                    .findViewById<TextView>(R.id.tvStatValue)
                    .text = "₹${stats.grocery.toInt()}"

                // SHOPPING
                binding.cardShoppingSpend
                    .findViewById<TextView>(R.id.tvStatValue)
                    .text = "₹${stats.shopping.toInt()}"

                // FOOD
                binding.cardAvgSpend
                    .findViewById<TextView>(R.id.tvStatValue)
                    .text = "₹${stats.food.toInt()}"
            }
        }
    }
    private fun formatDate(time: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return sdf.format(Date(time))
    }
}