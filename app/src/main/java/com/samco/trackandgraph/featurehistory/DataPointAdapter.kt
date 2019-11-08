package com.samco.trackandgraph.featurehistory

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.samco.trackandgraph.database.DataPoint
import com.samco.trackandgraph.databinding.ListItemDataPointBinding
import com.samco.trackandgraph.ui.FadeInListAdapter


class DataPointAdapter(private val clickListener: DataPointClickListener)
    : FadeInListAdapter<DataPoint, DataPointAdapter.ViewHolder>(DataPointDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent, clickListener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
        super.onBindViewHolder(holder, position)
    }

    override fun getFadeDuration() = 250L
    override fun getFadeStartDelay() = 100L

    class ViewHolder private constructor(private val binding: ListItemDataPointBinding,
                                         private val clickListener: DataPointClickListener)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(dataPoint: DataPoint) {
            binding.dataPoint = dataPoint
            binding.editButton.setOnClickListener { clickListener.editClicked(dataPoint) }
            binding.deleteButton.setOnClickListener { clickListener.deleteClicked(dataPoint) }
        }

        companion object {
            fun from(parent: ViewGroup, clickListener: DataPointClickListener): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListItemDataPointBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding, clickListener)
            }
        }
    }
}
class DataPointDiffCallback : DiffUtil.ItemCallback<DataPoint>() {
    override fun areItemsTheSame(oldItem: DataPoint, newItem: DataPoint) = oldItem.timestamp == newItem.timestamp && oldItem.featureId == newItem.featureId
    override fun areContentsTheSame(oldItem: DataPoint, newItem: DataPoint) = oldItem == newItem
}
class DataPointClickListener(private val onEditDataPoint: (DataPoint) -> Unit,
                             private val onDeleteDataPoint: (DataPoint) -> Unit) {
    fun editClicked(dataPoint: DataPoint) = onEditDataPoint(dataPoint)
    fun deleteClicked(dataPoint: DataPoint) = onDeleteDataPoint(dataPoint)
}
