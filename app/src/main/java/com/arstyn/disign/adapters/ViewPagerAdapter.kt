package com.arstyn.disign.adapters

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.arstyn.disign.R
import kotlinx.android.synthetic.main.activity_welcome.view.*
import kotlinx.android.synthetic.main.viewpager_item.view.*

class ViewPagerAdapter(private val text: List<String>, private val image: List<Int>, private val subText: List<String>) :
    RecyclerView.Adapter<ViewPagerAdapter.Pager2ViewHolder>() {

    inner class Pager2ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        fun bind(text: String, image: Int, subText: String) {
            itemView.viewpagerText.text = text
            itemView.imageViewInWelcome.setImageResource(image)
            itemView.viewPagerSubText.text = subText
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewPagerAdapter.Pager2ViewHolder {
        return Pager2ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.viewpager_item, parent, false))
    }

    override fun getItemCount(): Int {
        return text.size
    }

    override fun onBindViewHolder(holder: ViewPagerAdapter.Pager2ViewHolder, position: Int) {
        holder.bind(text[position], image[position], subText[position])
    }
}