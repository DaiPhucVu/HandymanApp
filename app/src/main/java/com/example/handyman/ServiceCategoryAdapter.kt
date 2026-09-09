package com.example.handyman

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class ServiceCategoryAdapter(private val customerId: String, private val onItemClick: (ServiceCategory) -> Unit) : ListAdapter<ServiceCategory, ServiceCategoryAdapter.ViewHolder>(ServiceCategoryDiff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.service_category_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        private val serviceImage: ImageView = itemView.findViewById(R.id.imgServiceIcon)
        private val serviceName: TextView = itemView.findViewById(R.id.txtServiceLabel)

        fun bind(item: ServiceCategory) {
            Log.d("ServiceCategoryAdapter", "Binding item: ${item.name}")

            serviceImage.setImageResource(item.iconResId)
            serviceName.text = item.name

            itemView.setOnClickListener {
                Log.d("ServiceCategoryAdapter", "Item clicked: ${item.name}")
                val navController = itemView.findNavController()
                if (navController.currentDestination?.id != R.id.serviceCategoryFragment) {
                    return@setOnClickListener
                }
                val action = ServiceCategoryFragmentDirections.actionServiceCategoryFragmentToJobPostingFragment(serviceCategory = item.name, customerId = customerId)
                Log.d("ServiceCategoryAdapter", "Navigating with action: $action")
                navController.navigate(action)
            }
        }
    }
}
