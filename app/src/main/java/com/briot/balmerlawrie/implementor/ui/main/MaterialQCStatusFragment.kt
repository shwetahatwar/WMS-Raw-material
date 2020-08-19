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
import kotlinx.android.synthetic.main.quality_check_fragment.*

class MaterialQCStatusFragment : Fragment() {

    companion object {
        fun newInstance() = MaterialQCStatusFragment()
    }

    private lateinit var viewModel: MaterialQCStatusViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.material_q_c_status_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MaterialQCStatusViewModel::class.java)

        (this.activity as AppCompatActivity).setTitle("Material QC Status")

        // TODO: Use the ViewModel
    }

}