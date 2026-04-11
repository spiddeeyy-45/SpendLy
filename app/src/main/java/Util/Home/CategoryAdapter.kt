package Util.Home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter

import androidx.recyclerview.widget.RecyclerView
import com.example.spendly.databinding.ItemCategoryBinding

class CategoryAdapter : ListAdapter<CategoryUIModel, CategoryAdapter.CategoryViewHolder>(DiffCallback()) {

    inner class CategoryViewHolder(val binding: ItemCategoryBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val item = getItem(position)

        with(holder.binding) {

            tvName.text = item.name.replaceFirstChar { it.uppercase() }
            tvAmount.text = "₹${item.amount.toInt()}"

            // ICON (based on category)
            tvIcon.text = getIcon(item.name)

            // PROGRESS BAR
            val max = currentList.maxOfOrNull { it.amount } ?: 1.0
            val percent = item.amount / max

            viewProgress.post {
                val maxWidth = (root.width * 0.8).toInt()
                viewProgress.layoutParams.width = (percent * maxWidth).toInt()
                viewProgress.requestLayout()
            }
        }
    }

    private fun getIcon(type: String): String {
        return when (type.lowercase().trim()) {

            // VEHICLE
            "fuel" -> "⛽"
            "oil" -> "🛢️"
            "service" -> "🛠️"
            "insurance" -> "📄"
            "gear" -> "⚙️"
            "brake" -> "🛑"
            "tyre" -> "🛞"
            "accessories" -> "🔧"

            // PERSONAL
            "food" -> "🍔"
            "grocery" -> "🛒"
            "shopping" -> "🛍️"
            "subscriptions" -> "📺"
            "medical" -> "💊"
            "stocks" -> "📈"
            "trip" -> "✈️"

            // DEFAULT
            "others" -> "💸"

            else -> "💸"
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<CategoryUIModel>() {
        override fun areItemsTheSame(oldItem: CategoryUIModel, newItem: CategoryUIModel) =
            oldItem.name == newItem.name

        override fun areContentsTheSame(oldItem: CategoryUIModel, newItem: CategoryUIModel) =
            oldItem == newItem
    }
}