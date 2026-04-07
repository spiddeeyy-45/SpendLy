package UI

import Model.Vehicle.AddVehicleRequest
import Repository.Vehicle.VehicleRepository
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


class AddVehicle : AppCompatActivity() {

    private lateinit var binding: ActivityAddVehicleBinding
    private lateinit var viewModel: VehicleViewModel

    private var selectedType = "bike"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_vehicle)

        val repo = VehicleRepository()
        val factory = VehicleViewModelFactory(repo)
        viewModel = ViewModelProvider(this, factory)[VehicleViewModel::class.java]


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

        // VEHICLE CLICK
        binding.btnAddVehicle.setOnClickListener {
            val company = binding.etCompany.text.toString().trim()
            val model = binding.etModel.text.toString().trim()
            val plate = binding.etNumberPlate.text.toString().trim()
            val year = binding.etYear.text.toString().toIntOrNull()
            if (company.isEmpty() || model.isEmpty() || plate.isEmpty()) {
                showError("All fields are required")
                return@setOnClickListener
            }

            val request = AddVehicleRequest(
                type = selectedType,
                company = company,
                model = model,
                number_plate = plate,
                year = year
            )

            viewModel.addVehicle(request)
        }

        // OBSERVE RESULT
        viewModel.addVehicleState.observe(this) { result ->

            binding.btnAddVehicle.isEnabled = true

            result.onSuccess {
                Toast.makeText(this, "Vehicle Added ", Toast.LENGTH_SHORT).show()
                goToHome()
            }

            result.onFailure {

                if (it.message == "Loading") {
                    binding.btnAddVehicle.isEnabled = false
                    binding.tvAddVehicleError.visibility = View.VISIBLE
                    binding.tvAddVehicleError.text = "Adding vehicle..."
                } else {
                    binding.tvAddVehicleError.visibility = View.VISIBLE
                    binding.tvAddVehicleError.text = it.message
                }
            }
        }
    }

    private fun showError(message: String) {
        binding.tvAddVehicleError.visibility = View.VISIBLE
        binding.tvAddVehicleError.text = message
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
        finish()
    }
    private fun updateStepDots(activeStep: Int) {

        val active = R.drawable.bg_step_dot_active
        val inactive = R.drawable.bg_step_dot_inactive

        binding.dot1.setBackgroundResource(if (activeStep == 1) active else inactive)
        binding.dot2.setBackgroundResource(if (activeStep == 2) active else inactive)
    }
}