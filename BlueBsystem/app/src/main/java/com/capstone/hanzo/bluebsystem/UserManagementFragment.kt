package com.capstone.hanzo.bluebsystem


import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.SystemClock
import android.os.VibrationEffect
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.*
import okhttp3.*
import org.altbeacon.beacon.Beacon
import org.altbeacon.beacon.BeaconConsumer
import org.altbeacon.beacon.Region
import org.jetbrains.anko.*
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.support.v4.runOnUiThread
import org.jetbrains.anko.support.v4.toast
import java.io.IOException
import java.lang.Exception
import java.util.*


// TODO: 승차, 하차, 환승, 서비스 종료에 따른 각각의 Notification을 만들어야 한다. (2019-04-23)
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 *
 */
class UserManagementFragment : Fragment(), AnkoLogger {

    companion object {
        const val TAG = "test_beacon"
        //        const val TRANS_VALID_TIME = 180000
        const val TRANS_VALID_TIME = 60000
        const val PRICE_NORMAL = 1250
        const val PRICE_EXPRESS = 1650
    }

    private lateinit var profileNm: TextView
    private lateinit var profileEm: TextView
    private lateinit var profileBalT: TextView
    private lateinit var profileBusNum: TextView
    private lateinit var btnRangeStart: Button
    private lateinit var btnRangeEnd: Button
    private val user = FirebaseAuth.getInstance().currentUser

    var rangingJob: Job? = null
    private var stop = false

    inner class UserBeaconConsumer : BeaconConsumer {
        val context = activity as MenuActivity

        override fun getApplicationContext() = context

        override fun unbindService(p0: ServiceConnection?) {
            context.unbindService(p0)
        }

        override fun bindService(p0: Intent?, p1: ServiceConnection?, p2: Int): Boolean =
            context.bindService(p0, p1, p2)

        override fun onBeaconServiceConnect() {
            rangingJob = startRangingBeacon()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_user_management, container, false).apply {
            profileBalT = findViewById(R.id.profileBalT)
            profileEm = findViewById(R.id.profileEm)
            profileNm = findViewById(R.id.profileNm)
            profileBusNum = findViewById(R.id.profileBusNum)
            btnRangeEnd = findViewById(R.id.btnRangeEnd)
            btnRangeStart = findViewById(R.id.btnRangeStart)
        }

        user?.let {
            profileNm.text = it.displayName
            profileEm.text = it.email
        }

        btnRangeStart.setOnClickListener {
            Log.d(TAG, "btnRangeStart 이벤트 발생")
            toast("서비스 시작합니다.").show()
            if ((activity as MenuActivity).sharedUserBalance.toInt() < PRICE_NORMAL) {
                toast("잔액이 부족합니다.").show()
            } else {
                /**
                 *  BeaconConsumer 객체의 경우 미리 만들어 사용하는 경우
                 *  감지 기능을 종료했다가 다시 켜지지 않는다
                 *  그래서 버튼을 누를 때 마다 객체가 새로 만들어지도록 설정함
                 */
                (activity as MenuActivity).beaconManager.bind(UserBeaconConsumer())
            }
        }

        btnRangeEnd.setOnClickListener {
            Log.d(TAG, "btnRangeEnd 이벤트 발생")
            toast("서비스 시작합니다.").show()
            stop = true
            rangingJob?.let {
                (activity as MenuActivity).run {
                    beaconManager.stopRangingBeaconsInRegion(Region("blueb", null, null, null))
                    sharedLastBusNumber = null
                    inFlag = false
                    transFlag = false
                    paymentFlag = false
                    profileBusNum.text = "미탑승"
                }
                onResume()
                updateUserInfoLaunch(null)
            } ?: toast("종료할 서비스가 없습니다").show()
        }
        return view
    }

    /**
     * 사용자가 버튼을 누르면 비콘 감지를 시작한다.
     * TODO : 환승과 하차 기능을 테스트 해야함 (2019.04.10)
     */

    // 2019.05.06 getOffCnt를 다른 함수에서도 변경할 수 있게함.(인스턴스 변수로 전환함)
    private var getOffCnt = 0

    fun startRangingBeacon() = CoroutineScope(Dispatchers.IO).launch {
        Log.d(TAG, "비콘 감지를 시작합니다")

        // 비콘 신호를 감지하지 못할 경우를 세는 계수
        var cnt = 0

        /**
         * 하차 후에 비콘 신호를 계속 받지 못할 경우를 대비하여
         * 맨 먼저 하차 루틴이 시작 될 때의 상황을 저장한다.
         * 그 이유는 하차 후에도 계속해서 하차 루틴이 반복되기 때문에 환승 시간을 체크하기 위해 가장 첫번째 상황만
         * 저장할 필요가 있기 때문이다.
         */

        var getOffTime: Long = 0
        stop = false

        (activity as MenuActivity).run {
            beaconManager.run {
                try {
                    if (!stop) {
                        Log.d(TAG, "서비스를 시작합니다")
                        startRangingBeaconsInRegion(Region("blueb", null, null, null))
                    }
                } catch (e: Exception) {
                }

                removeAllRangeNotifiers()
                addRangeNotifier { beacons, region ->
                    Log.d(TAG, "addRangeNotifier() 호출")
                    // 비콘 신호가 10초 동안 감지 되지 않은 경우 하차 루틴을 시작한다.
                    if (cnt == 10) {
                        // 첫 10초 이후에는 단순 하차 처리
                        if ((getOffCnt == 0) and inFlag) {
                            Log.d(TAG, "하차 처리 되었습니다")
                            inFlag = false

                            CoroutineScope(Dispatchers.Main).launch {
                                AlertDialog.Builder(activity as MenuActivity).apply {
                                    setTitle("하차")
                                    setMessage("${sharedLastBusNumber}번 : 하차하였습니다.")
                                    setPositiveButton("확인", null)
                                }.show()
                                profileBusNum.text = "환승 대기중"
                            }
                        }

                        /**
                         * 환승 플래그 참 && 탑승 플래그 false 인 경우 (즉, 사용자가 하차 한 후 직접 서비스 종료를 하지 않은 경우)
                         * 환승 가능 시간이 하차 시간부터 30분 이내에만 가능하기 때문에
                         * 30분 이후에는 자동적으로 서비스 종료를 하게 함
                         */

                        if (transFlag and inFlag.not()) {
                            when (++getOffCnt == 1) {
                                true -> {
                                    getOffTime = System.currentTimeMillis()
                                    Log.d(TAG, "하차 시간을 저장합니다. : ${Date(getOffTime)}")
                                    cnt = 0
                                }
                                false -> {
                                    val now = System.currentTimeMillis()
                                    val timeGap = now - getOffTime

                                    when (timeGap <= TRANS_VALID_TIME) {
                                        true -> {
                                            // 환승 상태 유지
                                            cnt = 0
                                            Log.d(TAG, "환승이 유효합니다")
                                        }

                                        false -> {
                                            Log.d(TAG, "환승 가능한 시간을 초과했습니다.")
                                            // 환승 가능 시간 초과 자동적으로 서비스 종료
                                            btnRangeEnd.callOnClick()
                                            stop = true
                                            getOffCnt = 0
                                            Log.d(TAG, "비콘 감지가 종료됩니다")
                                            CoroutineScope(Dispatchers.Main).launch {
                                                AlertDialog.Builder(activity as MenuActivity).apply {
                                                    setTitle("시간 초과")
                                                    setMessage(
                                                        "환승 가능한 시간을 초과하였습니다.\n" +
                                                                "서비스를 종료합니다."
                                                    )
                                                    setPositiveButton("확인", null)
                                                }.show()
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    when (beacons.isNotEmpty() && !stop) {
                        true -> {// 비콘이 감지되는 경우
                            Log.d(TAG, "비콘이 감지 되었습니다.")
                            for (b in beacons) {
                                // 비콘이 감지 되었기 때문에 0으로 리셋
                                cnt = 0
                                // 탑승 하지 않았을 경우의 루틴
                                if (!inFlag) uuidCheck(b)
                            }
                        }

                        false -> {// 비콘이 감지되지 않는 경우 계수를 증가 시킴
                            Log.d(TAG, "비콘이 감지 되지 않습니다.")
                            cnt++
                        }
                    }
                }
            }
        }
    }


    // 수신 받은 비콘과 데이터베이스에 저장된 버스의 uuid를 비교한다
    private fun uuidCheck(b: Beacon) = CoroutineScope(Dispatchers.IO).launch {
        Log.d(TAG, "uuid를 체크합니다")
        val bus: BusNoList? = (activity as MenuActivity).database.busDao().checkUUID(b.id1.toString())
        bus?.let {
            val job = onBroadLaunch(bus)
            updateUserInfoLaunch(job)
        } ?: launch(Dispatchers.Main) { toast("일치하는 uuid 값이 없습니다").show() }
    }

    // 사용자 정보를 수정한다
    private fun updateUserInfoLaunch(job: Job?) = CoroutineScope(Dispatchers.IO).launch {
        Log.d(TAG, "승차 처리를 대기중입니다.")
        job?.join()
        Log.d(TAG, "사용자 정보를 수정합니다.")

        val url = Request.Builder().url(UserInfoList.URL_2)
        FormBody.Builder().apply {
            add("user_email", FirebaseAuth.getInstance().currentUser?.email!!)
            (activity as MenuActivity).run {
                add("user_transfer", if (transFlag) "1" else "")
                add("user_in", if (inFlag) "1" else "")
                add("last_bus_no", sharedLastBusNumber.toString())
            }
        }.build()
            .run(url::post).build()
            .run(OkHttpClient()::newCall)
            .execute()
    }

    // 탑승했을 때의 처리 루틴이다
    private fun onBroadLaunch(bus: BusNoList) = CoroutineScope(Dispatchers.IO).launch {

        fun completeProcess(result: Boolean) = with((activity as MenuActivity)) {
            sharedLastBusNumber = bus.busNo
            inFlag = result
            transFlag = result
            paymentFlag = result
        }

        val successAlert = AlertDialog.Builder((activity as MenuActivity)).apply {
            setTitle("결제 완료")
            setMessage("탑승 처리가 완료되었습니다.")
            setPositiveButton("확인", null)
        }

        val failedAlert = AlertDialog.Builder((activity as MenuActivity)).apply {
            setTitle("잔액 부족")
            setMessage("잔액이 부족합니다.\n지갑을 충전해 주세요.")
            setPositiveButton("확인", null)
        }

        Log.d(TAG, "승차 루틴을 시작합니다.")

        (activity as MenuActivity).run {
            val complete = if (transFlag) ifTransfer(bus) else ifNotTransfer(bus)

            if (complete) {
                Log.d(TAG, "승차 처리가 완료되었습니다.")
                sendSignal()

                launch(Dispatchers.Main) {
                    successAlert.show()
                    profileBusNum.apply {
                        text = sharedLastBusNumber
                        textAppearance = R.style.ProfileBusNumStyle
                    }
                }
                completeProcess(complete)
            } else {
                Log.d(TAG, "금액 부족으로 인한 승차 실패")

                launch(Dispatchers.Main) { failedAlert.show() }
                btnRangeEnd.callOnClick()
                completeProcess(complete)
            }
        }
    }

    private fun payAndRenewalLaunch(payJob: Job) = CoroutineScope(Dispatchers.IO).launch {
        payJob.join()
        balanceRenewalLaunch()
    }

    // 환승이 아닌 경우의 루틴
    private fun ifNotTransfer(bus: BusNoList) =
        when ((activity as MenuActivity).sharedUserBalance.toInt() < PRICE_NORMAL) {
            // 잔액이 부족할 때
            true -> {
                Log.d(TAG, "ifNotTransfer() 실행"); false
            }

            // 잔액이 충분할 때
            false -> {
                Log.d(TAG, "ifNotTransfer() 실행")
                if ("급행" in bus.busNo) payLaunch(-PRICE_EXPRESS).run(this::payAndRenewalLaunch)
                else payLaunch(-PRICE_NORMAL).run(this::payAndRenewalLaunch)
                true
            }
        }


    // 환승일 경우의 루틴
    private fun ifTransfer(bus: BusNoList) =
        when (bus.busNo == (activity as MenuActivity).sharedLastBusNumber) { // 직전 버스와 현재 버스의 번호 비교
            // 같을 때 : 환승 불가능
            true -> {
                Log.d(TAG, "이전 버스와 같은 버스입니다."); ifNotTransfer(bus)
            }
            // 다를 때 : 환승 가능
            false -> if ("급행" in (activity as MenuActivity).sharedLastBusNumber!!) {
                getOffCnt = 0; true
            } else ifNormalBus(bus)

        }

    // 직전에 탔던 버스가 일반일 경우
    private fun ifNormalBus(bus: BusNoList) = if ("급행" in bus.busNo) { // 현재 탑승 버스가 급행일때
        when ((activity as MenuActivity).sharedUserBalance.toInt() < PRICE_EXPRESS - PRICE_NORMAL) {
            // 차액이 부족할 경우
            true -> {
                Log.d(TAG, "차액이 부족합니다"); false
            }

            // 차액이 충분할 경우
            false -> {
                payLaunch(-(PRICE_EXPRESS - PRICE_NORMAL)).run(this::payAndRenewalLaunch);
                getOffCnt = 0
                true
            }
        }
    } else true

    private fun payLaunch(money: Int) = CoroutineScope(Dispatchers.IO).launch {
        // 결제 처리 루틴
        val url = Request.Builder().url("http://13.125.170.17/CashRecharge.php")
        FormBody.Builder().apply {
            add("user_email", FirebaseAuth.getInstance().currentUser?.email!!)
            add("user_cash", money.toString())
        }.build()
            .run(url::post).build()
            .run(OkHttpClient()::newCall)
            .execute()
    }

    override fun onResume() {
        super.onResume()
        balanceRenewalLaunch()
    }

    // 버스의 단말기로 신호 송신
    private fun sendSignal() = CoroutineScope(Dispatchers.IO).launch {
        val url = Request.Builder().url("http://192.168.43.190/teest.php")

        FormBody.Builder().apply {
            add("parameter1", "data1")
            add("parameter2", "data2")
        }.build()
            .run(url::post).build()
            .run(OkHttpClient()::newCall).execute()
    }

    private fun balanceRenewalLaunch() = CoroutineScope(Dispatchers.IO).launch {
        val url = Request.Builder().url(UserInfoList.URL)

        FormBody.Builder().apply { add("user_email", "${user?.email}") }.build()
            .run(url::post).build()
            .run(OkHttpClient()::newCall)
            .enqueue(BalanceRenewalCallback())
    }

    // 잔액 갱신에 대한 응답
    inner class BalanceRenewalCallback : Callback {
        override fun onFailure(call: Call, e: IOException) = runOnUiThread { toast("잔액 갱신에 실패").show() }

        override fun onResponse(call: Call, response: Response) {
            val items = UserInfoList.parseJSON(response)
            (activity as MenuActivity).sharedUserBalance = items.first().userCash
            runOnUiThread { profileBalT.text = (activity as MenuActivity).sharedUserBalance }
        }
    }

}


