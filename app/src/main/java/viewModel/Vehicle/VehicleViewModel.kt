package viewModel.Vehicle

import Model.Vehicle.AddVehicleRequest
import Model.Vehicle.Vehicle
import Model.Vehicle.VehicleResponse
import Repository.Vehicle.VehicleRepository
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class VehicleViewModel(private val repo: VehicleRepository) : ViewModel() {

    val addVehicleState = MutableLiveData<Result<VehicleResponse>>()
    val vehicleListState = MutableLiveData<Result<List<Vehicle>>>()

    fun addVehicle(token: String, request: AddVehicleRequest) {
        viewModelScope.launch {
            try {
                val response = repo.addVehicle(token, request)

                if (response.isSuccessful && response.body() != null) {
                    addVehicleState.postValue(Result.success(response.body()!!))
                } else {
                    addVehicleState.postValue(
                        Result.failure(Exception(response.errorBody()?.string()))
                    )
                }

            } catch (e: Exception) {
                addVehicleState.postValue(Result.failure(e))
            }
        }
    }

    fun getVehicles(token: String) {
        viewModelScope.launch {
            try {
                val response = repo.getVehicles(token)

                if (response.isSuccessful && response.body() != null) {
                    vehicleListState.postValue(
                        Result.success(response.body()!!.vehicles)
                    )
                } else {
                    vehicleListState.postValue(
                        Result.failure(Exception("Failed to fetch"))
                    )
                }

            } catch (e: Exception) {
                vehicleListState.postValue(Result.failure(e))
            }
        }
    }
}