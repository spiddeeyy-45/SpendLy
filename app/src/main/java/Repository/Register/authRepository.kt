package Repository.Register

import Model.Register.RegisterApiInstance
import Model.Register.RegisterRequest
import Model.Register.RegisterResponse

import retrofit2.Response

class authRepository {

    suspend fun register(request: RegisterRequest): Response<RegisterResponse> {
        return RegisterApiInstance.api.register(request)
    }
}