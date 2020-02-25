package com.briot.balmerlawrie.implementor.ui.main

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.briot.balmerlawrie.implementor.R
import com.briot.balmerlawrie.implementor.UiHelper
import com.briot.balmerlawrie.implementor.repository.remote.DispatchSlipItem
import io.github.pierry.progress.Progress
import kotlinx.android.synthetic.main.dispatch_slip_loading_fragment.*

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
                } else if (it != oldDispatchSlipItems) {
                    loading_dispatchSlipItems.adapter?.notifyDataSetChanged()
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

        loading_materialBarcode.setOnEditorActionListener { _, i, keyEvent ->
            var handled = false
            if (keyEvent == null) {
                Log.d("materialDetailsScan: ", "event is null")
            } else if ((loading_materialBarcode.text != null && loading_materialBarcode.text!!.isNotEmpty()) && i == EditorInfo.IME_ACTION_DONE || ((keyEvent.keyCode == KeyEvent.KEYCODE_ENTER || keyEvent.keyCode == KeyEvent.KEYCODE_TAB) && keyEvent.action == KeyEvent.ACTION_DOWN)) {
                this.progress = UiHelper.showProgressIndicator(this.activity as AppCompatActivity, "Please wait")
                UiHelper.hideProgress(this.progress)
                this.progress = null
                handled = true
            }
            handled
        }

        loading_items_submit_button.setOnClickListener({
            if (viewModel.dispatchloadingItems  != null && viewModel.dispatchloadingItems.value != null && viewModel.dispatchloadingItems.value!!.size > 0) {
                val keyboard = activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                keyboard.hideSoftInputFromWindow(activity?.currentFocus?.getWindowToken(), 0)
                this.progress = UiHelper.showProgressIndicator(this.activity as AppCompatActivity, "Please wait")

                // viewModel.submitDispatchItems();

            } else {
                UiHelper.showToast(this.activity as AppCompatActivity, "No items for dispatch")
            }

        })


        this.progress = UiHelper.showProgressIndicator(activity!!, "Loading dispatch slip Items")
        viewModel.loadDispatchSlipLoadingItems()
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

        init {
            dispatchSlipItemBatchNumber = itemView.findViewById(R.id.dispatch_slip_item_batch_number)
            dispatchSlipItemMaterialCode = itemView.findViewById(R.id.dispatch_slip_item_material_product_code)
            dispatchSlipItemPackQuantity = itemView.findViewById(R.id.dispatch_slip_item_material_pack_quantity)
        }

        fun bind() {
            val dispatchSlipItem = dispatchSlipItems.value!![adapterPosition]!!

            dispatchSlipItemBatchNumber.text = dispatchSlipItem.batchNumber
            dispatchSlipItemMaterialCode.text = dispatchSlipItem.materialCode
            dispatchSlipItemPackQuantity.text = dispatchSlipItem.numberOfPacks.toString()
        }
    }
}
