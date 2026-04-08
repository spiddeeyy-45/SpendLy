package viewModel.Vehicle

import Model.Vehicle.Stats
import Model.Vehicle.Vehicle
import Model.Vehicle.VehicleExpenseRequest
import Repository.Vehicle.VehicleExpRepo
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class VehicleExpViewModel(private val repo: VehicleExpRepo) : ViewModel() {

    val expenseState = MutableLiveData<Result<Unit>>()
    val vehicleState = MutableLiveData<Result<List<Vehicle>>>()
    val statsState = MutableLiveData<Result<Stats>>()

    fun addExpense(request: VehicleExpenseRequest) {

        if (request.amount <= 0) {
            expenseState.value = Result.failure(Exception("Enter valid amount"))
            return
        }

        viewModelScope.launch {
            try {
                val result = repo.addExpense(request)

                expenseState.value = result

            } catch (e: Exception) {
                expenseState.value = Result.failure(e)
            }
        }
    }

    fun getVehicles() {
        viewModelScope.launch {
            try {
                val result = repo.getVehicles()
                vehicleState.value = result
            } catch (e: Exception) {
                vehicleState.value = Result.failure(e)
            }
        }
    }

    fun getStats(vehicleId: String) {
        viewModelScope.launch {
            try {
                val result = repo.getStats(vehicleId)
                statsState.value = result
            } catch (e: Exception) {
                statsState.value = Result.failure(e)
            }
        }
    }
}