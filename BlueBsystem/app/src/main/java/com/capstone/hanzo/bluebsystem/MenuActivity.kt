package com.capstone.hanzo.bluebsystem

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Vibrator
import android.support.design.widget.TabLayout
import android.support.v4.app.ActivityCompat
import com.capstone.hanzo.bluebsystem.room.InfoDB
import kotlinx.android.synthetic.main.activity_menu.*
import kotlinx.coroutines.*
import kr.co.bootpay.BootpayAnalytics
import org.altbeacon.beacon.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.longToast

class MenuActivity : AppCompatActivity(), AnkoLogger {
    companion object {
        const val BEACON_LAYOUT = "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"
        const val BOOTPAY_ID = "5c6e1824b6d49c7cbc505f9c"
    }

    private val tapIconList = intArrayOf(R.drawable.tab_usermanagement, R.drawable.tab_reserve, R.drawable.tab_payment)
    private val fragmentTab = TapPagerAdapter(supportFragmentManager)
    lateinit var vibrator: Vibrator
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
        BootpayAnalytics.init(this, BOOTPAY_ID)
        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION), 1)
        /**
         * 비콘 매니저 초기화
         * iBeacon만 받도록 설정
         */
        beaconManager = BeaconManager.getInstanceForApplication(this).apply {
            BEACON_LAYOUT.run(BeaconParser()::setBeaconLayout).run(beaconParsers::add)
        }


        // 진동 서비스 객체 받아오기
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        // 데이터베이스 받아오기
        database = InfoDB.getInstance(this)!!

        // 리스트 초기화
        informationDisplay()

        tabLayout.apply {
            tapIconList.forEach { this.newTab().setIcon(it).run(this::addTab) }
            addOnTabSelectedListener(TabSelectedListener())
            tabGravity = TabLayout.GRAVITY_FILL
        }

        pager.apply {
            adapter = fragmentTab
            addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))
        }
    }

    private fun informationDisplay() = CoroutineScope(Dispatchers.IO).launch {
        database.apply {
            busDao().getAll().run(busAdapter::addItem)
            platformDao().getAll().filter { it.platNo != "no_search" }.run(platAdapter::addItem)
        }
    }

    override fun onDestroy() {
        InfoDB.destroyInstance()
        super.onDestroy()
    }

    /**
     * System.currentTimeMillis()와 SystemClock.currentThreadTimeMillis()는 시간을 측정하는 단위가 다른것 같음
     * System.currentTimeMillis()을 사용하여 뒤로가기 버튼을 누를 때의 종료 여부 판별하기
     */
    private var backPressBtnTime: Long = 0L

    override fun onBackPressed() {
        /**
         * 프래그먼트 스택에 프래그먼트의 개수를 확인 한다.
         * 프래그먼트 수가 1개 이상 : 프래그먼트를 삭제한다.
         * 프래그먼트 수가 0개 : 프로그램 종료 루틴
         */

        fun applicationExitRoutine() {
            var now = System.currentTimeMillis()
            var result = now - backPressBtnTime

            when (result < 2000L) {
                true -> {
                    finishAffinity()
                    System.runFinalization()
                    System.exit(0)
                }
                false -> {
                    backPressBtnTime = System.currentTimeMillis()
                    longToast("뒤로 가기 버튼 한번 더 누르면 종료합니다.")
                }
            }
        }

        val fragmentCnt = supportFragmentManager.backStackEntryCount
        if (fragmentCnt != 0) supportFragmentManager.popBackStack() else applicationExitRoutine()
    }

    inner class TabSelectedListener : TabLayout.OnTabSelectedListener {
        override fun onTabReselected(p0: TabLayout.Tab?) {
        }

        override fun onTabUnselected(p0: TabLayout.Tab?) {
        }

        override fun onTabSelected(tab: TabLayout.Tab?) {
            tab?.let { pager.currentItem = it.position }
        }
    }
}