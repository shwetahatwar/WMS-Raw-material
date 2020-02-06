package com.briot.balmerlawrie.implementor.ui.main

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Layout
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.briot.balmerlawrie.implementor.MainActivity
import com.briot.balmerlawrie.implementor.R
import com.briot.balmerlawrie.implementor.repository.remote.MaterialDetails
import com.pascalwelsch.arrayadapter.ArrayAdapter
import io.github.pierry.progress.Progress
import kotlinx.android.synthetic.main.material_details_scan_fragment.*
import kotlinx.android.synthetic.main.material_item_list_row.view.*
import kotlinx.android.synthetic.main.key_value_info.view.*
import java.sql.Date
import java.util.Date as Date1


class MaterialDetailsScanFragment : Fragment() {

    companion object {
        fun newInstance() = MaterialDetailsScanFragment()
    }

    private lateinit var viewModel: MaterialDetailsScanViewModel
    private var progress: Progress? = null
    private var oldMaterialDetails: MaterialDetails? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.material_details_scan_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(MaterialDetailsScanViewModel::class.java)

        (this.activity as AppCompatActivity).setTitle("Material Details")

        materialScanText.requestFocus()

        materialItemsList.adapter = MaterialDetailsItemsAdapter(this.context!!)
        materialItemsList.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this.context)
        materialResultId.visibility = View.GONE

        viewModel.materialDetails.observe(this, Observer<MaterialDetails> {
            MainActivity.hideProgress(this.progress)
            this.progress = null

            materialResultId.visibility = View.GONE
            (materialItemsList.adapter as MaterialDetailsItemsAdapter).clear()
            if (it != null && it != oldMaterialDetails) {
                materialScanText.text?.clear()
                materialScanText.requestFocus()

                (materialItemsList.adapter as MaterialDetailsItemsAdapter).add(it)
//                (materialItemsList.adapter as MaterialDetailsItemsAdapter).notifyDataSetChanged()

                // dismiss keyboard now
                if (activity != null) {
                    val keyboard = activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    keyboard.hideSoftInputFromWindow(activity?.currentFocus?.getWindowToken(), 0)

                }


            }

            oldMaterialDetails = it

            if (it == null) {
                MainActivity.showToast(this.activity as AppCompatActivity, "Material not found for scanned Barcode")
                materialScanText.text?.clear()
                materialScanText.requestFocus()
            }
        })

        viewModel.networkError.observe(this, Observer<Boolean> {
            if (it == true) {
                MainActivity.hideProgress(this.progress)
                this.progress = null

                MainActivity.showAlert(this.activity as AppCompatActivity, "Server is not reachable, please check if your network connection is working");
            }
        })

        materialScanText.setOnEditorActionListener { _, i, keyEvent ->
            var handled = false
            if (keyEvent == null) {
                Log.d("materialDetailsScan: ", "event is null")
            } else if ((materialScanText.text != null && materialScanText.text!!.isNotEmpty()) && i == EditorInfo.IME_ACTION_DONE || ((keyEvent.keyCode == KeyEvent.KEYCODE_ENTER || keyEvent.keyCode == KeyEvent.KEYCODE_TAB) && keyEvent.action == KeyEvent.ACTION_DOWN)) {
                this.progress = MainActivity.showProgressIndicator(this.activity as AppCompatActivity, "Please wait")
                materialResultId.removeAllViews()
                (materialItemsList.adapter as MaterialDetailsItemsAdapter).clear()
                viewModel.loadMaterialDetails(materialScanText.text.toString())

                handled = true
            }
            handled
        }

        viewMaterialDetails.setOnClickListener {
            if (materialScanText.text != null && materialScanText.text!!.isNotEmpty()) {
                this.progress = MainActivity.showProgressIndicator(this.activity as AppCompatActivity, "Please wait")
                materialResultId.removeAllViews()
                (materialItemsList.adapter as MaterialDetailsItemsAdapter).clear()
                viewModel.loadMaterialDetails(materialScanText.text.toString())
            }
        }
    }

}

class MaterialDetailsItemsAdapter(val context: Context) : ArrayAdapter<MaterialDetails, MaterialDetailsItemsAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {

        val createdAtItemHeadingId: TextView
        val createdAtItemValueId: TextView
        val updatedAtItemHeadingId: TextView
        val updatedAtItemValueId: TextView
        val requestedQuantityItemHeadingId: TextView
        val requestedQuantityItemTextId: TextView
        val actualQuantityItemHeadingId: TextView
        val actualQuantityItemTextId: TextView
        val statusItemHeadingId: TextView
        val statusItemTextId: TextView
        val estimatedDateItemHeadingId: TextView
        val estimatedDateItemTextId: TextView
        val barcodeItemHeadingId: TextView
        val barcodeItemTextId: TextView
        val createdByItemHeadingId: TextView
        val createdByItemTextId: TextView
        val updatedByItemHeadingId: TextView
        val updatedByItemTextId: TextView
        var trolleyIdBarcodeText: TextView
        var trolleyIdTypeIdText: TextView
        var trolleyIdCapacityText: TextView

        init {
            createdAtItemHeadingId = itemView.createdAtItemHeadingId as TextView
            createdAtItemValueId = itemView.createdAtItemTextId as TextView
            updatedAtItemHeadingId = itemView.updatedAtItemHeadingId as TextView
            updatedAtItemValueId = itemView.updatedAtItemTextId as TextView
            requestedQuantityItemHeadingId = itemView.requestedQuantityItemHeadingId as TextView
            requestedQuantityItemTextId = itemView.requestedQuantityItemTextId as TextView
            actualQuantityItemHeadingId = itemView.actualQuantityItemHeadingId as TextView
            actualQuantityItemTextId = itemView.actualQuantityItemTextId as TextView
            statusItemHeadingId = itemView.statusItemHeadingId as TextView
            statusItemTextId = itemView.statusItemTextId as TextView
            estimatedDateItemHeadingId = itemView.estimatedDateItemHeadingId as TextView
            estimatedDateItemTextId = itemView.estimatedDateItemTextId as TextView
            barcodeItemHeadingId = itemView.barcodeItemHeadingId as TextView
            barcodeItemTextId = itemView.barcodeItemTextId as TextView

            createdByItemHeadingId = itemView.createdByItemHeadingId as TextView
            createdByItemTextId = itemView.createdByItemTextId as TextView
            updatedByItemHeadingId = itemView.updatedByItemHeadingId as TextView
            updatedByItemTextId = itemView.updatedByItemTextId as TextView

            trolleyIdBarcodeText = itemView.trolleyIdBarcodeText as TextView
            trolleyIdTypeIdText = itemView.trolleyIdTypeIdText as TextView
            trolleyIdCapacityText = itemView.trolleyIdCapacityText as TextView
        }
    }

    override fun getItemId(item: MaterialDetails): Any {
        return item
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val item = getItem(position) as MaterialDetails

        holder.createdAtItemHeadingId.setText("Created On")
        holder.createdAtItemValueId.setText("")
        if (item.createdAt != null) {
            val createDate = Date((item.createdAt!!).toLong())
            holder.createdAtItemValueId.setText(createDate.toString())
        }

        holder.updatedAtItemHeadingId.setText("Updated On")
        holder.updatedAtItemValueId.setText("")
        if (item.updatedAt != null) {
            val updateDate = Date((item.updatedAt!!).toLong())
            holder.updatedAtItemValueId.setText(updateDate.toString())
        }

        holder.requestedQuantityItemHeadingId.setText("Requested Quantity")
        holder.requestedQuantityItemTextId.setText(item.requestedQuantity.toString())
        holder.actualQuantityItemHeadingId.setText("Actual Quantity")
        holder.actualQuantityItemTextId.setText(item.actualQuantity.toString())
        holder.statusItemHeadingId.setText("Material Status")
        holder.statusItemTextId.setText(item.materialStatus)
        holder.estimatedDateItemHeadingId.setText("Estimated Date")
        holder.estimatedDateItemTextId.setText(item.estimatedDate.toString())
        holder.barcodeItemHeadingId.setText("Material Barcode")
        holder.barcodeItemTextId.setText(item.barcodeSerial)

        holder.createdByItemHeadingId.setText("Created By")
        if (item.createdBy != null) {
            holder.createdByItemTextId.setText(item.createdBy.toString())
        } else {
            holder.createdByItemTextId.setText("NA")
        }

        holder.updatedByItemHeadingId.setText("Updated By")
        if (item.updatedBy != null) {
            holder.updatedByItemTextId.setText(item.updatedBy.toString())
        } else {
            holder.updatedByItemTextId.setText("NA")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context)
                .inflate(R.layout.material_item_list_row, parent, false)
        return ViewHolder(view)
    }
}
