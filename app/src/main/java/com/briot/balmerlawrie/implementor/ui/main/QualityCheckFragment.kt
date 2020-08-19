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
import kotlinx.android.synthetic.main.quality_check_fragment.*

class QualityCheckFragment : Fragment() {

    companion object {
        fun newInstance() = QualityCheckFragment()
    }

    private lateinit var viewModel: QualityCheckViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.quality_check_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(QualityCheckViewModel::class.java)

        (this.activity as AppCompatActivity).setTitle("Quality Check")

        qc_pending.setOnClickListener { Navigation.findNavController(it).navigate(R.id.action_qualityCheckFragment_to_QCPendingFragment) }
        material_qc_status.setOnClickListener { Navigation.findNavController(it).navigate(R.id.action_qualityCheckFragment_to_materialQCStatusFragment) }

        // TODO: Use the ViewModel
    }

}