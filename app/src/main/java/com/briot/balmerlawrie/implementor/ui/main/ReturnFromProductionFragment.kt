package com.briot.balmerlawrie.implementor.ui.main

import android.content.Context
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.briot.balmerlawrie.implementor.MainApplication
import com.briot.balmerlawrie.implementor.R
import com.briot.balmerlawrie.implementor.UiHelper
import com.briot.balmerlawrie.implementor.repository.local.PrefConstants
import com.briot.balmerlawrie.implementor.repository.remote.*
import io.github.pierry.progress.Progress
import kotlinx.android.synthetic.main.issue_to_production_employee_fragment.*
import kotlinx.android.synthetic.main.issue_to_production_fragment.*
import kotlinx.android.synthetic.main.q_c_pending_fragment.*
import kotlinx.android.synthetic.main.return_from_production_fragment.*
import kotlinx.android.synthetic.main.return_from_production_fragment.employee_value
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class ReturnFromProductionFragment : Fragment() {

    companion object {
        fun newInstance() = ReturnFromProductionFragment()
    }

    private lateinit var viewModel: ReturnFromProductionViewModel
    lateinit var recyclerView: RecyclerView
    public var progress: Progress? = null
    private var oldReturnToProductionMaster: Array<ReturnFromProduction?>? = null
    private var oldIssueToProd: Array<IssueToProductionResponse?>? = null
    lateinit var layoutManager: LinearLayoutManager
    lateinit var adapter: ReturnFromProductionAdapter
    var isScrolling: Boolean = false

    //var qcpendingList: Array<ReturnFromProduction?> = emptyArray()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.return_from_production_fragment, container, false)
        viewModel = ViewModelProvider(this).get(ReturnFromProductionViewModel::class.java)
        this.recyclerView = rootView.findViewById(R.id.returntoproduction_recyclerlist)
        layoutManager = LinearLayoutManager(this.activity)
        recyclerView.layoutManager = layoutManager
        adapter = ReturnFromProductionAdapter(recyclerView, viewModel.returnfromproduction, viewModel)
        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (this.activity as AppCompatActivity).setTitle("Return from Production")
        recyclerView.adapter = ReturnFromProductionAdapter(recyclerView, viewModel.returnfromproduction, viewModel)
        if (this.arguments != null) {
            viewModel.employeeId = this.arguments!!.getString("employeeId")
            viewModel.projectId = this.arguments!!.getInt("projectId")

            employee_value.text = this.arguments!!.getString("employeeId")
        }
        // TODO: Use the ViewModel
        this.progress = UiHelper.showProgressIndicator(this.activity as AppCompatActivity, "Please wait")
        viewModel.loadReturnFromProductionItems(viewModel.projectId, viewModel.performedBy)
        viewModel.getItemsFromDB()
        issue_return_to_prod_count.text = viewModel.totalScannedItem.value.toString() + "/"+viewModel.returnfromproduction!!.toString()

        viewModel.returnfromproduction.observe(viewLifecycleOwner, Observer<Array<ReturnFromProduction?>> {
            if (it != null) {
                UiHelper.hideProgress(this.progress)
                this.progress = null
                if (viewModel.returnfromproduction.value.isNullOrEmpty()) {
                    //UiHelper.showErrorToast(this.activity as AppCompatActivity, "Enter valid Barcode")
                } else if (it != oldReturnToProductionMaster) {
                    issue_return_to_prod_count.text = viewModel.totalScannedItem.value.toString() + "/" + viewModel.returnfromproduction!!.value?.size.toString()
                            ?: 0.toString()
                    recyclerView.adapter?.notifyDataSetChanged()
                   //UiHelper.showSuccessToast(this.activity as AppCompatActivity, "Updated successfully")
                }
            }
            oldReturnToProductionMaster = viewModel.returnfromproduction.value
        })

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

        returntoproduction_items_submit_button.setOnClickListener {
            var thisObject = this
            if (MainApplication.hasNetwork(MainApplication.applicationContext())) {
                if (viewModel.totalScannedItem.value != 0) {
                    // thisObject.progress = UiHelper.showProgressIndicator(thisObject.activity as AppCompatActivity, "Please wait")
                    AlertDialog.Builder(this.activity as AppCompatActivity, R.style.MyDialogTheme).create().apply {
                        setTitle(" Confirm")
                        setMessage("Give remark for return from production.")
                        setButton(AlertDialog.BUTTON_POSITIVE, "No") { dialog, _ ->
                            dialog.dismiss()
                            //viewModel.submitScanItem(viewModel.remarks)
                        }

                        setButton(AlertDialog.BUTTON_NEUTRAL, "Remark") { dialog, _ ->
                            dialog.dismiss()
                            // thisObject.progress = UiHelper.showProgressIndicator(thisObject.activity as AppCompatActivity, "Please wait")
                            val li = LayoutInflater.from(context)
                            val promptsView: View = li.inflate(R.layout.alert_pop_up_layout, null)
                            val alertDialogBuilder = AlertDialog.Builder(
                                    context)
                            alertDialogBuilder.setTitle("Remark")
                            // alertDialogBuilder.setMessage("Remark for Rejecting scanned materials.")
                            alertDialogBuilder.setView(promptsView);
                            val userInput = promptsView.findViewById<View>(R.id.dialogRemarkEt) as EditText
                            alertDialogBuilder
                                    .setCancelable(false)
                                    .setPositiveButton("OK") { dialog, id -> // get user input and set it to result
                                        viewModel.remarks = userInput.text.toString()
                                        viewModel.submitScanItem(viewModel.remarks)
//                                    Toast.makeText(context, "Entered: " + userInput.text.toString(), Toast.LENGTH_LONG).show()
                                    }
                                    .setNegativeButton("Cancel"
                                    ) { dialog, id -> dialog.cancel() }

                            val alertDialog = alertDialogBuilder.create()
                            alertDialog.show()
                        }
                        show()
                    }
                } else {
                    UiHelper.showErrorToast(this.activity as AppCompatActivity, "No Item scanned")
                }
            }else {
                UiHelper.showErrorToast(this.activity as AppCompatActivity, "Please submit the list when in Network!")
            }
        }
//--
        viewModel.totalScannedItem.observe(viewLifecycleOwner, Observer<Number> {
            if (it != null) {
                issue_return_to_prod_count.text = viewModel.totalScannedItem.value.toString() + "/" + viewModel.returnfromproduction!!.value?.size.toString()
                        ?: 0.toString()


                // scanned item at top
                for (i in viewModel.scanedItems){
                    val scannedItemFound = viewModel.returnfromprodList?.filter{
                        it!!.materialinward.barcodeSerial.toString() == i!!.barcodeSerial.toString()}
                    if (scannedItemFound.size > 0){
                        viewModel.returnfromprodList.remove(scannedItemFound[0])
                        viewModel.returnfromprodList.add(0, scannedItemFound[0])
                    }
                }


                recyclerView.adapter?.notifyDataSetChanged()
            }
        })

        return_from_production_scanButton.setOnClickListener {
            val inputMaterialBarcode = returnfromproduction_materialBarcode.getText().toString()
            if (inputMaterialBarcode == "") {
                UiHelper.showErrorToast(this.activity as AppCompatActivity,
                        "Please enter Barcode value")
                returnfromproduction_materialBarcode.requestFocus()
            }else if (inputMaterialBarcode.length < 10 || inputMaterialBarcode.length > 10) {
                UiHelper.showErrorToast(this.activity as AppCompatActivity, "Enter valid Barcode");
            }

            val alreadyScanned = viewModel.scanedItems?.filter{ it!!.barcodeSerial == inputMaterialBarcode}
            if (alreadyScanned.isNullOrEmpty()) {
                val found = viewModel.returnfromproduction.value?.filter {
                    it!!.materialinward.barcodeSerial == inputMaterialBarcode
                }
                if (found!!.size > 0) {
                    println("found")
                    GlobalScope.launch {
                        viewModel.addReturnFromproductionItem(found[0]!!.materialinward.id!!.toInt(),
                                found[0]!!.projectId, found[0]!!.quantity, found[0]!!.remarks,
                                found[0]!!.doneBy.id, found[0]!!.materialinward.barcodeSerial)
                        // viewModel.getItemsFromDB()
                    }
                    returntoproduction_recyclerlist.adapter?.notifyDataSetChanged()
                }else{
                    UiHelper.showErrorToast(this.activity as AppCompatActivity, "Barcode not found")
                    returnfromproduction_materialBarcode.text?.clear()
                }
                viewModel.barcodeSerial = inputMaterialBarcode
                returntoproduction_recyclerlist.adapter?.notifyDataSetChanged()
                returnfromproduction_materialBarcode.text?.clear()
            } else{
                UiHelper.showErrorToast(this.activity as AppCompatActivity, "Already scanned")
                returnfromproduction_materialBarcode.text?.clear()
                returnfromproduction_materialBarcode.requestFocus()
            }
            returnfromproduction_materialBarcode.text?.clear()
        }

        returnfromproduction_materialBarcode.setOnEditorActionListener { _, i, keyEvent ->
            var handled = false
            if ((returnfromproduction_materialBarcode.text != null && returnfromproduction_materialBarcode.text!!.isNotEmpty()) && i == EditorInfo.IME_ACTION_DONE
                    || (keyEvent != null && (keyEvent.keyCode == KeyEvent.KEYCODE_ENTER || keyEvent.keyCode == KeyEvent.KEYCODE_TAB)
                            && keyEvent.action == KeyEvent.ACTION_DOWN)) {
                val keyboard = activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                keyboard.hideSoftInputFromWindow(activity?.currentFocus?.getWindowToken(), 0)
                val inputMaterialBarcode = returnfromproduction_materialBarcode.getText().toString()
                if (inputMaterialBarcode == "") {
                    UiHelper.showErrorToast(this.activity as AppCompatActivity,
                            "Please enter Barcode value")
                    returnfromproduction_materialBarcode.requestFocus()
                }else if (inputMaterialBarcode.length < 10 || inputMaterialBarcode.length > 10) {
                    UiHelper.showErrorToast(this.activity as AppCompatActivity, "Enter valid Barcode");
                }

                val alreadyScanned = viewModel.scanedItems?.filter{ it!!.barcodeSerial == inputMaterialBarcode}
                if (alreadyScanned.isNullOrEmpty()) {
                    val found = viewModel.returnfromproduction.value?.filter {
                        it!!.materialinward.barcodeSerial == inputMaterialBarcode
                    }
                    if (found!!.size > 0) {
                        println("found")
                        GlobalScope.launch {
                            viewModel.addReturnFromproductionItem(found[0]!!.materialinward.id!!.toInt(),
                                    found[0]!!.projectId, found[0]!!.quantity, found[0]!!.remarks,
                                    found[0]!!.doneBy.id, found[0]!!.materialinward.barcodeSerial)
                            // viewModel.getItemsFromDB()
                        }
                        returntoproduction_recyclerlist.adapter?.notifyDataSetChanged()
                    }else{
                        UiHelper.showErrorToast(this.activity as AppCompatActivity, "Barcode not found")
                        returnfromproduction_materialBarcode.text?.clear()
                    }
                    viewModel.barcodeSerial = inputMaterialBarcode
                    returntoproduction_recyclerlist.adapter?.notifyDataSetChanged()
                    returnfromproduction_materialBarcode.text?.clear()
                } else{
                    UiHelper.showErrorToast(this.activity as AppCompatActivity, "Already scanned")
                    returnfromproduction_materialBarcode.text?.clear()
                    returnfromproduction_materialBarcode.requestFocus()
                }
                returnfromproduction_materialBarcode.text?.clear()
                handled = true
            }
            handled
        }
    }
}

open class ReturnFromProductionAdapter(private val recyclerView: androidx.recyclerview.widget.RecyclerView,
                           private val returnfromproduction: LiveData<Array<ReturnFromProduction?>>,
                           private val viewModel: ReturnFromProductionViewModel) :
        androidx.recyclerview.widget.RecyclerView.Adapter<ReturnFromProductionAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.return_from_production_row, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind()
    }


    override fun getItemCount(): Int {
        return viewModel.returnfromprodList?.size ?: 0
    }

    open inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        protected val partNumber: TextView
        protected val barcodeSerial: TextView
        protected val batchNumber: TextView
        protected val quantity: TextView
        protected val linearLayout: LinearLayout

        init {
            partNumber = itemView.findViewById(R.id.issue_to_production_partNumberValue)
            barcodeSerial = itemView.findViewById(R.id.issue_to_production_barcodeSerialValue)
            batchNumber = itemView.findViewById(R.id.issue_to_production_descriptionValue)
            quantity = itemView.findViewById(R.id.issue_to_production_quantityValue)
            linearLayout = itemView.findViewById(R.id.return_from_production_layout)
        }

        fun bind() {
            val item = viewModel.returnfromprodList!![adapterPosition]!!
            partNumber.text = item.materialinward.partNumber
            barcodeSerial.text = item.materialinward.barcodeSerial
            batchNumber.text = item.materialinward.batchNumber
            quantity.text =item.materialinward.eachPackQuantity.toString()

//            if (viewModel.barcodeSerial.toString() == item!!.materialinward.barcodeSerial.toString()){
//                linearLayout.setBackgroundColor(PrefConstants().lightGreenColor)
//            } else{
//                linearLayout.setBackgroundColor(PrefConstants().lightGrayColor)
//            }

            val scannedItemFound = viewModel.scanedItems?.filter{
                it!!.barcodeSerial.toString() == item!!.materialinward.barcodeSerial.toString()}
            if (scannedItemFound.size > 0){
                linearLayout.setBackgroundColor(PrefConstants().lightGreenColor)
            }else{
                linearLayout.setBackgroundColor(PrefConstants().lightGrayColor)
            }

        }
    }
}



