package com.briot.balmerlawrie.implementor.ui.main

import android.app.Dialog
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

import com.briot.balmerlawrie.implementor.R
import kotlinx.android.synthetic.main.login_dialog_fragment.*
import kotlinx.android.synthetic.main.login_dialog_fragment.view.*

class LoginDialog : DialogFragment() {


    companion object {
        fun newInstance() = LoginDialog()
    }

    private lateinit var viewModel: LoginDialogViewModel

    var alertDialog: AlertDialog? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.login_dialog_fragment, container, false)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(LoginDialogViewModel::class.java)


        dialogLoginBtn.setOnClickListener {
            //dismiss dialog
//            mAlertDialog.dismiss()
            //get text from EditTexts of custom layout
            val name = dialogNameEt.text.toString()
            val password = dialogPasswEt.text.toString()
            alertDialog?.dismiss()

        }
        //cancel button click of custom layout
        dialogCancelBtn.setOnClickListener {
            //dismiss dialog
            alertDialog?.dismiss()
        }
    }

}
