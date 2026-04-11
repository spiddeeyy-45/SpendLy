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
import Model.Vehicle.VehicleExpenseRequest
import Repository.Vehicle.VehicleExpRepo
import Repository.Vehicle.VehicleRepository
import Util.vehicleAdapter.VehicleSelectAdapter
import android.graphics.Color
import android.util.Log
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.spendly.R
import com.example.spendly.databinding.FragmentVehicleBinding
import Util.vehicleAdapter.addVehicleAdapter
import android.widget.ImageView
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import viewModel.Vehicle.VehicleExpViewModel
import viewModel.Vehicle.VehicleExpViewModelFact
import viewModel.Vehicle.VehicleViewModel
import viewModel.Vehicle.VehicleViewModelFactory

class VehicleFragment : Fragment() {

    private var _binding: FragmentVehicleBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: VehicleExpViewModel
    private var selectedVehicleName: String? = null
    private lateinit var adapter: addVehicleAdapter
    private lateinit var vehicleViewModel: VehicleViewModel
    private var isYearly = false
    private var selectedVehicleId: String? = null


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
        setupViewModel()
        setupVehicleVM()
        setupRecyclerView()
        setupClicks()
        observeViewModel()

        binding.togglebtn.setOnClickListener {
            isYearly = !isYearly
            binding.togglebtn.text = if (isYearly) "Yearly" else "Monthly"
            selectedVehicleId?.let {
                viewModel.getChart(it, isYearly)
            }
        }


        viewModel.getVehicles()
        vehicleViewModel.getVehicles()
    }

    private fun setupViewModel() {
        val repo = VehicleExpRepo()
        viewModel = ViewModelProvider(
            this,
            VehicleExpViewModelFact(repo)
        )[VehicleExpViewModel::class.java]
    }
    private fun setupRecyclerView() {

        adapter = addVehicleAdapter(
            onVehicleClick = { vehicle ->
                selectedVehicleId = vehicle.id
                selectedVehicleName = "${vehicle.company} ${vehicle.model}"
                viewModel.getStats(vehicle.id)
                selectedVehicleId?.let {
                    viewModel.getChart(it, isYearly)
                }
            },
            onAddVehicleClick = {
                Log.d("ADD_CLICK", "Add clicked")
                openAddVehicleDialog()
            }
        )

        val layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL,
            false
        )

        binding.rvVehicles.layoutManager = layoutManager
        binding.rvVehicles.adapter = adapter

        binding.rvVehicles.apply {
            setHasFixedSize(true)
            clipToPadding = false
            clipChildren = false
            setPadding(0, 0, 80, 0)

        }
        val snapHelper = androidx.recyclerview.widget.LinearSnapHelper()
        snapHelper.attachToRecyclerView(binding.rvVehicles)
        binding.rvVehicles.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val centerView = snapHelper.findSnapView(layoutManager)
                    val pos = centerView?.let { layoutManager.getPosition(it) }

                    if (pos != null && pos != RecyclerView.NO_POSITION) {
                        adapter.setSelectedPosition(pos)
                    }
                }
            }
        })
    }
    private fun observeViewModel() {
        vehicleViewModel.addVehicleState.observe(viewLifecycleOwner) { result ->
            result.onSuccess {
                Toast.makeText(context, "Vehicle Added", Toast.LENGTH_SHORT).show()
                viewModel.getVehicles()
            }

            result.onFailure {
                if (it.message != "Loading...") {
                    Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
        viewModel.vehicleState.observe(viewLifecycleOwner) { result ->
            if (_binding == null) return@observe

            result.onSuccess { vehicles ->


                adapter.updateData(vehicles)
                if (vehicles.isEmpty()) {
                    Toast.makeText(context, "No vehicles yet, add one", Toast.LENGTH_SHORT).show()
                }

                // auto select first
                if (vehicles.isNotEmpty()) {
                    selectedVehicleId = vehicles[0].id
                    viewModel.getStats(selectedVehicleId!!)
                }
                selectedVehicleId?.let {
                    viewModel.getChart(it, isYearly)
                }
            }

            result.onFailure {
                Toast.makeText(context, "Failed to load vehicles", Toast.LENGTH_SHORT).show()
            }
        }
        viewModel.statsState.observe(viewLifecycleOwner) { result ->

            result.onSuccess { stats ->
                selectedVehicleId?.let { adapter.updateVehicleAmount(it,stats.total) }

                // ================= TOTAL =================
                val totalCard = binding.cardTotalSpend

                totalCard.findViewById<TextView>(R.id.tvStatValue)
                    .text = "₹${stats.total.toInt()}"

                val totalTrend = totalCard.findViewById<TextView>(R.id.tvStatTrend)

                if (stats.total > stats.totalLastMonth) {
                    totalTrend.background = resources.getDrawable(R.drawable.bg_pill_down, null)
                    totalTrend.text = "↑ vs last month"
                    totalTrend.setTextColor(Color.parseColor("#EF4444"))
                } else {
                    totalTrend.background=resources.getDrawable(R.drawable.bg_pill_up, null)
                    totalTrend.text = "↓ vs last month"
                    totalTrend.setTextColor(Color.parseColor("#34D399"))
                }


                // ================= FUEL =================
                val fuelCard = binding.cardFuelSpend

                fuelCard.findViewById<TextView>(R.id.tvStatValue)
                    .text = "₹${stats.fuel.toInt()}"

                val fuelTrend = fuelCard.findViewById<TextView>(R.id.tvStatMonth)

                if (stats.fuelToday > 0) {
                    fuelTrend.background=resources.getDrawable(R.drawable.bg_pill_down, null)
                    fuelTrend.text = "↑ Today ₹${stats.fuelToday.toInt()}"
                    fuelTrend.setTextColor(Color.parseColor("#EF4444"))
                } else {
                    fuelTrend.background=resources.getDrawable(R.drawable.bg_pill_up, null)
                    fuelTrend.text = "No expense today"
                    fuelTrend.setTextColor(Color.parseColor("#34D399"))
                }


                // ================= SERVICE =================
                val serviceCard = binding.cardServiceSpend

                serviceCard.findViewById<TextView>(R.id.tvStatValue)
                    .text = "₹${stats.service.toInt()}"

                val serviceTrend = serviceCard.findViewById<TextView>(R.id.tvStatLastDate)

                if (stats.service > stats.serviceLastMonth) {
                    serviceTrend.background=resources.getDrawable(R.drawable.bg_pill_down, null)
                    serviceTrend.text = "↑ vs last month"
                    serviceTrend.setTextColor(Color.parseColor("#EF4444"))
                } else {
                    serviceTrend.background=resources.getDrawable(R.drawable.bg_pill_up, null)
                    serviceTrend.text = "↓ vs last month"
                    serviceTrend.setTextColor(Color.parseColor("#34D399"))
                }


                // ================= AVG =================
                val avgCard = binding.cardAvgSpend

                avgCard.findViewById<TextView>(R.id.tvStatValue)
                    .text = "₹${stats.avgPerDay.toInt()}"

                val avgTrend = avgCard.findViewById<TextView>(R.id.tvStatTrend)

                if (stats.avgPerDay > 500) {
                    avgTrend.background=resources.getDrawable(R.drawable.bg_pill_down, null)
                    avgTrend.text = "↑ High spending"
                    avgTrend.setTextColor(Color.parseColor("#EF4444"))
                } else {
                    avgTrend.background=resources.getDrawable(R.drawable.bg_pill_up, null)
                    avgTrend.text = "↓ Under control"
                    avgTrend.setTextColor(Color.parseColor("#34D399"))
                }
            }

            result.onFailure {
                Toast.makeText(context, "Stats failed", Toast.LENGTH_SHORT).show()
            }
        }
        // EXPENSE RESPONSE
        viewModel.expenseState.observe(viewLifecycleOwner) { result ->

            result.onSuccess {
                Log.d("SUCCESS", it.toString())

                Toast.makeText(context, "Expense Added", Toast.LENGTH_SHORT).show()
                selectedVehicleId?.let { viewModel.getStats(it) }
                selectedVehicleId?.let {
                    viewModel.getChart(it, isYearly)
                }
            }

            result.onFailure {
                Log.e("ERROR", it.message ?: "Unknown error")
                Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
        viewModel.chartState.observe(viewLifecycleOwner) { result ->
            result.onSuccess {
                setupChart(it)
            }.onFailure {
                binding.chartContainer.removeAllViews()
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
                openExpenseBottomSheet(type)
            }
        }
    }
    private fun openExpenseBottomSheet(type: String) {

        val dialog = com.google.android.material.bottomsheet.BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.dialog_vehicle_add_expense, null)

        dialog.setContentView(view)
        dialog.show()

        // ---------------- VIEWS ---------------- //
        val etAmount = view.findViewById<EditText>(R.id.etAmount)
        val etNote = view.findViewById<EditText>(R.id.etNote)
        val tvDate = view.findViewById<TextView>(R.id.tvSelectedDate)
        val tvVehicle = view.findViewById<TextView>(R.id.tvSelectedVehicle)
        val btnSave = view.findViewById<TextView>(R.id.btnSaveExpense)
        val btnClose = view.findViewById<View>(R.id.btnClose)
        val tvCategory = view.findViewById<TextView>(R.id.tvCategoryName)
        val imgCategory = view.findViewById<ImageView>(R.id.imgCategoryIcon)
        val iconBg = view.findViewById<View>(R.id.ivCategoryIcon)

        // ---------------- VEHICLE AUTO SELECT ---------------- //
        tvVehicle.text = selectedVehicleName ?: "Select Vehicle"
        view.findViewById<View>(R.id.layoutVehiclePicker).setOnClickListener {
            showVehicleSelectionBottomSheet(tvVehicle)
        }
        tvCategory.text = type.replaceFirstChar { it.uppercase() }
        when (type) {
            "fuel" -> {
                imgCategory.setImageResource(R.drawable.ic_fuel)
                iconBg.setBackgroundResource(R.drawable.bg_expense_icon_fuel)
            }
            "service" -> {
                imgCategory.setImageResource(R.drawable.ic_service)
                iconBg.setBackgroundResource(R.drawable.bg_service)
            }
            "oil" -> {
                imgCategory.setImageResource(R.drawable.ic_oil)
                iconBg.setBackgroundResource(R.drawable.bg_oil)
            }
            "tyre" -> {
                imgCategory.setImageResource(R.drawable.ic_tyre)
                iconBg.setBackgroundResource(R.drawable.bg_service)
            }
            "insurance" -> {
                imgCategory.setImageResource(R.drawable.ic_insurance)
                iconBg.setBackgroundResource(R.drawable.bg_pill_down)
            }
            else -> {
                imgCategory.setImageResource(R.drawable.ic_add)
                iconBg.setBackgroundResource(R.drawable.bg_glass_card)
            }
        }

        // ---------------- DATE DEFAULT ---------------- //
        var selectedDate = System.currentTimeMillis()
        tvDate.text = formatDate(selectedDate)

        // DATE PICKER
        view.findViewById<View>(R.id.layoutDatePicker).setOnClickListener {

            val cal = java.util.Calendar.getInstance()

            val dp = android.app.DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    val c = java.util.Calendar.getInstance()
                    c.set(year, month, day)
                    selectedDate = c.timeInMillis
                    tvDate.text = formatDate(selectedDate)
                },
                cal.get(java.util.Calendar.YEAR),
                cal.get(java.util.Calendar.MONTH),
                cal.get(java.util.Calendar.DAY_OF_MONTH)
            )

            dp.show()
        }

        // CLOSE
        btnClose.setOnClickListener { dialog.dismiss() }

        // ---------------- SAVE ---------------- //
        btnSave.setOnClickListener {

            val amount = etAmount.text.toString().toDoubleOrNull()
            val note = etNote.text.toString()

            if (amount != null && selectedVehicleId != null) {

                val request = VehicleExpenseRequest(
                    vehicle_id = selectedVehicleId!!,
                    type = type,
                    amount = amount,
                    date = selectedDate,
                    note = note
                )

                viewModel.addExpense(request)
                dialog.dismiss()

            } else {
                Toast.makeText(context, "Enter valid amount", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }
    private fun setupVehicleVM() {
        val repo = VehicleRepository()

        vehicleViewModel = ViewModelProvider(
            this,
            VehicleViewModelFactory(repo)
        )[VehicleViewModel::class.java]
    }
    private fun openAddVehicleDialog() {

        val dialog = com.google.android.material.bottomsheet.BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.dialog_add_vehicle, null)

        dialog.setContentView(view)
        dialog.show()

        // Views
        val etCompany = view.findViewById<EditText>(R.id.etCompany)
        val etModel = view.findViewById<EditText>(R.id.etModel)
        val etPlate = view.findViewById<EditText>(R.id.etNumberPlate)
        val etYear = view.findViewById<EditText>(R.id.etYear)

        val btnAdd = view.findViewById<TextView>(R.id.btnAddVehicle)
        val btnClose = view.findViewById<View>(R.id.btnClose)
        val tvError = view.findViewById<TextView>(R.id.tvAddVehicleError)

        val btnBike = view.findViewById<View>(R.id.btnSelectBike)
        val btnCar = view.findViewById<View>(R.id.btnSelectCar)

        var selectedType = "bike"

        // TYPE SWITCH
        btnBike.setOnClickListener {
            selectedType = "bike"
            btnBike.setBackgroundResource(R.drawable.bg_vehicle_type_selected)
            btnCar.setBackgroundResource(R.drawable.bg_vehicle_default)
        }

        btnCar.setOnClickListener {
            selectedType = "car"
            btnCar.setBackgroundResource(R.drawable.bg_vehicle_type_selected)
            btnBike.setBackgroundResource(R.drawable.bg_vehicle_default)
        }

        // CLOSE
        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        // ADD VEHICLE
        btnAdd.setOnClickListener {

            val company = etCompany.text.toString().trim()
            val model = etModel.text.toString().trim()
            val plate = etPlate.text.toString().trim()
            val year = etYear.text.toString().toIntOrNull()

            val request = Model.Vehicle.AddVehicleRequest(
                type = selectedType,
                company = company,
                model = model,
                number_plate = plate,
                year = year
            )

            vehicleViewModel.addVehicle(request)
            dialog.dismiss()

        }
    }
    private fun formatDate(time: Long): String {
        val sdf = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(time))
    }
    private fun showVehicleSelectionBottomSheet(tvVehicle: TextView) {

        val dialog = com.google.android.material.bottomsheet.BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.dialog_select_vehicle_list, null)

        dialog.setContentView(view)
        dialog.show()

        val recycler = view.findViewById<RecyclerView>(R.id.rvVehicleSelect)

        val vehicles = viewModel.vehicleState.value?.getOrNull() ?: return

        val adapter = VehicleSelectAdapter(
            vehicles,
            selectedVehicleId
        ) { selected ->

            selectedVehicleId = selected.id
            selectedVehicleName = "${selected.company} ${selected.model}"
            tvVehicle.text = selectedVehicleName

            dialog.dismiss()
        }

        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter
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
                val fullWidth = container.width
                val newWidth = (fullWidth * percentage).toInt()

                val params = bar.layoutParams
                params.width = newWidth
                bar.layoutParams = params
            }

            when (type) {
                "fuel" -> bar.setBackgroundResource(R.drawable.bg_chart_bar_fuel)
                "oil" -> bar.setBackgroundResource(R.drawable.bg_chartbar_oil)
                "brake"->bar.setBackgroundResource(R.drawable.bg_chartbar_brake)
                "gear"->bar.setBackgroundResource(R.drawable.bg_chartbar_gear)
                "service" -> bar.setBackgroundResource(R.drawable.bg_chartbar_service)
                "tyre" -> bar.setBackgroundResource(R.drawable.bg_chartbar_tyre)
                "accessories"->bar.setBackgroundResource(R.drawable.bg_chartbar_accessory)
                "insurance" -> bar.setBackgroundResource(R.drawable.bg_chartbar_insurance)
                else -> bar.setBackgroundResource(R.drawable.bg_chartbar_others)
            }

            container.addView(view)
        }
    }
}