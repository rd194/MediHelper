package com.maruchin.medihelper.presentation.feature.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.maruchin.medihelper.R
import com.maruchin.medihelper.databinding.FragmentLauncherPatronConnectBinding
import com.maruchin.medihelper.presentation.feature.launcher.LauncherActivity
import com.maruchin.medihelper.presentation.feature.launcher.LauncherOptionFragment
import com.maruchin.medihelper.presentation.framework.bind
import com.maruchin.medihelper.presentation.framework.showShortSnackbar
import com.maruchin.medihelper.presentation.utils.LoadingScreen
import com.google.android.material.textfield.TextInputEditText
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.android.synthetic.main.fragment_launcher_patron_connect.*
import org.koin.android.ext.android.inject

class PatronConnectFragmentLauncher : LauncherOptionFragment() {

    private val viewModel: PatronConnectViewModel by inject()
    private val loadingScreen: LoadingScreen by inject()
    private val launcherActivity: LauncherActivity
        get() = requireActivity() as LauncherActivity

    fun onClickScanCode() = viewModel.scanQrCode(this)

    fun onClickConfirm() = viewModel.loadProfileData()

    fun onClickBack() = findNavController().popBackStack()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return bind<FragmentLauncherPatronConnectBinding>(
            inflater = inflater,
            container = container,
            layoutResId = R.layout.fragment_launcher_patron_connect,
            viewModel = viewModel
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupKeyEditTexts()
        observeViewModel()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        result?.contents?.let { connectionKey ->
            viewModel.setConnectionKey(connectionKey)
        }
    }

    private fun observeViewModel() {
        viewModel.errorConnectionKey.observe(viewLifecycleOwner, Observer { errorMessage ->
            if (errorMessage != null) {
                showShortSnackbar(rootLayout = root_lay, message = errorMessage)
            }
        })
        viewModel.loadingInProgress.observe(viewLifecycleOwner, Observer { inProgress ->
            if (inProgress == true) {
                loadingScreen.showLoadingScreen(childFragmentManager)
            } else {
                loadingScreen.closeLoadingScreen()
            }
        })
        viewModel.errorPatronConnect.observe(viewLifecycleOwner, Observer { errorMessage ->
            if (errorMessage == null) {
                launcherActivity.startMainActivity()
            } else {
                showShortSnackbar(rootLayout = root_lay, message = errorMessage)
            }
        })
    }

    private fun setupKeyEditTexts() {
        val etxList = listOf(etx_key_1, etx_key_2, etx_key_3, etx_key_4, etx_key_5, etx_key_6)
        for (i in etxList.indices) {
            if ((i + 1) < etxList.size) {
                addTextChangeListener(etxList[i], etxList[i + 1])
            }
        }
    }

    private fun addTextChangeListener(currEtx: TextInputEditText, nextEtx: TextInputEditText) {
        currEtx.addTextChangedListener {
            if (it.toString().length == 1) {
                currEtx.clearFocus()
                nextEtx.run {
                    requestFocus()
                    isCursorVisible = true
                }
            }
        }
    }
}