package UI

import Model.Vehicle.AddVehicleRequest
import Model.Vehicle.VehicleApiInstance
import Repository.Vehicle.VehicleRepository
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.spendly.R
import com.example.spendly.databinding.ActivityAddVehicleBinding
import viewModel.Vehicle.VehicleViewModel
import viewModel.Vehicle.VehicleViewModelFactory


class addVehicle : AppCompatActivity() {

    private lateinit var binding: ActivityAddVehicleBinding
    private lateinit var viewModel: VehicleViewModel

    private var selectedType = "bike"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // ✅ DataBinding setup (IMPORTANT)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_vehicle)

        // INIT MVVM (FIXED)
        val api = VehicleApiInstance.api
        val repo = VehicleRepository(api)
        val factory = VehicleViewModelFactory(repo)
        viewModel = ViewModelProvider(this, factory)[VehicleViewModel::class.java]

        // TYPE SELECTION

        binding.btnSelectBike.setOnClickListener {
            selectedType = "bike"
            updateVehicleSelection("bike")
            updateStepDots(1)
        }

        binding.btnSelectCar.setOnClickListener {
            selectedType = "car"
            updateVehicleSelection("car")
            updateStepDots(2)
        }
        binding.tvSkip.setOnClickListener {
            goToHome()
        }

        val token = getToken()

        // ADD VEHICLE CLICK
        binding.btnAddVehicle.setOnClickListener {

            val request = AddVehicleRequest(
                type = selectedType,
                company = binding.etCompany.text.toString().trim(),
                model = binding.etModel.text.toString().trim(),
                number_plate = binding.etNumberPlate.text.toString().trim(),
                year = binding.etYear.text.toString().toIntOrNull()
            )

            viewModel.addVehicle(token, request)
        }

        // OBSERVE RESULT
        viewModel.addVehicleState.observe(this) { result ->

            result.onSuccess {
                Toast.makeText(this, "Vehicle Added 🚀", Toast.LENGTH_SHORT).show()
                goToHome()
                // Clear fields
                binding.etCompany.text.clear()
                binding.etModel.text.clear()
                binding.etNumberPlate.text.clear()
                binding.etYear.text.clear()

                binding.tvAddVehicleError.visibility = View.GONE
            }

            result.onFailure {
                binding.tvAddVehicleError.visibility = View.VISIBLE
                binding.tvAddVehicleError.text = it.message
            }
        }
    }

    // 🔐 TOKEN
    private fun getToken(): String {
        return getSharedPreferences("app", Context.MODE_PRIVATE)
            .getString("token", "") ?: ""
    }
    private fun updateVehicleSelection(type: String) {

        if (type == "bike") {
            binding.btnSelectBike.setBackgroundResource(R.drawable.bg_vehicle_type_selected)
            binding.btnSelectCar.setBackgroundResource(R.drawable.bg_vehicle_default)

            binding.tvBikeLabel.setTextColor(getColor(R.color.accent_purple))
            binding.tvCarLabel.setTextColor(getColor(R.color.nav_inactive))
        } else {
            binding.btnSelectBike.setBackgroundResource(R.drawable.bg_vehicle_default)
            binding.btnSelectCar.setBackgroundResource(R.drawable.bg_vehicle_type_selected)

            binding.tvBikeLabel.setTextColor(getColor(R.color.nav_inactive))
            binding.tvCarLabel.setTextColor(getColor(R.color.accent_purple))
        }
    }
    private fun goToHome() {
        startActivity(Intent(this, MainActivity::class.java))
        finish() // prevents going back
    }
    private fun updateStepDots(activeStep: Int) {

        val active = R.drawable.bg_step_dot_active
        val inactive = R.drawable.bg_step_dot_inactive

        binding.dot1.setBackgroundResource(if (activeStep == 1) active else inactive)
        binding.dot2.setBackgroundResource(if (activeStep == 2) active else inactive)
    }
}