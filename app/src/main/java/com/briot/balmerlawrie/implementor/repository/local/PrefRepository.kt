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
        val sharedPref = context?.getSharedPreferences("default", Context.MODE_PRIVATE) ?: return
        val editor = sharedPref.edit();

        prefs.keys.asIterable().forEach {
            editor.putString(it, prefs[it] ?: "")
        }

        editor.commit();
    }

    fun deserializePrefs(context: Context) {
        val sharedPref = context?.getSharedPreferences("default", Context.MODE_PRIVATE) ?: return
        sharedPref.all.keys.forEach {
            prefs[it] = sharedPref.getString(it, "")
        }
    }
}

class PrefConstants {
    public val PRODUCT_PRODUCTNAME = "PRODUCT_PRODUCTNAME"
    public val PRODUCT_ACCOUNTNAME = "PRODUCT_ACCOUNTNAME"
    public val PRODUCT_ITEMNAME = "PRODUCT_ITEMNAME"
    public val PRODUCT_QUANTITY = "PRODUCT_QUANTITY"
    public val PRODUCT_UNITNAME = "PRODUCT_UNITNAME"
    public val PRODUCT_DETAILS_ID = "PRODUCT_DETAILS_ID"
    public val PRODUCT_STOCK_ID = "PRODUCT_STOCK_ID"
    public val PRODUCT_ITEM_ID = "PRODUCT_ITEM_ID"
    public val PROJECT_ID = "PROJECT_ID"
    public val PROJECT_NAME = "PROJECT_NAME"
    public val USERLOGGEDIN = "USERLOGGEDIN"
    public val PUTAWAYLOCATION = "PUTAWAYLOCATION"
    public val PICKLISTID = "PICKLISTID"
    public val USER_TOKEN = "USERTOKEN"
    public val USER_NAME = "USER_NAME"
    public val USER_ID = "USER_ID"
    public val ROLE_NAME = "ROLE_NAME"
    public val ROLE_ID = "ROLE_ID"
    public val EMPLOYEE_NAME = "EMPLOYEE_NAME"
    public val EMPLOYEE_EMAIL = "EMPLOYEE_EMAIL"
    public val EMPLOYEE_PHONE = "EMPLOYEE_PHONE"
    public val EMPLOYEE_STATUS = "EMPLOYEE_STATUS"
    public val SELECTED_MACHINE_ID = "SELECTED_MACHINE_ID"
    public val SELECTED_AUDIT_SITEID = "SELECTED_AUDIT_SITEID"
    public val SELECTED_AUDIT_LOCATIONID = "SELECTED_AUDIT_LOCATIONID"
    public val SELECTED_AUDIT_SUBLOCATIONID = "SELECTED_AUDIT_SUBLOCATIONID"
    public val SELECTED_AUDIT_SUBLOCATIONNAME = "SELECTED_AUDIT_SUBLOCATIONNAME"
    public val SELECTED_AUDIT_SUBLOCATIONBC = "SELECTED_AUDIT_SUBLOCATIONBC"

}