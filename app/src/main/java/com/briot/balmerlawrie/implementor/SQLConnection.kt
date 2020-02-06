package com.briot.balmerlawrie.implementor

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import android.util.Log

class SQLConnection {
    // private var classs = "net.sourceforge.jtds.jdbc.Driver";
    private var classs = "com.mysql.cj.jdbc.Driver";

    public fun connect() : Connection? {
        var conn : Connection? = null;

        try {
            Class.forName(classs).newInstance();
            conn = DriverManager.getConnection("jdbc:mysql://localhost/test", "test", "passw0rd");
        } catch (e: ClassNotFoundException) {
            Log.e("SQLConnection CNotFound", e.message);
        } catch (e: SQLException) {
            Log.e("SQLConnection SQL", e.message);
        } catch (e: Exception) {
            e.printStackTrace();
            Log.e("SQLConnection Error", e.toString());
        }

        return conn;
    }
}