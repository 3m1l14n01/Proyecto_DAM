package com.example.sportsgd.presentation.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.sportsgd.R
import com.example.sportsgd.databinding.ItemPlayerBinding
import com.example.sportsgd.domain.model.Player
import com.example.sportsgd.domain.model.PlayerStatus

class PlayerAdapter(
    private val onPlayerClick: (Player) -> Unit,
) : ListAdapter<Player, PlayerAdapter.PlayerViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
        val binding = ItemPlayerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PlayerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PlayerViewHolder(
        private val binding: ItemPlayerBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(player: Player) = with(binding) {
            tvPlayerInitials.text = player.initials()
            tvPlayerName.text = player.fullName
            tvPlayerMeta.text = "${player.sport} · ${player.semester}.º semestre"
            tvPlayerStatus.text = "●"
            tvPlayerStatus.contentDescription = "Estado: ${player.status.displayName()}"
            tvPlayerStatus.setTextColor(
                ContextCompat.getColor(root.context, player.status.colorResource()),
            )
            root.setOnClickListener { onPlayerClick(player) }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<Player>() {
        override fun areItemsTheSame(oldItem: Player, newItem: Player): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Player, newItem: Player): Boolean =
            oldItem == newItem
    }
}

private fun Player.initials(): String = listOf(firstName, lastName)
    .mapNotNull { it.trim().firstOrNull()?.uppercaseChar() }
    .joinToString("")
    .take(2)

private fun PlayerStatus.displayName(): String = when (this) {
    PlayerStatus.ACTIVE -> "Activo"
    PlayerStatus.INJURED -> "Lesionado"
    PlayerStatus.RECOVERING -> "En recuperación"
}

private fun PlayerStatus.colorResource(): Int = when (this) {
    PlayerStatus.ACTIVE -> R.color.sportsgd_success
    PlayerStatus.INJURED -> R.color.sportsgd_error
    PlayerStatus.RECOVERING -> R.color.sportsgd_warning
}
