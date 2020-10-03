package com.briot.balmerlawrie.implementor.ui.main

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.briot.balmerlawrie.implementor.R
import com.briot.balmerlawrie.implementor.UiHelper
import com.briot.balmerlawrie.implementor.repository.local.PrefConstants
import com.briot.balmerlawrie.implementor.repository.remote.MaterialInward
import com.briot.balmerlawrie.implementor.repository.remote.MaterialInwards
import com.briot.balmerlawrie.implementor.repository.remote.picklistMasterRes
import io.github.pierry.progress.Progress
import kotlinx.android.synthetic.main.home_fragment.*
import kotlinx.android.synthetic.main.picklist_master_fragment.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.coroutineContext

class PicklistMasterFragment : Fragment() {

    companion object {
        fun newInstance() = PicklistMasterFragment()
    }

    private lateinit var viewModel: PicklistMasterViewModel
    lateinit var recyclerView: RecyclerView
    public var progress: Progress? = null
    private var oldPickingMaster: Array<picklistMasterRes?>? = null
    var isScrolling: Boolean = false
    lateinit var layoutManager: LinearLayoutManager
    lateinit var adapter: PickingMasterAdapter
    var callCount = 1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.picklist_master_fragment, container, false)
        viewModel = ViewModelProvider(this).get(PicklistMasterViewModel::class.java)
        this.recyclerView = rootView.findViewById(R.id.picklist_master_recyclerview)
        layoutManager = LinearLayoutManager(this.activity)
        recyclerView.layoutManager = layoutManager
        adapter = PickingMasterAdapter(recyclerView, viewModel.picklistMasterData, viewModel)

        viewModel.picklistMasterResList = ArrayList<picklistMasterRes>()
        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // TODO: Use the ViewModel
       //viewModel = ViewModelProvider(this).get(PicklistMasterViewModel::class.java)
        (this.activity as AppCompatActivity).setTitle("Picklist Master")
        // picklist_master_layout.setOnClickListener { Navigation.findNavController(it).navigate(R.id.action_picklistMasterFragment_to_pickingFragment) }
        recyclerView.adapter = PickingMasterAdapter(recyclerView, viewModel.picklistMasterData, viewModel)

        viewModel.loadpicklistMasterData("0")
        viewModel.loadpicklistTotalCount()
        this.progress = UiHelper.showProgressIndicator(this.activity as AppCompatActivity, "Please wait")
        viewModel.picklistMasterData.observe(viewLifecycleOwner, Observer<Array<picklistMasterRes?>> {
            if (it != null) {
                UiHelper.hideProgress(this.progress)
                this.progress = null
                if (viewModel.picklistMasterData.value.orEmpty().isNotEmpty() &&
                        viewModel.picklistMasterData.value?.first() == null) {
                    UiHelper.showSomethingWentWrongSnackbarMessage(this.activity as AppCompatActivity)
                } else if (it != oldPickingMaster) {
                    recyclerView.adapter?.notifyDataSetChanged()
                }
            }
            oldPickingMaster = viewModel.picklistMasterData.value
        })

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING){
                    isScrolling = true
                }
            }
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    val visibleItemCount = layoutManager.childCount
                    val pastVisibleItem = layoutManager.findLastCompletelyVisibleItemPosition()
                    val total = adapter.itemCount
//                    println("total-->" + total)
//                    println("viewModel.picklistMasterCount-->"+viewModel.picklistMasterCount)
                    val loopCount =  viewModel.picklistMasterCount!!.toInt() / 100.toInt()
                    // println("callCount-->"+ callCount)
                    // println("loopCount-->"+ loopCount)
                    //println("visibleItemCount + pastVisibleItem-->" + (visibleItemCount + pastVisibleItem))
                    if (isScrolling && (visibleItemCount + pastVisibleItem) >= total && callCount <= loopCount) {
                        var offset = callCount * 100
                        //UiHelper.showProgressIndicator(activity!!, "Loading QC Pending list")
                        viewModel.loadpicklistMasterData(offset.toString())

                        adapter?.notifyDataSetChanged()
                        isScrolling = false
                        callCount +=1
                    }
                }
            }
        })
    }

}


open class PickingMasterAdapter(private val recyclerView: androidx.recyclerview.widget.RecyclerView,
                                private val picklistMasterData: LiveData<Array<picklistMasterRes?>>,
                                private val viewModel: PicklistMasterViewModel) :
        androidx.recyclerview.widget.RecyclerView.Adapter<PickingMasterAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.picklist_master_row, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind()
        // val putaway = putaway.value!![position]!!
        val putaway = viewModel.picklistMasterResList

        var idPosition = viewModel.picklistMasterResList.get(position)
        var id: Int? = idPosition!!.id
        var status1: String? =  idPosition!!.picklistStatus
        var picklistName: String? = idPosition!!.picklistName

        if (status1!!.toLowerCase() == "pending"){
            holder.itemView.setOnClickListener {
                // Toast.makeText(this.recyclerView.context,"Please activate",Toast.LENGTH_SHORT).show()
                UiHelper.showErrorToast(this.recyclerView.context as AppCompatActivity, "Please Activate Picklist First")
            }
        }else if (status1!!.toLowerCase() == "in progress" || status1!!.toLowerCase() == "completed"){
            holder.itemView.setOnClickListener {
                val bundle = Bundle()
                if (id != null) {
                    bundle.putInt("id", id)
                    bundle.putString("picklistName", picklistName)
                    bundle.putString("picklistStatus", status1)
                }
                Navigation.findNavController(it).navigate(R.id.action_picklistMasterFragment_to_pickingFragment, bundle)
            }
        }
    }


    override fun getItemCount(): Int {
        // println("size--> " + viewModel.picklistMasterResList.size)
        // return putaway.value?.size ?: 0
        return viewModel.picklistMasterResList.size ?: 0
    }

    open inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        protected val picklistID: TextView
        protected val createDate: TextView
        protected val status: TextView
        protected val linearLayout: LinearLayout

        init {

            //Toast.makeText(itemView.context,"Please activate ",Toast.LENGTH_SHORT).show()

            picklistID = itemView.findViewById(R.id.picklist_master_id_value)
            createDate = itemView.findViewById(R.id.picklist_master_created_date_value) as TextView
            status = itemView.findViewById(R.id.picklist_master_status_value)
            linearLayout = itemView.findViewById(R.id.picklist_master_layout)
        }

        fun bind() {
            val item = viewModel.picklistMasterResList!![adapterPosition]!!
            picklistID.text = item.picklistName
            var createdDate = ""
            if (item.createdAt!!.isNotEmpty() && item.createdAt!!.contains("T")){
                createdDate = item.createdAt?.split("T")?.get(0).toString()
            }
            createDate.text = createdDate
            status.text = item.picklistStatus

            if (item.picklistStatus?.toLowerCase() == "pending"){
                linearLayout.setBackgroundColor(PrefConstants().lightGrayColor)
            } else if (item.picklistStatus?.toLowerCase() == "completed"){
                linearLayout.setBackgroundColor(PrefConstants().lightGreenColor)
            } else if (item.picklistStatus?.toLowerCase() == "in progress"){
                linearLayout.setBackgroundColor(PrefConstants().lightOrangeColor)
            }
        }
    }
}

