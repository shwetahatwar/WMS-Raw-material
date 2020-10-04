package com.briot.wms.implementor.ui.main

import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.briot.wms.implementor.R
import com.briot.wms.implementor.UiHelper
import com.briot.wms.implementor.repository.remote.ProjectList
import io.github.pierry.progress.Progress
import java.util.ArrayList

class ReturnFromProjectListFragment : Fragment() {

    companion object {
        fun newInstance() = ReturnFromProjectListFragment()
    }

    private lateinit var viewModel: ReturnFromProjectListViewModel
    lateinit var recyclerView: RecyclerView
    public var progress: Progress? = null
    private var oldProjectList: Array<ProjectList?>? = null
    var isScrolling: Boolean = false
    lateinit var layoutManager: LinearLayoutManager
    lateinit var adapter: ReturnFromProdProjectListAdapter
    var callCount = 1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.return_from_project_list_fragment, container, false)
        viewModel = ViewModelProvider(this).get(ReturnFromProjectListViewModel::class.java)
        this.recyclerView = rootView.findViewById(R.id.return_from_production_recyclerview)
        layoutManager = LinearLayoutManager(this.activity)
        recyclerView.layoutManager = layoutManager
        adapter = ReturnFromProdProjectListAdapter(recyclerView, viewModel.projectList, viewModel)
        viewModel.projectListRes = ArrayList<ProjectList>()
        return rootView

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        //viewModel = ViewModelProvider(this).get(ReturnFromProjectListViewModel::class.java)
        (this.activity as AppCompatActivity).setTitle("Return from Production")

        recyclerView.adapter = ReturnFromProdProjectListAdapter(recyclerView, viewModel.projectList, viewModel)
        viewModel.loadProjectListData()
        this.progress = UiHelper.showProgressIndicator(this.activity as AppCompatActivity, "Please wait")
        viewModel.projectList.observe(viewLifecycleOwner, Observer<Array<ProjectList?>> {
            if (it != null) {
                UiHelper.hideProgress(this.progress)
                this.progress = null
                if (viewModel.projectList.value.orEmpty().isNotEmpty() &&
                        viewModel.projectList.value?.first() == null) {
                    UiHelper.showSomethingWentWrongSnackbarMessage(this.activity as AppCompatActivity)
                } else if (it != oldProjectList) {
                    recyclerView.adapter?.notifyDataSetChanged()
                }
            }
            oldProjectList = viewModel.projectList.value
        })
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    isScrolling = true
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    val visibleItemCount = layoutManager.childCount
                    val pastVisibleItem = layoutManager.findLastCompletelyVisibleItemPosition()
                    val total = adapter.itemCount
                    val loopCount = viewModel.ProjectCount!!.toInt() / 100.toInt()
                    // println("callCount-->" + callCount)
                    // println("loopCount-->" + loopCount)
                    //println("visibleItemCount + pastVisibleItem-->" + (visibleItemCount + pastVisibleItem))
                    if (isScrolling && (visibleItemCount + pastVisibleItem) >= total && callCount <= loopCount) {
                        var offset = callCount * 100
                        //UiHelper.showProgressIndicator(activity!!, "Loading QC Pending list")
                        viewModel.loadProjectListData()

                        adapter?.notifyDataSetChanged()
                        isScrolling = false
                        callCount += 1
                    }
                }
            }
        })
    }
}

open class ReturnFromProdProjectListAdapter(private val recyclerView: androidx.recyclerview.widget.RecyclerView,
                              private val projectList: LiveData<Array<ProjectList?>>,
                              private val viewModel: ReturnFromProjectListViewModel) :
        androidx.recyclerview.widget.RecyclerView.Adapter<ReturnFromProdProjectListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.project_row, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind()
        // val putaway = putaway.value!![position]!!
        val putaway = viewModel.projectListRes

        var idPosition = viewModel.projectListRes.get(position)
        var id: Int? = idPosition!!.id
        var name: String? =  idPosition!!.name
        var description: String? = idPosition!!.description
//        println("status:--"+status)

        holder.itemView.setOnClickListener {
            val bundle = Bundle()
            if (id != null) {
                bundle.putInt("projectId", id)
                bundle.putString("name", name)
                bundle.putString("description", description)
            }
            Navigation.findNavController(it).navigate(R.id.action_returnFromProjectListFragment_to_returntoProductionEmployeeFragment, bundle)
        }
    }


    override fun getItemCount(): Int {
        // println("size--> " + viewModel.projectListRes.size)
        // return putaway.value?.size ?: 0
        return viewModel.projectListRes.size ?: 0
    }

    open inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //  protected val project_id_value: TextView
        protected val project_name_value: TextView
        protected val project_description_value: TextView
        protected val linearLayout: LinearLayout

        init {
            //  project_id_value = itemView.findViewById(R.id.project_id_value)
            project_name_value = itemView.findViewById(R.id.project_name_value) as TextView
            project_description_value = itemView.findViewById(R.id.project_description_value)
            linearLayout = itemView.findViewById(R.id.project_layout)
        }

        fun bind() {
            val item = viewModel.projectListRes!![adapterPosition]!!
            //  project_id_value.text = item.id.toString()
            project_name_value.text = item.name
            project_description_value.text = item.description
        }
    }
}

