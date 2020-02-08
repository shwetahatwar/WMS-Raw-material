package com.briot.balmerlawrie.implementor.ui.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider

import com.briot.balmerlawrie.implementor.R

class DispatchPickingListDetailsFragment : Fragment() {

    companion object {
        fun newInstance() = DispatchPickingListDetailsFragment()
    }

    private lateinit var viewModel: DispatchPickingListDetailsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dispatch_picking_list_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(DispatchPickingListDetailsViewModel::class.java)

        (this.activity as AppCompatActivity).setTitle("Picking")

        // TODO: Use the ViewModel
    }

}
