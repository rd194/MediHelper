package com.maruchin.medihelper.presentation.feature.options.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import com.maruchin.medihelper.R
import com.maruchin.medihelper.databinding.DialogChangePasswordBinding
import com.maruchin.medihelper.presentation.framework.BaseBottomDialog
import com.maruchin.medihelper.presentation.utils.LoadingScreen
import kotlinx.android.synthetic.main.dialog_change_password.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class ChangePasswordDialog : BaseBottomDialog<DialogChangePasswordBinding>(R.layout.dialog_change_password) {
    override val TAG: String
        get() = "ChangePasswordDialog"
    override val viewModel: ChangePasswordViewModel by viewModel()

    private val loadingScreen: LoadingScreen by inject()

    fun onClickConfirm() {
        viewModel.changePassword()
    }

    fun onClickCancel() {
        dismiss()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadingScreen.bind(this, viewModel.loadingInProgress)
        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.actionPasswordChanged.observe(viewLifecycleOwner, Observer {
            dismiss()
        })
        viewModel.errorGlobal.observe(viewLifecycleOwner, Observer { message ->
            Snackbar.make(root_lay, message, Snackbar.LENGTH_SHORT).show()
        })
    }
}