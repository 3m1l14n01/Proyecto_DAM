package com.example.sportsgd.presentation.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sportsgd.R
import com.example.sportsgd.SportsGdApplication
import com.example.sportsgd.databinding.FragmentNotificationsBinding
import com.example.sportsgd.domain.model.AppNotification
import com.example.sportsgd.presentation.adapters.NotificationAdapter
import com.example.sportsgd.presentation.viewmodels.AppViewModelFactory
import com.example.sportsgd.presentation.viewmodels.NotificationsViewModel
import kotlinx.coroutines.launch

class NotificationsFragment : BaseAppFragment(R.layout.fragment_notifications) {
    private var binding: FragmentNotificationsBinding? = null
    private val viewModel: NotificationsViewModel by viewModels {
        AppViewModelFactory(requireActivity().application as SportsGdApplication)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val currentBinding = FragmentNotificationsBinding.bind(view)
        binding = currentBinding
        setupAppChrome(view, MainSection.HOME)
        val adapter = NotificationAdapter(::openNotification)
        currentBinding.rvNotifications.layoutManager = LinearLayoutManager(requireContext())
        currentBinding.rvNotifications.adapter = adapter
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state -> adapter.submitList(state.notifications) }
            }
        }
    }

    private fun openNotification(notification: AppNotification) {
        viewModel.markAsRead(notification)
        val parts = notification.destination?.split('/') ?: emptyList()
        val id = parts.getOrNull(1)?.toLongOrNull() ?: -1L
        when (parts.firstOrNull()) {
            "activity" -> findNavController().navigateSafely(
                R.id.activityDetailFragment,
                Bundle().apply { putLong("activityId", id) },
            )
            "routine" -> findNavController().navigateSafely(
                R.id.routineActiveFragment,
                Bundle().apply { putLong("routineId", id) },
            )
            "goal" -> findNavController().navigateSafely(R.id.goalsFragment)
        }
    }

    override fun onDestroyView() {
        binding?.rvNotifications?.adapter = null
        binding = null
        super.onDestroyView()
    }
}
