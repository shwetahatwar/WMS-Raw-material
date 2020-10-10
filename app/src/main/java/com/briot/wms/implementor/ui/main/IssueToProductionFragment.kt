package com.briot.wms.implementor.ui.main

import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.briot.wms.implementor.MainApplication
import com.briot.wms.implementor.R
import com.briot.wms.implementor.UiHelper
import com.briot.wms.implementor.data.IssueToProduction
import com.briot.wms.implementor.repository.local.PrefConstants
import com.briot.wms.implementor.repository.remote.IssueToProductionResponse
import com.briot.wms.implementor.repository.remote.MaterialData
import com.briot.wms.implementor.repository.remote.MaterialInward
import io.github.pierry.progress.Progress
import kotlinx.android.synthetic.main.issue_to_production_fragment.*
import kotlinx.android.synthetic.main.material_q_c_status_fragment.*
import kotlinx.android.synthetic.main.quantity_pop_up.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class IssueToProductionFragment : Fragment() {

    companion object {
        fun newInstance() = IssueToProductionFragment()
    }

    private lateinit var viewModel: IssueToProductionViewModel
    lateinit var recyclerView: RecyclerView
    public var progress: Progress? = null
    private var oldMaterialMaster: Array<MaterialData?>? = null
    var oldScanList: Array<IssueToProduction?>? = null
    private var oldMaterialInward: Array<MaterialInward?> = emptyArray()


    private var oldIssueToProd: Array<IssueToProductionResponse?>? = null
    lateinit var layoutManager: LinearLayoutManager
    lateinit var adapter: MaterialAdapter
    var isScrolling: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.issue_to_production_fragment, container, false)
        viewModel = ViewModelProvider(this).get(IssueToProductionViewModel::class.java)
        this.recyclerView = rootView.findViewById(R.id.issuetoproduction_recyclerlist)
        layoutManager = LinearLayoutManager(this.activity)
        recyclerView.layoutManager = layoutManager
        adapter = MaterialAdapter(recyclerView, viewModel.materialData, viewModel)
        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // TODO: Use the ViewModel
        viewModel = ViewModelProvider(this).get(IssueToProductionViewModel::class.java)
        recyclerView.adapter = MaterialAdapter(recyclerView, viewModel.materialData, viewModel)

        (this.activity as AppCompatActivity).setTitle("Issue to Production")
       // issuetoproduction_materialBarcode.requestFocus()
        if (this.arguments != null) {
            viewModel.employeeId = this.arguments!!.getString("employeeId")
            employee_value.text = this.arguments!!.getString("employeeId")
            // println("employeeId -->"+ this.arguments!!.getString("employeeId"))
            viewModel.name = this.arguments!!.getString("picklistName")
            picklist_value.text = this.arguments!!.getString("picklistName")

            viewModel.Projectname =  this.arguments!!.getString("name")
            project_name_value1.text = this.arguments!!.getString("name")
            viewModel.id = this.arguments!!.getInt("id")
            viewModel.userId = this.arguments!!.getInt("userId")
            viewModel.projectId = this.arguments!!.getInt("projectId")
        }

        this.progress = UiHelper.showProgressIndicator(this.activity as AppCompatActivity, "Please wait")
        viewModel.getScanedItems(viewModel.employeeId, viewModel.name)
        viewModel.loadMaterialItems(viewModel.id)
        // UiHelper.hideProgress(this.progress)

        viewModel.issuetoproductionlist.observe(viewLifecycleOwner, Observer<Array<IssueToProductionResponse?>> {
            if (it != null) {
                UiHelper.hideProgress(this.progress)
                this.progress = null
                if (viewModel.issuetoproductionlist.value.isNullOrEmpty()) {
                    UiHelper.showErrorToast(this.activity as AppCompatActivity, "Enter valid Barcode")
                } else if (it != oldIssueToProd) {
                    issue_to_prod_count.text = viewModel.totalScannedItem.value.toString() + "/"+viewModel.materialData!!.value?.size.toString() ?: 0.toString()
                    recyclerView.adapter?.notifyDataSetChanged()
                    UiHelper.showSuccessToast(this.activity as AppCompatActivity,"Updated successfully")
                }
            }
            oldIssueToProd = viewModel.issuetoproductionlist.value
        })



//        viewModel.scanedItems.observe(viewLifecycleOwner, Observer<Array<IssueToProduction?>> {
//            if (it != null) {
//                UiHelper.hideProgress(this.progress)
//                this.progress = null
//                if (it != oldScanList) {
//                    issue_to_prod_count.text = viewModel.totalScannedItem.value.toString() + "/"+viewModel.materialData!!.value?.size.toString() ?: 0.toString()
//                    recyclerView.adapter?.notifyDataSetChanged()
//                }
//            }
//            oldMaterialMaster = viewModel.materialData.value
//        })

        viewModel.materialData.observe(viewLifecycleOwner, Observer<Array<MaterialData?>> {
            if (it != null) {
                UiHelper.hideProgress(this.progress)
                this.progress = null
                if (viewModel.materialData.value.orEmpty().isNotEmpty() &&
                        viewModel.materialData.value?.first() == null) {
                    UiHelper.showSomethingWentWrongSnackbarMessage(this.activity as AppCompatActivity)
                } else if (it != oldMaterialMaster) {
                    issue_to_prod_count.text = viewModel.totalScannedItem.value.toString() + "/"+viewModel.materialData!!.value?.size.toString() ?: 0.toString()
                    recyclerView.adapter?.notifyDataSetChanged()
                }
            }
            oldMaterialMaster = viewModel.materialData.value
        })

        viewModel.totalScannedItem.observe(viewLifecycleOwner, Observer<Number> {
            if (it != null) {
                issue_to_prod_count.text = viewModel.totalScannedItem.value.toString() + "/"+viewModel.materialData!!.value?.size.toString() ?: 0.toString()

                // To show scanned item at top
                for (i in viewModel.scanedItems){
                    val scannedItemFound = viewModel.issueToProdList.filter{
                        it!!.batchNumber.toString() == i.inputMaterialBarcode.toString()}
                    if (scannedItemFound != null){
                        if (scannedItemFound?.size!! > 0){
                            // val position = viewModel.issueToProdList.indexOf(scannedItemFound[0])
                            //val removeItem = viewModel.issueToProdList.removeAt(position)
                            viewModel.issueToProdList.remove(scannedItemFound[0])
                            viewModel.issueToProdList.add(0, scannedItemFound[0])
                            recyclerView.adapter?.notifyDataSetChanged()
                        }
                    }
                }

                /*
                // change position of the scaned item to top
                for (i in viewModel.scanedItems){
                    val scannedItemFound = viewModel.materialData.value?.filter{
                        it!!.batchNumber.toString() == i.inputMaterialBarcode.toString()}
                    if (scannedItemFound != null){
                        if (scannedItemFound?.size!! > 0){
                            val position = viewModel.materialData.value!!.indexOf(scannedItemFound[0])
                            val removeItem = viewModel.materialData.value!!.drop(position)
                            println("removeItem-->"+removeItem)
                            viewModel.materialData.value!!.set(0, removeItem[0])
                            recyclerView.adapter?.notifyDataSetChanged()
                        }
                    }
                } */
                recyclerView.adapter?.notifyDataSetChanged()
            }
        })


        issuetoproduction_scanButton.setOnClickListener {
            //val inputLocationBarcode  = "17911"
            val inputMaterialBarcode = issuetoproduction_materialBarcode.getText().toString()
            if (inputMaterialBarcode == "") {
                UiHelper.showErrorToast(this.activity as AppCompatActivity,
                        "Please enter Material Barcode value")
                //issuetoproduction_materialBarcode.requestFocus()
            }else if (inputMaterialBarcode.length < 10 || inputMaterialBarcode.length > 10) {
                UiHelper.showErrorToast(this.activity as AppCompatActivity, "Enter valid Barcode");
            }
            else{

                viewModel.inputMaterialBarcode = inputMaterialBarcode
                viewModel.loadMaterialStatusItems(inputMaterialBarcode)

                /*
                val alreadyScaned = viewModel.scanedItems?.filter { it?.inputMaterialBarcode ==  inputMaterialBarcode}
                if (alreadyScaned.isNullOrEmpty()){
                    viewModel.inputMaterialBarcode = inputMaterialBarcode
                    val result = viewModel.materialData.value?.filter {
                        (it?.serialNumber.equals(inputMaterialBarcode))
                    }
                    if (result?.size!! > 0){
                        // add to room database
                        var quantity = result.get(0)?.quantityPicked
                        var materialInwardId = result.get(0)?.id
                        if (quantity != null) {
                            if (quantity > 1){
                                // Alert dialog with text box
                                val mDialogView = LayoutInflater.from(context).inflate(R.layout.quantity_pop_up, null)
                                val mBuilder = AlertDialog.Builder(context!!)
                                        .setView(mDialogView)
                                        .setTitle("Quantity")
                                val mAlertDialog = mBuilder.show()
                                mDialogView.quantitySubmitBtn.setOnClickListener{

                                    var inputQuantity = mDialogView.issue_to_prod_picked_quantity.text.trim().toString().toInt()
                                    viewModel.quantity = inputQuantity.toString()
                                    println("--inputQuantity->"+inputQuantity)

                                    if (inputQuantity > quantity!!){
                                        var error = "Quantity should not be greater than "+quantity
                                        mDialogView.issue_to_prod_picked_quantity.setError(error)

                                    }else {
                                        mAlertDialog.dismiss()
                                        GlobalScope.launch {
                                            withContext(Dispatchers.Main) {
                                                viewModel.addItemInDatabase(inputMaterialBarcode, viewModel.projectId,
                                                        viewModel.userId, viewModel.id, inputQuantity, materialInwardId,
                                                        viewModel.name, viewModel.employeeId)
                                            }
                                        }
                                    }

                                }
                                mDialogView.quantityCancelBtn.setOnClickListener{
                                    mAlertDialog.dismiss()
                                }
                            }else{
                                GlobalScope.launch {
                                    withContext(Dispatchers.Main) {
                                        viewModel.addItemInDatabase(inputMaterialBarcode, viewModel.projectId,
                                                viewModel.userId, viewModel.id, quantity, materialInwardId,
                                                viewModel.name, viewModel.employeeId)
                                    }
                                }
                            }
                        }
                    }
                }else{
                    UiHelper.showErrorToast(this.activity as AppCompatActivity, "Item already scaned")
                } */
            }
            issuetoproduction_materialBarcode.text?.clear()
            issuetoproduction_materialBarcode.requestFocus()
        }

        //-------------------------------------------------------------------
        issuetoproduction_materialBarcode.setOnEditorActionListener { _, i, keyEvent ->
            var handled = false
            if ((issuetoproduction_materialBarcode.text != null && issuetoproduction_materialBarcode.text!!.isNotEmpty()) && i == EditorInfo.IME_ACTION_DONE
                    || (keyEvent != null && (keyEvent.keyCode == KeyEvent.KEYCODE_ENTER || keyEvent.keyCode == KeyEvent.KEYCODE_TAB)
                            && keyEvent.action == KeyEvent.ACTION_DOWN)) {
                val keyboard = activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                keyboard.hideSoftInputFromWindow(activity?.currentFocus?.getWindowToken(), 0)
                val inputMaterialBarcode = issuetoproduction_materialBarcode.getText().toString()
                if (inputMaterialBarcode == "") {
                    UiHelper.showErrorToast(this.activity as AppCompatActivity,
                            "Please enter Material Barcode value")
                    //issuetoproduction_materialBarcode.requestFocus()
                }else if (inputMaterialBarcode.length < 10 || inputMaterialBarcode.length > 10) {
                    UiHelper.showErrorToast(this.activity as AppCompatActivity, "Enter valid Barcode");
                }
                else{
                    // load barcode data to get material inward id
                    viewModel.inputMaterialBarcode = inputMaterialBarcode
                    viewModel.loadMaterialStatusItems(inputMaterialBarcode)

                 /*
                    val alreadyScaned = viewModel.scanedItems?.filter { it?.inputMaterialBarcode ==  inputMaterialBarcode}
                    if (alreadyScaned.isNullOrEmpty()){

                        val result = viewModel.materialData.value?.filter {
                            (it?.serialNumber.equals(inputMaterialBarcode))
                        }
                        if (result?.size!! > 0){
                            // add to room database
                            var quantity = result.get(0)?.quantityPicked
                            var materialInwardId = result.get(0)?.id
                            if (quantity != null) {
                                if (quantity!! > 1){
                                    // Alert dialog with text box
                                    val mDialogView = LayoutInflater.from(context).inflate(R.layout.quantity_pop_up, null)
                                        val mBuilder = AlertDialog.Builder(context!!)
                                                .setView(mDialogView)
                                                .setTitle("Quantity")
                                        val mAlertDialog = mBuilder.show()
                                        mDialogView.quantitySubmitBtn.setOnClickListener{
                                            var inputQuantity = mDialogView.issue_to_prod_picked_quantity.text.trim().toString().toInt()
                                            viewModel.quantity = inputQuantity.toString()
                                            println("--inputQuantity->"+inputQuantity)
                                            if (inputQuantity > quantity!!){
                                                var error = "Quantity should not be greater than "+quantity
                                                mDialogView.issue_to_prod_picked_quantity.setError(error)

                                            }else {
                                                mAlertDialog.dismiss()
                                                GlobalScope.launch {
                                                    withContext(Dispatchers.Main) {
                                                        viewModel.addItemInDatabase(inputMaterialBarcode, viewModel.projectId,
                                                                viewModel.userId, viewModel.id, inputQuantity, materialInwardId,
                                                                viewModel.name, viewModel.employeeId)
                                                    }
                                                }
                                            }
                                        }
                                    mDialogView.quantityCancelBtn.setOnClickListener{
                                        mAlertDialog.dismiss()
                                    }
                                }else{
                                    GlobalScope.launch {
                                        withContext(Dispatchers.Main) {
                                            viewModel.addItemInDatabase(inputMaterialBarcode, viewModel.projectId,
                                                    viewModel.userId, viewModel.id, quantity, materialInwardId,
                                                    viewModel.name, viewModel.employeeId)
                                        }
                                    }
                                }
                            }
                        }
                    }else{
                        UiHelper.showErrorToast(this.activity as AppCompatActivity, "Item already scaned")
                    } */

                }
                issuetoproduction_materialBarcode.text?.clear()
                issuetoproduction_materialBarcode.requestFocus()

                handled = true
            }
            handled
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

        viewModel.itemSubmissionSuccessfulUpdate.observe(viewLifecycleOwner, Observer<Boolean> {
            if (it == true) {
                UiHelper.hideProgress(this.progress)
                this.progress = null
                // var thisObject = this
                recyclerView.adapter?.notifyDataSetChanged()

                UiHelper.showSuccessToast(this.activity as AppCompatActivity,
                        "Updated successfully")
                Navigation.findNavController(recyclerView).navigate(R.id.projectFragment)
            }
        })

        viewModel.qcStatusDisplay.observe(viewLifecycleOwner, Observer<Array<MaterialInward?>> {
            if (it != null && it != oldMaterialInward) {
                UiHelper.hideProgress(this.progress)
                this.progress = null

                val currentDisplayingItem = viewModel.issueToProdList!!.filter {
                    it?.serialNumber == viewModel.inputMaterialBarcode
                }
                if (currentDisplayingItem.size > 0) {
                    var orgQuantity = currentDisplayingItem.get(0)?.quantityPicked
                    var materialInwardId = viewModel.qcStatusDisplay.value?.get(0)?.id

                    val alredyScanCheck = viewModel.scanedItems?.filter { it?.inputMaterialBarcode == viewModel.inputMaterialBarcode }

                    if (alredyScanCheck.isNotEmpty()) {
                        // Update quantity of scanned item
                        orgQuantity = alredyScanCheck.get(0)?.totalQuantity
                        var scannedQuantity = alredyScanCheck.get(0).quantity

                        val remainingQuantity = orgQuantity?.minus(scannedQuantity!!)
                        viewModel.remainingQuantity = remainingQuantity

                        if (scannedQuantity == orgQuantity?.toInt()) {
                            UiHelper.showErrorToast(this.activity as AppCompatActivity, "Already scanned")
                        } else if (scannedQuantity?.toInt()!! < orgQuantity!!) {
                            val mDialogView = LayoutInflater.from(context).inflate(R.layout.quantity_pop_up, null)
                            val mBuilder = AlertDialog.Builder(context!!)
                                    .setView(mDialogView)
                                    .setTitle("Quantity")
                            mDialogView.issue_to_prod_picked_quantity.setHint("Expected Quantity " + remainingQuantity)

                            val mAlertDialog = mBuilder.show()
                            mDialogView.quantitySubmitBtn.setOnClickListener {
                                val inputQuantity = mDialogView.issue_to_prod_picked_quantity.text.trim().toString().toInt()
                                if (inputQuantity!! > remainingQuantity!!) {
                                    // val remainingQuantity = orgQuantity - scannedQuantity!!
                                    var error = "Expected quantity " + remainingQuantity
                                    mDialogView.issue_to_prod_picked_quantity.setError(error)
                                } else {
                                    mAlertDialog.dismiss()
                                    scannedQuantity = scannedQuantity!! + inputQuantity
                                    // update quantity to data base where barcode match with input barcode
                                    GlobalScope.launch {
                                        viewModel.updateQuantityScanWithPickNumber(alredyScanCheck.get(0).inputMaterialBarcode,
                                                viewModel.name, scannedQuantity)
                                    }
                                    recyclerView.adapter?.notifyDataSetChanged()
                                }
                            }
                            mDialogView.quantityCancelBtn.setOnClickListener {
                                mAlertDialog.dismiss()
                            }
                        }
                    }
                    // new item add to data base
                    else {
                        if (viewModel.qcStatusDisplay.value.isNullOrEmpty()) {
                            UiHelper.showErrorToast(this.activity as AppCompatActivity, "Invalid Barcode")
                            // UiHelper.showSomethingWentWrongSnackbarMessage(this.activity as AppCompatActivity)
                        }
                        if (orgQuantity != null) {
                            if (orgQuantity!! > 1) {
                                // Alert dialog with text box
                                val mDialogView = LayoutInflater.from(context).inflate(R.layout.quantity_pop_up, null)
                                val mBuilder = AlertDialog.Builder(context!!)
                                        .setView(mDialogView)
                                        .setTitle("Quantity")
                                val mAlertDialog = mBuilder.show()
                                mDialogView.quantitySubmitBtn.setOnClickListener {
                                    var inputQuantity = mDialogView.issue_to_prod_picked_quantity.text.trim().toString().toInt()
                                    viewModel.quantity = inputQuantity.toString()
                                    println("--inputQuantity->" + inputQuantity)
                                    if (inputQuantity > orgQuantity!!) {
                                        var error = "Quantity should not be greater than " + orgQuantity
                                        mDialogView.issue_to_prod_picked_quantity.setError(error)

                                    } else {
                                        mAlertDialog.dismiss()
                                        GlobalScope.launch {
                                            withContext(Dispatchers.Main) {
                                                viewModel.addItemInDatabase(viewModel.inputMaterialBarcode, viewModel.projectId,
                                                        viewModel.userId, viewModel.id, inputQuantity, materialInwardId,
                                                        viewModel.name, viewModel.employeeId, orgQuantity)
                                            }
                                        }
                                    }
                                }
                                mDialogView.quantityCancelBtn.setOnClickListener {
                                    mAlertDialog.dismiss()
                                }
                            } else {
                                GlobalScope.launch {
                                    withContext(Dispatchers.Main) {
                                        viewModel.addItemInDatabase(viewModel.inputMaterialBarcode, viewModel.projectId,
                                                viewModel.userId, viewModel.id, orgQuantity, materialInwardId,
                                                viewModel.name, viewModel.employeeId, orgQuantity)
                                    }
                                }
                            }
                        }
                    }


                    /*
                            // println("old- material inward id -->"+result.get(0)?.id)

                            // println("updataed material inward id -->"+materialInwardId)
                            if (orgQuantity != null) {
                                if (orgQuantity!! > 1){
                                    // Alert dialog with text box
                                    val mDialogView = LayoutInflater.from(context).inflate(R.layout.quantity_pop_up, null)
                                    val mBuilder = AlertDialog.Builder(context!!)
                                            .setView(mDialogView)
                                            .setTitle("Quantity")
                                    val mAlertDialog = mBuilder.show()
                                    mDialogView.quantitySubmitBtn.setOnClickListener{
                                        var inputQuantity = mDialogView.issue_to_prod_picked_quantity.text.trim().toString().toInt()
                                        viewModel.quantity = inputQuantity.toString()
                                        println("--inputQuantity->"+inputQuantity)
                                        if (inputQuantity > orgQuantity!!){
                                            var error = "Quantity should not be greater than "+orgQuantity
                                            mDialogView.issue_to_prod_picked_quantity.setError(error)

                                        }else {
                                            mAlertDialog.dismiss()
                                            GlobalScope.launch {
                                                withContext(Dispatchers.Main) {
                                                    viewModel.addItemInDatabase(viewModel.inputMaterialBarcode, viewModel.projectId,
                                                            viewModel.userId, viewModel.id, inputQuantity, materialInwardId,
                                                            viewModel.name, viewModel.employeeId)
                                                }
                                            }
                                        }
                                    }
                                    mDialogView.quantityCancelBtn.setOnClickListener{
                                        mAlertDialog.dismiss()
                                    }
                                }else{
                                    GlobalScope.launch {
                                        withContext(Dispatchers.Main) {
                                            viewModel.addItemInDatabase(viewModel.inputMaterialBarcode, viewModel.projectId,
                                                    viewModel.userId, viewModel.id, orgQuantity, materialInwardId,
                                                    viewModel.name, viewModel.employeeId)
                                        }
                                    }
                                }
                            }

                    }else{
                        UiHelper.showErrorToast(this.activity as AppCompatActivity, "Item already scaned")
                    }

                }

                if (viewModel.qcStatusDisplay.value.isNullOrEmpty()) {
                    UiHelper.showErrorToast(this.activity as AppCompatActivity, "Invalid Barcode")
                    // UiHelper.showSomethingWentWrongSnackbarMessage(this.activity as AppCompatActivity)
                } else if (it != oldMaterialInward) {
                    val alreadyScaned = viewModel.scanedItems?.filter { it?.inputMaterialBarcode ==  viewModel.inputMaterialBarcode}

                    if (alreadyScaned.isNullOrEmpty()){

                        val result = viewModel.materialData.value?.filter {
                            (it?.serialNumber.equals(viewModel.inputMaterialBarcode))
                        }
                        if (result?.size!! > 0){
                            // add to room database
                            var orgQuantity = result.get(0)?.quantityPicked
                            // println("old- material inward id -->"+result.get(0)?.id)
                            var materialInwardId = viewModel.qcStatusDisplay.value?.get(0)?.id
                            // println("updataed material inward id -->"+materialInwardId)
                            if (orgQuantity != null) {
                                if (orgQuantity!! > 1){
                                    // Alert dialog with text box
                                    val mDialogView = LayoutInflater.from(context).inflate(R.layout.quantity_pop_up, null)
                                    val mBuilder = AlertDialog.Builder(context!!)
                                            .setView(mDialogView)
                                            .setTitle("Quantity")
                                    val mAlertDialog = mBuilder.show()
                                    mDialogView.quantitySubmitBtn.setOnClickListener{
                                        var inputQuantity = mDialogView.issue_to_prod_picked_quantity.text.trim().toString().toInt()
                                        viewModel.quantity = inputQuantity.toString()
                                        println("--inputQuantity->"+inputQuantity)
                                        if (inputQuantity > orgQuantity!!){
                                            var error = "Quantity should not be greater than "+orgQuantity
                                            mDialogView.issue_to_prod_picked_quantity.setError(error)

                                        }else {
                                            mAlertDialog.dismiss()
                                            GlobalScope.launch {
                                                withContext(Dispatchers.Main) {
                                                    viewModel.addItemInDatabase(viewModel.inputMaterialBarcode, viewModel.projectId,
                                                            viewModel.userId, viewModel.id, inputQuantity, materialInwardId,
                                                            viewModel.name, viewModel.employeeId)
                                                }
                                            }
                                        }
                                    }
                                    mDialogView.quantityCancelBtn.setOnClickListener{
                                        mAlertDialog.dismiss()
                                    }
                                }else{
                                    GlobalScope.launch {
                                        withContext(Dispatchers.Main) {
                                            viewModel.addItemInDatabase(viewModel.inputMaterialBarcode, viewModel.projectId,
                                                    viewModel.userId, viewModel.id, orgQuantity, materialInwardId,
                                                    viewModel.name, viewModel.employeeId)
                                        }
                                    }
                                }
                            }
                        }
                    }else{
                        UiHelper.showErrorToast(this.activity as AppCompatActivity, "Item already scaned")
                    }*/

                    recyclerView.adapter?.notifyDataSetChanged()
                }
            }
        })


        //-------------------------------------------------------------------

        issuetoproduction_items_submit_button.setOnClickListener{
            if (MainApplication.hasNetwork(MainApplication.applicationContext())) {
                 if (viewModel.scanedItems.size == 0) {
                    UiHelper.showErrorToast(this.activity as AppCompatActivity, "Scan Barcode first")
                } else {
                    AlertDialog.Builder(this.activity as AppCompatActivity, R.style.MyDialogTheme).create().apply {
                        setTitle("Confirm")
                        setMessage("Are you sure you want to issued material")
                        setButton(AlertDialog.BUTTON_NEUTRAL, "No", { dialog, _ -> dialog.dismiss() })
                        setButton(AlertDialog.BUTTON_POSITIVE, "Yes", { dialog, _ ->
                            dialog.dismiss()
                            viewModel.submitIssueToProd()
                        })
                        show()
                    }
                }
            }else {
                UiHelper.showErrorToast(this.activity as AppCompatActivity, "Please submit the list when in Network!")
            }
        }
        //---
    }

}

open class MaterialAdapter(private val recyclerView: androidx.recyclerview.widget.RecyclerView,
                                private val materialData: LiveData<Array<MaterialData?>>,
                                private val viewModel: IssueToProductionViewModel) :
        androidx.recyclerview.widget.RecyclerView.Adapter<MaterialAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.issue_to_production_row, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind()
        //-----------------
        val materialData = viewModel.issueToProdList[position]!!
        val scannedItemFound = viewModel.scanedItems?.filter{
            it!!.inputMaterialBarcode.toString() == materialData.serialNumber
        }

        if (scannedItemFound!!.size > 0){
            holder.itemView.setOnClickListener{
            var removeItem = materialData.partNumber +"#"+materialData.batchNumber +"#"+ materialData.quantityPicked
//            if (viewModel.issuetoproductionlist.toString().toLowerCase().contains("complete")) {
//                return@setOnClickListener
//            }

                val list = mutableListOf<String>()
                list.add(removeItem)

                val listPopupWindow = ListPopupWindow(this.recyclerView.context)
                listPopupWindow.setAnchorView(it)
                listPopupWindow.setDropDownGravity(Gravity.CENTER_HORIZONTAL)
                listPopupWindow.height = ListPopupWindow.WRAP_CONTENT
                listPopupWindow.width = ListPopupWindow.MATCH_PARENT
                listPopupWindow.isModal = true
                listPopupWindow.setAdapter(ArrayAdapter(this.recyclerView.context,
                        android.R.layout.simple_list_item_1, list.toTypedArray())) // list_item is your textView with gravity.

                listPopupWindow.setOnItemClickListener { parent, view, position, id ->
                    listPopupWindow.dismiss()
//                    val item = viewModel.materialData.value?.get(position)
//                    var message = "Are you sure you want to remove this item  from scanned list?"
                    AlertDialog.Builder(recyclerView.context, R.style.MyDialogTheme).create().apply {
                        setTitle("Confirm")
                        setMessage("Are you sure you want to remove this item \n\n${list[position]}\n\nfrom scanned list?")
                        setButton(AlertDialog.BUTTON_NEUTRAL, "NO") { dialog, _ -> dialog.dismiss()
                        }
                        setButton(AlertDialog.BUTTON_POSITIVE, "YES") {
                            dialog, _ -> dialog.dismiss()
                            // viewModel.deleteScannedItem(viewModel.employeeId, viewModel.name)
                            viewModel.deleteScannedItemWithBarcode( viewModel.employeeId, viewModel.name, materialData.batchNumber)
                        }
                        show()
                    }
                }

                listPopupWindow.show()
            }
        }
    }


    override fun getItemCount(): Int {
        return viewModel.issueToProdList.size ?: 0
    }

    open inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        protected val partNumber: TextView
        protected val barcodeSerial: TextView
        protected val batchNumber: TextView
        protected val quantity: TextView
        protected val linearLayout: LinearLayout
        protected val issueToProdRemainingQuantityLabel: TextView
        protected val issueToProdRemainingQuantityvalue: TextView


        init {
            partNumber = itemView.findViewById(R.id.issue_to_production_partNumberValue)
            barcodeSerial = itemView.findViewById(R.id.issue_to_production_barcodeSerialValue)
            batchNumber = itemView.findViewById(R.id.issue_to_production_descriptionValue)
            quantity = itemView.findViewById(R.id.issue_to_production_quantityValue)
            linearLayout = itemView.findViewById(R.id.material_layout)
            issueToProdRemainingQuantityLabel = itemView.findViewById(R.id.issue_to_prod_remaining_quantityLabel)
            issueToProdRemainingQuantityvalue = itemView.findViewById(R.id.issue_to_prod_remaining_quantityValue)
        }

        fun bind() {
            val item = viewModel.issueToProdList[adapterPosition]!!
            partNumber.text = item.partNumber
            barcodeSerial.text = item.serialNumber
            batchNumber.text = item.batchNumber
            quantity.text =item.quantityPicked.toString()

            val scannedItemFound = viewModel.scanedItems?.filter{
                // println("----------->"+viewModel.scanedItems.size)
                it!!.inputMaterialBarcode.toString() == item!!.serialNumber.toString()}

            if (scannedItemFound.size > 0){
                if (scannedItemFound[0].quantity!! < scannedItemFound[0].totalQuantity!!){
                    linearLayout.setBackgroundColor(PrefConstants().lightOrangeColor)
                    issueToProdRemainingQuantityLabel.visibility = View.VISIBLE
                    issueToProdRemainingQuantityvalue.visibility = View.VISIBLE
                    quantity.text = scannedItemFound[0].quantity.toString()
                    issueToProdRemainingQuantityvalue.text = (scannedItemFound[0].totalQuantity?.minus(scannedItemFound[0].quantity!!)).toString()
                }else{
                    linearLayout.setBackgroundColor(PrefConstants().lightGreenColor)
                    quantity.text = scannedItemFound[0].quantity.toString()
                    issueToProdRemainingQuantityvalue.visibility = View.GONE
                    issueToProdRemainingQuantityLabel.visibility = View.GONE
                }
            }else{
                linearLayout.setBackgroundColor(PrefConstants().lightGrayColor)
            }
        }
    }
}


