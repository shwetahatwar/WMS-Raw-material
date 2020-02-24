package com.briot.balmerlawrie.implementor.ui.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import android.widget.ImageView
import android.widget.TextView
import io.github.pierry.progress.Progress
import com.briot.balmerlawrie.implementor.repository.remote.DispatchSlip
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

import com.briot.balmerlawrie.implementor.R
import com.briot.balmerlawrie.implementor.UiHelper
import kotlinx.android.synthetic.main.dispatch_slips_fragment.*

class DispatchSlipsFragment : Fragment() {

    companion object {
        fun newInstance() = DispatchSlipsFragment()
    }
    private lateinit var viewModel: DispatchSlipsViewModel
    private var progress: Progress? = null
    private var oldDispatchSlipList: Array<DispatchSlip?>? = null
    lateinit var recyclerView: RecyclerView
    private var userId = 1;

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.dispatch_slips_fragment, container, false)

        this.recyclerView = rootView.findViewById(R.id.dispatch_dispatchSlipsView)
        recyclerView.layoutManager = LinearLayoutManager(this.activity)

        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(DispatchSlipsViewModel::class.java)
        (this.activity as AppCompatActivity).setTitle("Loading Dispatch Slips")

        recyclerView.adapter = SimpleDispatchListAdapter(recyclerView, viewModel.dispatchLoadingList)

        viewModel.dispatchLoadingList.observe(viewLifecycleOwner, Observer<Array<DispatchSlip?>> {
            if (it != null) {
                UiHelper.hideProgress(this.progress)
                this.progress = null

                if (viewModel.dispatchLoadingList.value.orEmpty().isNotEmpty() && viewModel.dispatchLoadingList.value?.first() == null) {
                    UiHelper.showSomethingWentWrongSnackbarMessage(this.activity as AppCompatActivity)
                } else if (it != oldDispatchSlipList) {
                    dispatch_dispatchSlipsView.adapter?.notifyDataSetChanged()
                }
            }

            oldDispatchSlipList = viewModel.dispatchLoadingList.value
        })

        viewModel.networkError.observe(viewLifecycleOwner, Observer<Boolean> {
            if (it == true) {
                UiHelper.hideProgress(this.progress)
                this.progress = null

                UiHelper.showNoInternetSnackbarMessage(this.activity as AppCompatActivity)
            }
        })

        this.progress = UiHelper.showProgressIndicator(activity!!, "Loading dispatch list")
        viewModel.loadDispatchLoadingLists(userId)
    }

}

open class SimpleDispatchListAdapter(private val recyclerView: androidx.recyclerview.widget.RecyclerView, private val dispatchSlips: LiveData<Array<DispatchSlip?>>) : androidx.recyclerview.widget.RecyclerView.Adapter<SimpleDispatchListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.dispatch_list_picking_row_item, parent, false)

        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind()
    }

    override fun getItemCount(): Int {
        return dispatchSlips.value?.size ?: 0
    }

    open inner class ViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        protected val dispatchSlipId: TextView

        init {
            dispatchSlipId = itemView.findViewById(R.id.dispatch_list_row_title)
        }

        fun bind() {
            val dispatchSlip = dispatchSlips.value!![adapterPosition]!!

            dispatchSlipId.text = dispatchSlip.dispatchSlipNumber
        }
    }
}
