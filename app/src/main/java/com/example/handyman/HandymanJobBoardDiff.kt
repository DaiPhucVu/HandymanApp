package com.example.handyman

import androidx.recyclerview.widget.DiffUtil

object HandymanJobBoardDiff: DiffUtil.ItemCallback<Job>() {
    override fun areContentsTheSame(oldItem: Job, newItem: Job): Boolean {
        return oldItem == newItem
    }

    override fun areItemsTheSame(oldItem: Job, newItem: Job): Boolean {
        return oldItem.jobId == newItem.jobId
    }
}
