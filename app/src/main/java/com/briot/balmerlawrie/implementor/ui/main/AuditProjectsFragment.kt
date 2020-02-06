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

class AuditProjectsFragment : Fragment() {

    companion object {
        fun newInstance() = AuditProjectsFragment()
    }

    private lateinit var viewModel: AuditProjectsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.audit_projects_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(AuditProjectsViewModel::class.java)

        (this.activity as AppCompatActivity).setTitle("Audit Projects")

        // TODO: Use the ViewModel
    }

}
