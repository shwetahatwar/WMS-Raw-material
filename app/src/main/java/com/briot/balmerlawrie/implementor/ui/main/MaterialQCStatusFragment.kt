package com.briot.balmerlawrie.implementor.ui.main

import android.content.Context
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.briot.balmerlawrie.implementor.R
import com.briot.balmerlawrie.implementor.UiHelper
import com.briot.balmerlawrie.implementor.repository.remote.MaterialInward
import com.briot.balmerlawrie.implementor.repository.remote.QCStatus
import com.briot.balmerlawrie.implementor.repository.remote.qcStatusDisplay
import io.github.pierry.progress.Progress
import kotlinx.android.synthetic.main.dispatch_picking_list_fragment.*
import kotlinx.android.synthetic.main.material_q_c_status_fragment.*
import kotlinx.android.synthetic.main.material_qc_status_row.view.*
import kotlinx.android.synthetic.main.q_c_pending_fragment.*
import kotlinx.android.synthetic.main.quality_check_fragment.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MaterialQCStatusFragment : Fragment() {

    companion object {
        fun newInstance() = MaterialQCStatusFragment()
    }

    private lateinit var viewModel: MaterialQCStatusViewModel
    private var progress: Progress? = null
    private var oldMaterialInward: Array<qcStatusDisplay?> = emptyArray()
    lateinit var recyclerView: RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.material_q_c_status_fragment, container, false)
        this.recyclerView = rootView.findViewById(R.id.qcStatus_recyclerlist)
        recyclerView.layoutManager = LinearLayoutManager(this.activity)
        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MaterialQCStatusViewModel::class.java)

        (this.activity as AppCompatActivity).setTitle("Material QC Status")
        qcstatus_materialBarcode.requestFocus()
        viewModel.getItemsFromDB()
        recyclerView.adapter = SimpleMaterialItemAdapter(recyclerView, viewModel.qcStatusDisplay, viewModel)

        if (this.arguments != null) {
            viewModel.barcodeSerial = this.arguments!!.getString("barcodeSerial")
        }


        qcstatus_scanButton.setOnClickListener {
            var materialBarcodeSerial = qcstatus_materialBarcode.text.toString()
            if (materialBarcodeSerial == "") {
                UiHelper.showErrorToast(this.activity as AppCompatActivity, "Enter Material Barcode");
            }
            else if (materialBarcodeSerial.length < 10 || materialBarcodeSerial.length > 10) {
                UiHelper.showErrorToast(this.activity as AppCompatActivity, "Enter valid Barcode");
            } else  {
                viewModel.loadMaterialStatusItems(materialBarcodeSerial)
            }
        }

        viewModel.qcStatusDisplay.observe(viewLifecycleOwner, Observer<Array<qcStatusDisplay?>> {
            if (it != null && it != oldMaterialInward) {
                UiHelper.hideProgress(this.progress)
                this.progress = null
                if (viewModel.qcStatusDisplay.value.orEmpty().isNotEmpty() && viewModel.qcStatusDisplay.value?.first() == null) {
                    UiHelper.showSomethingWentWrongSnackbarMessage(this.activity as AppCompatActivity)
                } else if (it != oldMaterialInward) {
                    qcStatus_recyclerlist.adapter?.notifyDataSetChanged()
                }
            }
            if (it == null) {
                UiHelper.showErrorToast(this.activity as AppCompatActivity, "Wrong material scan")
                oldMaterialInward = viewModel.qcStatusDisplay.value!!
            }
        })

        qcstatus_items_submit_button.setOnClickListener {
            var thisObject = this
            viewModel.getItemsFromDB()
            println(viewModel.scanedItems)

            for (item in viewModel.qcStatusDisplay.value!!){
                if (item != null) {
                    if (item.QCStatus == "QC Approved"){
                        val qcRemark: TextView
                        AlertDialog.Builder(this.activity as AppCompatActivity, R.style.MyDialogTheme).create().apply {
                            setTitle(" Confirm")
                            setMessage("changes the QC status of the material.")
                            setButton(AlertDialog.BUTTON_NEGATIVE, "Reject") { dialog, _ -> dialog.dismiss()
                                viewModel.submitScanItem(2)
                            }
                            show()
                        }
                    } else if(item.QCStatus == "QC Rejected"){
                        AlertDialog.Builder(this.activity as AppCompatActivity, R.style.MyDialogTheme).create().apply {
                            setTitle(" Confirm")
                            setMessage("changes the QC status of the material.")
                            setButton(AlertDialog.BUTTON_POSITIVE, "Approve") { dialog, _ -> dialog.dismiss()
                                viewModel.submitScanItem(1)
                            }
                            show()
                        }
                    } else if(item.QCStatus == "QC Pending"){
                        AlertDialog.Builder(this.activity as AppCompatActivity, R.style.MyDialogTheme).create().apply {
                            setTitle(" Confirm")
                            setMessage("changes the QC status of the material.")
                            setButton(AlertDialog.BUTTON_NEUTRAL, "Approve") { dialog, _ -> dialog.dismiss()
                                viewModel.submitScanItem(1)
                            }
                            setButton(AlertDialog.BUTTON_NEUTRAL, "Reject") { dialog, _ -> dialog.dismiss()
                                viewModel.submitScanItem(2)
                            }
                            show()
                            }
                        }
                    }
                }
            }
        }
    }

    open class SimpleMaterialItemAdapter(private val recyclerView: androidx.recyclerview.widget.RecyclerView,
                                      private val materialInwards: LiveData<Array<qcStatusDisplay?>>,
                                      private val viewModel: MaterialQCStatusViewModel) :
            androidx.recyclerview.widget.RecyclerView.Adapter<SimpleMaterialItemAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
                SimpleMaterialItemAdapter.ViewHolder {
            val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.material_qc_status_row, parent, false)
            return ViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: SimpleMaterialItemAdapter.ViewHolder, position: Int) {
            holder.bind()
        }

        override fun getItemCount(): Int {
            return materialInwards.value?.size ?: 0
        }

        open inner class ViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
            protected val barcodeSerial: TextView
            protected val partNumber: TextView
            protected val QCStatus: TextView
            protected val name: TextView
            protected val description: TextView

            init {
                partNumber = itemView.findViewById(R.id.part_number_value)
                barcodeSerial = itemView.findViewById(R.id.barcode_serial_value)
                QCStatus = itemView.findViewById(R.id.Qc_Status_value)
                name = itemView.findViewById(R.id.current_location_value)
                description = itemView.findViewById(R.id.description_value)
            }

            fun bind() {
                try {
                    val materialItems = materialInwards.value!![adapterPosition]!!
                    barcodeSerial.text = materialItems.barcodeSerial
                    QCStatus.text = materialItems.QCStatus
                    partNumber.text = materialItems.partnumber
                    description.text = materialItems.description
                    name.text = materialItems.location
                    }
                 catch (e: Exception) {
                    println("Getting exception " + e)
                }
            }
        }
    }
