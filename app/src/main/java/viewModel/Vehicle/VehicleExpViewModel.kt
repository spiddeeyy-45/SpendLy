package viewModel.Vehicle

import Model.Vehicle.Stats
import Model.Vehicle.Vehicle
import Model.Vehicle.VehicleExpenseRequest
import Model.Vehicle.VehicleExpenseResponse
import Repository.Vehicle.VehicleExpRepo
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class VehicleExpViewModel(private val repo:VehicleExpRepo):ViewModel() {
    private val _expenseState = MutableLiveData<Result<VehicleExpenseResponse>>()
    val expenseState: LiveData<Result<VehicleExpenseResponse>> = _expenseState
    private val _vehicleState = MutableLiveData<Result<List<Vehicle>>>()
    val vehicleState: LiveData<Result<List<Vehicle>>> = _vehicleState
    private val _statsState = MutableLiveData<Result<Stats>>()
    val statsState: LiveData<Result<Stats>> = _statsState

    fun VaddExpense(token: String, request: VehicleExpenseRequest) {
        viewModelScope.launch {
            val result = repo.VaddExpense(token, request)
            _expenseState.postValue(result)
        }
    }
    fun getVehicles(token: String) {
        viewModelScope.launch {
            val result = repo.getVehicles(token)
            _vehicleState.postValue(result)
        }
    }
    fun getStats(token: String, vehicleId: String) {
        viewModelScope.launch {
            val result = repo.getStats(token, vehicleId)
            _statsState.postValue(result)
        }
    }
}