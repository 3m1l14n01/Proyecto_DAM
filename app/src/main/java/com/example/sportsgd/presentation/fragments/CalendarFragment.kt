package com.example.sportsgd.presentation.fragments

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sportsgd.R
import com.example.sportsgd.SportsGdApplication
import com.example.sportsgd.databinding.FragmentCalendarBinding
import com.example.sportsgd.presentation.adapters.ActivityAdapter
import com.example.sportsgd.presentation.viewmodels.AppViewModelFactory
import com.example.sportsgd.presentation.viewmodels.CalendarViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.launch

class CalendarFragment : BaseAppFragment(R.layout.fragment_calendar) {
    private var binding: FragmentCalendarBinding? = null
    private val viewModel: CalendarViewModel by viewModels {
        AppViewModelFactory(requireActivity().application as SportsGdApplication)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val currentBinding = FragmentCalendarBinding.bind(view)
        binding = currentBinding
        setupAppChrome(view, MainSection.CALENDAR)
        val adapter = ActivityAdapter { activity ->
            findNavController().navigateSafely(
                R.id.activityDetailFragment,
                Bundle().apply { putLong("activityId", activity.id) },
            )
        }
        currentBinding.rvActivities.layoutManager = LinearLayoutManager(requireContext())
        currentBinding.rvActivities.adapter = adapter
        val now = Calendar.getInstance()
        currentBinding.tvCalendarMonth.text = SimpleDateFormat("MMMM yyyy", Locale.forLanguageTag("es-MX"))
            .format(now.time).uppercase(Locale.forLanguageTag("es-MX"))
        currentBinding.tvCalendarWeek.text = weekLabel(now)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    adapter.submitList(state.upcomingActivities)
                    binding?.tvCalendarEmpty?.isVisible = !state.isLoading && state.upcomingActivities.isEmpty()
                }
            }
        }
    }

    private fun weekLabel(calendar: Calendar): String {
        val start = calendar.clone() as Calendar
        start.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        return (0..4).joinToString(" · ") { offset ->
            val day = start.clone() as Calendar
            day.add(Calendar.DAY_OF_MONTH, offset)
            SimpleDateFormat("EEE d", Locale.forLanguageTag("es-MX")).format(day.time)
                .replaceFirstChar { it.uppercase() }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshTime()
    }

    override fun onDestroyView() {
        binding?.rvActivities?.adapter = null
        binding = null
        super.onDestroyView()
    }
}
