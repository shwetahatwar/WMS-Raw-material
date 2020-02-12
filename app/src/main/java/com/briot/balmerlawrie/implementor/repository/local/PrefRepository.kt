package com.briot.balmerlawrie.implementor.repository.local

import android.content.Context
import com.mysql.cj.x.protobuf.MysqlxExpr

class PrefRepository {
    companion object {
        val singleInstance = PrefRepository();
    }

    private val prefs = HashMap<String, String>()

    fun setKeyValue(key: String, value: String) {
        prefs[key] = value;
    }

    fun getValueOrDefault(key: String, defaultValue: String) : String {
        return prefs[key] ?: defaultValue;
    }

    fun serializePrefs(context: Context) {
        val sharedPref = context.getSharedPreferences("default", Context.MODE_PRIVATE) ?: return
        val editor = sharedPref.edit();

        prefs.keys.asIterable().forEach {
            editor.putString(it, prefs[it] ?: "")
        }

        editor.commit();
    }

    fun deserializePrefs(context: Context) {
        val sharedPref = context.getSharedPreferences("default", Context.MODE_PRIVATE) ?: return
        sharedPref.all.keys.forEach {
            prefs[it] = sharedPref.getString(it, "").orEmpty()
        }
    }
}

class PrefConstants {
    public val USER_TOKEN = "USERTOKEN"
    public val USER_NAME = "USER_NAME"
    public val USER_ID = "USER_ID"
    public val ROLE_NAME = "ROLE_NAME"
    public val ROLE_ID = "ROLE_ID"
    public val EMPLOYEE_NAME = "EMPLOYEE_NAME"
    public val EMPLOYEE_EMAIL = "EMPLOYEE_EMAIL"
    public val EMPLOYEE_PHONE = "EMPLOYEE_PHONE"
    public val EMPLOYEE_STATUS = "EMPLOYEE_STATUS"
}