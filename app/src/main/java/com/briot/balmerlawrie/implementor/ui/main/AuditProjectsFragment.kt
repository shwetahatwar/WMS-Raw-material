package com.briot.balmerlawrie.implementor.ui.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.briot.balmerlawrie.implementor.R
import com.briot.balmerlawrie.implementor.UiHelper
import com.briot.balmerlawrie.implementor.repository.local.PrefConstants
import com.briot.balmerlawrie.implementor.repository.local.PrefRepository
import com.briot.balmerlawrie.implementor.repository.remote.DispatchSlip
import com.briot.balmerlawrie.implementor.repository.remote.Project
import io.github.pierry.progress.Progress
import kotlinx.android.synthetic.main.dispatch_slips_fragment.*

class AuditProjectsFragment : Fragment() {

    companion object {
        fun newInstance() = AuditProjectsFragment()
    }

    private lateinit var viewModel: AuditProjectsViewModel
    private var progress: Progress? = null
    private var oldProjectList: Array<Project?>? = null
    lateinit var recyclerView: RecyclerView
    private var userId = PrefRepository.singleInstance.getValueOrDefault(PrefConstants().USER_ID, "0").toInt()

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.audit_projects_fragment, container, false)

        this.recyclerView = rootView.findViewById(R.id.auditprojects_projectlist)
        recyclerView.layoutManager = LinearLayoutManager(this.activity)

        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(AuditProjectsViewModel::class.java)
        (this.activity as AppCompatActivity).setTitle("Audit Projects")

        recyclerView.adapter = SimpleProjectListAdapter(recyclerView, viewModel.projects)

        viewModel.projects.observe(viewLifecycleOwner, Observer<Array<Project?>> {
            if (it != null) {
                UiHelper.hideProgress(this.progress)
                this.progress = null

                if (viewModel.invalidProjects.first() == null) {
                    UiHelper.showSomethingWentWrongSnackbarMessage(this.activity as AppCompatActivity)
                } else if (it != oldProjectList) {
                    dispatch_dispatchSlipsView.adapter?.notifyDataSetChanged()
                }
            }

            oldProjectList = viewModel.projects.value
        })

        viewModel.networkError.observe(viewLifecycleOwner, Observer<Boolean> {
            if (it == true) {
                UiHelper.hideProgress(this.progress)
                this.progress = null

                UiHelper.showNoInternetSnackbarMessage(this.activity as AppCompatActivity)
            }
        })

        viewModel.loadAuditProjects("In-Progress")
    }

}

open class SimpleProjectListAdapter(private val recyclerView: androidx.recyclerview.widget.RecyclerView, private val projects: LiveData<Array<Project?>>) : androidx.recyclerview.widget.RecyclerView.Adapter<SimpleProjectListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.projects_row_layout, parent, false)

        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind()
    }

    override fun getItemCount(): Int {
        return projects.value?.size ?: 0
    }

    open inner class ViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        protected val projecNameValue: TextView

        init {
            projecNameValue = itemView.findViewById(R.id.projects_title_value)
        }

        fun bind() {
            val project = projects.value!![adapterPosition]!!

            projecNameValue.text = project.name
        }
    }
}


