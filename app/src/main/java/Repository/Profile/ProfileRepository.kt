package Repository.Profile

import Model.Profile.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ProfileRepository {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun getProfile(): Result<UserProfile> {
        return try {
            val uid = auth.currentUser?.uid
                ?: return Result.failure(Exception("User not logged in"))

            val doc = firestore.collection("Users")
                .document(uid)
                .get()
                .await()

            val profile = doc.toObject(UserProfile::class.java)
                ?: return Result.failure(Exception("No data found"))

            Result.success(profile)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProfile(profile: UserProfile): Result<Unit> {
        return try {
            val uid = auth.currentUser?.uid
                ?: return Result.failure(Exception("User not logged in"))

            firestore.collection("Users")
                .document(uid)
                .update(
                    mapOf(
                        "name" to profile.name,
                        "location" to profile.location,
                        "profileImage" to profile.profileImage,
                        "income" to profile.income
                    )
                )
                .await()

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}