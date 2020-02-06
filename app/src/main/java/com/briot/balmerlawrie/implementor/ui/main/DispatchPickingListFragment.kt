package com.briot.balmerlawrie.implementor.ui.main

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity

import com.briot.balmerlawrie.implementor.R

class DispatchPickingListFragment : Fragment() {

    companion object {
        fun newInstance() = DispatchPickingListFragment()
    }

    private lateinit var viewModel: DispatchPickingListViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dispatch_picking_list_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(DispatchPickingListViewModel::class.java)

        (this.activity as AppCompatActivity).setTitle("Material Picking List")

        // TODO: Use the ViewModel
    }

}
