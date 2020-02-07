package com.briot.balmerlawrie.implementor.ui.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider

import com.briot.balmerlawrie.implementor.R

class DispatchPickingListsFragment : Fragment() {

    companion object {
        fun newInstance() = DispatchPickingListsFragment()
    }

    private lateinit var viewModel: DispatchPickingListsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dispatch_picking_lists_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(DispatchPickingListsViewModel::class.java)

        (this.activity as AppCompatActivity).setTitle("Picking Dispatch Slips")

        // TODO: Use the ViewModel
    }

}
