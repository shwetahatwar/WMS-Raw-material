package com.briot.wms.implementor.ui.main

import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.briot.wms.implementor.MainApplication
import com.briot.wms.implementor.R
import com.briot.wms.implementor.UiHelper
import com.briot.wms.implementor.repository.local.PrefConstants
import com.briot.wms.implementor.repository.remote.QCPending
import io.github.pierry.progress.Progress
import kotlinx.android.synthetic.main.q_c_pending_fragment.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class QCPendingFragment : Fragment() {

    companion object {
        fun newInstance() = QCPendingFragment()
    }

    private lateinit var viewModel: QCPendingViewModel
    lateinit var recyclerView: RecyclerView
    public var progress1: Progress? = null
    public var progress: Progress? = null
    private var oldQcPendingItems: Array<QCPending?>? = null
    private var loading = false
    lateinit var qcPendingMaterialTextValue: EditText
    private var listOfQCPending = mutableListOf<QCPending>()
    lateinit var layoutManager: LinearLayoutManager
    lateinit var adapter: SimpleQcPendingItemAdapter
    var isScrolling: Boolean = false
    var callCount = 1
    var qcpendingList: Array<QCPending?> = emptyArray()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.q_c_pending_fragment, container, false)
        viewModel = ViewModelProvider(this).get(QCPendingViewModel::class.java)
        this.recyclerView = rootView.findViewById(R.id.qcpending_recyclerlist)
        layoutManager = LinearLayoutManager(this.activity)
        recyclerView.layoutManager = layoutManager
        qcPendingMaterialTextValue = rootView.findViewById(R.id.qcpending_materialBarcode)
        adapter = SimpleQcPendingItemAdapter(recyclerView, viewModel.QCPendingItem, viewModel)
        viewModel.getQcTotalCount()
        viewModel.getItemsFromDB()
        viewModel.loadQCPendingItems("100","0")
        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (this.activity as AppCompatActivity).setTitle("QC Pending")
        qcpending_materialBarcode.requestFocus()
        viewModel.getQcTotalCount()
        if (this.arguments != null) {
            viewModel.barcodeSerial = this.arguments!!.getInt("barcodeSerial").toString()
            viewModel.description = this.arguments!!.getString("description")
            viewModel.partnumber = this.arguments!!.getInt("partnumber")
        }

        this.progress = UiHelper.showProgressIndicator(this.activity as AppCompatActivity, "Please wait")
        recyclerView.adapter = SimpleQcPendingItemAdapter(recyclerView, viewModel.QCPendingItem, viewModel)
        count_value.text = viewModel.totalScannedItem.value.toString() + "/"+viewModel.qcTotalCount!!.toString()

        viewModel.totalScannedItem.observe(viewLifecycleOwner, Observer<Number> {
            if (it != null) {
                count_value.text = viewModel.totalScannedItem.value.toString() + "/"+viewModel.qcTotalCount!!.toString() ?: 0.toString()

                // scanned item at top
                for (i in viewModel.scanedItems){
                    // change position of the scaned item to top
                    val scannedItemFound = viewModel.qcpendingList?.filter{
                        it!!.barcodeSerial.toString() == i.barcodeSerial.toString()}
                    if (scannedItemFound.size > 0){
                        // val position = viewModel.qcpendingList.indexOf(scannedItemFound[0])
                        // Collections.swap(viewModel.qcpendingList, position,0)
                        viewModel.qcpendingList.remove(scannedItemFound[0])
                        viewModel.qcpendingList.add(0, scannedItemFound[0])
                        qcpending_recyclerlist.adapter?.notifyDataSetChanged()
                    }
                }
                qcpending_recyclerlist.adapter?.notifyDataSetChanged()
            }
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

        viewModel.itemSubmissionSuccessful.observe(viewLifecycleOwner, Observer<Boolean> {
            if (it == true) {
                UiHelper.hideProgress(this.progress)
                this.progress = null
                var thisObject = this
                UiHelper.showSuccessToast(this.activity as AppCompatActivity,"Updated successfully")
            }
        })

        viewModel.QCPendingItem.observe(viewLifecycleOwner, Observer<Array<QCPending?>> {
            if (it != null) {
                UiHelper.hideProgress(this.progress)
                this.progress = null
                if (viewModel.QCPendingItem.value.orEmpty().isNotEmpty() && viewModel.QCPendingItem.value?.first() == null) {
                    UiHelper.showSomethingWentWrongSnackbarMessage(this.activity as AppCompatActivity)
                } else if (it != oldQcPendingItems) {
                    // count_value.text = "0/"+viewModel.qcTotalCount!!.toString() ?: 0.toString()
                    count_value.text = viewModel.totalScannedItem.value.toString() + "/"+viewModel.qcTotalCount!!.toString() ?: 0.toString()
                    qcpending_recyclerlist.adapter?.notifyDataSetChanged()
                }
            }
            oldQcPendingItems = viewModel.QCPendingItem.value
        })

        qcpending_scanButton.setOnClickListener {
            val inputMaterialBarcode = qcPendingMaterialTextValue.getText().toString()
            if (inputMaterialBarcode == "") {
                UiHelper.showErrorToast(this.activity as AppCompatActivity,
                        "Please enter MATERIAL Barcode value")
                qcpending_materialBarcode.requestFocus()
            }else if (inputMaterialBarcode.length < 10 || inputMaterialBarcode.length > 10) {
                UiHelper.showErrorToast(this.activity as AppCompatActivity, "Enter valid Barcode");
                qcpending_materialBarcode.requestFocus()
            }

            val scannedItems = viewModel.scanedItems?.filter{ it!!.barcodeSerial.toString() == inputMaterialBarcode}
            if (scannedItems!!.size > 0) {
                UiHelper.showErrorToast(this.activity as AppCompatActivity,
                        "Already scanned")
                qcpending_materialBarcode.text?.clear()
                qcpending_materialBarcode.requestFocus()
            } else {
                val found = viewModel.QCPendingItem.value?.filter{ it!!.barcodeSerial.toString() == inputMaterialBarcode}
                if (found!!.size > 0){
                    println("found")
                    GlobalScope.launch {
                        viewModel.addQcScanItem(inputMaterialBarcode, found[0]!!.id!!.toInt(),
                                found[0]!!.QCStatus!!.toInt())
                        // viewModel.getItemsFromDB()
                    }
                    qcpending_recyclerlist.adapter?.notifyDataSetChanged()
                }
                viewModel.barcodeSerial = inputMaterialBarcode
                qcpending_recyclerlist.adapter?.notifyDataSetChanged()
                qcpending_materialBarcode.text?.clear()
            }
            qcpending_materialBarcode.requestFocus()
//            val inputMaterialBarcode = qcPendingMaterialTextValue.getText().toString()
//            if (inputMaterialBarcode == "") {
//                UiHelper.showErrorToast(this.activity as AppCompatActivity,
//                        "Please enter MATERIAL Barcode value")
//                qcpending_materialBarcode.requestFocus()
//            }
//
//            val scannedItems = viewModel.scanedItems?.filter{ it!!.barcodeSerial.toString() == inputMaterialBarcode}
//            if (scannedItems?.orEmpty()?.isNotEmpty()!!) {
//                if (scannedItems!!.size > 0) {
//                    UiHelper.showErrorToast(this.activity as AppCompatActivity,
//                            "Already scanned")
//                    qcpending_materialBarcode.text?.clear()
//                    qcpending_materialBarcode.requestFocus()
//                } else {
//                    val found = viewModel.QCPendingItem.value?.filter { it!!.barcodeSerial.toString() == inputMaterialBarcode }
//                    if (found?.orEmpty()?.isNotEmpty()!!) {
//                        if (found!!.size > 0) {
//                            GlobalScope.launch {
//                                viewModel.addQcScanItem(inputMaterialBarcode, found[0]!!.id!!.toInt(),
//                                        found[0]!!.QCStatus!!.toInt())
//                                // viewModel.getItemsFromDB()
//                            }
//                            qcpending_recyclerlist.adapter?.notifyDataSetChanged()
//                        }
//                    }
//                    viewModel.barcodeSerial = inputMaterialBarcode
//                    qcpending_recyclerlist.adapter?.notifyDataSetChanged()
//                    qcpending_materialBarcode.text?.clear()
//                }
//            }else{
//                //UiHelper.showErrorToast(this.activity as AppCompatActivity, "Wrong barcode scan")
//            }
        }

        qcpending_items_submit_button.setOnClickListener {
            var thisObject = this
            if (MainApplication.hasNetwork(MainApplication.applicationContext())) {
                if (viewModel.totalScannedItem.value != 0) {
                   // thisObject.progress = UiHelper.showProgressIndicator(thisObject.activity as AppCompatActivity, "Please wait")
                    AlertDialog.Builder(this.activity as AppCompatActivity, R.style.MyDialogTheme).create().apply {
                        setTitle(" Confirm")
                        setMessage("Accept or Reject scanned materials.")
                        setButton(AlertDialog.BUTTON_POSITIVE, "Accept") { dialog, _ ->
                            dialog.dismiss()
                            viewModel.submitScanItem(1)
                        }

                        setButton(AlertDialog.BUTTON_NEUTRAL, "Reject") { dialog, _ ->
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
                                        viewModel.QCRemarks = userInput.text.toString()
                                        viewModel.submitScanItem(2, viewModel.QCRemarks)
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
            }else{
                UiHelper.showErrorToast(this.activity as AppCompatActivity, "Please submit the list when in Network!")
            }
        }
//---------------------------------------------------------------------------------
        //on click of keyboard enter data added in a list API call
        qcpending_materialBarcode.setOnEditorActionListener { _, i, keyEvent ->
            var handled = false
            if ((qcpending_materialBarcode.text != null && qcpending_materialBarcode.text!!.isNotEmpty()) && i == EditorInfo.IME_ACTION_DONE
                    || (keyEvent != null && (keyEvent.keyCode == KeyEvent.KEYCODE_ENTER || keyEvent.keyCode == KeyEvent.KEYCODE_TAB)
                            && keyEvent.action == KeyEvent.ACTION_DOWN)) {
                val keyboard = activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                keyboard.hideSoftInputFromWindow(activity?.currentFocus?.getWindowToken(), 0)
                var value = qcpending_materialBarcode.text!!.toString().trim()
                qcpending_materialBarcode.requestFocus()

                val inputMaterialBarcode = qcPendingMaterialTextValue.getText().toString()
                if (inputMaterialBarcode == "") {
                    UiHelper.showErrorToast(this.activity as AppCompatActivity,
                            "Please enter MATERIAL Barcode value")
                    qcpending_materialBarcode.requestFocus()
                }else if (inputMaterialBarcode.length < 10 || inputMaterialBarcode.length > 10) {
                    UiHelper.showErrorToast(this.activity as AppCompatActivity, "Enter valid Barcode");
                    qcpending_materialBarcode.requestFocus()
                }

                val scannedItems = viewModel.scanedItems?.filter{ it!!.barcodeSerial.toString() == inputMaterialBarcode}
                if (scannedItems!!.size > 0) {
                    UiHelper.showErrorToast(this.activity as AppCompatActivity,
                            "Already scanned")
                    qcpending_materialBarcode.text?.clear()
                    qcpending_materialBarcode.requestFocus()
                } else {
                    val found = viewModel.QCPendingItem.value?.filter{ it!!.barcodeSerial.toString() == inputMaterialBarcode}
                    if (found!!.size > 0){
                        println("found")

                        GlobalScope.launch {
                            viewModel.addQcScanItem(inputMaterialBarcode, found[0]!!.id!!.toInt(),
                                    found[0]!!.QCStatus!!.toInt())
                            // viewModel.getItemsFromDB()
                        }
                        qcpending_recyclerlist.adapter?.notifyDataSetChanged()
                    }
                    viewModel.barcodeSerial = inputMaterialBarcode
                    qcpending_recyclerlist.adapter?.notifyDataSetChanged()
                    qcpending_materialBarcode.text?.clear()
                    qcpending_materialBarcode.requestFocus()
                }

                qcpending_materialBarcode.requestFocus()
                handled = true
            }
            qcpending_materialBarcode.requestFocus()
            handled
        }


//-------------------------------------------------------
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING){
                    isScrolling = true
                }
            }


            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    val visibleItemCount = layoutManager.childCount
                    val pastVisibleItem = layoutManager.findLastCompletelyVisibleItemPosition()
                    val total = adapter.itemCount
                    // println("total-->" + total)
                    val loopCount =  viewModel.qcTotalCount!!.toInt() / 100.toInt()
                    //println("callCount-->"+ callCount)
                    //println("loopCount-->"+ loopCount)
                    // println("visibleItemCount + pastVisibleItem-->" + (visibleItemCount + pastVisibleItem))
                    if (isScrolling && (visibleItemCount + pastVisibleItem) >= total && callCount <= loopCount) {
                        var offset = callCount * 100
                        println("offset-->"+offset)
                        //UiHelper.showProgressIndicator(activity!!, "Loading QC Pending list")
                        viewModel.loadQCPendingItems("100", offset.toString())

                        qcpending_recyclerlist.adapter?.notifyDataSetChanged()
                        isScrolling = false
                        callCount +=1
                    }
                }
            }
        })
    }
}

open class SimpleQcPendingItemAdapter(private val recyclerView: androidx.recyclerview.widget.RecyclerView,
                                    private val QCPendingItem: LiveData<Array<QCPending?>>,
                                    private val viewModel: QCPendingViewModel) :
        androidx.recyclerview.widget.RecyclerView.Adapter<SimpleQcPendingItemAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.qcpending_row, parent, false)
        return ViewHolder(itemView)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind()
        val QCPendingItem = viewModel.qcpendingList


//        val scannedItemFound = viewModel.scanedItems?.filter{
//            it!!.barcodeSerial.toString() == item!!.barcodeSerial.toString()}

    }


    override fun getItemCount(): Int {
        //println("qc count---->"+ viewModel.qcpendingList)
         return viewModel.qcpendingList.size
    }

    open inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        protected val partnumber: TextView
        protected val description: TextView
        protected val barcodeSerial: TextView
        protected val linearLayout: LinearLayout

        init {
            partnumber = itemView.findViewById(R.id.partNumberValue)
            description = itemView.findViewById(R.id.descriptionValue)
            barcodeSerial = itemView.findViewById(R.id.barcodeSerialValue)
            linearLayout = itemView.findViewById(R.id.qcpending_layout)
        }

        fun bind() {
            val item = viewModel.qcpendingList[adapterPosition]!!
            //println("qc adapter pos--->"+adapterPosition)
            partnumber.text = item.partnumber.partNumber.toString()
            description.text = item.partnumber.description.toString()
            barcodeSerial.text = item.barcodeSerial.toString()

            // println("item.barcodeSerial-->"+item.barcodeSerial)
            val scannedItem = viewModel.scanedItems?.filter { it.barcodeSerial!!.trim() ==  item!!.barcodeSerial.toString().trim()}
            if (scannedItem.size > 0){
                    linearLayout.setBackgroundColor(PrefConstants().lightGreenColor)
            } else{
                    linearLayout.setBackgroundColor(PrefConstants().lightGrayColor)
            }
        }
    }
}
