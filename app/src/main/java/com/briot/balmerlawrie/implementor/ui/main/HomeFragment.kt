package com.briot.balmerlawrie.implementor.ui.main

import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
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
import com.briot.balmerlawrie.implementor.BuildConfig
import com.briot.balmerlawrie.implementor.repository.local.PrefConstants
import com.briot.balmerlawrie.implementor.repository.local.PrefRepository


class HomeFragment : androidx.fragment.app.Fragment() {

    companion object {
        fun newInstance() = HomeFragment()
    }

    private lateinit var viewModel: HomeViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.home_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(HomeViewModel::class.java)

        (this.activity as AppCompatActivity).setTitle("Home")

        /*this.viewModel.roleAccessRelations.observe(this, Observer<Array<RoleAccessRelation>> {
            if (it != null) {
                val roleName = PrefRepository.singleInstance.getValueOrDefault(PrefConstants().ROLE_NAME, "")
                val roleId = PrefRepository.singleInstance.getValueOrDefault(PrefConstants().ROLE_ID, "0").toInt()
                if (roleName.toLowerCase().equals("admin")) {
                    viewStatus(true)
                } else {
                    for (item in it) {
                        if (item.roleId?.id == roleId) {
                            if (item.accessId?.uri?.toLowerCase().equals("/JobProcessSequenceRelation/create".toLowerCase())) {
                                materialDetails.visibility = View.VISIBLE
                            } else if (item.accessId?.uri?.toLowerCase().equals("jobProcessSequenceRelation/update".toLowerCase())) {
                                materialInward.visibility =  View.VISIBLE
                            } else if (item.accessId?.uri?.toLowerCase().equals("joblocationrelation".toLowerCase())) {
                                materialPicking.visibility = View.VISIBLE
                                receiveAtStore.visibility = View.VISIBLE
                            } else if (item.accessId?.uri?.toLowerCase().equals("MaintenanceTransaction".toLowerCase())) {
                                machineMaintenance.visibility = View.VISIBLE
                            }

                        }
                    }
                }

            }

        })*/

//        this.viewModel.loadRoleAccess()
        versiontext.text = "app version " + BuildConfig.VERSION_NAME;

        // hide all options initially,  enable it as per role only
        viewStatus(true)

        materialDetails.setOnClickListener { Navigation.findNavController(it).navigate(R.id.action_homeFragment_to_materialDetailsScanFragment) }
        materialDetails.setOnClickListener { Navigation.findNavController(it).navigate(R.id.action_homeFragment_to_materialDetailsScanFragment) }
        materialInward.setOnClickListener { Navigation.findNavController(it).navigate(R.id.action_homeFragment_to_materialInwardFragment) }
        materialPicking.setOnClickListener { Navigation.findNavController(it).navigate(R.id.action_homeFragment_to_dispatchPickingListsFragment) }
        materialLoading.setOnClickListener { Navigation.findNavController(it).navigate(R.id.action_homeFragment_to_dispatchSlipsFragment) }
        auditProject.setOnClickListener { Navigation.findNavController(it).navigate(R.id.action_homeFragment_to_auditProjectsFragment) }
    }

    fun viewStatus(show: Boolean) {
        if (show) {
//            materialDetails.visibility = View.VISIBLE
            materialLoading.visibility = View.VISIBLE
            materialDetails.visibility = View.VISIBLE
            materialInward.visibility = View.VISIBLE
            materialPicking.visibility = View.VISIBLE
        } else {
//            materialDetails.visibility = View.GONE
            materialLoading.visibility = View.VISIBLE
            materialDetails.visibility = View.GONE
            materialInward.visibility = View.GONE
            materialPicking.visibility = View.GONE
        }

    }

}
