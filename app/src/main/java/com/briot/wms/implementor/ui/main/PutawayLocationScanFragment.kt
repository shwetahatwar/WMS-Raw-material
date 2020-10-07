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
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.briot.wms.implementor.R
import com.briot.wms.implementor.UiHelper
import com.briot.wms.implementor.repository.remote.*
import io.github.pierry.progress.Progress
import kotlinx.android.synthetic.main.putaway_location_scan_fragment.*
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView


class PutawayLocationScanFragment : Fragment() {

    companion object {
        fun newInstance() = PutawayLocationScanFragment()
    }

    private lateinit var viewModel: PutawayLocationScanViewModel
    private var oldPutawayLocationScanItems: Array<PutawayLocationScan?>? = null
    lateinit var putaway_Location_scanBarcode: EditText
    public var progress: Progress? = null
    lateinit var recyclerView: RecyclerView
    private var oldPutawayItems: Array<PutawayLocationScan?>? = null



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.putaway_location_scan_fragment, container, false)
        putaway_Location_scanBarcode = rootView.findViewById(R.id.putaway_Location_scanBarcode)
        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(PutawayLocationScanViewModel::class.java)
        (this.activity as AppCompatActivity).setTitle("Putaway Location")
        putaway_Location_scanBarcode.requestFocus()

        if (this.arguments != null) {
            viewModel.name = this.arguments!!.getString("name")
        }

        val imm = activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view!!.windowToken, 0)

        viewModel.putawayLocationScan.observe(viewLifecycleOwner, Observer<Array<PutawayLocationScan?>> {
            if (it != null) {
                UiHelper.hideProgress(this.progress)
                this.progress = null
                if (viewModel.putawayLocationScan.value!!.isEmpty() ||
                        viewModel.putawayLocationScan.value?.first() == null) {
                    UiHelper.showErrorToast(this.activity as AppCompatActivity,
                            "Wrong location barcode scan")
//                    UiHelper.showSomethingWentWrongSnackbarMessage(this.activity as AppCompatActivity)
                } else if (it != oldPutawayItems) {
                    val fragment = Fragment()
                    val bundle = Bundle()
                    bundle.putString("locationName", viewModel.putawayLocationScan.value?.get(0)?.name.toString())
                    bundle.putString("shelfId", viewModel.putawayLocationScan.value?.get(0)?.id.toString())
                    fragment.arguments = bundle;
                    view?.let { it1 -> Navigation.findNavController(it1).navigate(
                            R.id.action_putawayLocationScanFragment_to_putawayFragment, bundle) }
                    // recyclerView.adapter?.notifyDataSetChanged()
                }
            }
            oldPutawayItems = viewModel.putawayLocationScan.value
        })


        putaway_Location_scanButton.setOnClickListener {
            // val inputLocationBarcode  = "10-26-032-002-02"
            println("inside scann click")
            val inputLocationBarcode = putaway_Location_scanBarcode.getText().toString()
            if (inputLocationBarcode == "") {
                UiHelper.showErrorToast(this.activity as AppCompatActivity,
                        "Please enter Location Barcode value")
                putaway_Location_scanBarcode.requestFocus()
            }
            else{
                this.progress = UiHelper.showProgressIndicator(this.activity as AppCompatActivity, "Please wait")
                viewModel.locationBarcode = inputLocationBarcode
                viewModel.loadPutawayLocationScanItems(inputLocationBarcode)
                UiHelper.hideProgress(this.progress)
                // Navigation.findNavController(it).navigate(R.id.action_putawayLocationScanFragment_to_putawayFragment)
            }
            putaway_Location_scanBarcode.text?.clear()
            putaway_Location_scanBarcode.requestFocus()
        }

        //---------------------------------------------------------------------------------------
        putaway_Location_scanBarcode.setOnEditorActionListener { _, i, keyEvent ->
            var handled = false
            if ((putaway_Location_scanBarcode.text != null && putaway_Location_scanBarcode.text!!.isNotEmpty()) && i == EditorInfo.IME_ACTION_DONE
                    || (keyEvent != null && (keyEvent.keyCode == KeyEvent.KEYCODE_ENTER || keyEvent.keyCode == KeyEvent.KEYCODE_TAB)
                            && keyEvent.action == KeyEvent.ACTION_DOWN)) {
                val keyboard = activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                keyboard.hideSoftInputFromWindow(activity?.currentFocus?.getWindowToken(), 0)
                var value = putaway_Location_scanBarcode.text!!.toString().trim()

                // val inputLocationBarcode  = "10-26-032-002-02"
                println("inside scann click")
                val inputLocationBarcode = putaway_Location_scanBarcode.getText().toString()
                if (inputLocationBarcode == "") {
                    UiHelper.showErrorToast(this.activity as AppCompatActivity,
                            "Please enter Location Barcode value")
                    putaway_Location_scanBarcode.requestFocus()
                }
                else{
                    this.progress = UiHelper.showProgressIndicator(this.activity as AppCompatActivity, "Please wait")
                    viewModel.locationBarcode = inputLocationBarcode
                    viewModel.loadPutawayLocationScanItems(inputLocationBarcode)
                    // Navigation.findNavController(it).navigate(R.id.action_putawayLocationScanFragment_to_putawayFragment)
                }
                putaway_Location_scanBarcode.text?.clear()
                putaway_Location_scanBarcode.requestFocus()

                handled = true
            }
            handled
        }

        //---------------------------------------------------------------------------------------

    }
}