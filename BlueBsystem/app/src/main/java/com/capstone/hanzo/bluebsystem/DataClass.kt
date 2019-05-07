package com.capstone.hanzo.bluebsystem

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey
import android.os.Parcel
import android.os.Parcelable
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import okhttp3.Request
import okhttp3.Response
import java.net.URL

// Okhttp Request 객체 만들기
fun makeRequest(url: String) = Request.Builder().url(url).build()

// 버스 목록 리스트
@Entity(tableName = "bus", primaryKeys = ["busId", "busNo"])
data class BusNoList(
    val busId: String,
    val busNo: String,
    @ColumnInfo(name = "uuid") val uuid: String,
    @ColumnInfo(name = "major") val major: String,
    @ColumnInfo(name = "start") val start: String,
    @ColumnInfo(name = "end") val end: String
) {

    @Ignore
    constructor() : this("", "", "", "", "", "")

    companion object {
        const val URL = "http://13.125.170.17/busInfoSearch.php"

        fun parseJSON(response: Response): ArrayList<BusNoList> {
            return jacksonObjectMapper().readValue(response.body()?.byteStream()!!)
        }
    }
}

// 정류장 목록 리스트
@Entity(tableName = "platform", primaryKeys = ["platId", "platNo", "platName"])
data class PlatformArvlInfoList(val platId: String, val platNo: String, val platName: String) {
    @Ignore
    constructor() : this("", "", "")

    companion object {
        const val URL = "http://13.125.170.17/platformInfoSearch.php"
        fun parseJSON(response: Response): ArrayList<PlatformArvlInfoList> {
            return jacksonObjectMapper().readValue(response.body()?.byteStream()!!)
        }
    }
}

// 선택한 정류장의 도착정보 리스트
data class PlatformArvlInfoList2(val number: String, val time: String, val type: String)

// 사용자의 정보 리스트
data class UserInfoList(
    val userCash: String,
    val userTrans: String,
    val userGetOffTime: String?,
    val userIn: String,
    val userLastBusNo: String?
) {
    companion object {
        const val URL = "http://13.125.170.17/googleUserInfoSelect.php"
        const val URL_2 = "http://13.125.170.17/userBookingStateRenewal.php"
        fun parseJSON(response: Response): ArrayList<UserInfoList> {
            return jacksonObjectMapper().readValue(response.body()?.byteStream()!!)
        }
    }
}
