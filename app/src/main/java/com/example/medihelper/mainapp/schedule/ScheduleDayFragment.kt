package com.example.medihelper.mainapp.schedule


import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.medihelper.AppDate
import com.example.medihelper.R
import com.example.medihelper.custom.RecyclerAdapter
import com.example.medihelper.custom.RecyclerItemViewHolder
import com.example.medihelper.databinding.FragmentScheduleDayBinding
import com.example.medihelper.localdatabase.pojos.PlannedMedicineItem
import kotlinx.android.synthetic.main.fragment_schedule_day.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import java.sql.Date


class ScheduleDayFragment : Fragment() {
    private val TAG = ScheduleDayFragment::class.simpleName

    var date: AppDate? = null
    val plannedMedicinesAvailableLive = MutableLiveData(false)
    private val viewModel: ScheduleViewModel by sharedViewModel(from = { parentFragment!! })
    private val directions by lazyOf(ScheduleFragmentDirections)

    fun onClickOpenPlannedMedicineOptions(plannedMedicineID: Int) {
        val parentFrag = parentFragment as ScheduleFragment
        parentFrag.findNavController().navigate(directions.toPlannedMedicineOptionsDialog(plannedMedicineID))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding: FragmentScheduleDayBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_schedule_day, container, false)
        binding.handler = this
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeViewModel()
    }

    private fun observeViewModel() {
        if (date != null) {
            viewModel.getPlannedMedicineItemListByDateLive(date!!).observe(viewLifecycleOwner, Observer { plannedMedicineItemList ->
                Log.d(TAG, "date = $date, scheduledMedicinesList change = $plannedMedicineItemList")
                val adapter = recycler_view_scheduled_medicine_for_day.adapter as PlannedMedicineAdapter
                adapter.updateItemsList(plannedMedicineItemList.sortedBy { plannedMedicineItem -> plannedMedicineItem.plannedTime })
                plannedMedicinesAvailableLive.value = !plannedMedicineItemList.isNullOrEmpty()
            })
        }
    }

    private fun setupRecyclerView() {
        recycler_view_scheduled_medicine_for_day.adapter = PlannedMedicineAdapter()
    }

    // Inner classes
    inner class PlannedMedicineAdapter : RecyclerAdapter<PlannedMedicineItem>(
        layoutResId = R.layout.recycler_item_planned_medicine,
        areItemsTheSameFun = { oldItem, newItem -> oldItem.plannedMedicineID == newItem.plannedMedicineID }
    ) {
        override fun onBindViewHolder(holder: RecyclerItemViewHolder, position: Int) {
            val plannedMedicine = itemsList[position]
            holder.bind(plannedMedicine, this@ScheduleDayFragment)
        }
    }
}


