package com.briot.balmerlawrie.implementor.ui.main

import android.content.ContentValues.TAG
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.briot.balmerlawrie.implementor.R
import com.briot.balmerlawrie.implementor.UiHelper
import com.briot.balmerlawrie.implementor.repository.local.PrefConstants
import com.briot.balmerlawrie.implementor.repository.remote.Project
import com.briot.balmerlawrie.implementor.repository.remote.auditProjectItem
import kotlinx.android.synthetic.main.audit_project_list_fragment.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.w3c.dom.Text

class AuditProjectList : Fragment() {

    companion object {
        fun newInstance() = AuditProjectList()
    }

    private lateinit var viewModel: AuditProjectListViewModel
    lateinit var recyclerView: RecyclerView
    lateinit var auditsubmit: Button
//    lateinit var materialBarcode: TextView
//    lateinit var barcodeSerial: TextView
//    lateinit var batchSNumber: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.audit_project_list_fragment, container, false)
        this.recyclerView = rootView.findViewById(R.id.auditprojects_projectlist)
        recyclerView.layoutManager = LinearLayoutManager(this.activity)
        auditsubmit = rootView.findViewById(R.id.audit_items_submit_button)
        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(AuditProjectListViewModel::class.java)
        // TODO: Use the ViewModel

        viewModel.loadAuditProjects("In Progress")
        audit_materialBarcode.requestFocus()

        audit_scanButton.setOnClickListener {
            var thisObject = this
            var value = audit_materialBarcode.text!!.toString().trim()
            var arguments  = value.split("#")
            if (arguments.size < 3 || arguments[0].length == 0 || arguments[1].length == 0 || arguments[2].length == 0) {
                UiHelper.showErrorToast(this.activity as AppCompatActivity, "Please Enter barcode")
            } else {
                viewModel.material_barcode = arguments[0].toString()
                viewModel.batch_number = arguments[1].toString()
                viewModel.barcode_serial = value
                recyclerView.adapter = SimpleAuditItemAdapter(recyclerView, viewModel)
            }

            audit_materialBarcode.text?.clear()
            GlobalScope.launch {
            }
        };

        audit_items_submit_button.setOnClickListener {
            if (viewModel.barcode_serial != "") {
                var projectId = viewModel.projects.value?.get(0)!!.id
                var auditProjectItem = auditProjectItem()
                auditProjectItem.projectId = projectId
                auditProjectItem.serialNumber = viewModel.barcode_serial
                viewModel.updateAuditProjects(auditProjectItem)
            }else{
                UiHelper.showErrorToast(this.activity as AppCompatActivity, "Please provide barcode");
            }
        }
    }

    open class SimpleAuditItemAdapter(private val recyclerView: androidx.recyclerview.widget.RecyclerView,
                   private val viewModel: AuditProjectListViewModel):
            androidx.recyclerview.widget.RecyclerView.Adapter<SimpleAuditItemAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleAuditItemAdapter.ViewHolder {
            val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.audit_project_row, parent, false)

            return ViewHolder(itemView)
        }
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind()
            // val putawayItems = putawayItems.value!![position]!!
            holder.itemView.setOnClickListener{
                    Log.d(TAG, "on bind")
//                if (viewModel.putawayItems.toString().toLowerCase().contains("complete")) {
//                    return@setOnClickListener
//                }
            }
        }
        override fun getItemCount(): Int {
            Log.d(TAG, "inside get count")
            // return dispatchSlipItems.value?.size ?: 0
            return 1
        }
        open inner class ViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
            protected val material_barcode: TextView
            protected val batch_number: TextView
            protected val barcode_serial: TextView
            init {
                material_barcode = itemView.findViewById(R.id.material_barcode)
                batch_number = itemView.findViewById(R.id.batch_number)
                barcode_serial = itemView.findViewById(R.id.barcode_serial)
            }
            fun bind() {
                material_barcode.text = viewModel.material_barcode
                batch_number.text = viewModel.batch_number
                barcode_serial.text = viewModel.barcode_serial
            }
        }
    }
}



