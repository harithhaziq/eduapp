package com.makeit.eduapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.makeit.eduapp.R
import com.makeit.eduapp.model.Child

class ChildAdapter(private val childList: List<Child>) : RecyclerView.Adapter<ChildAdapter.ChildItemHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChildItemHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.child_item_holder, parent, false)
        return ChildItemHolder(itemView)
    }

    override fun onBindViewHolder(holder: ChildItemHolder, position: Int) {
        val currentChild = childList[position]
        holder.bind(currentChild)
    }

    override fun getItemCount() = childList.size

    inner class ChildItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val childNameTextView: TextView = itemView.findViewById(R.id.tv_name)
//        private val childAgeTextView: TextView = itemView.findViewById(R.id.textViewChildAge)

        fun bind(child: Child) {
            childNameTextView.text = child.name
//            childAgeTextView.text = child.age.toString()
        }
    }
}