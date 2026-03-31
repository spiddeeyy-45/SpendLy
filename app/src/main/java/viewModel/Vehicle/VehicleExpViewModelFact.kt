package viewModel.Vehicle

import Repository.Vehicle.VehicleExpRepo
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class VehicleExpViewModelFact(
    private val repo: VehicleExpRepo
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return VehicleExpViewModel(repo) as T
    }
}