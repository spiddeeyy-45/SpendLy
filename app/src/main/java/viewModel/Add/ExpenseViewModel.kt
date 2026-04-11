package viewModel.Add

import Model.Add.ExpenseRequest
import Model.Add.ExpenseStats
import Repository.Add.ExpenseRepo
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class ExpenseViewModel(private val repo:ExpenseRepo) :ViewModel(){
    val expenseState = MutableLiveData<Result<Unit>>()
    val statsState = MutableLiveData<Result<ExpenseStats>>()
    val chartState = MutableLiveData<Result<Map<String, Double>>>()
    fun addExpense(request: ExpenseRequest) {

        if (request.amount <= 0) {
            expenseState.value = Result.failure(Exception("Enter valid amount"))
            return
        }
        viewModelScope.launch {
            try {
                val result = repo.addDailyExpense(request)

                expenseState.value = result

            } catch (e: Exception) {
                expenseState.value = Result.failure(e)
            }
        }
    }

    fun getStats() {
        viewModelScope.launch {
            try {
                val result = repo.getStats()
                statsState.value = result
            } catch (e: Exception) {
                statsState.value = Result.failure(e)
            }
        }
    }


    fun getChart(isYearly: Boolean) {
        viewModelScope.launch {
            try {
                val result = repo.getExpenseChartData(isYearly)
                chartState.value = result
            } catch (e: Exception) {
                chartState.value = Result.failure(e)
            }
        }
    }

}