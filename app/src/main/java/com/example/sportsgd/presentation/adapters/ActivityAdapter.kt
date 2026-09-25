package com.example.sportsgd.presentation.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.sportsgd.databinding.ItemActivityBinding
import com.example.sportsgd.domain.model.ActivityType
import com.example.sportsgd.domain.model.SportActivity
import java.text.DateFormat
import java.util.Date
import java.util.Locale

class ActivityAdapter(
    private val onActivityClick: (SportActivity) -> Unit,
) : ListAdapter<SportActivity, ActivityAdapter.ActivityViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityViewHolder {
        val binding = ItemActivityBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ActivityViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ActivityViewHolder(
        private val binding: ItemActivityBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(activity: SportActivity) = with(binding) {
            tvActivityTitle.text = activity.title
            tvActivityDate.text = formatDate(activity.startAt)
            tvActivityMeta.text = listOf(activity.type.displayName(), activity.location)
                .filter(String::isNotBlank)
                .joinToString(" · ")
            root.setOnClickListener { onActivityClick(activity) }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<SportActivity>() {
        override fun areItemsTheSame(oldItem: SportActivity, newItem: SportActivity): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: SportActivity, newItem: SportActivity): Boolean =
            oldItem == newItem
    }

    private companion object {
        fun formatDate(timestamp: Long): String = DateFormat.getDateTimeInstance(
            DateFormat.MEDIUM,
            DateFormat.SHORT,
            Locale.forLanguageTag("es-MX"),
        ).format(Date(timestamp))
    }
}

private fun ActivityType.displayName(): String = when (this) {
    ActivityType.TRAINING -> "Entrenamiento"
    ActivityType.MATCH -> "Partido"
    ActivityType.ACADEMIC -> "Académica"
    ActivityType.OTHER -> "Actividad"
}
