package Util.Home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.spendly.databinding.ItemTransactionBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TransactionAdapter :
    ListAdapter<TransactionUIModel, TransactionAdapter.TransactionViewHolder>(DiffCallback()) {

    inner class TransactionViewHolder(val binding: ItemTransactionBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding = ItemTransactionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val item = getItem(position)

        with(holder.binding) {

            txName.text = item.title

            txAmount.text = "-₹${item.amount.toInt()}"

            txDate.text = "${formatDate(item.selectedDate)} · ${item.type}"

            txIcon.text = getIcon(item.type)
        }
    }

    private fun getIcon(type: String): String {
        return when (type.lowercase().trim()) {

            "fuel" -> "⛽"
            "oil" -> "🛢️"
            "service" -> "🛠️"
            "insurance" -> "📄"
            "gear" -> "⚙️"
            "brake" -> "🛑"
            "tyre" -> "🛞"
            "accessory", "accessories" -> "🔧"

            "food" -> "🍔"
            "grocery" -> "🛒"
            "shopping" -> "🛍️"
            "subscriptions" -> "📺"
            "medical" -> "💊"
            "stocks" -> "📈"
            "trip" -> "✈️"

            else -> "💸"
        }
    }

    private fun formatDate(time: Long): String {
        val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
        return sdf.format(Date(time))
    }

    class DiffCallback : DiffUtil.ItemCallback<TransactionUIModel>() {
        override fun areItemsTheSame(oldItem: TransactionUIModel, newItem: TransactionUIModel) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: TransactionUIModel, newItem: TransactionUIModel) =
            oldItem == newItem
    }
}