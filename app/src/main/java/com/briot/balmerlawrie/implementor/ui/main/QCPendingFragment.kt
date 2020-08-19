package com.briot.balmerlawrie.implementor.ui.main
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.briot.balmerlawrie.implementor.R
import com.briot.balmerlawrie.implementor.UiHelper
import com.briot.balmerlawrie.implementor.repository.local.PrefConstants
import com.briot.balmerlawrie.implementor.repository.remote.QCPending
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.q_c_pending_fragment, container, false)
        viewModel = ViewModelProvider(this).get(QCPendingViewModel::class.java)
        this.recyclerView = rootView.findViewById(R.id.qcpending_recyclerlist)
        recyclerView.layoutManager = LinearLayoutManager(this.activity)
        qcPendingMaterialTextValue = rootView.findViewById(R.id.qcpending_materialBarcode)
        viewModel.loadQCPendingItems("100")
        viewModel.getQcTotalCount()
        viewModel.getItemsFromDB()
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

        recyclerView.adapter = SimpleQcPendingItemAdapter(recyclerView, viewModel.QCPendingItem, viewModel)
        count_value.text = viewModel.qcScannedCount.toString() + "/"+viewModel.qcTotalCount!!.toString()

        viewModel.QCPendingItem.observe(viewLifecycleOwner, Observer<Array<QCPending?>> {
            if (it != null) {
                UiHelper.hideProgress(this.progress)
                this.progress = null

                if (viewModel.QCPendingItem.value.orEmpty().isNotEmpty() && viewModel.QCPendingItem.value?.first() == null) {
                    UiHelper.showSomethingWentWrongSnackbarMessage(this.activity as AppCompatActivity)
                } else if (it != oldQcPendingItems) {
                    qcpending_recyclerlist.adapter?.notifyDataSetChanged()
                    // count_value.text = "0/"+viewModel.qcTotalCount!!.toString() ?: 0.toString()
                    count_value.text = viewModel.qcScannedCount.toString() + "/"+viewModel.qcTotalCount!!.toString() ?: 0.toString()
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
                        viewModel.getItemsFromDB()
                    }
                    qcpending_recyclerlist.adapter?.notifyDataSetChanged()
                }
                count_value.text = viewModel.qcScannedCount.toString() + "/"+viewModel.qcTotalCount!!.toString() ?: 0.toString()
                viewModel.barcodeSerial = inputMaterialBarcode
                qcpending_recyclerlist.adapter?.notifyDataSetChanged()
                qcpending_materialBarcode.text?.clear()
            }
        }

        qcpending_items_submit_button.setOnClickListener {
            var thisObject = this
            AlertDialog.Builder(this.activity as AppCompatActivity, R.style.MyDialogTheme).create().apply {
                setTitle(" Confirm")
                setMessage("Accept or Reject scanned materials.")
                setButton(AlertDialog.BUTTON_POSITIVE, "Accept", {
                    dialog, _ -> dialog.dismiss()
                    viewModel.submitScanItem(1)
                })
                setButton(AlertDialog.BUTTON_NEUTRAL, "Reject", {
                    dialog, _ -> dialog.dismiss()
                    viewModel.submitScanItem(2)
                })
                show()
            }
        }

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                var progress1: Progress? = null
                val linearLayoutManager = recyclerView.layoutManager as LinearLayoutManager?

                if (!loading && linearLayoutManager!!.itemCount <= linearLayoutManager.findLastVisibleItemPosition() + 2) {
                    loading = true
                    val loopCount =  viewModel.qcTotalCount!!.toInt() / 100 .toInt()

                    for (i in 2..loopCount+1 as Int){
                        val c = i * 100
                        viewModel.loadQCPendingItems(c.toString())
                        qcpending_recyclerlist.adapter?.notifyDataSetChanged()
                    }
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

            }
        })
    }
}

private operator fun Number?.div(i: Int) {

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
        val QCPendingItem = viewModel.QCPendingItem.value!![position]!!
    }


    override fun getItemCount(): Int {
         return viewModel.QCPendingItem.value?.size ?: 0
    }

    open inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        protected val partnumber: TextView
        protected val description: TextView
        protected val barcodeSerial: TextView

        // protected val count_value: TextView
        protected val linearLayout: LinearLayout

        init {
            partnumber = itemView.findViewById(R.id.partNumberValue)
            description = itemView.findViewById(R.id.descriptionValue)
            barcodeSerial = itemView.findViewById(R.id.barcodeSerialValue)
            linearLayout = itemView.findViewById(R.id.qcpending_layout)
        }

        fun bind() {
            val item = viewModel.QCPendingItem.value!![adapterPosition]!!
            partnumber.text = item.partnumber.partNumber.toString()
            description.text = item.partnumber.description.toString()
            barcodeSerial.text = item.barcodeSerial.toString()
            if (viewModel.barcodeSerial.toString() == item!!.barcodeSerial.toString()){
                    linearLayout.setBackgroundColor(PrefConstants().lightGreenColor)
            } else{
                    linearLayout.setBackgroundColor(PrefConstants().lightGrayColor)
            }
            val scannedItemFound = viewModel.scanedItems?.filter{ it!!.barcodeSerial.toString() == item!!.barcodeSerial.toString()}
            if (scannedItemFound.size > 0){
                linearLayout.setBackgroundColor(PrefConstants().lightGreenColor)
            }
        }
    }
}

