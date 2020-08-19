package com.briot.balmerlawrie.implementor.ui.main

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.briot.balmerlawrie.implementor.R

class IssueToProductionFragment : Fragment() {

    companion object {
        fun newInstance() = IssueToProductionFragment()
    }

    private lateinit var viewModel: IssueToProductionViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.issue_to_production_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // TODO: Use the ViewModel
        viewModel = ViewModelProvider(this).get(IssueToProductionViewModel::class.java)

        (this.activity as AppCompatActivity).setTitle("Issue To Production")
    }

}