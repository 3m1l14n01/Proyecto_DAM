package com.example.sportsgd.presentation.fragments

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.example.sportsgd.R
import com.example.sportsgd.databinding.FragmentCheckEmailBinding

class CheckEmailFragment : BaseAppFragment(R.layout.fragment_check_email) {
    override val darkStatusBarIcons: Boolean = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        FragmentCheckEmailBinding.bind(view).btnReturnToLogin.setOnClickListener {
            findNavController().navigateClearingSession(R.id.loginFragment)
        }
    }
}
