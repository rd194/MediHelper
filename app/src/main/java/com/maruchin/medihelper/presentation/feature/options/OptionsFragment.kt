package com.maruchin.medihelper.presentation.feature.options

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.maruchin.medihelper.R
import com.maruchin.medihelper.databinding.FragmentOptionsBinding
import com.maruchin.medihelper.presentation.dialogs.ConfirmDialog
import com.maruchin.medihelper.presentation.feature.options.saved_types.MedicineTypesDialog
import com.maruchin.medihelper.presentation.feature.options.saved_types.MedicineUnitsDialog
import com.maruchin.medihelper.presentation.framework.BaseHomeFragment
import com.maruchin.medihelper.presentation.framework.hideOnScroll
import com.maruchin.medihelper.presentation.utils.LoadingScreen
import kotlinx.android.synthetic.main.fragment_options.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class OptionsFragment : BaseHomeFragment<FragmentOptionsBinding>(R.layout.fragment_options) {

    private val viewModel: OptionsViewModel by viewModel()
    private val loadingScreen: LoadingScreen by inject()

    fun onClickChangePassword() {
        findNavController().navigate(OptionsFragmentDirections.toChangePasswordDialog())
    }

    fun onClickSignOut() {
        ConfirmDialog(
            title = "Wyloguj",
            message = "Czy na pewno chcesz się wylogować z aplikacji?",
            iconResId = R.drawable.baseline_exit_to_app_black_36
        ).apply {
            setOnConfirmClickListener {
                viewModel.signOutUser()
            }
        }.show(childFragmentManager)
    }

    fun onClickNotificationsHelp() {
        HelpDialog(
            helpItems = viewModel.getNotificationsHelp()
        ).show(childFragmentManager)
    }

    fun onClickSavedMedicineTypes() {
        MedicineTypesDialog().show(childFragmentManager)
    }

    fun onClickSavedMedicineUnits() {
        MedicineUnitsDialog().show(childFragmentManager)
    }

    fun onClickShareApp() {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "Polecam  aplikację MediHelper do zarządzania domową apteczką: https://play.google.com/apps/testing/pl.dgcs.ringling")
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.bindingViewModel = viewModel
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setColorPrimaryStatusBas()
        bindLoadingScreen()
        setupFabScrollBehavior()
        setupMedicinesRemindingOptions()
        observeViewModel()
    }

    private fun setColorPrimaryStatusBas() {
        super.setStatusBarColor(R.color.colorPrimary)
    }

    private fun bindLoadingScreen() {
        loadingScreen.bind(this, viewModel.loadingInProgress)
    }

    private fun setupFabScrollBehavior() {
        fab_share.hideOnScroll(scroll_view)
    }

    private fun setupMedicinesRemindingOptions() {
        switch_notifications_enabled.setOnCheckedChangeListener { _, checked ->
            viewModel.setNotificationsEnabled(checked)
        }
    }

    private fun observeViewModel() {
        viewModel.actionSignOutSuccessful.observe(viewLifecycleOwner, Observer {
            super.mainActivity.restartApp()
        })
    }
}