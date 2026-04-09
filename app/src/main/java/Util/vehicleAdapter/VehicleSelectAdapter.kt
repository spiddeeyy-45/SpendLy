package Util.vehicleAdapter

import Model.Vehicle.Vehicle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.spendly.R

class VehicleSelectAdapter(
    private val vehicles: List<Vehicle>,
    private var selectedId: String?,
    private val onClick: (Vehicle) -> Unit
) : RecyclerView.Adapter<VehicleSelectAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.tvVehicleName)
        val check: TextView = view.findViewById(R.id.tvCheck)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_vehicle_list, parent, false)
        return VH(view)
    }

    override fun getItemCount() = vehicles.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val vehicle = vehicles[position]

        holder.name.text = "${vehicle.company} ${vehicle.model}"

        if (vehicle.id == selectedId) {
            holder.check.visibility = View.VISIBLE
        } else {
            holder.check.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            selectedId = vehicle.id
            notifyDataSetChanged()
            onClick(vehicle)
        }
    }
}