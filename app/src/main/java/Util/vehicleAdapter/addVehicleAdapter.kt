package util.vehicleAdapter

import Model.Vehicle.Vehicle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.spendly.R

class addVehicleAdapter(
    private val onVehicleClick: (Vehicle) -> Unit
) : RecyclerView.Adapter<addVehicleAdapter.VehicleViewHolder>() {

    private var vehicles: List<Vehicle> = emptyList()
    private var selectedPosition = 0

    inner class VehicleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvVehicleName)
        val tvPlate: TextView = itemView.findViewById(R.id.tvVehiclePlate)
        val tvAmount: TextView = itemView.findViewById(R.id.tvVehicleAmount)
        val tvBadge: TextView = itemView.findViewById(R.id.tvVehicleBadge)
        val root: View = itemView.findViewById(R.id.blurVehicleCard)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VehicleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_vehicle_card, parent, false)
        return VehicleViewHolder(view)
    }

    override fun onBindViewHolder(holder: VehicleViewHolder, position: Int) {
        val vehicle = vehicles[position]

        holder.tvName.text = "${vehicle.company} ${vehicle.model}"
        holder.tvPlate.text = vehicle.number_plate
        holder.tvAmount.text = "₹${vehicle.total_this_month.toInt()}"

        holder.tvBadge.visibility = if (position == 0) View.VISIBLE else View.GONE

        if (position == selectedPosition) {
            holder.root.alpha = 1f
            holder.root.scaleX = 1.05f
            holder.root.scaleY = 1.05f
            holder.itemView.setBackgroundResource(R.drawable.bg_vehicle_type_selected)
        } else {
            holder.root.alpha = 0.7f
            holder.root.scaleX = 1f
            holder.root.scaleY = 1f
            holder.itemView.setBackgroundResource(R.drawable.bg_vehicle_default)
        }

        holder.itemView.setOnClickListener {
            val pos = holder.adapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                selectedPosition = pos
                notifyDataSetChanged()
                onVehicleClick(vehicles[pos])
            }
        }
    }

    override fun getItemCount(): Int = vehicles.size

    fun updateData(newList: List<Vehicle>) {
        vehicles = newList
        selectedPosition = 0
        notifyDataSetChanged()
    }
}