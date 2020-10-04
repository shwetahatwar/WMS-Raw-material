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
import androidx.lifecycle.Observer
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.briot.wms.implementor.R
import com.briot.wms.implementor.UiHelper
import com.briot.wms.implementor.repository.remote.Employee
import io.github.pierry.progress.Progress
import kotlinx.android.synthetic.main.issue_to_production_employee_fragment.*

class IssueToProductionEmployeeFragment : Fragment() {

    companion object {
        fun newInstance() = IssueToProductionEmployeeFragment()
    }

    private lateinit var viewModel: IssueToProductionEmployeeViewModel

    public var progress: Progress? = null
    lateinit var recyclerView: RecyclerView
    private var oldEmployeeItems: Array<Employee?>? = null
    lateinit var employee_scanBarcode: EditText

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView =  inflater.inflate(R.layout.issue_to_production_employee_fragment, container, false)
       employee_scanBarcode = rootView.findViewById(R.id.employee_scanBarcode)
//        this.recyclerView = rootView.findViewById(R.id.action_issueToProductionEmployeeFragment_to_issueToProductionFragment)
//        recyclerView.layoutManager = LinearLayoutManager(this.activity)
        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(IssueToProductionEmployeeViewModel::class.java)
        (this.activity as AppCompatActivity).setTitle("Issue to Production")
        // employee_scanBarcode.requestFocus()

        if (this.arguments != null) {
            // viewModel.employeeId = this.arguments!!.getString("employeeId")
            viewModel.picklistName = this.arguments!!.getString("picklistName")
            viewModel.id = this.arguments!!.getInt("id")
            viewModel.projectId = this.arguments!!.getInt("projectId")
            viewModel.projectName = this.arguments!!.getString("name")

        }
       // this.progress = UiHelper.showProgressIndicator(this.activity as AppCompatActivity, "Please wait")
        viewModel.employeeScan.observe(viewLifecycleOwner, Observer<Array<Employee?>> {
            if (it != null) {
                UiHelper.hideProgress(this.progress)
                this.progress = null
                if (viewModel.employeeScan.value.isNullOrEmpty()) {
                    UiHelper.showErrorToast(this.activity as AppCompatActivity, "Enter valid Barcode")
                } else if (it != oldEmployeeItems) {
                    val fragment = Fragment()
                    val bundle = Bundle()
                    bundle.putString("employeeId", viewModel.employeeScan.value?.get(0)?.employeeId.toString())
                    bundle.putString("picklistName", viewModel.picklistName)
                    bundle.putString("name", viewModel.projectName)
                    viewModel.id?.let { it1 -> bundle.putInt("id", it1) }
                    viewModel.employeeScan.value?.get(0)?.id?.let { it1 -> bundle.putInt("userId", it1) }
                    viewModel.projectId?.let { it1 -> bundle.putInt("projectId", it1) }

                    fragment.arguments = bundle;
                    view?.let { it1 -> Navigation.findNavController(it1).navigate(
                            R.id.action_issueToProductionEmployeeFragment_to_issueToProductionFragment, bundle) }
                    // recyclerView.adapter?.notifyDataSetChanged()
                } else if (employee_scanBarcode.getText().toString() != "") {
                    UiHelper.showErrorToast(this.activity as AppCompatActivity,
                            "Please enter valid Employee Barcode value")
                }
            }
            oldEmployeeItems = viewModel.employeeScan.value
        })

        employee_scanButton.setOnClickListener {
            //val inputLocationBarcode  = "17911"
            val inputLocationBarcode = employee_scanBarcode.getText().toString()
            if (inputLocationBarcode == "") {
                UiHelper.showErrorToast(this.activity as AppCompatActivity,
                        "Please enter Employee Barcode value")
                employee_scanBarcode.requestFocus()
            }
            else{
                viewModel.employeeId = inputLocationBarcode
                this.progress = UiHelper.showProgressIndicator(this.activity as AppCompatActivity, "Please wait")
                viewModel.loadEmployeeScanItems(inputLocationBarcode)
                // Navigation.findNavController(it).navigate(R.id.action_putawayLocationScanFragment_to_putawayFragment)
            }
            employee_scanBarcode.text?.clear()
        }
        //-----------------------------------------------------------
        employee_scanBarcode.setOnEditorActionListener { _, i, keyEvent ->
            var handled = false
            if ((employee_scanBarcode.text != null && employee_scanBarcode.text!!.isNotEmpty()) && i == EditorInfo.IME_ACTION_DONE
                    || (keyEvent != null && (keyEvent.keyCode == KeyEvent.KEYCODE_ENTER || keyEvent.keyCode == KeyEvent.KEYCODE_TAB)
                            && keyEvent.action == KeyEvent.ACTION_DOWN)) {
                val keyboard = activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                keyboard.hideSoftInputFromWindow(activity?.currentFocus?.getWindowToken(), 0)
                val inputLocationBarcode = employee_scanBarcode.getText().toString()
                if (inputLocationBarcode == "") {
                    UiHelper.showErrorToast(this.activity as AppCompatActivity,
                            "Please enter Employee Barcode value")
                    employee_scanBarcode.requestFocus()
                }
                else{
                    viewModel.employeeId = inputLocationBarcode
                    this.progress = UiHelper.showProgressIndicator(this.activity as AppCompatActivity, "Please wait")
                    viewModel.loadEmployeeScanItems(inputLocationBarcode)
                }
                employee_scanBarcode.text?.clear()


                handled = true
            }
            handled
        }
        //------------------------------------------------------------
    }
}