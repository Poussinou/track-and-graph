package com.samco.trackandgraph.displaytrackgroup

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.samco.trackandgraph.R
import com.samco.trackandgraph.database.DisplayFeature
import com.samco.trackandgraph.databinding.ListItemFeatureBinding
import com.samco.trackandgraph.ui.OrderedListAdapter

class FeatureAdapter(private val clickListener: FeatureClickListener)
    : OrderedListAdapter<DisplayFeature, FeatureViewHolder>(DisplayFeatureDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeatureViewHolder {
        return FeatureViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: FeatureViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        holder.bind(getItem(position), clickListener)
    }
}

class FeatureViewHolder private constructor(private val binding: ListItemFeatureBinding)
    : RecyclerView.ViewHolder(binding.root), PopupMenu.OnMenuItemClickListener {

    private var clickListener: FeatureClickListener? = null
    private var feature: DisplayFeature? = null
    private var dropElevation = 0f

    fun bind(feature: DisplayFeature, clickListener: FeatureClickListener) {
        this.feature = feature
        this.clickListener = clickListener
        this.dropElevation = binding.cardView.cardElevation
        binding.feature = feature
        binding.clickListener = clickListener
        binding.menuButton.setOnClickListener { createContextMenu(binding.menuButton) }
        binding.addButton.setOnClickListener { clickListener.onAdd(feature) }
        binding.cardView.setOnClickListener { clickListener.onHistory(feature) }
    }

    fun elevateCard() {
        binding.cardView.postDelayed({
            binding.cardView.cardElevation = binding.cardView.cardElevation * 3f
        }, 10)
    }

    fun dropCard() {
        binding.cardView.cardElevation = dropElevation
    }

    private fun createContextMenu(view: View) {
        val popup = PopupMenu(view.context, view)
        popup.menuInflater.inflate(R.menu.edit_feature_context_menu, popup.menu)
        popup.setOnMenuItemClickListener(this)
        popup.show()
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        feature?.let {
            when (item?.itemId) {
                R.id.rename -> clickListener?.onRename(it)
                R.id.delete -> clickListener?.onDelete(it)
                else -> {}
            }
        }
        return false
    }

    companion object {
        fun from(parent: ViewGroup): FeatureViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = ListItemFeatureBinding.inflate(layoutInflater, parent, false)
            return FeatureViewHolder(binding)
        }
    }
}

class DisplayFeatureDiffCallback : DiffUtil.ItemCallback<DisplayFeature>() {
    override fun areItemsTheSame(oldItem: DisplayFeature, newItem: DisplayFeature): Boolean {
        return oldItem.id == newItem.id
    }
    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: DisplayFeature, newItem: DisplayFeature): Boolean {
        return oldItem == newItem
    }
}

class FeatureClickListener(private val onRenameListener: (feature: DisplayFeature) -> Unit,
                           private val onDeleteListener: (feature: DisplayFeature) -> Unit,
                           private val onAddListener: (feature: DisplayFeature) -> Unit,
                           private val onHistoryListener: (feature: DisplayFeature) -> Unit) {
    fun onRename(feature: DisplayFeature) = onRenameListener(feature)
    fun onDelete(feature: DisplayFeature) = onDeleteListener(feature)
    fun onAdd(feature: DisplayFeature) = onAddListener(feature)
    fun onHistory(feature: DisplayFeature) = onHistoryListener(feature)
}
