package Repository.Add

import Model.Add.ExpenseRequest
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
                "createdAt" to System.currentTimeMillis()
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
}