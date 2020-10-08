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
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.briot.wms.implementor.R
import com.briot.wms.implementor.UiHelper
import com.briot.wms.implementor.repository.local.PrefConstants
import com.briot.wms.implementor.repository.remote.PickingMasterDisplay
import io.github.pierry.progress.Progress
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import com.briot.wms.implementor.MainApplication
import com.briot.wms.implementor.data.PickingScanList
import com.briot.wms.implementor.repository.remote.MaterialInward
import kotlinx.android.synthetic.main.picking_fragment.*
import kotlinx.android.synthetic.main.picking_fragment.picking_items_submit_button
import kotlinx.android.synthetic.main.picking_fragment.picking_materialBarcode
import kotlinx.android.synthetic.main.picking_fragment.picking_scanButton
import kotlinx.android.synthetic.main.quantity_pop_up.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class PickingFragment : Fragment() {

    companion object {
        fun newInstance() = PickingFragment()
    }

    private lateinit var viewModel: PickingViewModel
    lateinit var recyclerView: RecyclerView
    public var progress: Progress? = null
    private var oldPickingMaster: Array<PickingMasterDisplay?>? = null
    private var oldViolatedData: Array<MaterialInward?>? = null
    private var oldScannList: Array<PickingScanList?>? = null
    private var oldPickingMasterDB: Array<PickingScanList?>? = null
    var isScrolling: Boolean = false
    lateinit var layoutManager: LinearLayoutManager
    lateinit var adapter: PickingMasterDetailsAdapter
    var callCount = 1


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val rootView = inflater.inflate(R.layout.picking_fragment, container, false)
        viewModel = ViewModelProvider(this).get(PickingViewModel::class.java)
        this.recyclerView = rootView.findViewById(R.id.picking_recyclerview)
        layoutManager = LinearLayoutManager(this.activity)
        recyclerView.layoutManager = layoutManager
        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(PickingViewModel::class.java)
        recyclerView.adapter = PickingMasterDetailsAdapter(recyclerView, viewModel.picklistMasterDisplay, viewModel)

        (this.activity as AppCompatActivity).setTitle("Picking")
        picking_materialBarcode.requestFocus()

        if (this.arguments != null) {
            viewModel.id = this.arguments!!.getInt("id")

            viewModel.status = this.arguments!!.getString("picklistStatus")
            viewModel.picklistName = this.arguments!!.getString("picklistName")
            println("---viewModel.picklistName-->"+viewModel.picklistName)
            picklist_id_value.text = this.arguments!!.getString("picklistName")
        }
        this.progress = UiHelper.showProgressIndicator(this.activity as AppCompatActivity, "Please wait")
        viewModel.loadPendingInProgCompltedList()
        // viewModel.deleteWithPickName(viewModel.picklistName)

        // Load scanned item from room database
        viewModel.getScanWithPickNumber(viewModel.picklistName)
        // picking_count_value.text  = "0/"+viewModel.picklistMasterDisplay.value?.size?: 0.toString()
        if (viewModel.status?.toLowerCase() == "completed"){
            picking_count_value.text = viewModel.picklistMasterDisplay.value?.size.toString() ?: 0.toString()
            picking_scanButton.setVisibility(View.GONE)
            picking_materialBarcode.setVisibility(View.GONE)
            picking_items_submit_button.setVisibility(View.GONE)
        }


        picking_items_submit_button.setOnClickListener {
            if (MainApplication.hasNetwork(MainApplication.applicationContext())) {
                if (viewModel.totalScannedItem.value == 0) {
                    UiHelper.showErrorToast(this.activity as AppCompatActivity,
                            "Please Scan Barcode before submit")
                } else {
                    if (viewModel.totalScannedItem.value?.toInt()!! < viewModel.picklistMasterDisplay!!.value?.size?.toInt()!!) {
                        AlertDialog.Builder(this.activity as AppCompatActivity, R.style.MyDialogTheme).create().apply {
                            setTitle(" Confirm")
                            setMessage("You have not scanned all items, still want to continue")
                            setButton(AlertDialog.BUTTON_POSITIVE, "Yes", { dialog, _ ->
                                viewModel.submitPickingMaster()
                                dialog.dismiss()
                            })
                            setButton(AlertDialog.BUTTON_NEUTRAL, "No", { dialog, _ ->
                                dialog.dismiss()
                            })
                            show()
                        }
                    } else if (viewModel.totalScannedItem.value?.toInt()!! >= viewModel.picklistMasterDisplay!!.value?.size?.toInt()!!) {
                        AlertDialog.Builder(this.activity as AppCompatActivity, R.style.MyDialogTheme).create().apply {
                            setTitle(" Confirm")
                            setMessage("Are you sure you want to continue")
                            setButton(AlertDialog.BUTTON_POSITIVE, "Yes", { dialog, _ ->
                                viewModel.submitPickingMaster()
                                dialog.dismiss()
                            })
                            setButton(AlertDialog.BUTTON_NEUTRAL, "No", { dialog, _ ->
                                dialog.dismiss()
                            })
                            show()
                        }
                    }
                }
            }else {
                UiHelper.showErrorToast(this.activity as AppCompatActivity, "Please submit the list when in Network!")
            }
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

        picking_scanButton.setOnClickListener {
            val inputMaterialBarcode = picking_materialBarcode.getText().toString()
            if (inputMaterialBarcode == "") {
                UiHelper.showErrorToast(this.activity as AppCompatActivity,
                        "Please enter MATERIAL Barcode value")
               // picking_materialBarcode.requestFocus()
            }else {
                // recyclerView.adapter?.notifyDataSetChanged()

//                val alreadyScaned = viewModel.scanedItems.filter { it.serialNumber == inputMaterialBarcode ||
//                        it.violatedSerialNumber == inputMaterialBarcode}
//                if (alreadyScaned.size > 0){
//                    UiHelper.showErrorToast(this.activity as AppCompatActivity,
//                            "Item already scanned")
//                } else
                if (viewModel.totalScannedItem.value!!.toInt() >= viewModel.picklistMasterDisplay!!.value?.size!!.toInt()){
                    UiHelper.showErrorToast(this.activity as AppCompatActivity, "Picklist completed")
                }
                else {
                    // load data from server if response in not empty then do further steps
                    viewModel.barcodeSerial = inputMaterialBarcode
                    this.progress = UiHelper.showProgressIndicator(this.activity as AppCompatActivity, "Please wait")
                    viewModel.loadPutawayMaterialScan(inputMaterialBarcode)
                    UiHelper.hideProgress(this.progress)
                }
            }
            picking_materialBarcode.text!!.clear()
            picking_materialBarcode.requestFocus()
        }

        //-----------------------------------------------------------------------------
        picking_materialBarcode.setOnEditorActionListener { _, i, keyEvent ->
            var handled = false
            if ((picking_materialBarcode.text != null && picking_materialBarcode.text!!.isNotEmpty()) && i == EditorInfo.IME_ACTION_DONE
                    || (keyEvent != null && (keyEvent.keyCode == KeyEvent.KEYCODE_ENTER || keyEvent.keyCode == KeyEvent.KEYCODE_TAB)
                            && keyEvent.action == KeyEvent.ACTION_DOWN)) {
                val keyboard = activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                keyboard.hideSoftInputFromWindow(activity?.currentFocus?.getWindowToken(), 0)

                val inputMaterialBarcode = picking_materialBarcode.getText().toString()

                if (inputMaterialBarcode == "") {
                    UiHelper.showErrorToast(this.activity as AppCompatActivity,
                            "Please enter MATERIAL Barcode value")
                    // picking_materialBarcode.requestFocus()
                }else {
//                    val alreadyScaned = viewModel.scanedItems.filter { it.serialNumber == inputMaterialBarcode ||
//                            it.violatedSerialNumber == inputMaterialBarcode}
//                    if (alreadyScaned.size > 0){
//                        UiHelper.showErrorToast(this.activity as AppCompatActivity,
//                                "Item already scanned")
//                    } else


                    if (viewModel.totalScannedItem.value!!.toInt() >= viewModel.picklistMasterDisplay!!.value?.size!!.toInt()){
                        UiHelper.showErrorToast(this.activity as AppCompatActivity, "Picklist completed")
                    } else {
                        // load data from server if response in not empty then do further steps
                        viewModel.barcodeSerial = inputMaterialBarcode
                        this.progress = UiHelper.showProgressIndicator(this.activity as AppCompatActivity, "Please wait")
                        viewModel.loadPutawayMaterialScan(inputMaterialBarcode)
                        UiHelper.hideProgress(this.progress)
                    }
                }
                picking_materialBarcode.text!!.clear()
                picking_materialBarcode.requestFocus()

                handled = true
            }
            handled
        }

        //---------------------------------------------------------------------------

        viewModel.violatedData.observe(viewLifecycleOwner, Observer<Array<MaterialInward?>> {
            if (it != null) {
                UiHelper.hideProgress(this.progress)
                this.progress = null

                val currentDisplayingItem = viewModel.picklistMasterDisplayList!!.filter {
                    it?.batchNumber == viewModel.barcodeSerial
                }

//                val currentDisplayingItem = viewModel.violatedData.value!!.filter {
//                    it?.barcodeSerial == viewModel.barcodeSerial
//                }
                if (currentDisplayingItem.size > 0){
                    var orgQuantity = currentDisplayingItem.get(0)?.numberOfPacks
                    viewModel.orgQuantity = orgQuantity
                    // println("orgQuantity 1-->"+orgQuantity)
                    val alredyScanCheck = viewModel.scanedItems?.filter {
                        it!!.serialNumber == viewModel.barcodeSerial
                    }
                    if (alredyScanCheck.isNotEmpty()){
                        var scannedQuantity = alredyScanCheck.get(0).quantity
                        orgQuantity = alredyScanCheck.get(0).totalQuantity
                        //println("scannedQuantity 2 ->"+scannedQuantity)
                        // println("orgQuantity 2-->"+orgQuantity)

                        val remainingQuantity = orgQuantity?.minus(scannedQuantity!!)
                        // println("remainingQuantity 2-->"+remainingQuantity)

                        viewModel.remainingQuantity = remainingQuantity

                        if (scannedQuantity == orgQuantity?.toInt()){
                            UiHelper.showErrorToast(this.activity as AppCompatActivity, "Already scanned")
                        }else if ( scannedQuantity?.toInt()!! < orgQuantity!!){
                            val mDialogView = LayoutInflater.from(context).inflate(R.layout.quantity_pop_up, null)
                            val mBuilder = AlertDialog.Builder(context!!)
                                    .setView(mDialogView)
                                    .setTitle("Quantity")
                            mDialogView.issue_to_prod_picked_quantity.setHint("Expected Quantity "+remainingQuantity)

                            val mAlertDialog = mBuilder.show()
                            mDialogView.quantitySubmitBtn.setOnClickListener{
                                // viewModel.quantity = mDialogView.issue_to_prod_picked_quantity.text.toString()
                                // var error = ""
//                                var inputQuantity = 0
//                                scannedQuantity = scannedQuantity!! + inputQuantity
                                val inputQuantity = mDialogView.issue_to_prod_picked_quantity.text.trim().toString().toInt()
                                if (inputQuantity!! > remainingQuantity!!){
                                    // val remainingQuantity = orgQuantity - scannedQuantity!!
                                    var error = "Expected quantity "+remainingQuantity
                                    mDialogView.issue_to_prod_picked_quantity.setError(error)
                                }
                                else{
                                    mAlertDialog.dismiss()
                                    scannedQuantity = scannedQuantity!! + inputQuantity
                                    // update quantity to data base where barcode match with input barcode
                                    GlobalScope.launch {
                                        viewModel.updateQuantityScanWithPickNumber(alredyScanCheck.get(0).serialNumber,
                                                viewModel.picklistName, scannedQuantity)
                                    }
                                    recyclerView.adapter?.notifyDataSetChanged()
                                }
                            }
                            mDialogView.quantityCancelBtn.setOnClickListener{
                                mAlertDialog.dismiss()
                            }
                        }

                    }
                    // new material add to database
                    else{
                        if (viewModel.violatedData.value.isNullOrEmpty()) {
                            UiHelper.showErrorToast(this.activity as AppCompatActivity, "Invalid Barcode")
                        } else if (it != oldPickingMaster) {

                            val pendingItemCheck = viewModel.violatedData.value?.filter {
                                it!!.QCStatus == 0
                            }
                            if (pendingItemCheck?.size!! > 0) {
                                UiHelper.showErrorToast(this.activity as AppCompatActivity, "Material QC status is pending please enter approved material baracode")
                            } else {
                                // check for item is displaying in list
                                val result = viewModel.picklistMasterDisplayList!!.filter {
                                    it?.batchNumber == viewModel.barcodeSerial
                                }
                                if (result.isNotEmpty()) {
                                    if (result.size > 0) {
                                        var quantity = result?.get(0)?.numberOfPacks

                                        // println("quantity 255-->"+quantity)
                                        if (quantity!! > 1){
                                            val mDialogView = LayoutInflater.from(context).inflate(R.layout.quantity_pop_up, null)
                                            val mBuilder = AlertDialog.Builder(context!!)
                                                    .setView(mDialogView)
                                                    .setTitle("Quantity")
                                            val mAlertDialog = mBuilder.show()


                                            mDialogView.quantitySubmitBtn.setOnClickListener{
                                                // viewModel.quantity = mDialogView.issue_to_prod_picked_quantity.text.toString()
                                                // var error = ""
                                                val inputQuantity = mDialogView.issue_to_prod_picked_quantity.text.trim().toString().toInt()
                                                if (inputQuantity > quantity!!){
                                                    var error = "Quantity should not be greater than "+quantity
                                                    mDialogView.issue_to_prod_picked_quantity.setError(error)
                                                }
                                                else{
                                                    mAlertDialog.dismiss()
                                                    GlobalScope.launch {
                                                        viewModel.addItemInDatabase(result.first()!!,
                                                                viewModel.picklistName, viewModel.id.toString(),
                                                                false, inputQuantity, orgQuantity)
                                                    }
                                                }
                                                recyclerView.adapter?.notifyDataSetChanged()
                                            }
                                            mDialogView.quantityCancelBtn.setOnClickListener{
                                                mAlertDialog.dismiss()
                                            }
                                        } else {
                                            GlobalScope.launch {
                                                viewModel.addItemInDatabase(result.first()!!,
                                                        viewModel.picklistName, viewModel.id.toString(),
                                                        false, quantity, orgQuantity)
                                            }
                                            recyclerView.adapter?.notifyDataSetChanged()
                                        }
                                    }
                                }
                                else {
                                    // check for violated material
                                    var partNmberToCheck = viewModel.violatedData.value?.get(0)?.partnumber?.partNumber
                                    var partNumberValidate = viewModel.picklistMasterDisplayList?.filter {
                                        it!!.partNumber.toString() == partNmberToCheck
                                    }
                                    if (viewModel.diffpicklistMasterDisplayList.size > 0){
                                        partNumberValidate = viewModel.diffpicklistMasterDisplayList?.filter {
                                            it!!.partNumber.toString() == partNmberToCheck
                                        }
                                    }

                                    if (partNumberValidate?.size!! > 0) {
                                        var batchNoToUpdate = partNumberValidate?.get(0)?.batchNumber
                                        viewModel.batchNoToUpdate = batchNoToUpdate
                                        viewModel.partNumber = partNmberToCheck

                                        var quantity = partNumberValidate?.get(0)?.numberOfPacks
                                        println("quantity 296-->"+quantity)
                                        // post call to submit data
                                        AlertDialog.Builder(this.activity as AppCompatActivity, R.style.MyDialogTheme).create().apply {
                                            setTitle("Violate Picklist Data")
                                            setMessage("Entered pack does not belongs to picklist item, Do you still want to pick this?")
                                            setButton(AlertDialog.BUTTON_POSITIVE, "Yes") { dialog, _ ->

                                                if (quantity!! > 1){

                                                    val mDialogView = LayoutInflater.from(context).inflate(R.layout.quantity_pop_up, null)
                                                    val mBuilder = AlertDialog.Builder(context!!)
                                                            .setView(mDialogView)
                                                            .setTitle("Quantity")
                                                    val mAlertDialog = mBuilder.show()
                                                    mDialogView.quantitySubmitBtn.setOnClickListener{

                                                        val inputQuantity = mDialogView.issue_to_prod_picked_quantity.text.trim().toString().toInt()
                                                        if (inputQuantity > quantity!!){
                                                            var error = "Quantity should not be greater than "+quantity
                                                            mDialogView.issue_to_prod_picked_quantity.setError(error)
                                                        } else{
                                                            mAlertDialog.dismiss()
                                                            GlobalScope.launch {
                                                                viewModel.addItemInDatabase(partNumberValidate.first()!!,
                                                                        viewModel.picklistName, viewModel.id.toString(),
                                                                        true, inputQuantity, quantity)
                                                            }
                                                        }
                                                    }
                                                    mDialogView.quantityCancelBtn.setOnClickListener{
                                                        mAlertDialog.dismiss()
                                                    }
                                                    dialog.dismiss()
                                                    recyclerView.adapter?.notifyDataSetChanged()
                                                } else {
                                                    GlobalScope.launch {
                                                        viewModel.addItemInDatabase(partNumberValidate.first()!!,
                                                                viewModel.picklistName, viewModel.id.toString(),
                                                                true, quantity, quantity)
                                                    }
                                                }
                                            }
                                            setButton(AlertDialog.BUTTON_NEUTRAL, "No") { dialog, _ ->
                                                dialog.dismiss()
                                            }
                                            show()
                                        }
                                    } else {
                                        UiHelper.showErrorToast(this.activity as AppCompatActivity, "Barcode not belong to any partnumber")
                                    }
                                }
                            }
                        }
                    }

                }else{
                    UiHelper.showErrorToast(this.activity as AppCompatActivity, "something gone wrong")
                }


                // old code
                /*
                val alredyScanCheck = viewModel.scanedItems?.filter {
                    it!!.serialNumber == viewModel.barcodeSerial
                }
                if (alredyScanCheck.isNotEmpty()){
                    UiHelper.showErrorToast(this.activity as AppCompatActivity, "Already scanned")
                }
                else{
                    if (viewModel.violatedData.value.isNullOrEmpty()) {
                        UiHelper.showErrorToast(this.activity as AppCompatActivity, "Invalid Barcode")
                    } else if (it != oldPickingMaster) {

                        val pendingItemCheck = viewModel.violatedData.value?.filter {
                            it!!.QCStatus == 0
                        }
                        if (pendingItemCheck?.size!! > 0) {
                            UiHelper.showErrorToast(this.activity as AppCompatActivity, "Material QC status is pending please enter approved material baracode")
                        } else {
                            // check for item is displaying in list
                            val result = viewModel.picklistMasterDisplayList!!.filter {
                                it?.batchNumber == viewModel.barcodeSerial
                            }
                            if (result.isNotEmpty()) {
                                if (result.size > 0) {
                                    var quantity = result?.get(0)?.numberOfPacks

                                    // println("quantity 255-->"+quantity)
                                    if (quantity!! > 1){
                                        val mDialogView = LayoutInflater.from(context).inflate(R.layout.quantity_pop_up, null)
                                        val mBuilder = AlertDialog.Builder(context!!)
                                                .setView(mDialogView)
                                                .setTitle("Quantity")
                                        val mAlertDialog = mBuilder.show()


                                        mDialogView.quantitySubmitBtn.setOnClickListener{
                                            // viewModel.quantity = mDialogView.issue_to_prod_picked_quantity.text.toString()
                                            // var error = ""
                                            val inputQuantity = mDialogView.issue_to_prod_picked_quantity.text.trim().toString().toInt()
                                            if (inputQuantity > quantity!!){
                                                var error = "Quantity should not be greater than "+quantity
                                                mDialogView.issue_to_prod_picked_quantity.setError(error)
                                            }
                                            else{
                                                mAlertDialog.dismiss()
                                                GlobalScope.launch {
                                                    viewModel.addItemInDatabase(result.first()!!,
                                                            viewModel.picklistName, viewModel.id.toString(),
                                                            false, inputQuantity)
                                                }
                                            }
                                            recyclerView.adapter?.notifyDataSetChanged()
                                        }
                                        mDialogView.quantityCancelBtn.setOnClickListener{
                                            mAlertDialog.dismiss()
                                        }
                                    } else {
                                        GlobalScope.launch {
                                            viewModel.addItemInDatabase(result.first()!!,
                                                    viewModel.picklistName, viewModel.id.toString(),
                                                    false, quantity)
                                        }
                                        recyclerView.adapter?.notifyDataSetChanged()
                                    }
                                }
                            }
                            else {
                                // check for violated material
                                var partNmberToCheck = viewModel.violatedData.value?.get(0)?.partnumber?.partNumber
                                var partNumberValidate = viewModel.picklistMasterDisplayList?.filter {
                                    it!!.partNumber.toString() == partNmberToCheck
                                }
                                if (viewModel.diffpicklistMasterDisplayList.size > 0){
                                    partNumberValidate = viewModel.diffpicklistMasterDisplayList?.filter {
                                        it!!.partNumber.toString() == partNmberToCheck
                                    }
                                }

                                if (partNumberValidate?.size!! > 0) {
                                    var batchNoToUpdate = partNumberValidate?.get(0)?.batchNumber
                                    viewModel.batchNoToUpdate = batchNoToUpdate
                                    viewModel.partNumber = partNmberToCheck

                                    var quantity = partNumberValidate?.get(0)?.numberOfPacks
                                    println("quantity 296-->"+quantity)
                                    // post call to submit data
                                    AlertDialog.Builder(this.activity as AppCompatActivity, R.style.MyDialogTheme).create().apply {
                                        setTitle("Violate Picklist Data")
                                        setMessage("Entered pack does not belongs to picklist item, Do you still want to pick this?")
                                        setButton(AlertDialog.BUTTON_POSITIVE, "Yes") { dialog, _ ->

                                            if (quantity!! > 1){

                                                val mDialogView = LayoutInflater.from(context).inflate(R.layout.quantity_pop_up, null)
                                                val mBuilder = AlertDialog.Builder(context!!)
                                                        .setView(mDialogView)
                                                        .setTitle("Quantity")
                                                val mAlertDialog = mBuilder.show()
                                                mDialogView.quantitySubmitBtn.setOnClickListener{

                                                    val inputQuantity = mDialogView.issue_to_prod_picked_quantity.text.trim().toString().toInt()
                                                    if (inputQuantity > quantity!!){
                                                        var error = "Quantity should not be greater than "+quantity
                                                        mDialogView.issue_to_prod_picked_quantity.setError(error)
                                                    } else{
                                                        mAlertDialog.dismiss()
                                                        GlobalScope.launch {
                                                            viewModel.addItemInDatabase(partNumberValidate.first()!!,
                                                                    viewModel.picklistName, viewModel.id.toString(),
                                                                    true, inputQuantity)
                                                        }
                                                    }
                                                }
                                                mDialogView.quantityCancelBtn.setOnClickListener{
                                                    mAlertDialog.dismiss()
                                                }
                                                dialog.dismiss()
                                                recyclerView.adapter?.notifyDataSetChanged()
                                            } else {
                                                GlobalScope.launch {
                                                    viewModel.addItemInDatabase(partNumberValidate.first()!!,
                                                            viewModel.picklistName, viewModel.id.toString(),
                                                            true, quantity)
                                                }
                                            }
                                        }
                                        setButton(AlertDialog.BUTTON_NEUTRAL, "No") { dialog, _ ->
                                            dialog.dismiss()
                                        }
                                        show()
                                    }
                                } else {
                                    UiHelper.showErrorToast(this.activity as AppCompatActivity, "Barcode not belong to any partnumber")
                                }
                            }
                        }
                    }
                } */
            }
            oldViolatedData = viewModel.violatedData.value
        })

        viewModel.itemSubmissionSuccessful.observe(viewLifecycleOwner, Observer<Boolean> {
            if (it == true) {
                UiHelper.hideProgress(this.progress)
                this.progress = null
                // var thisObject = this
                UiHelper.showSuccessToast(this.activity as AppCompatActivity,
                        "Updated successfully")
                Navigation.findNavController(recyclerView).navigate(R.id.picklistMasterFragment)

            }
        })

        viewModel.itemSubmissionSuccessfulUpdate.observe(viewLifecycleOwner, Observer<Boolean> {
            if (it == true) {
                UiHelper.hideProgress(this.progress)
                this.progress = null
                // var thisObject = this
                recyclerView.adapter?.notifyDataSetChanged()
            }
        })

        viewModel.totalScannedItem.observe(viewLifecycleOwner, Observer<Number> {
            if (it != null) {
                if (viewModel.status?.toLowerCase()=="completed"){
                    picking_count_value.text = viewModel.picklistMasterDisplay!!.value?.size.toString() ?: 0.toString()
                }else{
                    picking_count_value.text = viewModel.totalScannedItem.value.toString() + "/"+viewModel.picklistMasterDisplay!!.value?.size.toString() ?: 0.toString()
                    recyclerView.adapter?.notifyDataSetChanged()

                    for (i in viewModel.scanedItems){
                        val scannedItemFound = viewModel.picklistMasterDisplayList?.filter{
                            it!!.batchNumber.toString() == viewModel.barcodeSerial.toString() ||
                                    it!!.batchNumber.toString() == viewModel.batchNoToUpdate.toString()}
                        if (scannedItemFound.size > 0){
                            viewModel.picklistMasterDisplayList.remove(scannedItemFound[0])
                            // scannedItemFound[0].numberOfPacks = i.quantity
                            viewModel.picklistMasterDisplayList.add(0, scannedItemFound[0])
                        }

/*
                        if (i.isViolated!!){
//                            for (i in viewModel.scanedItems){
//                                for (j in viewModel.picklistMasterDisplayList){
//                                    if (((i.serialNumber != j.batchNumber )) && !viewModel.picklistMasterDisplayList.contains(j)) {
//                                        viewModel.diffpicklistMasterDisplayList.add(j)
//                                    }
//                                }
//                            }
//                            for (i in viewModel.diffpicklistMasterDisplayList){
//                                println("diff item --"+i.barcodeSerial + "--batchnumber-> "+i.batchNumber)
//                            }
                            val scannedItemFound = viewModel.picklistMasterDisplayList?.filter{
                                it!!.partNumber.toString() == viewModel.partNumber.toString() }

                            if (scannedItemFound.size > 0 ){
                                viewModel.picklistMasterDisplayList.remove(scannedItemFound[0])
                                scannedItemFound[0].numberOfPacks = i.quantity
                                viewModel.picklistMasterDisplayList.add(0, scannedItemFound[0])
                                // viewModel.diffpicklistMasterDisplayList = ArrayList<PickingMasterDisplay>()
                            }
                        }else{

                            //  change position of the scanned item to top
                            val scannedItemFound = viewModel.picklistMasterDisplayList?.filter{
                                it!!.batchNumber.toString() == viewModel.barcodeSerial.toString() ||
                                        it!!.batchNumber.toString() == viewModel.batchNoToUpdate.toString()}
                            if (scannedItemFound.size > 0){
                                viewModel.picklistMasterDisplayList.remove(scannedItemFound[0])
                                scannedItemFound[0].numberOfPacks = i.quantity
                                viewModel.picklistMasterDisplayList.add(0, scannedItemFound[0])
                            }
                            // recyclerView.adapter?.notifyDataSetChanged()
                        }
                        // recyclerView.adapter?.notifyDataSetChanged() */
                     }
                    recyclerView.adapter?.notifyDataSetChanged()
                }
                recyclerView.adapter?.notifyDataSetChanged()
            }
        })

        viewModel.picklistMasterDisplay.observe(viewLifecycleOwner, Observer<Array<PickingMasterDisplay?>> {
            if (it != null) {
                UiHelper.hideProgress(this.progress)
                this.progress = null
                if (viewModel.picklistMasterDisplay.value.orEmpty().isNotEmpty() &&
                        viewModel.picklistMasterDisplay.value?.first() == null) {
                    UiHelper.showSomethingWentWrongSnackbarMessage(this.activity as AppCompatActivity)
                } else if (it != oldPickingMaster) {
                    if (viewModel.status?.toLowerCase()=="completed"){
                        picking_count_value.text = viewModel.picklistMasterDisplay!!.value?.size.toString() ?: 0.toString()
                    }else{
                        picking_count_value.text = viewModel.totalScannedItem.value.toString() + "/"+ viewModel.picklistMasterDisplay.value?.size
                                ?: 0.toString()
                    }

                    recyclerView.adapter?.notifyDataSetChanged()
                }
            }
            oldPickingMaster = viewModel.picklistMasterDisplay.value
            })
        }
    }

    open class PickingMasterDetailsAdapter(private val recyclerView: androidx.recyclerview.widget.RecyclerView,
                                           private val picklistMasterDisplay: LiveData<Array<PickingMasterDisplay?>>,
                                           private val viewModel: PickingViewModel) :
            androidx.recyclerview.widget.RecyclerView.Adapter<PickingMasterDetailsAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.picking_row, parent, false)
            return ViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind()
        }


        override fun getItemCount(): Int {
            // println("picking count---->" + viewModel.picklistMasterDisplay.value?.size ?: 0)
            return viewModel.picklistMasterDisplayList?.size ?: 0
        }

        open inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            protected val partNumber: TextView
            protected val barcodeSerial: TextView
            protected val description: TextView
            protected val quantity: TextView
            protected val location: TextView
            protected val barcodePickedValue: TextView
            protected val barcodePickedLabel: TextView
            protected val violatedBarcodeLabel: TextView
            protected val violatedBarcodeValue: TextView
            protected val linearLayout: LinearLayout
            protected val pickingRemainingQuantityLabel: TextView
            protected val pickingRemainingQuantityvalue: TextView



            init {
                partNumber = itemView.findViewById(R.id.picking_partNumberValue)
                barcodeSerial = itemView.findViewById(R.id.picking_barcodeSerialValue)
                description = itemView.findViewById(R.id.picking_descriptionValue)
                quantity = itemView.findViewById(R.id.picking_quantityValue)
                location = itemView.findViewById(R.id.picking_locationValue)
                barcodePickedValue = itemView.findViewById(R.id.picking_pickedbarcodeValue)
                barcodePickedLabel = itemView.findViewById(R.id.picking_pickedbarcodeLabel)
                violatedBarcodeLabel = itemView.findViewById(R.id.violated_barcodeLabel)
                violatedBarcodeValue = itemView.findViewById(R.id.violated_barcodeValue)
                linearLayout = itemView.findViewById(R.id.picking_layout)
                pickingRemainingQuantityLabel = itemView.findViewById(R.id.picking_remaining_quantityLabel)
                pickingRemainingQuantityvalue = itemView.findViewById(R.id.picking_remaining_quantityValue)
            }

            fun bind() {
                val item = viewModel.picklistMasterDisplayList!![adapterPosition]!!
                partNumber.text = item.partNumber
                barcodeSerial.text = item.batchNumber
                description.text = item.partDescription
                quantity.text = item.numberOfPacks.toString()
                location.text = item.location
                if (viewModel.status?.toLowerCase() == "completed"){
                    barcodePickedLabel.visibility = View.VISIBLE
                    barcodePickedValue.visibility =  View.VISIBLE
                    barcodePickedValue.text = item.batchNumber
                }
                val scannedItemFound = viewModel.scanedItems?.filter{
                    it!!.serialNumber.toString() == item!!.batchNumber.toString()}

                if (scannedItemFound.size > 0){
                    if (scannedItemFound[0].quantity!! < scannedItemFound[0].totalQuantity!!){
                            linearLayout.setBackgroundColor(PrefConstants().lightOrangeColor)
                            pickingRemainingQuantityLabel.visibility = View.VISIBLE
                            pickingRemainingQuantityvalue.visibility = View.VISIBLE
                            quantity.text = scannedItemFound[0].quantity.toString()
                            pickingRemainingQuantityvalue.text = (scannedItemFound[0].totalQuantity?.minus(scannedItemFound[0].quantity!!)).toString()
                    }else{
                            linearLayout.setBackgroundColor(PrefConstants().lightGreenColor)
                            pickingRemainingQuantityvalue.visibility = View.GONE
                            pickingRemainingQuantityLabel.visibility = View.GONE
                        }
                    if (scannedItemFound[0].isViolated!!){
                        violatedBarcodeLabel.visibility = View.VISIBLE
                        violatedBarcodeValue.visibility = View.VISIBLE
                        violatedBarcodeValue.text = scannedItemFound[0].violatedSerialNumber
                    }

                }else{
                    linearLayout.setBackgroundColor(PrefConstants().lightGrayColor)
                }
            }
        }
    }
