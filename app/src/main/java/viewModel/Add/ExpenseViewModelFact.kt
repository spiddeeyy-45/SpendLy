package viewModel.Add

import Repository.Add.ExpenseRepo
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ExpenseViewModelFact(
    private val repo: ExpenseRepo
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        if (modelClass.isAssignableFrom(ExpenseViewModel::class.java)) {
            return ExpenseViewModel(repo) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}