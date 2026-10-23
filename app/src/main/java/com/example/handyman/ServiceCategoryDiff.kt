package com.example.handyman

import androidx.recyclerview.widget.DiffUtil

object ServiceCategoryDiff: DiffUtil.ItemCallback<ServiceCategory>() {
    override fun areContentsTheSame(oldItem: ServiceCategory, newItem: ServiceCategory): Boolean {
        return oldItem == newItem
    }

    override fun areItemsTheSame(oldItem: ServiceCategory, newItem: ServiceCategory): Boolean {
        return oldItem.name == newItem.name
    }
}
