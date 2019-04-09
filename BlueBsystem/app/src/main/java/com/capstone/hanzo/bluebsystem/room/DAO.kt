package com.capstone.hanzo.bluebsystem.room

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import com.capstone.hanzo.bluebsystem.BusNoList
import android.arch.persistence.room.OnConflictStrategy.REPLACE
import com.capstone.hanzo.bluebsystem.PlatformArvlInfoList
import com.capstone.hanzo.bluebsystem.UserInfoList


@Dao
interface BusDao {
    @Query("select count(*) from bus")
    fun getCount(): Int

    @Query("select * from bus")
    fun getAll(): List<BusNoList>

    @Query("select * from bus where uuid = :uuid")
    fun checkUUID(uuid: String): BusNoList

    @Query("delete from bus")
    fun deleteBusAll()

    @Insert(onConflict = REPLACE)
    fun insert(bus: BusNoList)
}

@Dao
interface PlatformDao {
    @Query("select count(*) from platform")
    fun getCount(): Int

    @Query("select * from platform")
    fun getAll(): List<PlatformArvlInfoList>

    @Query("delete from platform")
    fun deleteAll()

    @Insert(onConflict = REPLACE)
    fun insert(platform: PlatformArvlInfoList)
}

//@Dao
//interface UserDao{
//    @Query("select * from user")
//    fun getAll():List<UserInfoList>
//
//    @Query("delete from user")
//    fun deleteAll()
//
//    // 앱이 로딩화면을 거칠때마다 외부로부터 새로운 정보를 받아온다
//    @Insert(onConflict = REPLACE)
//    fun insertUser(user:UserInfoList)
//}
