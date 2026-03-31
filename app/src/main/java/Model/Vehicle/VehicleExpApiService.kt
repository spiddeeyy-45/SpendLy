package Model.Vehicle

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface VehicleExpApiService {
    @POST("/add-vexpense")
    suspend fun VaddExpense(
        @Header("Authorization") token:String?,
        @Body request: VehicleExpenseRequest
    ):Response<VehicleExpenseResponse>
    @GET("/vehicles")
    suspend fun getVehicles(
        @Header("Authorization") token: String
    ): Response<GetVehiclesResponse>
    @GET("vehicle-stats")
    suspend fun getStats(
        @Header("Authorization") token: String,
        @Query("vehicle_id") vehicleId: String
    ): Response<VehicleStatsResponse>
}