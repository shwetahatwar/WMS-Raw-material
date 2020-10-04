package com.briot.wms.implementor.ui.main

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.briot.wms.implementor.R
import com.briot.wms.implementor.repository.local.PrefConstants
import com.briot.wms.implementor.repository.local.PrefRepository
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.main_fragment.*
import java.util.concurrent.TimeUnit

class MainFragment : androidx.fragment.app.Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }

    private lateinit var viewModel: MainViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        // TODO: Use the ViewModel


        context?.let { PrefRepository.singleInstance.deserializePrefs(it) }

        var userToken: String = PrefRepository.singleInstance.getValueOrDefault(PrefConstants().USER_TOKEN, "")
        var navCtrl = Navigation.findNavController(img)
        Log.d("MainFragment - ", "navCtrl: " + navCtrl.toString())
        if (userToken.isNotEmpty()) {
            Observable.timer(2000, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ navCtrl.navigate(R.id.homeFragment) });
        } else {
            Observable.timer(2000, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ Navigation.findNavController(img).navigate(R.id.action_mainFragment_to_loginFragment) });
        }
    }

}
