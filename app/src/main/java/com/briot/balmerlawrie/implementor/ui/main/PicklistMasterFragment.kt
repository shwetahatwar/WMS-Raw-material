package com.briot.balmerlawrie.implementor.ui.main

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.briot.balmerlawrie.implementor.R
import kotlinx.android.synthetic.main.home_fragment.*
import kotlinx.android.synthetic.main.picklist_master_fragment.*

class PicklistMasterFragment : Fragment() {

    companion object {
        fun newInstance() = PicklistMasterFragment()
    }

    private lateinit var viewModel: PicklistMasterViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.picklist_master_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // TODO: Use the ViewModel
        viewModel = ViewModelProvider(this).get(PicklistMasterViewModel::class.java)

        (this.activity as AppCompatActivity).setTitle("Picklist Master")

        picklist_master_layout.setOnClickListener { Navigation.findNavController(it).navigate(R.id.action_picklistMasterFragment_to_pickingFragment) }

    }

}