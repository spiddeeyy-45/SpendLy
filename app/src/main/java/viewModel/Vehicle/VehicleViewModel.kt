package viewModel.Vehicle

import Model.Vehicle.AddVehicleRequest
import Model.Vehicle.Vehicle
import Repository.Vehicle.VehicleRepository
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
class VehicleViewModel(private val repo: VehicleRepository) : ViewModel() {

    val addVehicleState = MutableLiveData<Result<Unit>>()
    val vehicleListState = MutableLiveData<Result<List<Vehicle>>>()

    fun addVehicle(request: AddVehicleRequest) {

        if (request.company.length < 2) {
            addVehicleState.value = Result.failure(Exception("Enter valid company"))
            return
        }
        if (request.model.length < 1) {
            addVehicleState.value = Result.failure(Exception("Enter model name"))
            return
        }
        val plateRegex = Regex("^[A-Z]{2}[0-9]{2}[A-Z]{1,2}[0-9]{4}$")
        val cleanPlate = request.number_plate.replace(" ", "").uppercase()

        if (!cleanPlate.matches(plateRegex)) {
            addVehicleState.value =
                Result.failure(Exception("Invalid number plate (e.g. MH02AB1234)"))
            return
        }
        val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)

        if (request.year != null && (request.year < 1980 || request.year > currentYear)) {
            addVehicleState.value =
                Result.failure(Exception("Enter valid year"))
            return
        }
        val updatedRequest = request.copy(number_plate = cleanPlate)
        viewModelScope.launch {
            try {
                addVehicleState.value = Result.failure(Exception("Loading..."))

                val result = repo.addVehicle(request)

                addVehicleState.value = result
            } catch (e:Exception){
              addVehicleState.value=Result.failure(e)
            }
        }
    }
    fun getVehicles() {
        viewModelScope.launch {
            try {
                val result = repo.getVehicles()
                vehicleListState.value = result
            } catch (e: Exception) {
                vehicleListState.value = Result.failure(e)
            }
        }
    }
}