package UI

import Model.Profile.CloudinaryClient
import Model.Profile.UserProfile
import Repository.Profile.ProfileRepository
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.spendly.R
import com.example.spendly.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import viewModel.Profile.ProfileViewModel

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ProfileViewModel
    private var currentProfile: UserProfile? = null

    private val IMAGE_PICK_CODE = 1001

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ProfileViewModel(ProfileRepository())

        setupObservers()
        setupClicks()

        viewModel.loadProfile()
    }
    private fun setupObservers() {

        viewModel.profileState.observe(viewLifecycleOwner) { result ->

            result.onSuccess { profile ->

                currentProfile = profile
                if (profile.isPremium) {
                    binding.tvBadgePlan.text = "Premium"
                    binding.tvBadgePlan.setBackgroundResource(R.drawable.bg_badge_purple)
                } else {
                    binding.tvBadgePlan.text = "Free"
                    binding.tvBadgePlan.setBackgroundResource(R.drawable.bg_setting_default)
                }

                binding.tvProfileName.text = profile.name

                binding.tvProfileSub.text =
                    "Member since ${formatDate(profile.createdAt)} · ${profile.location}"

                binding.tvBadgeCity.text = profile.location

                if (profile.profileImage.isNullOrEmpty()) {
                    binding.tvAvatar.visibility = View.VISIBLE
                    binding.ivProfileImage.visibility = View.GONE

                    binding.tvAvatar.text =
                        profile.name.firstOrNull()?.toString() ?: "🧑"

                } else {
                    binding.tvAvatar.visibility = View.GONE
                    binding.ivProfileImage.visibility = View.VISIBLE

                    Glide.with(requireContext())
                        .load(profile.profileImage)
                        .circleCrop()
                        .into(binding.ivProfileImage)
                }

            }.onFailure {
                Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.updateState.observe(viewLifecycleOwner) { result ->
            result.onSuccess {
                Toast.makeText(requireContext(), "Profile updated", Toast.LENGTH_SHORT).show()
                viewModel.loadProfile()
            }.onFailure {
                if (it.message != "Updating...") {
                    Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    //CLICK HANDLERS
    private fun setupClicks() {

        binding.ivProfileImage.setOnClickListener { openGallery() }
        binding.tvAvatar.setOnClickListener { openGallery() }

        // Edit Profile click
        binding.rowEditProfile.setOnClickListener {
            Toast.makeText(requireContext(), "Edit profile coming soon", Toast.LENGTH_SHORT).show()
        }
        binding.rowLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val prefs = Util.Login.SecurePrefs.getPrefs(requireContext())
            prefs.edit().clear().apply()

            Toast.makeText(requireContext(), "Logged out", Toast.LENGTH_SHORT).show()
            val intent = Intent(requireContext(), Login::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
    // OPEN GALLERY
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }
    // IMAGE RESULT
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == IMAGE_PICK_CODE && resultCode == Activity.RESULT_OK) {
            val imageUri = data?.data ?: return
            uploadToCloudinary(imageUri)
        }
    }
    private fun uploadToCloudinary(imageUri: Uri) {

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val inputStream = requireContext().contentResolver.openInputStream(imageUri)
                val bytes = inputStream?.readBytes() ?: return@launch

                val requestFile = bytes.toRequestBody("image/*".toMediaTypeOrNull())

                val body = MultipartBody.Part.createFormData(
                    "file",
                    "profile.jpg",
                    requestFile
                )

                val preset = "SpendLyProfile".toRequestBody("text/plain".toMediaTypeOrNull())

                val response = CloudinaryClient.api.uploadImage(body, preset)

                val imageUrl = response.secure_url
                val updatedProfile = currentProfile?.copy(profileImage = imageUrl)
                    ?: return@launch
                CoroutineScope(Dispatchers.Main).launch {
                    viewModel.updateProfile(updatedProfile)
                }

            } catch (e: Exception) {
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    //  DATE FORMAT
    private fun formatDate(timestamp: Long): String {
        val sdf = java.text.SimpleDateFormat("MMM yyyy", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(timestamp))
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}