package com.example.sportsgd.presentation.adapters

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.sportsgd.R
import com.example.sportsgd.databinding.ItemNotificationBinding
import com.example.sportsgd.domain.model.AppNotification
import com.example.sportsgd.domain.model.NotificationType
import java.text.DateFormat
import java.util.Date
import java.util.Locale

class NotificationAdapter(
    private val onNotificationClick: (AppNotification) -> Unit,
) : ListAdapter<AppNotification, NotificationAdapter.NotificationViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val binding = ItemNotificationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NotificationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class NotificationViewHolder(
        private val binding: ItemNotificationBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(notification: AppNotification) = with(binding) {
            tvNotificationTitle.text = notification.title
            tvNotificationMessage.text = notification.message
            tvNotificationDate.text = formatDate(notification.createdAt)
            viewUnreadIndicator.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(root.context, notification.accentColor()),
            )
            root.alpha = if (notification.isRead) READ_ALPHA else UNREAD_ALPHA
            root.setOnClickListener { onNotificationClick(notification) }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<AppNotification>() {
        override fun areItemsTheSame(
            oldItem: AppNotification,
            newItem: AppNotification,
        ): Boolean = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: AppNotification,
            newItem: AppNotification,
        ): Boolean = oldItem == newItem
    }

    private companion object {
        const val READ_ALPHA = 0.88f
        const val UNREAD_ALPHA = 1f

        fun formatDate(timestamp: Long): String = DateFormat.getDateTimeInstance(
            DateFormat.MEDIUM,
            DateFormat.SHORT,
            Locale.forLanguageTag("es-MX"),
        ).format(Date(timestamp))
    }
}

private fun AppNotification.accentColor(): Int = when {
    title.contains("evidencia", ignoreCase = true) -> R.color.sportsgd_error
    type == NotificationType.GOAL -> R.color.sportsgd_success
    type == NotificationType.ACTIVITY -> R.color.sportsgd_warning
    type == NotificationType.ROUTINE -> R.color.sportsgd_primary
    else -> R.color.sportsgd_info
}
