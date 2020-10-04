package com.briot.wms.implementor.ui.main

import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.briot.wms.implementor.R
import com.briot.wms.implementor.UiHelper
import com.briot.wms.implementor.repository.local.PrefConstants
import com.briot.wms.implementor.repository.local.PrefRepository
import kotlinx.android.synthetic.main.setting_fragment.*
import java.net.Inet4Address


class SettingFragment : Fragment() {

    companion object {
        fun newInstance() = SettingFragment()
    }

    private lateinit var viewModel: SettingViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.setting_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(SettingViewModel::class.java)
        (this.activity as AppCompatActivity).setTitle("Setting")

        var old_ip = PrefRepository.singleInstance.getValueOrDefault(PrefConstants().IPADDRESS, "")
        println("ipaddress 4333-->"+PrefRepository.singleInstance.getValueOrDefault(PrefConstants().IPADDRESS, ""))
        fun String.toEditable(): Editable =  Editable.Factory.getInstance().newEditable(this)
        setting_textView.text = old_ip.toEditable()

        setting_save_button.setOnClickListener {
            var url = setting_textView.getText()
            // var url = "https://briot-wms-app.herokuapp.com"
            var ip = ""
            var flag : Boolean = false
            if (url!!.contains("//")) {
                ip = url.split("//")[1];
                if (ip.contains(":")) {
                    ip = ip.split(":")[0];
                    val result = ValidateIPv4.isValidInet4Address(ip)
                    if (result) {
                        // valid ip address
                        flag=true
                        PrefRepository.singleInstance.setKeyValue(PrefConstants().IPADDRESS, url.toString())
                    } else {
                        UiHelper.showErrorToast(this.activity as AppCompatActivity,
                                "Please enter valid url")
                    }
                } else {
                    flag=true
                    PrefRepository.singleInstance.setKeyValue(PrefConstants().IPADDRESS, url.toString())
                }
            }else {
                UiHelper.showErrorToast(this.activity as AppCompatActivity,
                        "Please enter valid url address")
            }
            if (flag){
                println("ipaddress-->"+PrefRepository.singleInstance.getValueOrDefault(PrefConstants().IPADDRESS, ""))
                UiHelper.showSuccessToast(this.activity as AppCompatActivity,
                        "submitted successfully")
//                val intent = Intent(context, MainActivity::class.java)
//                context!!.startActivity(intent)
                Navigation.findNavController(it).navigate(R.id.loginFragment)
            }
        }
    }

    internal object ValidateIPv4 {
        fun isValidInet4Address(ip: String?): Boolean {
            return try {
                Inet4Address.getByName(ip)
                        .getHostAddress().equals(ip)
            } catch (ex: Exception) {
                // Log.d("Exception in ip ", ex.getLocalizedMessage());
                false
            }
        }
    }
}