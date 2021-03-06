/* 
* This file is part of Track & Graph
* 
* Track & Graph is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* 
* Track & Graph is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
* 
* You should have received a copy of the GNU General Public License
* along with Foobar.  If not, see <https://www.gnu.org/licenses/>.
*/
package com.samco.trackandgraph.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.cardview.widget.CardView
import com.samco.trackandgraph.databinding.GraphStatCardViewBinding
import com.samco.trackandgraph.databinding.GraphStatViewBinding

class GraphStatCardView : GraphStatViewBase {
    private lateinit var binding: GraphStatCardViewBinding
    lateinit var cardView: CardView
        private set

    constructor(context: Context) : super(context)
    constructor(context: Context, attrSet: AttributeSet) : super(context, attrSet)

    var menuButtonClickListener: ((v: View) -> Unit)? = null

    init {
        listenToMenuButton()
    }

    private fun listenToMenuButton() {
        binding.menuButton.setOnClickListener {
            menuButtonClickListener?.invoke(binding.menuButton)
        }
    }

    fun hideMenuButton() {
        binding.menuButton.visibility = View.GONE
    }

    override fun getBinding(): GraphStatViewBinding {
        binding = GraphStatCardViewBinding.inflate(LayoutInflater.from(context), this, true)
        cardView = binding.demoCardView
        return binding.graphStatView
    }
}