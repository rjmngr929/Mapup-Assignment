package com.my.mapupassessment.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.my.mapupassessment.R
import com.my.mapupassessment.data.local.entities.SessionEntity
import com.my.mapupassessment.utils.Helper.formatDistance
import com.my.mapupassessment.utils.Helper.formatTime

class SessionHistoryAdapter(private val onClick: (SessionEntity) -> Unit) :
    ListAdapter<SessionEntity, SessionHistoryAdapter.SessionViewHolder>(DIFF) {

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<SessionEntity>() {
            override fun areItemsTheSame(oldItem: SessionEntity, newItem: SessionEntity) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: SessionEntity, newItem: SessionEntity) = oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        SessionViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.session_item, parent, false))

    override fun onBindViewHolder(holder: SessionViewHolder, position: Int) = holder.bind(getItem(position))

    inner class SessionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(session: SessionEntity) {
            itemView.findViewById<TextView>(R.id.tvSessionName).text = String.format("Session #%s", session.id)
            itemView.findViewById<TextView>(R.id.tvSessionInfo).text = String.format("Distance: %s | Duration: %s", formatDistance(session.distance) , formatTime(session.duration))

            itemView.setOnClickListener { onClick(session) }
        }
    }
}