package com.briot.balmerlawrie.implementor

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.navigation.Navigation
import androidx.navigation.Navigation.findNavController
import com.briot.balmerlawrie.implementor.repository.local.PrefConstants
import com.briot.balmerlawrie.implementor.repository.local.PrefRepository
import com.google.android.material.snackbar.Snackbar
import io.github.pierry.progress.Progress
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import okhttp3.Interceptor
import okhttp3.Response
import com.google.gson.GsonBuilder
import com.google.gson.Gson
import de.hdodenhof.circleimageview.CircleImageView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.main_fragment.*
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException


class ResponseHeaderAuthTokenInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalResponse = chain.proceed(chain.request())

        val localheaders = originalResponse.headers("token")

        val jwtTokenExists = localheaders.isNotEmpty()

        if (jwtTokenExists) {
            val jwtToken = localheaders.get(0)
            PrefRepository.singleInstance.setKeyValue("token", jwtToken ?: "")
        }

        return originalResponse
    }

}

class RequestHeaderAuthTokenInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val builder = chain.request().newBuilder()

        val tokenStr = PrefRepository.singleInstance.getValueOrDefault(PrefConstants().USER_TOKEN, "")
        if (tokenStr != null && tokenStr.length > 1) {
            val token: String = "JWT " + tokenStr
            builder.addHeader("Authorization", token)
        }
        builder.addHeader("Content-Type", "application/json")

        return chain.proceed(builder.build())
    }

}


class RetrofitHelper {
    companion object {
        val BASE_URL = BuildConfig.HOSTNAME;


        private fun getOkHttpClient(): OkHttpClient {
            val okHttpClient: OkHttpClient.Builder = OkHttpClient().newBuilder()
//                    .connectTimeout((30).toLong(), TimeUnit.SECONDS)
                    .readTimeout((90).toLong(), TimeUnit.SECONDS)
                    .writeTimeout((60).toLong(), TimeUnit.SECONDS)

            okHttpClient.interceptors().add(RequestHeaderAuthTokenInterceptor())
            okHttpClient.interceptors().add(ResponseHeaderAuthTokenInterceptor())

            return okHttpClient.build()
        }

        val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(getOkHttpClient())
                .build()
    }
}

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
    }

    override fun onSupportNavigateUp()
            = findNavController(findViewById(R.id.nav_host_fragment)).navigateUp()

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        getMenuInflater().inflate(R.menu.activity_main_drawer, menu);
        return true;
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        var savedToken: String = PrefRepository.singleInstance.getValueOrDefault(PrefConstants().USER_TOKEN, "")
        if (savedToken.isEmpty()) {
            return false
        }

        return super.onPrepareOptionsMenu(menu)
    }


    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.nav_logout -> {
                logout()
                true
            }
            R.id.nav_user_profile -> {
                showUserProfile()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showUserProfile() {
        findNavController(findViewById(R.id.nav_host_fragment)).navigate(R.id.action_homeFragment_to_userProfileFragment)
    }

    private fun logout() {
        var savedToken: String = PrefRepository.singleInstance.getValueOrDefault(PrefConstants().USER_TOKEN, "")
        if (savedToken.isEmpty()) {
            return
        }

        invalidateOptionsMenu()
        PrefRepository.singleInstance.setKeyValue(PrefConstants().USER_TOKEN, "")
        PrefRepository.singleInstance.setKeyValue(PrefConstants().USER_NAME, "")
        PrefRepository.singleInstance.setKeyValue(PrefConstants().USER_ID, "")
        PrefRepository.singleInstance.setKeyValue(PrefConstants().EMPLOYEE_STATUS, "")
        PrefRepository.singleInstance.setKeyValue(PrefConstants().EMPLOYEE_NAME, "")
        PrefRepository.singleInstance.setKeyValue(PrefConstants().EMPLOYEE_EMAIL, "")
        PrefRepository.singleInstance.setKeyValue(PrefConstants().EMPLOYEE_PHONE, "")
        PrefRepository.singleInstance.setKeyValue(PrefConstants().ROLE_ID, "")
        PrefRepository.singleInstance.setKeyValue(PrefConstants().ROLE_NAME, "")

        this.applicationContext.let { PrefRepository.singleInstance.serializePrefs(it) }

        val userToken: String = PrefRepository.singleInstance.getValueOrDefault(PrefConstants().USER_TOKEN, "")
        val navController = findNavController(findViewById(R.id.nav_host_fragment))
        if (userToken.isEmpty()) {
            navController.popBackStack(R.id.mainFragment, false)
        }

    }
}

class UiHelper {
    companion object {
        val SNACKBAR_COLOR = Color.parseColor("#E9E67E22")

        fun showAlert(activity: AppCompatActivity, message: String, cancellable: Boolean = false) {
            AlertDialog.Builder(activity).create().apply {
                setTitle("Alert")
                setMessage(message)
                setCancelable(cancellable)
                setButton(AlertDialog.BUTTON_NEUTRAL, "OK", { dialog, _ -> dialog.dismiss() })
                show()
            }
        }

        fun showSnackbarMessage(activity: AppCompatActivity, message: String, duration: Int = Snackbar.LENGTH_INDEFINITE) {
            val coordinatorLayout = activity.findViewById<androidx.coordinatorlayout.widget.CoordinatorLayout>(R.id.container)

            val snackbar = Snackbar.make(coordinatorLayout!!, message, duration)
                    .setAction("OK", View.OnClickListener {

                    })

            snackbar.setActionTextColor(Color.WHITE)
            val actionButton = snackbar.view.findViewById<Button>(com.google.android.material.R.id.snackbar_action)
//            actionButton.typeface = ResourcesCompat.getFont(activity, R.font.lato_bold)
            actionButton.textSize = 18f

            val snackbarTextView = snackbar.view
                    .findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
            snackbarTextView.setTextColor(Color.WHITE)
//            snackbarTextView.typeface = ResourcesCompat.getFont(activity, R.font.lato)
            snackbarTextView.textSize = 16f

            snackbar.view.setBackgroundColor(SNACKBAR_COLOR)
            snackbar.show()
        }

        fun showNoInternetSnackbarMessage(activity: AppCompatActivity) {
            showSnackbarMessage(activity, activity.getString(R.string.no_internet_connection_message), Snackbar.LENGTH_INDEFINITE)
        }

        fun showSomethingWentWrongSnackbarMessage(activity: AppCompatActivity) {
            showSnackbarMessage(activity, activity.getString(R.string.oops_something_went_wrong))
        }

        fun showTryAgainLaterSnackbarMessage(activity: AppCompatActivity) {
            showSnackbarMessage(activity, activity.getString(R.string.try_again_later), Snackbar.LENGTH_LONG)
        }

        fun showProgressIndicator(context: Context, message: String): Progress {
            val progress = Progress(context)

            progress.setBackgroundColor(Color.parseColor("#EEEEEE"))
                    .setMessage(message)
                    .setMessageColor(Color.parseColor("#444444"))
                    .setProgressColor(Color.parseColor("#444444"))
                    .show()

            return progress
        }

        fun hideProgress(progress: Progress?) {
            progress?.dismiss()
        }

        fun isNetworkError(error: Throwable) =
                error is SocketException || error is SocketTimeoutException || error is UnknownHostException

        fun showAlert(activity: AppCompatActivity, message: String) {
            AlertDialog.Builder(activity).create().apply {
                setTitle("Alert")
                setMessage(message)
                setButton(AlertDialog.BUTTON_NEUTRAL, "OK", { dialog, _ -> dialog.dismiss() })
                show()
            }
        }

        fun showToast(activity: AppCompatActivity, message: String) {
            Toast.makeText(activity, message, Toast.LENGTH_LONG).show()
        }

    }


}
