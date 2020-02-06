package com.briot.balmerlawrie.implementor

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.briot.balmerlawrie.implementor.repository.local.PrefConstants
import com.briot.balmerlawrie.implementor.repository.local.PrefRepository
import kotlinx.android.synthetic.main.user_profile_fragment.*


class UserProfileFragment : Fragment() {

    companion object {
        fun newInstance() = UserProfileFragment()
    }

    private lateinit var viewModel: UserProfileViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.user_profile_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
        viewModel = ViewModelProviders.of(this).get(UserProfileViewModel::class.java)

        (this.activity as AppCompatActivity).setTitle("User Profile")


        val userId = PrefRepository.singleInstance.getValueOrDefault(PrefConstants().USER_NAME, "")
        val userName = PrefRepository.singleInstance.getValueOrDefault(PrefConstants().EMPLOYEE_NAME, "")
        val roleName = PrefRepository.singleInstance.getValueOrDefault(PrefConstants().ROLE_NAME, "")
        val emailAddress = PrefRepository.singleInstance.getValueOrDefault(PrefConstants().EMPLOYEE_EMAIL, "")

        userNameValue.text = userId
        userFullNameValue.text = userName
        userRoleValue.text = roleName
        userEmailValue.text = emailAddress
        this.activity?.invalidateOptionsMenu()

//        PrefRepository.singleInstance.setKeyValue(PrefConstants().USER_TOKEN, "")
//        PrefRepository.singleInstance.setKeyValue(PrefConstants().USER_NAME, "")
//        PrefRepository.singleInstance.setKeyValue(PrefConstants().USER_ID, "")
//        PrefRepository.singleInstance.setKeyValue(PrefConstants().EMPLOYEE_STATUS, "")
//        PrefRepository.singleInstance.setKeyValue(PrefConstants().EMPLOYEE_NAME, "")
//        PrefRepository.singleInstance.setKeyValue(PrefConstants().EMPLOYEE_EMAIL, "")
//        PrefRepository.singleInstance.setKeyValue(PrefConstants().EMPLOYEE_PHONE, "")
//        PrefRepository.singleInstance.setKeyValue(PrefConstants().ROLE_ID, "")
//        PrefRepository.singleInstance.setKeyValue(PrefConstants().ROLE_NAME, "")
//
//        val userProfileLayout = R.id.userProfileLinearLayoutId
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        super.onPrepareOptionsMenu(menu)
        menu?.clear()
    }

}
