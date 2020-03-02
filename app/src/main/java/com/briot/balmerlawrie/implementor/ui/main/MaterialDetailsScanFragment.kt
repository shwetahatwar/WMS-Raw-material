package com.briot.balmerlawrie.implementor.ui.main

import androidx.lifecycle.Observer
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.briot.balmerlawrie.implementor.MainActivity
import com.briot.balmerlawrie.implementor.R
import com.briot.balmerlawrie.implementor.UiHelper
import com.briot.balmerlawrie.implementor.repository.remote.DispatchSlip
import com.briot.balmerlawrie.implementor.repository.remote.Material
import com.briot.balmerlawrie.implementor.repository.remote.MaterialInward
import com.pascalwelsch.arrayadapter.ArrayAdapter
import io.github.pierry.progress.Progress
import kotlinx.android.synthetic.main.material_details_row.view.*
import kotlinx.android.synthetic.main.material_details_scan_fragment.*


class MaterialDetailsScanFragment : Fragment() {

    companion object {
        fun newInstance() = MaterialDetailsScanFragment()
    }

    private lateinit var viewModel: MaterialDetailsScanViewModel
    private var progress: Progress? = null
    private var oldMaterialInward: MaterialInward? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.material_details_scan_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MaterialDetailsScanViewModel::class.java)

        (this.activity as AppCompatActivity).setTitle("Material Details")

        materialScanText.requestFocus()

        materialItemsList.adapter = MaterialItemsAdapter(this.context!!)
        materialItemsList.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this.context)
//        materialResultId.visibility = View.GONE

        viewModel.materialInwards.observe(this, Observer<MaterialInward> {
            UiHelper.hideProgress(this.progress)
            this.progress = null

//            materialResultId.visibility = View.GONE
            (materialItemsList.adapter as MaterialItemsAdapter).clear()
            if (it != null && it != oldMaterialInward) {
                materialScanText.text?.clear()
                materialScanText.requestFocus()

                (materialItemsList.adapter as MaterialItemsAdapter).add(it)

                // dismiss keyboard now
                if (activity != null) {
                    val keyboard = activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    keyboard.hideSoftInputFromWindow(activity?.currentFocus?.getWindowToken(), 0)
                }

                if (it.dispatchSlip == null)  {
                    UiHelper.hideProgress(this.progress)
                    this.progress = null
                    (materialItemsList.adapter as MaterialItemsAdapter).notifyDataSetChanged()
                }  else  {
                    viewModel.getMaterialDispatchSlip(it.dispatchSlip!!.dispatchSlipNumber)
                }
            }

            oldMaterialInward = it

            if (it == null) {
                UiHelper.showErrorToast(this.activity as AppCompatActivity, "Material not found for scanned Barcode")
                materialScanText.text?.clear()
                materialScanText.requestFocus()
            }
        })

        viewModel.networkError.observe(this, Observer<Boolean> {
            if (it == true) {
                UiHelper.hideProgress(this.progress)
                this.progress = null

                UiHelper.showAlert(this.activity as AppCompatActivity, "Server is not reachable, please check if your network connection is working");
            }
        })

        viewModel.dispatchSlip.observe(this, Observer<DispatchSlip> {
            if (it != null) {
                (this.materialItemsList.adapter as MaterialItemsAdapter).getItem(0)?.dispatchSlip = it
            }

            (materialItemsList.adapter as MaterialItemsAdapter).notifyDataSetChanged()
        })

        materialScanText.setOnEditorActionListener { _, i, keyEvent ->
            var handled = false
            if (keyEvent == null) {
                Log.d("materialDetailsScan: ", "event is null")
            } else if ((materialScanText.text != null && materialScanText.text!!.isNotEmpty()) && i == EditorInfo.IME_ACTION_DONE || ((keyEvent.keyCode == KeyEvent.KEYCODE_ENTER || keyEvent.keyCode == KeyEvent.KEYCODE_TAB) && keyEvent.action == KeyEvent.ACTION_DOWN)) {
                this.progress = UiHelper.showProgressIndicator(this.activity as AppCompatActivity, "Please wait")
//                materialResultId.removeAllViews()
                (materialItemsList.adapter as MaterialItemsAdapter).clear()
                viewModel.loadMaterialItems(materialScanText.text.toString())

                handled = true
            }
            handled
        }

        viewMaterialDetails.setOnClickListener {
            if (materialScanText.text != null && materialScanText.text!!.isNotEmpty()) {
                this.progress = UiHelper.showProgressIndicator(this.activity as AppCompatActivity, "Please wait")
//                materialResultId.removeAllViews()
                (materialItemsList.adapter as MaterialItemsAdapter).clear()
                viewModel.loadMaterialItems(materialScanText.text.toString())
            }
        }
    }

}

class MaterialItemsAdapter(val context: Context) : ArrayAdapter<MaterialInward, MaterialItemsAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {

        val materialBarcode: TextView
        val materialType: TextView
        val materialProductCode: TextView
        val materialProductName: TextView
        val materialGenericName: TextView
        var materialUOM: TextView
        val materialGrossWeight: TextView
        val materialTareWeight: TextView
        val materialNetWeight: TextView
        val materialBatchCode: TextView
        val materialInwardDate: TextView
        val materialDispatchSlipNumber: TextView
        val materialPicker: TextView
        val materialLoader: TextView
        val materialDispatchTruckNumber: TextView
        val depot: TextView
        var materialScrapped: TextView

        init {
            materialBarcode = itemView.material_serialnumber_value as TextView
            materialType = itemView.material_type_value as TextView
            materialProductCode = itemView.material_productcode_value as TextView
            materialProductName = itemView.material_productname_value as TextView
            materialGenericName = itemView.material_genericname_value as TextView
            materialUOM = itemView.material_uom_value as TextView
            materialGrossWeight = itemView.material_grossweight_value as TextView
            materialTareWeight = itemView.material_tareweight_value as TextView
            materialNetWeight = itemView.material_netweight_value as TextView
            materialBatchCode = itemView.material_batchcode_value as TextView
            materialInwardDate = itemView.material_inwardon_value as TextView
            materialDispatchSlipNumber = itemView.material_dispatchslipnumber_value as TextView
            materialPicker = itemView.material_picker_value as TextView
            materialLoader = itemView.material_loader_value as TextView
            materialDispatchTruckNumber = itemView.material_trucknumber_value as TextView
            depot = itemView.material_depot_value as TextView
            materialScrapped = itemView.material_scrap_value as TextView
        }
    }

    override fun getItemId(item: MaterialInward): Any {
        return item
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val item = getItem(position) as MaterialInward

        holder.materialBarcode.text = item.serialNumber
        holder.materialBatchCode.text = item.batchNumber
        if(item.isScrapped == true) {
            holder.materialScrapped.text = "Scrapped"
        } else {
            holder.materialScrapped.text = "Active"
        }

        if (item.materialId != null) {
            holder.materialType.text = item.material!!.materialType
            holder.materialProductCode.text = item.material!!.materialCode
            holder.materialProductName.text = item.material!!.materialDescription
            holder.materialGenericName.text = item.material!!.genericName
            holder.materialUOM.text = item.material!!.UOM
            holder.materialGrossWeight.text = item.material!!.grossWeight
            holder.materialTareWeight.text = item.material!!.tareWeight
            holder.materialNetWeight.text = item.material!!.netWeight
        }

//        holder.materialInwardDate.text = item.

        if (item.dispatchSlip != null) {
            holder.materialPicker.text = item.dispatchSlip!!.toString()
            holder.materialLoader.text = item.dispatchSlipId!!.toString()

            holder.materialDispatchSlipNumber.text = item.dispatchSlip!!.dispatchSlipNumber

            if  (item.dispatchSlip!!.truckId != null) {
                holder.materialDispatchTruckNumber.text = item.dispatchSlip!!.ttat!!.truckNumber
            }

            if (item.dispatchSlip!!.depoId != null) {
                holder.depot.text = item.dispatchSlip!!.depot!!.name
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context)
                .inflate(R.layout.material_details_row, parent, false)
        return ViewHolder(view)
    }
}
