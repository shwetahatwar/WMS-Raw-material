package com.briot.balmerlawrie.implementor.ui.main

import android.content.ContentValues
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.briot.balmerlawrie.implementor.R
//import com.briot.balmerlawrie.implementor.repository.remote.RoleAccessRelation
import kotlinx.android.synthetic.main.home_fragment.*
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.briot.balmerlawrie.implementor.BuildConfig
import com.briot.balmerlawrie.implementor.UiHelper
import com.briot.balmerlawrie.implementor.repository.local.PrefConstants
import com.briot.balmerlawrie.implementor.repository.local.PrefRepository
import com.briot.balmerlawrie.implementor.repository.remote.PickingDashboardData
import com.briot.balmerlawrie.implementor.repository.remote.PutawayDashboardData
import io.github.pierry.progress.Progress


class HomeFragment : androidx.fragment.app.Fragment() {

    companion object {
        fun newInstance() = HomeFragment()
    }

    private var oldPutawayDashboardItems: PutawayDashboardData? = null
    private var oldPickingDashboardItems: PickingDashboardData? = null
    private lateinit var viewModel: HomeViewModel
    private var progress: Progress? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView =  inflater.inflate(R.layout.home_fragment, container, false)
        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        viewModel.loadPutawayDashboardItems()
        viewModel.loadPickingsDashboardItems()
        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (this.activity as AppCompatActivity).setTitle("Dashboard")

        if (this.arguments != null) {
            viewModel.totalData = this.arguments!!.getInt("totalData")
            viewModel.pendingForPutaway = this.arguments!!.getInt("pendingForPutaway")

            viewModel.inProgress = this.arguments!!.getInt("inProgress")
            viewModel.pending = this.arguments!!.getInt("pending")
            viewModel.completed = this.arguments!!.getInt("completed")
            viewModel.total = this.arguments!!.getInt("total")

        }
        // recyclerView.adapter = SimpleDashboardItemAdapter(recyclerView, viewModel.putawayDashboardData, viewModel)
        viewModel.putawayDashboardData.observe(viewLifecycleOwner, Observer<PutawayDashboardData?> {
            if (it != null) {
                UiHelper.hideProgress(this.progress)
                this.progress = null

                pendingText.text = viewModel.putawayDashboardData.value?.responseData?.pendingForPutaway.toString()
                totalText.text = viewModel.putawayDashboardData.value?.responseData?.totalData.toString()

                if ( viewModel.putawayDashboardData.value == null) {
                    UiHelper.showSomethingWentWrongSnackbarMessage(this.activity as AppCompatActivity)
                } else if (it != oldPutawayDashboardItems) {
                    Log.d(ContentValues.TAG, "oldPutwayDashboard data")
                    //putawayItems.adapter?.notifyDataSetChanged()
                }
            }
            oldPutawayDashboardItems = viewModel.putawayDashboardData.value
        })

        viewModel.pickingDashboardData.observe(viewLifecycleOwner, Observer<PickingDashboardData?> {
            if (it != null) {
                UiHelper.hideProgress(this.progress)
                this.progress = null

                totalPickingValue.text = viewModel.total.toString()
                pickingValue.text = viewModel.completed.toString()
                pickingPendingValue.text = viewModel.pending.toString()
                pickingInProgressValue.text = viewModel.inProgress.toString()

                if ( viewModel.pickingDashboardData.value == null) {
                    UiHelper.showSomethingWentWrongSnackbarMessage(this.activity as AppCompatActivity)
                } else if (it != oldPickingDashboardItems) {
                    Log.d(ContentValues.TAG, "oldPutwayDashboard data")
                    //putawayItems.adapter?.notifyDataSetChanged()
                }
            }
            oldPickingDashboardItems = viewModel.pickingDashboardData.value
        })

        materialLoading.setOnClickListener { Navigation.findNavController(it).navigate(R.id.action_homeFragment_to_qualityCheckFragment) }
        materialPutaway.setOnClickListener { Navigation.findNavController(it).navigate(R.id.action_homeFragment_to_putawayFragment) }
       issue_to_production.setOnClickListener { Navigation.findNavController(it).navigate(R.id.action_homeFragment_to_issueToProductionFragment) }
        materialPicking.setOnClickListener { Navigation.findNavController(it).navigate(R.id.action_homeFragment_to_picklistMasterFragment) }

    }
}
