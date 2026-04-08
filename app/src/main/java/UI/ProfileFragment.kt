package UI

import Model.Profile.CloudinaryClient
import Model.Profile.UserProfile
import Repository.Profile.ProfileRepository
import Repository.Vehicle.VehicleRepository
import Util.Profile.VehicleAdapter
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
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
import viewModel.Vehicle.VehicleViewModel
import viewModel.Vehicle.VehicleViewModelFactory

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var vehicleViewModel: VehicleViewModel
    private lateinit var adapter: VehicleAdapter
    private var dialogImageView: ImageView? = null
    private var dialogAvatarView: TextView? = null
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

        setupVehicleViewModel()
        setupVehicleRecycler()
        observeVehicles()

        setupObservers()
        setupClicks()

        viewModel.loadProfile()
        vehicleViewModel.getVehicles()
        vehicleViewModel.addVehicleState.observe(viewLifecycleOwner) { result ->
            result.onSuccess {
                Toast.makeText(context, "Vehicle Added", Toast.LENGTH_SHORT).show()
                vehicleViewModel.getVehicles()
            }

            result.onFailure {
                if (it.message != "Loading...") {
                    Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
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


                    Glide.with(binding.root)
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
            openUpdateProfileDialog()
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
        binding.btnAddVehicle.setOnClickListener {
            openAddVehicleDialog()
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
            dialogAvatarView?.visibility = View.GONE
            dialogImageView?.visibility = View.VISIBLE
            dialogImageView?.setImageURI(imageUri)
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
    private fun setupVehicleViewModel() {
        val repo = VehicleRepository()

        vehicleViewModel = ViewModelProvider(
            this,
            VehicleViewModelFactory(repo)
        )[VehicleViewModel::class.java]
    }
    private fun setupVehicleRecycler() {

        adapter = VehicleAdapter { vehicle ->
            Toast.makeText(requireContext(), "Selected: ${vehicle.company}", Toast.LENGTH_SHORT).show()
        }
            val layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)
        binding.rvVehicles.layoutManager=layoutManager
        binding.rvVehicles.adapter=adapter

    }
    private fun observeVehicles() {

        vehicleViewModel.vehicleListState.observe(viewLifecycleOwner) { result ->

            result.onSuccess { vehicles ->

                if (vehicles.isEmpty()) {
                    Toast.makeText(context, "No vehicles found", Toast.LENGTH_SHORT).show()
                    return@onSuccess
                }

               adapter.updateData(vehicles)
            }

            result.onFailure {
                Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun openAddVehicleDialog() {

        val dialog = com.google.android.material.bottomsheet.BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.dialog_add_vehicle, null)

        dialog.setContentView(view)
        dialog.show()

        // Views
        val etCompany = view.findViewById<EditText>(R.id.etCompany)
        val etModel = view.findViewById<EditText>(R.id.etModel)
        val etPlate = view.findViewById<EditText>(R.id.etNumberPlate)
        val etYear = view.findViewById<EditText>(R.id.etYear)

        val btnAdd = view.findViewById<TextView>(R.id.btnAddVehicle)
        val btnClose = view.findViewById<View>(R.id.btnClose)
        val tvError = view.findViewById<TextView>(R.id.tvAddVehicleError)

        val btnBike = view.findViewById<View>(R.id.btnSelectBike)
        val btnCar = view.findViewById<View>(R.id.btnSelectCar)

        var selectedType = "bike"

        // TYPE SWITCH
        btnBike.setOnClickListener {
            selectedType = "bike"
            btnBike.setBackgroundResource(R.drawable.bg_vehicle_type_selected)
            btnCar.setBackgroundResource(R.drawable.bg_vehicle_default)
        }

        btnCar.setOnClickListener {
            selectedType = "car"
            btnCar.setBackgroundResource(R.drawable.bg_vehicle_type_selected)
            btnBike.setBackgroundResource(R.drawable.bg_vehicle_default)
        }

        // CLOSE
        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        // ADD VEHICLE
        btnAdd.setOnClickListener {

            val company = etCompany.text.toString().trim()
            val model = etModel.text.toString().trim()
            val plate = etPlate.text.toString().trim()
            val year = etYear.text.toString().toIntOrNull()

            val request = Model.Vehicle.AddVehicleRequest(
                type = selectedType,
                company = company,
                model = model,
                number_plate = plate,
                year = year
            )

            vehicleViewModel.addVehicle(request)
            dialog.dismiss()

        }
    }
    private fun openUpdateProfileDialog() {

        val dialog = com.google.android.material.bottomsheet.BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.dialog_update_profile, null)

        dialog.setContentView(view)
        dialog.show()

        // Views
        val etName = view.findViewById<EditText>(R.id.etFullName)
        val etEmail = view.findViewById<EditText>(R.id.etEmail)
        val etPhone = view.findViewById<EditText>(R.id.etPhone)
        val etLocation = view.findViewById<EditText>(R.id.etLocation)

        val btnSave = view.findViewById<TextView>(R.id.btnSaveProfile)
        val btnClose = view.findViewById<View>(R.id.btnClose)
        val tvError = view.findViewById<TextView>(R.id.tvUpdateError)

        val ivPreview = view.findViewById<ImageView>(R.id.ivProfilePreview)
        val tvAvatar = view.findViewById<TextView>(R.id.tvAvatarPreview)
        ivPreview.setOnClickListener {
            openGallery()
        }

        tvAvatar.setOnClickListener {
            openGallery()
        }
        dialogImageView = ivPreview
        dialogAvatarView = tvAvatar

        currentProfile?.let { profile ->
            etName.setText(profile.name)
            etEmail.setText(profile.email)
            etPhone.setText(profile.phone ?: "")
            etLocation.setText(profile.location)

            if (!profile.profileImage.isNullOrEmpty()) {
                tvAvatar.visibility = View.GONE
                ivPreview.visibility = View.VISIBLE

                Glide.with(binding.root)
                        .load(profile.profileImage)
                        .circleCrop()
                        .into(ivPreview)

            } else {
                tvAvatar.text = profile.name.firstOrNull()?.toString() ?: "🧑"
            }
        }

        // CLOSE
        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        // SAVE
        btnSave.setOnClickListener {

            val updatedProfile = currentProfile?.copy(
                name = etName.text.toString().trim(),
                email = etEmail.text.toString().trim(),
                phone = etPhone.text.toString().trim(),
                location = etLocation.text.toString().trim()
            ) ?: return@setOnClickListener

            viewModel.updateProfile(updatedProfile)
            dialog.dismiss()
        }

    }
}