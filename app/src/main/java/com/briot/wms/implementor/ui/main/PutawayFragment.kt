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
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.briot.wms.implementor.MainApplication
import com.briot.wms.implementor.R
import com.briot.wms.implementor.UiHelper
import com.briot.wms.implementor.repository.local.PrefConstants
import com.briot.wms.implementor.repository.remote.MaterialInward
import com.briot.wms.implementor.repository.remote.MaterialInwards
import com.briot.wms.implementor.repository.remote.Putaway
import io.github.pierry.progress.Progress
import kotlinx.android.synthetic.main.putaway_fragment.*

class PutawayFragment : Fragment() {

    companion object {
        fun newInstance() = PutawayFragment()
    }

    private lateinit var viewModel: PutawayViewModel
    lateinit var recyclerView: RecyclerView
    public var progress: Progress? = null
    private var oldPutawayItems: Array<MaterialInwards?>? = null
    //lateinit var putaway_materialBarcode: EditText
    private var loading = false
    var putawayList: Array<Putaway?> = emptyArray()

    var callCount = 1
    val limit = 100
    lateinit var adapter: SimplePutawayItemAdapter
    lateinit var layoutManager: LinearLayoutManager
    var isScrolling: Boolean = false


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        viewModel = ViewModelProvider(this).get(PutawayViewModel::class.java)
        val rootView = inflater.inflate(R.layout.putaway_fragment, container, false)
        this.recyclerView = rootView.findViewById(R.id.putaway_recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(this.activity)
        //putaway_materialBarcode = rootView.findViewById(R.id.putaway_materialBarcode)
        // viewModel.loadPutawayDashboardItems()
        viewModel.getItemsFromDB()
        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
       // viewModel = ViewModelProvider(this).get(PutawayViewModel::class.java)
        (this.activity as AppCompatActivity).setTitle("Putaway")
//        putaway_materialBarcode.text?.clear()
         //putaway_materialBarcode.requestFocus()
        viewModel.deletePutawayScanItemsFromDB()
        put_count_value.text = viewModel.totalScannedItem.value.toString()

        if (this.arguments != null) {
            viewModel.name = this.arguments!!.getString("locationName")
            location_value.text = this.arguments!!.getString("locationName")
            println("selfid -->"+ this.arguments!!.getString("shelfId"))
            viewModel.shelfId = this.arguments!!.getString("shelfId")
        }

        val imm = activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view!!.windowToken, 0)

        recyclerView.adapter = SimplePutawayItemAdapter(recyclerView,
                viewModel.putawayMaterialScanData,
                viewModel)

        viewModel.materialInwardsPutaway.observe(viewLifecycleOwner, Observer<Array<MaterialInwards?>> {
            if (it != null) {
                UiHelper.hideProgress(this.progress)
                this.progress = null
                if (viewModel.materialInwardsPutaway.value!!.isEmpty() ||
                        viewModel.materialInwardsPutaway.value?.first() == null) {
                    //UiHelper.showSomethingWentWrongSnackbarMessage(this.activity as AppCompatActivity)
                   // UiHelper.showErrorToast(this.activity as AppCompatActivity, "Wrong barcode scan")
                } else if (it != oldPutawayItems) {
                    put_count_value.text = viewModel.totalScannedItem.value.toString()
                    putaway_materialBarcode.requestFocus()
                    recyclerView.adapter?.notifyDataSetChanged()
                }
                recyclerView.adapter?.notifyDataSetChanged()
            }
            oldPutawayItems = viewModel.materialInwardsPutaway.value
        })

        viewModel.totalScannedItem.observe(viewLifecycleOwner, Observer<Number> {
            if (it != null) {
                put_count_value.text = viewModel.totalScannedItem.value.toString()
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

        viewModel.pendingItem.observe(viewLifecycleOwner, Observer<Boolean> {
            if (it == true) {
                var thisObject = this
                UiHelper.showErrorToast(this.activity as AppCompatActivity,
                        "Material putaway cannot be performed as QC status is pending")
            }
        })

        viewModel.rejected.observe(viewLifecycleOwner, Observer<Boolean> {
            if (it == true) {
                var thisObject = this
                UiHelper.showErrorToast(this.activity as AppCompatActivity,
                        "Material putaway cannot be performed as QC status is Rejected")
            }
        })

        viewModel.invalidMaterial.observe(viewLifecycleOwner, Observer<Boolean> {
            if (it == true) {
                var thisObject = this
                UiHelper.showSuccessToast(this.activity as AppCompatActivity,
                        "Invalid material scan")
            }
        })
        putaway_scanButton.setOnClickListener {
            if (MainApplication.hasNetwork(MainApplication.applicationContext())) {
                val inputMaterialBarcode = putaway_materialBarcode.getText().toString()
                 if (inputMaterialBarcode == "") {
                    UiHelper.showErrorToast(this.activity as AppCompatActivity,
                            "Please enter Location Barcode value")
                    // putaway_materialBarcode.requestFocus()
                } else if (inputMaterialBarcode.length < 10 || inputMaterialBarcode.length > 10) {
                    UiHelper.showErrorToast(this.activity as AppCompatActivity, "Enter valid Barcode");
                } else {
                    val alreadyScan = viewModel.materialInwardsPutaway.value?.filter {
                        it?.barcodeSerial == inputMaterialBarcode
                    }
                    if (alreadyScan?.size!! > 0) {
                        // already scann
                        UiHelper.showErrorToast(this.activity as AppCompatActivity,
                                "Barcode already scanned")
                    } else {
                        this.progress = UiHelper.showProgressIndicator(this.activity as AppCompatActivity, "Please wait")
                        viewModel.barcodeSerial = inputMaterialBarcode
                        viewModel.loadPutawayMaterialScan(inputMaterialBarcode)
                        UiHelper.hideProgress(this.progress)
                        putaway_materialBarcode.requestFocus()
                        recyclerView.adapter?.notifyDataSetChanged()
                    }
                }
                putaway_materialBarcode.text?.clear()
                putaway_materialBarcode.requestFocus()
            }else {
                UiHelper.showErrorToast(this.activity as AppCompatActivity, "Please check your Network connection and try again!!")
            }
        }
        //-----------------------------------------------------------------
        putaway_materialBarcode.setOnEditorActionListener { _, i, keyEvent ->
            var handled = false
            if ((putaway_materialBarcode.text != null && putaway_materialBarcode.text!!.isNotEmpty()) && i == EditorInfo.IME_ACTION_DONE
                    || (keyEvent != null && (keyEvent.keyCode == KeyEvent.KEYCODE_ENTER || keyEvent.keyCode == KeyEvent.KEYCODE_TAB)
                            && keyEvent.action == KeyEvent.ACTION_DOWN)) {
                val keyboard = activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                keyboard.hideSoftInputFromWindow(activity?.currentFocus?.getWindowToken(), 0)
                if (MainApplication.hasNetwork(MainApplication.applicationContext())) {
                    val inputMaterialBarcode = putaway_materialBarcode.getText().toString()
                    if (inputMaterialBarcode == "") {
                        UiHelper.showErrorToast(this.activity as AppCompatActivity,
                                "Please enter Location Barcode value")
                        // putaway_materialBarcode.requestFocus()
                    } else if (inputMaterialBarcode.length < 10 || inputMaterialBarcode.length > 10) {
                        UiHelper.showErrorToast(this.activity as AppCompatActivity, "Enter valid Barcode");
                    } else {
                        val alreadyScan = viewModel.materialInwardsPutaway.value?.filter {
                            it?.barcodeSerial == inputMaterialBarcode
                        }
                        if (alreadyScan?.size!! > 0) {
                            // already scann
                            UiHelper.showErrorToast(this.activity as AppCompatActivity,
                                    "Barcode already scanned")
                        } else {
                            this.progress = UiHelper.showProgressIndicator(this.activity as AppCompatActivity, "Please wait")
                            viewModel.barcodeSerial = inputMaterialBarcode
                            viewModel.loadPutawayMaterialScan(inputMaterialBarcode)
                            UiHelper.hideProgress(this.progress)
                            recyclerView.adapter?.notifyDataSetChanged()
                            putaway_materialBarcode.requestFocus()
                        }
                    }
                    putaway_materialBarcode.text?.clear()
                    putaway_materialBarcode.requestFocus()
                }else {
                    UiHelper.showErrorToast(this.activity as AppCompatActivity, "Please check your Network connection and try again!!")
                }
                handled = true
            }
            handled
        }

        //-----------------------------------------------------------------
    }
}

open class SimplePutawayItemAdapter(private val recyclerView: androidx.recyclerview.widget.RecyclerView,
                                      private val putawayMaterialScanData: LiveData<Array<MaterialInward?>>,
                                      private val viewModel: PutawayViewModel) :
        androidx.recyclerview.widget.RecyclerView.Adapter<SimplePutawayItemAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.putaway_fragment_row, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind()
        // val putaway = putaway.value!![position]!!
        val putaway = viewModel.materialInwardsPutaway.value
    }


    override fun getItemCount(): Int {
        // println("size--> "+ viewModel.materialInwardsPutaway.value?.size)
        // return putaway.value?.size ?: 0
        return viewModel.materialInwardsPutaway.value?.size ?: 0
    }

    open inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        protected val partnumber: TextView
        protected val description: TextView
        protected val barcodeSerial: TextView
        protected val eachPackQuantity: TextView
        protected val linearLayout: LinearLayout

        init {
            partnumber = itemView.findViewById(R.id.putaway_partNumberValue)
            description = itemView.findViewById(R.id.putaway_descriptionValue)
            barcodeSerial = itemView.findViewById(R.id.putaway_barcodeSerialValue)
            eachPackQuantity = itemView.findViewById(R.id.moved_to_location_value)
            linearLayout = itemView.findViewById(R.id.putaway_layout)
        }

        fun bind() {
            val item = viewModel.materialInwardsPutaway.value!![adapterPosition]!!
            partnumber.text = item.partnumber
            description.text = item.description
            barcodeSerial.text = item.barcodeSerial.toString()
            eachPackQuantity.text = item.eachPackQuantity.toString()
            linearLayout.setBackgroundColor(PrefConstants().lightGreenColor)

//            if (viewModel.barcodeSerial.toString() == item!!.barcodeSerial.toString()) {
//                linearLayout.setBackgroundColor(PrefConstants().lightGreenColor)
//            }
        }
    }
}