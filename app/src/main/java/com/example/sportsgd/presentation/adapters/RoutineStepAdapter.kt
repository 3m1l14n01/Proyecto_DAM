package com.example.sportsgd.presentation.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.sportsgd.databinding.ItemRoutineStepBinding
import com.example.sportsgd.domain.model.RoutineStep

class RoutineStepAdapter(
    private val onStepChecked: (RoutineStep, Boolean) -> Unit,
) : ListAdapter<RoutineStep, RoutineStepAdapter.RoutineStepViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoutineStepViewHolder {
        val binding = ItemRoutineStepBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RoutineStepViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RoutineStepViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class RoutineStepViewHolder(
        private val binding: ItemRoutineStepBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(step: RoutineStep) = with(binding) {
            tvStepTitle.text = step.title
            tvStepDescription.text = step.description.ifBlank { formatDuration(step.durationSeconds) }

            cbStepCompleted.setOnCheckedChangeListener(null)
            cbStepCompleted.isChecked = step.isCompleted
            cbStepCompleted.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked != step.isCompleted) onStepChecked(step, isChecked)
            }
            root.setOnClickListener { cbStepCompleted.performClick() }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<RoutineStep>() {
        override fun areItemsTheSame(oldItem: RoutineStep, newItem: RoutineStep): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: RoutineStep, newItem: RoutineStep): Boolean =
            oldItem == newItem
    }

    private companion object {
        fun formatDuration(seconds: Int): String {
            val minutes = seconds.coerceAtLeast(0) / 60
            val remainingSeconds = seconds.coerceAtLeast(0) % 60
            return when {
                minutes > 0 && remainingSeconds > 0 -> "${minutes} min ${remainingSeconds} s"
                minutes > 0 -> "${minutes} min"
                else -> "${remainingSeconds} s"
            }
        }
    }
}
