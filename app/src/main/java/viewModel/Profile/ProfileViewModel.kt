package viewModel.Profile

import Model.Profile.UserProfile
import Repository.Profile.ProfileRepository
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val repo: ProfileRepository
) : ViewModel() {

    val profileState = MutableLiveData<Result<UserProfile>>()
    val updateState = MutableLiveData<Result<Unit>>()

    fun loadProfile() {
        viewModelScope.launch {
            val result = repo.getProfile()
            profileState.value = result
        }
    }

    fun updateProfile(profile: UserProfile) {
        viewModelScope.launch {
            updateState.value = Result.failure(Exception("Updating..."))

            val result = repo.updateProfile(profile)
            updateState.value = result
        }
    }
}