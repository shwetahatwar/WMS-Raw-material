package com.briot.wms.implementor.ui.main

import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.briot.wms.implementor.MainApplication
import com.briot.wms.implementor.R
import com.briot.wms.implementor.UiHelper
import com.briot.wms.implementor.repository.remote.MaterialInward
import io.github.pierry.progress.Progress
import kotlinx.android.synthetic.main.material_q_c_status_fragment.*

class MaterialQCStatusFragment : Fragment() {

    companion object {
        fun newInstance() = MaterialQCStatusFragment()
    }

    private lateinit var viewModel: MaterialQCStatusViewModel
    private var progress: Progress? = null
    private var oldMaterialInward: Array<MaterialInward?> = emptyArray()
    lateinit var recyclerView: RecyclerView
    var checkVariable = false;

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
        // viewModel.getItemsFromDB()
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
                this.progress = UiHelper.showProgressIndicator(this.activity as AppCompatActivity, "Please wait")
                viewModel.barcodeSerial = materialBarcodeSerial
                viewModel.loadMaterialStatusItems(materialBarcodeSerial)
            }
            qcstatus_materialBarcode.text?.clear()
            qcstatus_materialBarcode.requestFocus()
        }

        viewModel.networkError.observe(viewLifecycleOwner, Observer<Boolean> {
            if (it == true) {
                UiHelper.hideProgress(this.progress)
                this.progress = null

                if (viewModel.errorMessage != null) {
                    UiHelper.showErrorToast(this.activity as AppCompatActivity, viewModel.errorMessage)
                } else {
                    UiHelper.showNoInternetSnackbarMessage(this.activity as AppCompatActivity)
                }
            }
        })

//---------------------------------------------------------------------------------
        //on click of keyboard enter data added in a list API call
        qcstatus_materialBarcode.setOnEditorActionListener { _, i, keyEvent ->
            var handled = false
            if ((qcstatus_materialBarcode.text != null && qcstatus_materialBarcode.text!!.isNotEmpty()) && i == EditorInfo.IME_ACTION_DONE
                    || (keyEvent != null && (keyEvent.keyCode == KeyEvent.KEYCODE_ENTER || keyEvent.keyCode == KeyEvent.KEYCODE_TAB)
                            && keyEvent.action == KeyEvent.ACTION_DOWN)) {
                val keyboard = activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                keyboard.hideSoftInputFromWindow(activity?.currentFocus?.getWindowToken(), 0)
                var value = qcstatus_materialBarcode.text!!.toString().trim()
                var materialBarcodeSerial = qcstatus_materialBarcode.text.toString()
                if (materialBarcodeSerial == "") {
                    UiHelper.showErrorToast(this.activity as AppCompatActivity, "Enter Material Barcode");
                }
                else if (materialBarcodeSerial.length < 10 || materialBarcodeSerial.length > 10) {
                    UiHelper.showErrorToast(this.activity as AppCompatActivity, "Enter valid Barcode");
                } else  {
                    this.progress = UiHelper.showProgressIndicator(this.activity as AppCompatActivity, "Please wait")
                    viewModel.barcodeSerial = materialBarcodeSerial
                    viewModel.loadMaterialStatusItems(materialBarcodeSerial)
                }
                qcstatus_materialBarcode.text?.clear()
                qcstatus_materialBarcode.requestFocus()
                handled = true
            }
            handled
        }

//-------------------------------------------------------


        viewModel.qcStatusDisplay.observe(viewLifecycleOwner, Observer<Array<MaterialInward?>> {
            if (it != null && it != oldMaterialInward) {
                UiHelper.hideProgress(this.progress)
                this.progress = null
                if (viewModel.qcStatusDisplay.value.orEmpty().isNotEmpty() && viewModel.qcStatusDisplay.value?.first() == null) {
                    UiHelper.showSomethingWentWrongSnackbarMessage(this.activity as AppCompatActivity)
                } else if (it != oldMaterialInward) {
//                    viewModel.loadMaterialStatusItems(materialBarcodeSerial)
                    qcStatus_recyclerlist.adapter?.notifyDataSetChanged()
                }
            }
            if (it == null) {
                UiHelper.showErrorToast(this.activity as AppCompatActivity, "Wrong material scan")
                oldMaterialInward = viewModel.qcStatusDisplay.value!!
            }
        })

        viewModel.itemSubmissionSuccessful.observe(viewLifecycleOwner, Observer<Boolean> {
            if (it == true) {
                UiHelper.showSuccessToast(this.activity as AppCompatActivity,
                        "QC Status changed successfully, please scan next barcode")
            }
        })

        qcstatus_items_submit_button.setOnClickListener {
            var thisObject = this
            if (MainApplication.hasNetwork(MainApplication.applicationContext())) {
                // viewModel.getItemsFromDB()
                // println(viewModel.scanedItems)
                var materialBarcodeSerial = qcstatus_materialBarcode.text.toString()

                if (viewModel.qcStatusDisplay.value.isNullOrEmpty()) {
                    UiHelper.showErrorToast(this.activity as AppCompatActivity, "Please Scan Material Barcode");
                } else {

//                AlertDialog.Builder(this.activity as AppCompatActivity, R.style.MyDialogTheme).create().apply {
//                    setTitle("Confirm")
//                    setMessage("Are you sure you want to change QC Status")
//                    setButton(AlertDialog.BUTTON_NEUTRAL, "No", { dialog, _ -> dialog.dismiss() })
//                    setButton(AlertDialog.BUTTON_POSITIVE, "Yes", {
//                        dialog, _ -> dialog.dismiss()
//                    })
//                    show()
//                }

//                println("fdfsdfdsf")
                    for (item in viewModel.qcStatusDisplay.value!!) {
                        if (item != null) {
                            if (item.QCStatus == 1) { // "QC Approved"
                                val qcRemark: TextView
                                AlertDialog.Builder(this.activity as AppCompatActivity, R.style.MyDialogTheme).create().apply {
                                    setTitle(" Confirm")
                                    setMessage("changes the QC status of the material.")
                                    setButton(AlertDialog.BUTTON_NEGATIVE, "Reject") { dialog, _ ->
                                        dialog.dismiss()
                                        viewModel.submitScanItem(2)
                                    }
                                    show()
                                }
                            } else if (item.QCStatus == 2) { // QC Rejected
                                AlertDialog.Builder(this.activity as AppCompatActivity, R.style.MyDialogTheme).create().apply {
                                    setTitle(" Confirm")
                                    setMessage("changes the QC status of the material.")
                                    setButton(AlertDialog.BUTTON_POSITIVE, "Approve") { dialog, _ ->
                                        dialog.dismiss()
                                        viewModel.submitScanItem(1)
                                    }
                                    show()
                                }
                            } else if (item.QCStatus == 0) { // "QC Pending"
                                AlertDialog.Builder(this.activity as AppCompatActivity, R.style.MyDialogTheme).create().apply {
                                    setTitle(" Confirm")
                                    setMessage("changes the QC status of the material.")
                                    setButton(AlertDialog.BUTTON_NEUTRAL, "Approve") { dialog, _ ->
                                        dialog.dismiss()
                                        viewModel.submitScanItem(1)
                                    }
                                    setButton(AlertDialog.BUTTON_NEUTRAL, "Reject") { dialog, _ ->
                                        dialog.dismiss()
                                        viewModel.submitScanItem(2)
                                    }
                                    show()
                                }
                            }
                        }
                    }
                }
            }else {
                UiHelper.showErrorToast(this.activity as AppCompatActivity, "Please submit the list when in Network!")
            }
        }
        }
    }

    open class SimpleMaterialItemAdapter(private val recyclerView: androidx.recyclerview.widget.RecyclerView,
                                      private val qcStatusDisplay: LiveData<Array<MaterialInward?>>,
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
            return qcStatusDisplay.value?.size ?: 0
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
                    val materialItems = qcStatusDisplay.value!![adapterPosition]!!
                    barcodeSerial.text = materialItems.barcodeSerial
                    var result: String = ""
                    if (materialItems.QCStatus == 1){
                        result = "QC Approved"
                    }else if (materialItems.QCStatus == 2){
                        result = "QC Rejected"
                    }else if (materialItems.QCStatus == 0){
                        result = "QC Pending"
                    }
                    QCStatus.text = result
                    partNumber.text = materialItems.partnumber.partNumber
                    description.text = materialItems.partnumber.description
                    name.text = materialItems.shelf.name
                    }
                 catch (e: Exception) {
                    println("Getting exception " + e)
                }
            }
        }
    }
