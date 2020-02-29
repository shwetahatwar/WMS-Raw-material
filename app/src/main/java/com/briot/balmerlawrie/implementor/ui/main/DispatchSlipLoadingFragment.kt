package com.briot.balmerlawrie.implementor.ui.main

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
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
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.briot.balmerlawrie.implementor.MainApplication
import com.briot.balmerlawrie.implementor.R
import com.briot.balmerlawrie.implementor.UiHelper
import com.briot.balmerlawrie.implementor.repository.local.PrefConstants
import com.briot.balmerlawrie.implementor.repository.remote.DispatchSlipItem
import io.github.pierry.progress.Progress
import kotlinx.android.synthetic.main.dispatch_slip_loading_fragment.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DispatchSlipLoadingFragment : Fragment() {

    companion object {
        fun newInstance() = DispatchSlipLoadingFragment()
    }

    private lateinit var viewModel: DispatchSlipLoadingViewModel
    private var progress: Progress? = null
    private var oldDispatchSlipItems: Array<DispatchSlipItem?>? = null
    lateinit var recyclerView: RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val rootView = inflater.inflate(R.layout.dispatch_slip_loading_fragment, container, false)

        this.recyclerView = rootView.findViewById(R.id.loading_dispatchSlipItems)
        recyclerView.layoutManager = LinearLayoutManager(this.activity)

        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel = ViewModelProvider(this).get(DispatchSlipLoadingViewModel::class.java)
        (this.activity as AppCompatActivity).setTitle("Loading Dispatch Slip")

        if (this.arguments != null) {
            viewModel.dispatchSlipId = this.arguments!!.getInt("loadingDispatchSlip_id")
            viewModel.dispatchSlipVehicleNumber = this.arguments!!.getString("loadingDispatchSlip_vehicle_number")
            viewModel.dispatchSlipNumber = this.arguments!!.getString("loadingDispatchSlip_slipnumber")
            viewModel.dispatchSlipStatus = this.arguments!!.getString("loadingDispatchSlip_slipstatus")
            viewModel.dispatchSlipTruckId = this.arguments!!.getInt("loadingDispatchSlip_truckid")


            loading_dispatchSlipId.text = viewModel.dispatchSlipNumber
            loading_dispatchListStatusId.text = viewModel.dispatchSlipStatus
            loading_truckNumber.text = viewModel.dispatchSlipVehicleNumber
        }

        recyclerView.adapter = SimpleDispatchSlipLoadingItemAdapter(recyclerView, viewModel.dispatchloadingItems)
        viewModel.dispatchloadingItems.observe(viewLifecycleOwner, Observer<Array<DispatchSlipItem?>> {
            if (it != null) {
                UiHelper.hideProgress(this.progress)
                this.progress = null

                if (viewModel.dispatchloadingItems.value.orEmpty().isNotEmpty() && viewModel.dispatchloadingItems.value?.first() == null) {
                    UiHelper.showSomethingWentWrongSnackbarMessage(this.activity as AppCompatActivity)
                    loading_scanned_count.text = "0/0"
                } else if (it != oldDispatchSlipItems) {
                    loading_dispatchSlipItems.adapter?.notifyDataSetChanged()
                    loading_scanned_count.text = viewModel.totalScannedItems.toString() + "/" + it.size.toString()
                }
            }

            oldDispatchSlipItems = viewModel.dispatchloadingItems.value
        })

        viewModel.networkError.observe(viewLifecycleOwner, Observer<Boolean> {
            if (it == true) {
                UiHelper.hideProgress(this.progress)
                this.progress = null

                UiHelper.showNoInternetSnackbarMessage(this.activity as AppCompatActivity)
            }
        })

        viewModel.itemSubmissionSuccessful.observe(viewLifecycleOwner, Observer<Boolean> {
            if (it == true) {
                UiHelper.hideProgress(this.progress)
                this.progress = null

                var thisObject = this
                AlertDialog.Builder(this.activity as AppCompatActivity, R.style.MyDialogTheme).create().apply {
                setTitle("Success")
                    setMessage("Dispatch slip for loading oberation submitted successfully.")
                    setButton(AlertDialog.BUTTON_NEUTRAL, "Ok", {
                        dialog, _ -> dialog.dismiss()
                        Navigation.findNavController(thisObject.recyclerView).popBackStack(R.id.homeFragment, false)
                    })
                    show()
                }
            }
        })

        loading_materialBarcode.setOnEditorActionListener { _, i, keyEvent ->
            var handled = false
            /*if (keyEvent == null) {
                Log.d("materialDetailsScan: ", "event is null")
            } else*/
            if ((loading_materialBarcode.text != null && loading_materialBarcode.text!!.isNotEmpty()) && i == EditorInfo.IME_ACTION_DONE || (keyEvent != null && (keyEvent.keyCode == KeyEvent.KEYCODE_ENTER || keyEvent.keyCode == KeyEvent.KEYCODE_TAB) && keyEvent.action == KeyEvent.ACTION_DOWN)) {
                val keyboard = activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                keyboard.hideSoftInputFromWindow(activity?.currentFocus?.getWindowToken(), 0)

                var value = loading_materialBarcode.text!!.toString()
                var arguments  = value.split("#")
                var productCode = ""
                var batchCode = ""
                var serialNumber =  ""
                if (arguments.size < 2 || arguments[0].length == 0 || arguments[1].length == 0 || arguments[2].length == 0) {
                    UiHelper.showToast(this.activity as AppCompatActivity, "Invalid barcode, please try again!")
                } else {
                    productCode = arguments[0].toString()
                    batchCode = arguments[1].toString()
                    serialNumber = arguments[2].toString()

                    if (viewModel.isMaterialBelongToSameGroup(productCode, batchCode)) {
                        if (viewModel.materialQuantityPickingCompleted(productCode, batchCode)) {
                            UiHelper.showToast(this.activity as AppCompatActivity, "For given batch and material, quantity is already picked for dispatch!")
                        } else {
                            if (viewModel.isSameSerialNumber(productCode, batchCode, serialNumber)) {
                                UiHelper.showToast(this.activity as AppCompatActivity, "This barcode is already added, please add other item")
                            } else {
                                this.progress = UiHelper.showProgressIndicator(this.activity as AppCompatActivity, "Please wait")
                                // prodeed to add the material in database
                                GlobalScope.launch {
                                    viewModel.addMaterial(productCode, batchCode, serialNumber)
                                }
                            }
                        }

                    } else {
                        UiHelper.showToast(this.activity as AppCompatActivity, "Scanned material batch and material is not matching with dispatch slip!")
                        // @dinesh gajjar: get admin permission flow
                    }
                }

                handled = true
            }
            handled
        }

        loading_items_submit_button.setOnClickListener({
            if (viewModel.dispatchloadingItems  != null && viewModel.dispatchloadingItems.value != null && viewModel.dispatchloadingItems.value!!.size > 0) {
                val keyboard = activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                keyboard.hideSoftInputFromWindow(activity?.currentFocus?.getWindowToken(), 0)
                if (viewModel.dispatchSlipStatus.toString().toLowerCase().contains("complete")) {
                    UiHelper.showToast(this.activity as AppCompatActivity, "Items can not be scanned for completed Dispatch Slip")
                } else if (MainApplication.hasNetwork(MainApplication.applicationContext())) {

                    if (viewModel.isDispatchListSubmitted()) {
                        UiHelper.showToast(this.activity as AppCompatActivity, "Items listed in this dispatch list is already submitted")

                    } else if (!viewModel.isDispatchSlipHasEntries()) {
                        UiHelper.showToast(this.activity as AppCompatActivity, "There is no item added to selected dispatch list")

                    } else {

                        var thisObject = this
                        AlertDialog.Builder(this.activity as AppCompatActivity, R.style.MyDialogTheme).create().apply {
                            setTitle("Confirm")
                            setMessage("Are you sure you want to submit this dispatch slip items")
                            setButton(AlertDialog.BUTTON_NEUTRAL, "No", { dialog, _ -> dialog.dismiss() })
                            setButton(AlertDialog.BUTTON_POSITIVE, "Yes", {
                                dialog, _ -> dialog.dismiss()
                                thisObject.progress = UiHelper.showProgressIndicator(thisObject.activity as AppCompatActivity, "Please wait")

                                GlobalScope.launch {
                                    viewModel.handleSubmitLoadingList()
                                }
                            })
                            show()
                        }
                    }
                } else {
                    UiHelper.showToast(this.activity as AppCompatActivity, "Please submit the list when in Network!")
                }
            }

        })

        loading_scanButton.setOnClickListener({

        })


        this.progress = UiHelper.showProgressIndicator(activity!!, "Loading dispatch slip Items")
        viewModel.loadDispatchSlipLoadingItems()

        loading_materialBarcode.requestFocus()
    }
}

open class SimpleDispatchSlipLoadingItemAdapter(private val recyclerView: androidx.recyclerview.widget.RecyclerView, private val dispatchSlipItems: LiveData<Array<DispatchSlipItem?>>) : androidx.recyclerview.widget.RecyclerView.Adapter<SimpleDispatchSlipLoadingItemAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.dispatch_slip_item_row, parent, false)

        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind()

        val dispatchSlipItem = dispatchSlipItems.value!![position]!!
        holder.itemView.setOnClickListener{

        }
    }

    override fun getItemCount(): Int {
        return dispatchSlipItems.value?.size ?: 0
    }


    open inner class ViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        protected val dispatchSlipItemBatchNumber: TextView
        protected val dispatchSlipItemMaterialCode: TextView
        protected val dispatchSlipItemPackQuantity: TextView
        protected val linearLayout: LinearLayout

        init {
            dispatchSlipItemBatchNumber = itemView.findViewById(R.id.dispatch_slip_item_batch_number)
            dispatchSlipItemMaterialCode = itemView.findViewById(R.id.dispatch_slip_item_material_product_code)
            dispatchSlipItemPackQuantity = itemView.findViewById(R.id.dispatch_slip_item_material_pack_quantity)
            linearLayout = itemView.findViewById(R.id.dispatch_slip_layout)
        }

        fun bind() {
            val dispatchSlipItem = dispatchSlipItems.value!![adapterPosition]!!

            dispatchSlipItemBatchNumber.text = dispatchSlipItem.batchNumber
            dispatchSlipItemMaterialCode.text = dispatchSlipItem.materialCode
            dispatchSlipItemPackQuantity.text = dispatchSlipItem.scannedPacks.toString() + "/" + dispatchSlipItem.numberOfPacks.toString()
            if (dispatchSlipItem.scannedPacks.toInt() == 0) {
                linearLayout.setBackgroundColor(PrefConstants().lightGrayColor)
            } else if (dispatchSlipItem.scannedPacks.toInt() < dispatchSlipItem.numberOfPacks.toInt()) {
                linearLayout.setBackgroundColor(PrefConstants().lightOrangeColor)
            } else if (dispatchSlipItem.scannedPacks.toInt() >= dispatchSlipItem.numberOfPacks.toInt()) {
                linearLayout.setBackgroundColor(PrefConstants().lightGreenColor)
            }
        }
    }
}
