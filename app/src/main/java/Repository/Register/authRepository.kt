package Repository.Register

import Model.Register.RegisterRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class authRepository {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun register(request: RegisterRequest): Result<String> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(
                request.email,
                request.password
            ).await()

            val user = authResult.user
                ?: return Result.failure(Exception("User creation failed"))

            val uid = user.uid
            val userMap = hashMapOf(
                "uid" to uid,
                "name" to request.name,
                "email" to request.email,
                "phone" to request.phone,
                "location" to request.location,
                "fcmToken" to request.fcmToken,
                "isPremium" to false,
                "createdAt" to System.currentTimeMillis()
            )
            firestore.collection("Users")
                .document(uid)
                .set(userMap)
                .await()
            Result.success(uid)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}