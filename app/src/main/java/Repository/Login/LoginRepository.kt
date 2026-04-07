package Repository.Login

import Model.Login.LoginRequest
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

class LoginRepository {

    private val auth = FirebaseAuth.getInstance()

    suspend fun login(request: LoginRequest): Result<Pair<String, String>> {
        return try {
            val result = auth.signInWithEmailAndPassword(
                request.email,
                request.password
            ).await()

            val user = result.user ?: return Result.failure(Exception("User not found"))

            val tokenResult = user.getIdToken(true).await()

            val token = tokenResult.token
                ?: return Result.failure(Exception("Token fetch failed"))

            Result.success(Pair(user.uid, token))

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendPasswordReset(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}