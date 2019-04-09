package com.capstone.hanzo.bluebsystem

import android.content.Context
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.support.design.widget.TabLayout
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.util.Log
import com.capstone.hanzo.bluebsystem.room.InfoDB
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_menu.*
import kotlinx.android.synthetic.main.fragment_user_management.*
import kotlinx.coroutines.*
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

class MenuActivity : AppCompatActivity(), AnkoLogger{
    private val tapIconList = intArrayOf(R.drawable.tab_usermanagement, R.drawable.tab_reserve, R.drawable.tab_payment)
    private val fragmentTab = TapPagerAdapter(supportFragmentManager)

    lateinit var database: InfoDB // room 데이터베이스
    lateinit var beaconManager: BeaconManager // 비콘 매니저

    val platAdapter = PlatformListAdapter() // 정류장 어댑터
    val busAdapter = NumberListAdapter() // 버스 어댑터

    lateinit var sharedPlatformId: String
    lateinit var sharedPlatformName: String
    lateinit var sharedUserBalance: String
    var sharedLastBusNumber: String? = null
    var paymentFlag = false // 결제 플래그
    var inFlag = false // 탑승 플래그
    var transFlag = false // 환승 플래그

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)
        BootpayAnalytics.init(this, "5c6e1824b6d49c7cbc505f9c")
        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION),1)
        /**
         * 비콘 매니저 초기화
         * iBeacon만 받도록 설정
         */
        beaconManager = BeaconManager.getInstanceForApplication(this).apply {
            beaconParsers.add(BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"))
        }

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
            adapter = fragmentTab
            addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))
        }
    }

    private fun infomationDisplay() = CoroutineScope(Dispatchers.IO).launch {
        busAdapter.addItem(database.busDao().getAll())
        platAdapter.addItem(database.platformDao().getAll().filter { it.platNo != "no_search" })
    }

    override fun onDestroy() {
        InfoDB.destroyInstance()
        super.onDestroy()
    }


    /**
     * TODO : onBackPressed()를 오버라이딩을 해야함 (두 번 연속으로 누를 시에 앱이 완전 종료되도록...)
     */

}