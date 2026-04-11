package viewModel.Profile

import Repository.Profile.ProfileRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import viewModel.Vehicle.VehicleExpViewModel

class ProfileViewModelFact (private val repo:ProfileRepository):ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            return ProfileViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")

    }
}