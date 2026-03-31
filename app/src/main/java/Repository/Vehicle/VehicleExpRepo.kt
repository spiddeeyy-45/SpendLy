package Repository.Vehicle

import Model.Vehicle.Stats
import Model.Vehicle.Vehicle
import Model.Vehicle.VehicleExpApiService
import Model.Vehicle.VehicleExpenseRequest
import Model.Vehicle.VehicleExpenseResponse

class VehicleExpRepo(private val api:VehicleExpApiService) {
    suspend fun VaddExpense(
        token: String,
        request: VehicleExpenseRequest
    ): Result<VehicleExpenseResponse> {

        return try {
            val response = api.VaddExpense(token, request)

            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message()))
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun getVehicles(token: String): Result<List<Vehicle>> {
        return try {
            val response = api.getVehicles(token)

            if (response.isSuccessful) {
                Result.success(response.body()?.vehicles ?: emptyList())
            } else {
                Result.failure(Exception(response.message()))
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun getStats(token: String, vehicleId: String): Result<Stats> {
        return try {
            val response = api.getStats(token, vehicleId)

            if (response.isSuccessful) {
                Result.success(response.body()!!.stats)
            } else {
                Result.failure(Exception(response.message()))
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}