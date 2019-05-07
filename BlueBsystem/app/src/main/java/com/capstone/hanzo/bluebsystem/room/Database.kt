package com.capstone.hanzo.bluebsystem.room

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context
import com.capstone.hanzo.bluebsystem.BusNoList
import com.capstone.hanzo.bluebsystem.PlatformArvlInfoList
import com.capstone.hanzo.bluebsystem.UserInfoList


@Database(entities = [BusNoList::class, PlatformArvlInfoList::class], version = 4)
abstract class InfoDB : RoomDatabase() {
    abstract fun busDao(): BusDao
    abstract fun platformDao(): PlatformDao

    companion object {
        private var INSTANCE: InfoDB? = null

        fun getInstance(context: Context): InfoDB? {
            if (INSTANCE == null) {
                synchronized(InfoDB::class) {
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        InfoDB::class.java, "info.db"
                    ).fallbackToDestructiveMigration().build()
                }
            }
            return INSTANCE
        }

        fun destroyInstance() {
            INSTANCE = null
        }
    }
}
