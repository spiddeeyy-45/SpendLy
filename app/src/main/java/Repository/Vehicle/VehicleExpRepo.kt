package Repository.Vehicle

import Model.Vehicle.Stats
import Model.Vehicle.Vehicle
import Model.Vehicle.VehicleExpenseRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.*

class VehicleExpRepo {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    //  ADD EXPENSE + UPDATE VEHICLE TOTAL
    suspend fun addExpense(request: VehicleExpenseRequest): Result<Unit> {
        return try {
            val uid = auth.currentUser?.uid
                ?: return Result.failure(Exception("User not logged in"))

            val expenseId = UUID.randomUUID().toString()

            val expense = hashMapOf(
                "id" to expenseId,
                "vehicle_id" to request.vehicle_id,
                "type" to request.type,
                "amount" to request.amount,
                "createdAt" to System.currentTimeMillis()
            )

            val userRef = firestore.collection("Users").document(uid)

            firestore.runTransaction { transaction ->

                val vehicleRef = userRef
                    .collection("Vehicles")
                    .document(request.vehicle_id)

                val expenseRef = userRef
                    .collection("VehicleExpenses")
                    .document(expenseId)

                val snapshot = transaction.get(vehicleRef)
                val currentTotal = snapshot.getDouble("total_this_month") ?: 0.0

                transaction.set(expenseRef, expense)

                transaction.update(
                    vehicleRef,
                    "total_this_month",
                    currentTotal + request.amount
                )

            }.await()

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // GET VEHICLES
    suspend fun getVehicles(): Result<List<Vehicle>> {
        return try {
            val uid = auth.currentUser?.uid
                ?: return Result.failure(Exception("User not logged in"))

            val snapshot = firestore.collection("Users")
                .document(uid)
                .collection("Vehicles")
                .get()
                .await()

            val vehicles = snapshot.toObjects(Vehicle::class.java)

            Result.success(vehicles)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // GET STATS
    suspend fun getStats(vehicleId: String): Result<Stats> {
        return try {
            val uid = auth.currentUser?.uid
                ?: return Result.failure(Exception("User not logged in"))

            val now = Calendar.getInstance()

            // START OF MONTH
            val startOfMonth = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }.timeInMillis

            // START OF LAST MONTH
            val startOfLastMonth = Calendar.getInstance().apply {
                add(Calendar.MONTH, -1)
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }.timeInMillis

            //  START OF TODAY
            val startOfToday = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }.timeInMillis

            val snapshot = firestore.collection("Users")
                .document(uid)
                .collection("VehicleExpenses")
                .whereEqualTo("vehicle_id", vehicleId)
                .get()
                .await()

            var total = 0.0
            var totalLastMonth = 0.0

            var fuel = 0.0
            var fuelToday = 0.0
            var fuelLastMonth = 0.0

            var service = 0.0
            var serviceToday = 0.0
            var serviceLastMonth = 0.0

            for (doc in snapshot.documents) {

                val amount = doc.getDouble("amount") ?: 0.0
                val type = doc.getString("type") ?: ""
                val createdAt = doc.getLong("createdAt") ?: 0L

                // THIS MONTH
                if (createdAt >= startOfMonth) {
                    total += amount

                    when (type) {
                        "fuel" -> fuel += amount
                        "service" -> service += amount
                    }
                }

                // LAST MONTH
                if (createdAt in startOfLastMonth until startOfMonth) {
                    totalLastMonth += amount

                    when (type) {
                        "fuel" -> fuelLastMonth += amount
                        "service" -> serviceLastMonth += amount
                    }
                }

                // TODAY
                if (createdAt >= startOfToday) {
                    when (type) {
                        "fuel" -> fuelToday += amount
                        "service" -> serviceToday += amount
                    }
                }
            }

            // AVG PER DAY
            val daysPassed = now.get(Calendar.DAY_OF_MONTH)
            val avgPerDay = if (daysPassed > 0) total / daysPassed else 0.0

            val stats = Stats(
                total = total,
                totalLastMonth = totalLastMonth,

                fuel = fuel,
                fuelToday = fuelToday,
                fuelLastMonth = fuelLastMonth,

                service = service,
                serviceToday = serviceToday,
                serviceLastMonth = serviceLastMonth,

                avgPerDay = avgPerDay
            )

            Result.success(stats)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}