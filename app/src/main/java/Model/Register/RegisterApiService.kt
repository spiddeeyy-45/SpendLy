package Model.Register

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface RegisterApiService {
    @POST("/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<RegisterResponse>
}