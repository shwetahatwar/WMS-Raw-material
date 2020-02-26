
package com.briot.balmerlawrie.implementor.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = arrayOf(DispatchSlipLoadingListItem::class), version = 1, exportSchema = false)
public abstract class AppDatabase : RoomDatabase() {

    abstract fun dispatchSlipLoadingItemDuo() : DispatchSlipLoadingListItemDao

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
                        ).allowMainThreadQueries().build() // Dinesh Gajjar: not recommended allowMainThreadQueries but for now kept it like, need to find suitable way
                INSTANCE = instance
                return instance
            }
        }
    }

}