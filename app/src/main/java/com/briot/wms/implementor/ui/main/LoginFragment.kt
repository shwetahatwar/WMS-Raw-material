package com.briot.wms.implementor.ui.main

import android.content.Context
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.CompoundButton
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.briot.wms.implementor.R
import com.briot.wms.implementor.UiHelper
import com.briot.wms.implementor.repository.local.PrefConstants
import com.briot.wms.implementor.repository.local.PrefRepository
import com.briot.wms.implementor.repository.remote.SignInResponse
import io.github.pierry.progress.Progress
import kotlinx.android.synthetic.main.login_fragment.*


class LoginFragment : androidx.fragment.app.Fragment() {

    companion object {
        fun newInstance() = LoginFragment()
    }

    private lateinit var viewModel: LoginViewModel
    private var progress: Progress? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.login_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(LoginViewModel::class.java)

        (this.activity as AppCompatActivity).setTitle("Raw Material WMS")

        username.requestFocus()

        val imm = activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view!!.windowToken, 0)

        val hostname = PrefRepository.singleInstance.getValueOrDefault(PrefConstants().IPADDRESS, "")

        showpasswordCheckBox.setOnCheckedChangeListener() { compoundButton: CompoundButton, b: Boolean ->
            if (showpasswordCheckBox.isChecked) {
                password.setTransformationMethod(HideReturnsTransformationMethod())
            } else (
                    password.setTransformationMethod(PasswordTransformationMethod())
                    )
        }

        viewModel.signInResponse.observe(this, Observer<SignInResponse> {
            UiHelper.hideProgress(this.progress)
            this.progress = null

            if (it != null) {
                this.activity?.invalidateOptionsMenu()
                PrefRepository.singleInstance.setKeyValue(PrefConstants().USER_TOKEN, it.token!!)
                PrefRepository.singleInstance.setKeyValue(PrefConstants().USER_ID, it.userId!!.toString())
                PrefRepository.singleInstance.setKeyValue(PrefConstants().USER_NAME, it.username!!)
                PrefRepository.singleInstance.setKeyValue(PrefConstants().ROLE_ID, it.roleId!!.toString())
                PrefRepository.singleInstance.setKeyValue(PrefConstants().ROLE_NAME, it.role!!)
                PrefRepository.singleInstance.setKeyValue(PrefConstants().EMPLOYEE_ID, it.employeeId!!)

                this.context?.let { it1 -> PrefRepository.singleInstance.serializePrefs(it1) }

                Navigation.findNavController(login).navigate(R.id.action_loginFragment_to_homeFragment)
            } else {
                UiHelper.showErrorToast(this.activity as AppCompatActivity, "An error has occurred, please try again.");
                UiHelper.hideProgress(this.progress)
                username.requestFocus()
            }

        })

        // set on-click listener
        ip_image.setOnClickListener {
            // your code to perform when the user clicks on the ImageView
            Navigation.findNavController(login).navigate(R.id.settingFragment)
        }


        viewModel.networkError.observe(this, Observer<Boolean> {

            if (it == true) {
                UiHelper.hideProgress(this.progress)
                this.progress = null

                var message: String = "Server is not reachable, please check if your network connection is working"
                if (viewModel.errorMessage.isNotEmpty()) {
                    message = viewModel.errorMessage
                }

                UiHelper.showSnackbarMessage(this.activity as AppCompatActivity, message, 3000);
                username.requestFocus()
            }
        })


        login.setOnClickListener {
            val keyboard = activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            keyboard.hideSoftInputFromWindow(activity?.currentFocus?.getWindowToken(), 0)
            this.progress = UiHelper.showProgressIndicator(this.activity as AppCompatActivity, "Please wait")

            if (hostname == "" || hostname.isEmpty() || hostname == null){
                UiHelper.showErrorToast(this.activity as AppCompatActivity, "Please set hostname in setting");
                return@setOnClickListener
            }
            viewModel.loginUser(username.text.toString(), password.text.toString(), hostname)
            username.text?.clear();
            password.text?.clear();
            username.requestFocus()
        }

        //-------------------------------------------------------------------
        password.setOnEditorActionListener { _, i, keyEvent ->
            var handled = false
            if ((password.text != null && password.text!!.isNotEmpty()) && i == EditorInfo.IME_ACTION_DONE
                    || (keyEvent != null && (keyEvent.keyCode == KeyEvent.KEYCODE_ENTER || keyEvent.keyCode == KeyEvent.KEYCODE_TAB)
                            && keyEvent.action == KeyEvent.ACTION_DOWN)) {
                val keyboard = activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                keyboard.hideSoftInputFromWindow(activity?.currentFocus?.getWindowToken(), 0)
                this.progress = UiHelper.showProgressIndicator(this.activity as AppCompatActivity, "Please wait")
                if (hostname == "" || hostname.isEmpty() || hostname == null){
                    UiHelper.showErrorToast(this.activity as AppCompatActivity, "Please set hostname in setting");
                    // return@setOnEditorActionListener
                }else{
                    viewModel.loginUser(username.text.toString(), password.text.toString(), hostname)
                    username.text?.clear();
                    password.text?.clear();
                    username.requestFocus()
                    handled = true
                }
            }
            handled
        }
        //-------------------------------------------------------------------

    }

}
