package Util.vehicleAdapter

import Model.Vehicle.Vehicle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.spendly.R
import kotlin.math.abs

class addVehicleAdapter(
    private val onVehicleClick: (Vehicle) -> Unit,
    private val onAddVehicleClick: () -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var vehicles: List<Vehicle> = emptyList()
    private var selectedPosition = 0

    companion object {
        private const val TYPE_VEHICLE = 0
        private const val TYPE_ADD = 1
    }

    // ---------------- VIEW HOLDERS ---------------- //

    inner class VehicleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvVehicleName)
        val tvPlate: TextView = itemView.findViewById(R.id.tvVehiclePlate)
        val tvAmount: TextView = itemView.findViewById(R.id.tvVehicleAmount)
        val tvBadge: TextView = itemView.findViewById(R.id.tvVehicleBadge)
        val root: View = itemView.findViewById(R.id.blurVehicleCard)
        val tvEmoji: TextView = itemView.findViewById(R.id.tvVehicleEmoji)
    }

    inner class AddVehicleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    // ---------------- CORE ---------------- //

    override fun getItemCount(): Int = vehicles.size + 1

    override fun getItemViewType(position: Int): Int {
        return if (position == vehicles.size) TYPE_ADD else TYPE_VEHICLE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return if (viewType == TYPE_ADD) {
            val view = inflater.inflate(R.layout.item_add_vehicle_card, parent, false)
            AddVehicleViewHolder(view)
        } else {
            val view = inflater.inflate(R.layout.item_vehicle_card, parent, false)
            VehicleViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        // ---------------- ADD CARD ---------------- //
        if (holder is AddVehicleViewHolder) {
            holder.itemView.setOnClickListener {
                onAddVehicleClick()
            }

            applyCarouselEffect(holder.itemView, position)
            return
        }

        // ---------------- VEHICLE CARD ---------------- //
        holder as VehicleViewHolder
        val vehicle = vehicles[position]

        holder.tvName.text = "${vehicle.company} ${vehicle.model}"
        holder.tvPlate.text = vehicle.number_plate
        holder.tvAmount.text = "₹${vehicle.total_this_month.toInt()}"
        holder.tvEmoji.text = getVehicleEmoji(vehicle.type)

        holder.tvBadge.visibility = if (position == 0) View.VISIBLE else View.GONE

        if (position == selectedPosition) {
            holder.root.setBackgroundResource(R.drawable.bg_vehicle_type_selected)
        } else {
            holder.root.setBackgroundResource(R.drawable.bg_vehicle_default)
        }

        holder.itemView.setOnClickListener {
            val pos = holder.adapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                val old = selectedPosition
                selectedPosition = pos
                notifyItemChanged(old)
                notifyItemChanged(pos)
                onVehicleClick(vehicle)
            }
        }

        applyCarouselEffect(holder.itemView, position)
    }

    // ---------------- SCROLL EFFECT ---------------- //

    private fun applyCarouselEffect(view: View, position: Int) {
        val center = selectedPosition
        val distance = abs(position - center)

        val scale = 1f - (0.12f * distance.coerceAtMost(2))
        val alpha = 1f - (0.3f * distance.coerceAtMost(2))

        view.scaleX = scale
        view.scaleY = scale
        view.alpha = alpha
    }

    // ---------------- DATA ---------------- //

    fun updateData(newList: List<Vehicle>) {
        vehicles = newList
        selectedPosition = 0
        notifyDataSetChanged()
    }

    // ---------------- EMOJI ---------------- //

    private fun getVehicleEmoji(type: String): String {
        return when (type.lowercase()) {
            "bike", "motorcycle" -> "🏍️"
            "car" -> "🚗"
            else -> "🚘"
        }
    }
    fun setSelectedPosition(position: Int) {
        val old = selectedPosition
        selectedPosition = position
        notifyItemChanged(old)
        notifyItemChanged(position)
    }
    fun updateVehicleAmount(vehicleId: String, newAmount: Double) {
        val index = vehicles.indexOfFirst { it.id == vehicleId }
        if (index != -1) {
            val updatedList = vehicles.toMutableList()
            updatedList[index] = updatedList[index].copy(
                total_this_month = newAmount
            )
            vehicles = updatedList
            notifyItemChanged(index)
        }
    }
}