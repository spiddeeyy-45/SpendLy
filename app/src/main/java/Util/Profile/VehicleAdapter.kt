package Util.Profile

import Model.Vehicle.Vehicle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.spendly.R

class VehicleAdapter(
    private val onVehicleClick: (Vehicle) -> Unit
) : RecyclerView.Adapter<VehicleAdapter.VehicleViewHolder>() {

    private var vehicles: List<Vehicle> = emptyList()
    private var selectedPosition = 0

    // ---------------- VIEW HOLDER ---------------- //

    inner class VehicleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvVehicleName)
        val tvPlate: TextView = itemView.findViewById(R.id.tvVehiclePlate)
        val tvBadge: TextView = itemView.findViewById(R.id.tvVehicleBadge)
        val tvEmoji: TextView = itemView.findViewById(R.id.tvVehicleEmoji)
        val root: View = itemView.findViewById(R.id.rowVehicleItem)
    }

    // ---------------- CORE ---------------- //

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VehicleViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_vehicle_add, parent, false)
        return VehicleViewHolder(view)
    }

    override fun getItemCount(): Int = vehicles.size

    override fun onBindViewHolder(holder: VehicleViewHolder, position: Int) {
        val vehicle = vehicles[position]

        holder.tvName.text = "${vehicle.company} ${vehicle.model}"
        holder.tvPlate.text = vehicle.number_plate
        holder.tvEmoji.text = getVehicleEmoji(vehicle.type)

        // Badge (Active / Selected)
        holder.tvBadge.visibility = if (position == selectedPosition) View.VISIBLE else View.GONE

        // Click handling
        holder.root.setOnClickListener {
            val old = selectedPosition
            selectedPosition = holder.adapterPosition
            notifyItemChanged(old)
            notifyItemChanged(selectedPosition)

            onVehicleClick(vehicle)
        }
    }

    // ---------------- DATA ---------------- //

    fun updateData(newList: List<Vehicle>) {
        vehicles = newList
        selectedPosition = 0
        notifyDataSetChanged()
    }

    fun setSelectedPosition(position: Int) {
        val old = selectedPosition
        selectedPosition = position
        notifyItemChanged(old)
        notifyItemChanged(position)
    }

    // ---------------- EMOJI ---------------- //

    private fun getVehicleEmoji(type: String): String {
        return when (type.lowercase()) {
            "bike" -> "🏍️"
            "car" -> "🚘"
            else ->""
        }
    }
}