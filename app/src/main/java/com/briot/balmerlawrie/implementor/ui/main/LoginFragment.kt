package com.briot.balmerlawrie.implementor.ui.main

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.navigation.Navigation
import com.briot.balmerlawrie.implementor.MainActivity
import com.briot.balmerlawrie.implementor.R
import com.briot.balmerlawrie.implementor.repository.local.PrefConstants
import com.briot.balmerlawrie.implementor.repository.local.PrefRepository
import com.briot.balmerlawrie.implementor.repository.remote.User
import io.github.pierry.progress.Progress
import kotlinx.android.synthetic.main.login_fragment.*
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.ViewModelProvider


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
        // TODO: Use the ViewModel

        username.requestFocus()

        viewModel.networkError.observe(this, Observer<Boolean> {

            if (it == true) {
                MainActivity.hideProgress(this.progress)
                this.progress = null

                var message: String = "Server is not reachable, please check if your network connection is working"
                if (viewModel.errorMessage != null && viewModel.errorMessage.isNotEmpty()) {
                    message = viewModel.errorMessage
                }

                MainActivity.showToast(this.activity as AppCompatActivity, message);
            }
        })


        login.setOnClickListener {
            val keyboard = activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            keyboard.hideSoftInputFromWindow(activity?.currentFocus?.getWindowToken(), 0)

            // @dineshgajjar - remove following statement later on
            Navigation.findNavController(login).navigate(R.id.action_loginFragment_to_homeFragment)


            // @dineshgajjar - remove following coments later on
//            this.progress = MainActivity.showProgressIndicator(this.activity as AppCompatActivity, "Please wait")
//            viewModel.loginUser(username.text.toString(), password.text.toString())
        }

//        username.setOnEditorActionListener { _, i, keyEvent ->
//            var handled = false
//
//            if (i == EditorInfo.IME_ACTION_DONE || (keyEvent.keyCode == KeyEvent.KEYCODE_ENTER && keyEvent.action == KeyEvent.ACTION_DOWN)) {
////                password.requestFocus()
//            }
//            handled
//
//        }
//
//        password.setOnEditorActionListener { _, i, keyEvent ->
//            var handled = false
//
//            if (i == EditorInfo.IME_ACTION_DONE || (keyEvent.keyCode == KeyEvent.KEYCODE_ENTER && keyEvent.action == KeyEvent.ACTION_DOWN)) {
////                this.progress = MainActivity.showProgressIndicator(this.activity as AppCompatActivity, "Please wait")
////                viewModel.loginUser(username.text.toString(), password.text.toString())
//            }
//            handled
//
//        }

    }

}
