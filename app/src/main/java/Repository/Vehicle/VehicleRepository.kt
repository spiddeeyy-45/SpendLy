package Repository.Vehicle

import Model.Vehicle.AddVehicleRequest
import Model.Vehicle.GetVehiclesResponse
import Model.Vehicle.VehicleApiService
import Model.Vehicle.VehicleResponse
import android.util.Log
import retrofit2.Response

class VehicleRepository(private val api: VehicleApiService) {

    suspend fun addVehicle(token: String, request: AddVehicleRequest)
            : Response<VehicleResponse> {
        return api.addVehicle("Bearess $token", request)
        Log.d("TOKEN_DEBUG", "Token: $token")
    }

    suspend fun getVehicles(token: String)
            : Response<GetVehiclesResponse> {
        return api.getVehicles("Bearer $token")
    }
}