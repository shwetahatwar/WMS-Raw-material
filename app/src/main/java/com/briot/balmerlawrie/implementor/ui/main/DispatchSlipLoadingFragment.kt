package com.briot.balmerlawrie.implementor.ui.main

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider

import com.briot.balmerlawrie.implementor.R
import kotlinx.android.synthetic.main.dispatch_slip_loading_fragment.*

class DispatchSlipLoadingFragment : Fragment() {

    companion object {
        fun newInstance() = DispatchSlipLoadingFragment()
    }

    private lateinit var viewModel: DispatchSlipLoadingViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dispatch_slip_loading_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

//        Log.d("Loading", "successful selected loader's dispatch list details" + dispatchLoadingList.toString())
        viewModel = ViewModelProvider(this).get(DispatchSlipLoadingViewModel::class.java)
        (this.activity as AppCompatActivity).setTitle("Loading Dispatch Slip")

        if (this.arguments != null) {
            viewModel.dispatchSlipId = this.arguments!!.getInt("loadingDispatchSlip_id")
            viewModel.dispatchSlipVehicleNumber = this.arguments!!.getString("loadingDispatchSlip_vehicle_number")
            viewModel.dispatchSlipNumber = this.arguments!!.getString("loadingDispatchSlip_slipnumber")
            viewModel.dispatchSlipStatus = this.arguments!!.getString("loadingDispatchSlip_slipstatus")

            loading_dispatchSlipId.text = viewModel.dispatchSlipNumber
            loading_dispatchListStatusId.text = viewModel.dispatchSlipStatus
            loading_truckNumber.text = viewModel.dispatchSlipVehicleNumber
        }
    }
}
