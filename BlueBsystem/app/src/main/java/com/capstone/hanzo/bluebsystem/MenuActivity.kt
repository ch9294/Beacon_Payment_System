package com.capstone.hanzo.bluebsystem

import android.content.Context
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.util.Log
import com.capstone.hanzo.bluebsystem.room.InfoDB
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_menu.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kr.co.bootpay.Bootpay
import kr.co.bootpay.BootpayAnalytics
import okhttp3.*
import org.altbeacon.beacon.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import org.jetbrains.anko.support.v4.runOnUiThread
import org.jetbrains.anko.support.v4.toast
import org.jetbrains.anko.toast
import java.io.IOException
import java.net.URL

class MenuActivity : AppCompatActivity(), AnkoLogger, BeaconConsumer {
    private val tapIconList = intArrayOf(R.drawable.tab_reserve, R.drawable.tab_payment, R.drawable.tab_usermanagement)

    private lateinit var database: InfoDB
    private lateinit var beaconManager: BeaconManager

    val platAdapter = PlatformListAdapter()
    val busAdapter = NumberListAdapter()

    lateinit var sharedPlatformId: String
    lateinit var sharedPlatformName: String
    lateinit var sharedUserBalance: String
    lateinit var sharedLastBusNumber: String

    /**
     * 일정 시간 동안 비콘이 감지 되어있지 않은 상황이라면 하차 및 대기 상태라고 생각해야 함
     */
    override fun onBeaconServiceConnect() {
        beaconManager.run {
            removeAllRangeNotifiers()
            addRangeNotifier(RangeBeacon())
        }
    }

    inner class RangeBeacon : RangeNotifier {
        override fun didRangeBeaconsInRegion(beacons: MutableCollection<Beacon>?, region: Region?) {
            beacons?.let {
                if (it.isNotEmpty()) {
                    for (beacon in it) {

                    }
                }
            }
        }
    }

    private fun firstOnBoardBus(beacons:Beacon) = GlobalScope.launch {

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)
        BootpayAnalytics.init(this, "5c6e1824b6d49c7cbc505f9c")

        /**
         * 비콘 매니저 초기화
         * iBeacon만 받도록 설정
         */
        beaconManager = BeaconManager.getInstanceForApplication(this).apply {
            beaconParsers.add(BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"))
        }

        // 비콘의 콜백 함수를 호출함
        beaconManager.bind(this)

        // 데이터베이스 받아오기
        database = InfoDB.getInstance(this)!!

        // 리스트 초기화
        infomationDisplay()

        tabLayout.apply {
            tapIconList.forEach {
                addTab(this.newTab().setIcon(it))
            }
            addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    tab?.let {
                        pager.currentItem = it.position
                    }
                }

                override fun onTabReselected(p0: TabLayout.Tab?) {
                }

                override fun onTabUnselected(p0: TabLayout.Tab?) {
                }
            })
            tabGravity = TabLayout.GRAVITY_FILL
        }

        pager.apply {
            adapter = TapPagerAdapter(supportFragmentManager)
            addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))
        }
    }

    private fun infomationDisplay() = GlobalScope.launch {
        if (database != null) {
            busAdapter.addItem(database.busDao().getAll())
            platAdapter.addItem(database.platformDao().getAll().filter { it.platNo != "no_search" })
        } else {
            runOnUiThread { toast("데이터베이스가 없습니다.").show() }
        }
    }

    override fun onDestroy() {
        InfoDB.destroyInstance()
        super.onDestroy()
    }
//    private fun informationInit() = runBlocking {
//        val job = launch {
//            val request = makeRequest(BusNoList.URL)
//            OkHttpClient().newCall(request).enqueue(NumberListInitCallback())
//        }
//
//        launch {
//            job.join()
//            val request = makeRequest(PlatformArvlInfoList.URL)
//            OkHttpClient().newCall(request).enqueue(PlatformListInitCallback())
//        }
//    }
//
//    // 버스 번호 리스트를 초기화하는 서버의 응답
//    inner class NumberListInitCallback : Callback {
//        override fun onFailure(call: Call, e: IOException) {
//            runOnUiThread { toast("접속 실패") }
//        }
//
//        override fun onResponse(call: Call, response: Response) {
//            val items: ArrayList<BusNoList> = BusNoList.parseJSON(response)
//            runOnUiThread {
//                items.forEach { busAdapter.addItem(it.busId, it.busNo, it.uuid, it.major, it.start, it.end) }
//            }
//        }
//    }
//
//    // 정류장 리스트를 초기화하는 서버의 응답
//    inner class PlatformListInitCallback : Callback {
//        override fun onFailure(call: Call, e: IOException) {
//            runOnUiThread { toast("접속 실패") }
//        }
//
//        override fun onResponse(call: Call, response: Response) {
//            val items: ArrayList<PlatformArvlInfoList> = PlatformArvlInfoList.parseJSON(response)
//            runOnUiThread {
//                items.filter { it.platNo != "no_search" }
//                    .forEach { platAdapter.addItem(it.platId, it.platName, it.platNo) }
//            }
//        }
//    }

    /**
     * TODO : onBackPressed()를 오버라이딩을 해야함 (두 번 연속으로 누를 시에 앱이 완전 종료되도록...)
     */

}