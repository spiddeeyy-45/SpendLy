package Repository.Add

import Model.Add.ExpenseRequest
import Model.Add.ExpenseStats
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.*

class ExpenseRepo {
    private val firestore= FirebaseFirestore.getInstance()
    private val auth=FirebaseAuth.getInstance()
    suspend fun addDailyExpense(request:ExpenseRequest):Result<Unit>{
        return try {
            val uid=auth.currentUser?.uid?:return Result.failure(Exception("User not Logged in"))
            val expenseId= UUID.randomUUID().toString()
            val expense= hashMapOf(
                "id" to expenseId,
                "type" to request.type,
                "amount" to request.amount,
                "note" to request.note,
                "createdAt" to request.date
            )
            val userRef=firestore.collection("Users").document(uid)
            firestore.runTransaction { transaction ->
                val expenseRef=userRef.collection("Expenses")
                    .document(expenseId)
                val snapshot=transaction.get(expenseRef)
                val curTotal=snapshot.getDouble("total_this_month")?:0.0
                transaction.set(expenseRef,expense)
                transaction.update(
                    expenseRef,"total_this_month",curTotal+request.amount
                )
            }.await()

            Result.success(Unit)

        }catch (e:Exception)
        {
            Result.failure(e)

        }

    }
    suspend fun getStats(): Result<ExpenseStats> {
        return try {
            val uid = auth.currentUser?.uid ?: return Result.failure(Exception("User not logged in"))

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
                .collection("Expenses")
                .get()
                .await()

            var total = 0.0
            var totalLastMonth = 0.0

            var grocery = 0.0
            var groceryToday = 0.0
            var groceryLastMonth = 0.0

            var shopping = 0.0
            var shoppingToday = 0.0
            var shoppingLastMonth = 0.0

            var food = 0.0
            var foodToday = 0.0
            var foodLastMonth = 0.0

            for (doc in snapshot.documents) {

                val amount = doc.getDouble("amount") ?: 0.0
                val type = doc.getString("type") ?: ""
                val createdAt = doc.getLong("createdAt") ?: 0L

                // THIS MONTH
                if (createdAt >= startOfMonth) {
                    total += amount

                    when (type) {
                        "food" -> food += amount
                        "shopping" -> shopping += amount
                        "grocery"->grocery += amount
                    }
                }

                // LAST MONTH
                if (createdAt in startOfLastMonth until startOfMonth) {
                    totalLastMonth += amount

                    when (type) {
                        "food" -> foodLastMonth += amount
                        "shopping" -> shoppingLastMonth += amount
                        "grocery"->groceryLastMonth += amount
                    }
                }

                // TODAY
                if (createdAt >= startOfToday) {
                    when (type) {
                        "food" -> foodToday += amount
                        "shopping" -> shoppingToday += amount
                        "grocery"->groceryToday += amount
                    }
                }
            }

            val stats = ExpenseStats(
                total = total,
                totalLastMonth = totalLastMonth,

                food = food,
                foodToday = foodToday,
                foodLastMonth = foodLastMonth,

                shopping = shopping,
                shoppingToday = shoppingToday,
                shoppingLastMonth = shoppingLastMonth,

                grocery=grocery,
                groceryToday=groceryToday,
                groceryLastMonth=groceryLastMonth
            )

            Result.success(stats)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}