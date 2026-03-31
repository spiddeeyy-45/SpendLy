package viewModel.Vehicle

import Repository.Vehicle.VehicleRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class VehicleViewModelFactory(
    private val repo: VehicleRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VehicleViewModel::class.java)) {
            return VehicleViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}