package Repository.Vehicle

import Model.Vehicle.AddVehicleRequest
import Model.Vehicle.Vehicle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.UUID

class VehicleRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun addVehicle(request: AddVehicleRequest): Result<Unit> {
        return try {
            val uid = auth.currentUser?.uid
                ?: return Result.failure(Exception("User not logged in"))

            val vehicleId = UUID.randomUUID().toString()

            val vehicle = hashMapOf(
                "id" to vehicleId,
                "type" to request.type,
                "company" to request.company,
                "model" to request.model,
                "number_plate" to request.number_plate.uppercase(),
                "year" to request.year,
                "total_this_month" to 0.0,
                "createdAt" to System.currentTimeMillis()
            )

            firestore.collection("Users")
                .document(uid)
                .collection("Vehicles")
                .document(vehicleId)
                .set(vehicle)
                .await()

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getVehicles(): Result<List<Vehicle>> {
        return try {
            val uid = auth.currentUser?.uid
                ?: return Result.failure(Exception("User not logged in"))

            val snapshot = firestore.collection("Users")
                .document(uid)
                .collection("Vehicles")
                .get()
                .await()

            val vehicles = snapshot.documents.mapNotNull {
                it.toObject(Vehicle::class.java)
            }

            Result.success(vehicles)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}