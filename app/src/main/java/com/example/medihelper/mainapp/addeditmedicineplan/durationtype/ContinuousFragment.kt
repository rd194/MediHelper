package com.example.medihelper.mainapp.addeditmedicineplan.durationtype


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders

import com.example.medihelper.R
import com.example.medihelper.dialogs.SelectDateDialog
import com.example.medihelper.databinding.FragmentScheduleTypeContinuousBinding
import com.example.medihelper.mainapp.addeditmedicineplan.AddEditMedicinePlanViewModel

class ContinuousFragment : Fragment() {
    private val TAG = ContinuousFragment::class.simpleName

    private lateinit var planViewModel: AddEditMedicinePlanViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.run {
            planViewModel = ViewModelProviders.of(this).get(AddEditMedicinePlanViewModel::class.java)
        } ?: throw Exception("Invalid Activity")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return bindLayout(inflater, container)
    }

    fun onClickSelectDate() {
        val dialog = SelectDateDialog()
        dialog.defaultDate = planViewModel.startDateLive.value
        dialog.setDateSelectedListener { date ->
            planViewModel.startDateLive.value = date
        }
        dialog.show(childFragmentManager, dialog.TAG)
    }

    private fun bindLayout(inflater: LayoutInflater, container: ViewGroup?): View {
        val binding: FragmentScheduleTypeContinuousBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_schedule_type_continuous, container, false)
        binding.viewModel = planViewModel
        binding.handler = this
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }
}