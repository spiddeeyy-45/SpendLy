package UI

import Model.Add.ExpenseRequest
import Model.Add.ExpenseStats
import Repository.Add.ExpenseRepo
import android.app.DatePickerDialog
import android.graphics.Color
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
    private var isYearly = false

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
        binding.togglebtn.setOnClickListener {
            isYearly = !isYearly
            binding.togglebtn.text = if (isYearly) "Yearly" else "Monthly"
            viewModel.getChart(isYearly)
        }
        viewModel.getStats()
        viewModel.getChart(isYearly)
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
                viewModel.getChart(isYearly)

            }

            result.onFailure {
                Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
            }
        }
        viewModel.statsState.observe(viewLifecycleOwner) { result ->
            result.onSuccess { stats ->

                // ---------- TOTAL ----------
                binding.cardTotalSpend.findViewById<TextView>(R.id.tvStatValue).text =
                    "₹${stats.total.toInt()}"

                val totalTrend = binding.cardTotalSpend.findViewById<TextView>(R.id.tvStatTrend)
                val totalDiff = stats.total - stats.totalLastMonth

                if (totalDiff > 0) {
                    totalTrend.background = resources.getDrawable(R.drawable.bg_pill_down, null)
                    totalTrend.text = "↑ ₹${totalDiff.toInt()} vs last month"
                    totalTrend.setTextColor(Color.parseColor("#EF4444")) // red
                } else if (totalDiff < 0) {
                    totalTrend.background = resources.getDrawable(R.drawable.bg_pill_up, null)
                    totalTrend.text = "↓ ₹${kotlin.math.abs(totalDiff).toInt()} vs last month"
                    totalTrend.setTextColor(Color.parseColor("#34D399")) // green
                } else {
                    totalTrend.text = "No change"
                }

                // ---------- GROCERY ----------
                binding.cardGrocerySpend.findViewById<TextView>(R.id.tvStatValue).text =
                    "₹${stats.grocery.toInt()}"

                val groceryTrend = binding.cardGrocerySpend.findViewById<TextView>(R.id.tvStatTrend)
                val groceryDiff = stats.grocery - stats.groceryLastMonth

                if (groceryDiff > 0) {
                    groceryTrend.background = resources.getDrawable(R.drawable.bg_pill_down, null)
                    groceryTrend.text = "↑ ₹${groceryDiff.toInt()} vs last month"
                    groceryTrend.setTextColor(Color.parseColor("#EF4444"))
                } else if (groceryDiff < 0) {
                    groceryTrend.background = resources.getDrawable(R.drawable.bg_pill_up, null)
                    groceryTrend.text = "↓ ₹${kotlin.math.abs(groceryDiff).toInt()} vs last month"
                    groceryTrend.setTextColor(Color.parseColor("#34D399"))
                } else {
                    groceryTrend.text = "No change"
                }

                // ---------- SHOPPING ----------
                binding.cardShoppingSpend.findViewById<TextView>(R.id.tvStatValue).text =
                    "₹${stats.shopping.toInt()}"

                val shoppingTrend = binding.cardShoppingSpend.findViewById<TextView>(R.id.tvStatTrend)
                val shoppingDiff = stats.shopping - stats.shoppingLastMonth

                if (shoppingDiff > 0) {
                    shoppingTrend.background = resources.getDrawable(R.drawable.bg_pill_down, null)
                    shoppingTrend.text = "↑ ₹${shoppingDiff.toInt()} vs last month"
                    shoppingTrend.setTextColor(Color.parseColor("#EF4444"))
                } else if (shoppingDiff < 0) {
                    shoppingTrend.background = resources.getDrawable(R.drawable.bg_pill_up, null)
                    shoppingTrend.text = "↓ ₹${kotlin.math.abs(shoppingDiff).toInt()} vs last month"
                    shoppingTrend.setTextColor(Color.parseColor("#34D399"))
                } else {
                    shoppingTrend.text = "No change"
                }

                // ---------- FOOD ----------
                binding.cardFoodSpend.findViewById<TextView>(R.id.tvStatValue).text =
                    "₹${stats.food.toInt()}"

                val foodTrend = binding.cardFoodSpend.findViewById<TextView>(R.id.tvStatTrend)
                val foodDiff = stats.food - stats.foodLastMonth

                if (foodDiff > 0) {
                    foodTrend.background = resources.getDrawable(R.drawable.bg_pill_down, null)
                    foodTrend.text = "↑ ₹${foodDiff.toInt()} vs last month"
                    foodTrend.setTextColor(Color.parseColor("#EF4444"))
                } else if (foodDiff < 0) {
                    foodTrend.background = resources.getDrawable(R.drawable.bg_pill_up, null)
                    foodTrend.text = "↓ ₹${kotlin.math.abs(foodDiff).toInt()} vs last month"
                    foodTrend.setTextColor(Color.parseColor("#34D399"))
                } else {
                    foodTrend.text = "No change"
                }
            }
        }
        viewModel.chartState.observe(viewLifecycleOwner){ result ->
            result.onSuccess {
                setupChart(it)
            }
            result.onFailure {
                binding.chartContainer.removeAllViews()
            }

        }
    }
    private fun formatDate(time: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return sdf.format(Date(time))
    }
    private fun setupChart(data: Map<String, Double>) {

        val container = binding.chartContainer
        container.removeAllViews()

        if (data.isEmpty()) {
            val tv = TextView(requireContext())
            tv.text = "No expenses yet"
            tv.setTextColor(Color.GRAY)
            container.addView(tv)
            return
        }

        val maxAmount = data.values.maxOrNull() ?: 1.0

        data.forEach { (type, amount) ->

            val view = layoutInflater.inflate(R.layout.item_expense_chart, container, false)

            val tvLabel = view.findViewById<TextView>(R.id.tvChartLabel)
            val tvAmount = view.findViewById<TextView>(R.id.tvChartAmount)
            val bar = view.findViewById<View>(R.id.viewBar)

            tvLabel.text = type.replaceFirstChar { it.uppercase() }
            tvAmount.text = "₹${amount.toInt()}"

            val percentage = amount / maxAmount

            bar.post {
                bar.scaleX = 0f
                bar.animate().scaleX(1f).setDuration(600).start()
                val fullWidth = container.width
                val newWidth = (fullWidth * percentage).toInt()

                val params = bar.layoutParams
                params.width = newWidth
                bar.layoutParams = params
            }

            when (type) {
                "food" -> bar.setBackgroundResource(R.drawable.bg_chart_bar_fuel)
                "grocery" -> bar.setBackgroundResource(R.drawable.bg_chartbar_oil)
                "medical"->bar.setBackgroundResource(R.drawable.bg_chartbar_brake)
                "shopping"->bar.setBackgroundResource(R.drawable.bg_chartbar_gear)
                "trip" -> bar.setBackgroundResource(R.drawable.bg_chartbar_service)
                "stocks" -> bar.setBackgroundResource(R.drawable.bg_chartbar_tyre)
                "subscription" -> bar.setBackgroundResource(R.drawable.bg_chartbar_insurance)
                else -> bar.setBackgroundResource(R.drawable.bg_chartbar_others)
            }

            container.addView(view)
        }
    }
}