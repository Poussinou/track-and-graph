package com.samco.trackandgraph.ui

import android.animation.ObjectAnimator
import android.view.View
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

abstract class FadeInListAdapter<T, G : RecyclerView.ViewHolder>(
    diffCallback: DiffUtil.ItemCallback<T>
) : ListAdapter<T, G>(diffCallback) {

    private var firstLoad = true

    override fun onBindViewHolder(holder: G, position: Int) {
        setAnimation(holder.itemView, position)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        recyclerView.itemAnimator = null
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                firstLoad = false
                super.onScrollStateChanged(recyclerView, newState)
            }
        })
    }

    private fun setAnimation(itemView: View, position: Int) {
        itemView.alpha = 0f
        itemView.refreshDrawableState()
        val animator = ObjectAnimator.ofFloat(itemView, "alpha", 0f, 1.0f)
        animator.startDelay = (if (firstLoad) position else 1) * getFadeStartDelay() / 3
        animator.duration = getFadeDuration()
        animator.start()
    }

    open fun getFadeStartDelay() = 250L
    open fun getFadeDuration() = 400L
}
