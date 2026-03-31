package Model.Vehicle

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface VehicleApiService {
    @POST("/add-vehicle")
    suspend fun addVehicle(
        @Header("Authorization") token: String,
        @Body request: AddVehicleRequest
    ): Response<VehicleResponse>

    @GET("/vehicles")
    suspend fun getVehicles(
        @Header("Authorization") token: String
    ): Response<GetVehiclesResponse>
}