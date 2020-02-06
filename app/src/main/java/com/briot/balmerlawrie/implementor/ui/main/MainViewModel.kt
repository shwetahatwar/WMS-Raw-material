package com.briot.balmerlawrie.implementor.ui.main

import androidx.lifecycle.ViewModel
import com.briot.balmerlawrie.implementor.SQLConnection

class MainViewModel : ViewModel() {
    // TODO: Implement the ViewModel
    var sqlConnection = SQLConnection()

    init {
        // Thread(Runnable { kotlin.run { sqlConnection.connect(); } }).start()
    }
}
