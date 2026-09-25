package com.example.sportsgd.presentation.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.sportsgd.R
import com.example.sportsgd.SportsGdApplication
import com.example.sportsgd.databinding.FragmentActivityDetailBinding
import com.example.sportsgd.presentation.scheduleText
import com.example.sportsgd.presentation.statusText
import com.example.sportsgd.presentation.viewmodels.ActivityDetailViewModel
import com.example.sportsgd.presentation.viewmodels.AppViewModelFactory
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class ActivityDetailFragment : BaseAppFragment(R.layout.fragment_activity_detail) {
    private var binding: FragmentActivityDetailBinding? = null
    private val activityId: Long by lazy { arguments?.getLong("activityId", -1L) ?: -1L }
    private val viewModel: ActivityDetailViewModel by viewModels {
        AppViewModelFactory(
            application = requireActivity().application as SportsGdApplication,
            activityId = activityId,
        )
    }
    private var missingActivityHandled = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val currentBinding = FragmentActivityDetailBinding.bind(view)
        binding = currentBinding
        setupAppChrome(view)
        currentBinding.btnBackToCalendar.setOnClickListener { findNavController().popBackStack() }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    val safeBinding = binding ?: return@collect
                    state.activity?.let { activity ->
                        safeBinding.tvActivityTitle.text = activity.title
                        safeBinding.tvActivitySchedule.text = activity.scheduleText()
                        safeBinding.tvActivityStatus.text = activity.statusText()
                        safeBinding.tvActivityDescription.text = activity.description
                    }
                    if (state.notFound && !missingActivityHandled) {
                        missingActivityHandled = true
                        Snackbar.make(view, "No se encontró la actividad solicitada.", Snackbar.LENGTH_LONG).show()
                        findNavController().popBackStack()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }
}
