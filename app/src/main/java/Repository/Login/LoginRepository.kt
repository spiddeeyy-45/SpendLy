package Repository.Login

import Model.Login.LoginApiInstance
import Model.Login.LoginRequest

class LoginRepository {
    suspend fun login(request: LoginRequest)=LoginApiInstance.api.login(request)
}