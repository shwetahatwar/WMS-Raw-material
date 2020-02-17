
package com.briot.balmerlawrie.implementor.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import java.security.AccessControlContext

@Database(entities = arrayOf(DispatchSlipPickingListItem::class), version = 1, exportSchema = false)
public abstract class AppDatabase : RoomDatabase() {

    abstract fun dispatchSlipPickingItemDuo() : DispatchSlipPickingListItemDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return  tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(context.applicationContext,
                        AppDatabase::class.java, "AppDatabase"
                        ).build()
                INSTANCE = instance
                return instance
            }
        }
    }

}