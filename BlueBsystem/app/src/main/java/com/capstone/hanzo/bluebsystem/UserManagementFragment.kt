package com.capstone.hanzo.bluebsystem


import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserInfo
import kotlinx.android.synthetic.main.activity_menu.*
import kotlinx.coroutines.*
import okhttp3.*
import org.altbeacon.beacon.Beacon
import org.altbeacon.beacon.BeaconConsumer
import org.altbeacon.beacon.Region
import org.jetbrains.anko.*
import org.jetbrains.anko.support.v4.intentFor
import org.jetbrains.anko.support.v4.runOnUiThread
import org.jetbrains.anko.support.v4.toast
import java.io.IOException
import java.lang.Exception
import java.text.Normalizer


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 *
 */
class UserManagementFragment : Fragment(), AnkoLogger {

    companion object {
        const val TAG = "비콘"
    }

    private lateinit var profileNm: TextView
    private lateinit var profileEm: TextView
    private lateinit var profileBalT: TextView
    private lateinit var btnRangeStart: Button
    private lateinit var btnRangeEnd: Button
    private val user = FirebaseAuth.getInstance().currentUser


    var rangingJob: Job? = null
    private var stop = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_user_management, container, false).apply {
            profileBalT = findViewById(R.id.profileBalT)
            profileEm = findViewById(R.id.profileEm)
            profileNm = findViewById(R.id.profileNm)
            btnRangeEnd = findViewById(R.id.btnRangeEnd)
            btnRangeStart = findViewById(R.id.btnRangeStart)
        }

        val controller = activity as MenuActivity

        user?.let {
            profileNm.text = it.displayName
            profileEm.text = it.email
        }

        btnRangeStart.setOnClickListener {
            Log.d(TAG, "btnRangeStart 이벤트 발생")

            if (controller.sharedUserBalance.toInt() < 1250) {
                toast("잔액이 부족합니다.").show()
            } else {
                /**
                 *  BeaconConsumer 객체의 경우 미리 만들어 사용하는 경우
                 *  감지 기능을 종료했다가 다시 켜지지 않는다
                 *  그래서 버튼을 누를 때 마다 객체가 새로 만들어지도록 설정함
                 */
                controller.beaconManager.bind(UserBeaconConsumer())
            }
        }

        btnRangeEnd.setOnClickListener {
            Log.d(TAG, "btnRangeEnd 이벤트 발생")
            stop = true
            rangingJob?.let {
                controller.run {
                    beaconManager.stopRangingBeaconsInRegion(Region("blueb",null,null,null))
                    sharedLastBusNumber = null
                    inFlag = false
                    transFlag = false
                    paymentFlag = false
                }
                // TODO : 플래그가 데이터베이스에 전달되지 않는다.(2019.04.10)
                updateUserInfoLaunch(null)
            } ?: toast("종료할 서비스가 없습니다").show()
        }
        return view
    }

    inner class UserBeaconConsumer : BeaconConsumer {
        val context = activity as MenuActivity
        override fun getApplicationContext(): Context = context

        override fun unbindService(p0: ServiceConnection?) {
            context.unbindService(p0)
        }

        override fun bindService(p0: Intent?, p1: ServiceConnection?, p2: Int): Boolean =
            context.bindService(p0, p1, p2)

        override fun onBeaconServiceConnect() {
            rangingJob = startRangingBeacon()
        }
    }

    /**
     * 사용자가 버튼을 누르면 비콘 감지를 시작한다.
     * TODO : 환승과 하차 기능을 테스트 해야함 (2019.04.10)
     */

    fun startRangingBeacon() = CoroutineScope(Dispatchers.IO).launch {
        Log.d(TAG, "비콘 감지를 시작합니다")
        var cnt = 0
        stop = false
        (activity as MenuActivity).run {
            beaconManager.run {
                try {
                    if(!stop) {
                        startRangingBeaconsInRegion(Region("blueb", null, null, null))
                    }
                } catch (e: Exception) {
                }
                removeAllRangeNotifiers()
                addRangeNotifier { beacons, region ->
                    if (cnt == 12) {
                        // 비콘 신호가 12초 동안 감지 되지 않은 경우 비콘 감지 서비스를 강제 종료한다
                        btnRangeEnd.performClick()
                        stop = true
                        Log.d(TAG, "비콘 감지가 종료됩니다")
                    }
                    if (beacons.isNotEmpty()) {
                        Log.d(TAG, "비콘이 감지 되었습니다.")
                        // 비콘이 감지되는 경우
                        beacons.forEach { b ->
                            cnt = 0
                            if (!inFlag) {
                                // 탑승 하지 않았을 경우의 루틴
                                uuidCheck(b)
                            }
                        }
                    } else {
                        Log.d(TAG, "비콘이 감지 되지 않습니다.")
                        // 비콘이 감지되지 않는 경우 계수를 증가 시킴
                        cnt++
                    }
                }

            }
        }
    }

    // 수신 받은 비콘과 데이터베이스에 저장된 버스의 uuid를 비교한다
    private fun uuidCheck(b: Beacon) = CoroutineScope(Dispatchers.IO)
        .launch {
            Log.d(TAG, "uuid를 체크합니다")
            val bus: BusNoList? = (activity as MenuActivity).database.busDao().checkUUID(b.id1.toString())
            bus?.let {
                val job = onBroadLaunch(bus)
                updateUserInfoLaunch(job)
            }
        }

    // 사용자 정보를 수정한다
    private fun updateUserInfoLaunch(job: Job?) = CoroutineScope(Dispatchers.IO).launch {
        Log.d(TAG, "승차 처리를 대기중입니다.")
        job?.join()
        Log.d(TAG, "사용자 정보 수정 시작합니다.")

        val url = Request.Builder().url(UserInfoList.URL)
        val body = FormBody.Builder().apply {
            add("user_email", FirebaseAuth.getInstance().currentUser?.email!!)
            (activity as MenuActivity).run {
                add("user_transfer", transFlag.toString())
                add("user_in", inFlag.toString())
                add("last_bus_no", sharedLastBusNumber.toString())
            }
        }.build()

        val request = url.post(body).build()

        OkHttpClient().newCall(request).execute()
    }

    // 탑승했을 때의 처리 루틴이다
    private fun onBroadLaunch(bus: BusNoList) = CoroutineScope(Dispatchers.IO)
        .launch {
            Log.d(TAG, "승차 루틴을 시작합니다.")
            (activity as MenuActivity).run {
                when (transFlag) {
                    true -> {
                        ifTransfer(bus)
                    }
                    false -> {
                        ifNotTransfer(bus)
                    }
                }
                Log.d(TAG, "승차 처리가 완료되었습니다.")

                launch(Dispatchers.Main) {
                    AlertDialog.Builder(activity as MenuActivity).apply {
                        setTitle("결제 완료")
                        setMessage("탑승 처리가 완료되었습니다.")
                        setPositiveButton("확인", null)
                    }.show()
                }

                sharedLastBusNumber = bus.busNo
                inFlag = true
                transFlag = true
                paymentFlag = true
            }
        }

    // 환승이 아닌 경우의 루틴
    private fun ifNotTransfer(bus: BusNoList) {
        Log.d(TAG, "환승이 아닐 경우의 처리입니다")
        when (bus.busNo.contains("급행")) {
            true -> {
                Log.d(TAG, "급행입니다.")

                val payJob = payLaunch(-1450)

                CoroutineScope(Dispatchers.IO).launch {
                    payJob.join()
                    balanceRenewalLaunch()
                }
            }
            false -> {
                // 일반 버스 결제 실행
                Log.d(TAG, "일반버스입니다.")

                val payJob = payLaunch(-1250)
                CoroutineScope(Dispatchers.IO).launch {
                    payJob.join()
                    balanceRenewalLaunch()
                }
            }
        }
    }

    // 환승일 경우의 루틴
    private fun ifTransfer(bus: BusNoList) {
        Log.d(TAG, "환승일 경우입니다.")

        when ((activity as MenuActivity).sharedLastBusNumber?.contains("급행")) {
            // 마지막으로 탄 버스가 급행일 때
            true -> {
                Log.d(TAG, "차액을 지불하지 않아도 됩니다.")
            }
            // 마지막으로 탄 버스가 일반일 때
            false -> {
                ifNotNormalBus(bus)
            }
            else -> {
            }
        }
    }

    // 일반 버스가 아닐 경우의 루틴(급행버스일때)
    private fun ifNotNormalBus(bus: BusNoList) {
        Log.d(TAG, "급행버스가 아닐때입니다.")
        when (bus.busNo.contains("급행")) {
            true -> {
                // 일반에서 급행으로 환승하는 경우에는 차액을 더 지불해야함
                if ((activity as MenuActivity).sharedUserBalance.toInt() < 450) {
                    // 차액 지불에 실패 할 경우 비콘 감지를 강제 종료
                    btnRangeEnd.callOnClick()
                    CoroutineScope(Dispatchers.Main).launch {
                        AlertDialog.Builder(activity as MenuActivity).apply {
                            setTitle("환승 불가!")
                            setMessage("잔액이 부족해여 환승이 불가능합니다.\n충전을 해주세요")
                            setPositiveButton("확인", null)
                        }.show()
                    }
                    return
                } else {
                    // 차액을 지불한다
                    val job = payLaunch(-450)
                    CoroutineScope(Dispatchers.IO).launch {
                        job.join()
                        balanceRenewalLaunch()
                    }
                }
            }
            false -> {
            }
        }
    }


    // 결제 처리 루틴
    fun payLaunch(money: Int) = CoroutineScope(Dispatchers.IO).launch {
        val url = Request.Builder().url("http://13.125.170.17/CashRecharge.php")
        val body = FormBody.Builder().apply {
            add("user_email", FirebaseAuth.getInstance().currentUser?.email!!)
            add("user_cash", money.toString())
        }.build()
        OkHttpClient().newCall(url.post(body).build()).execute()
    }

    override fun onResume() {
        super.onResume()
        balanceRenewalLaunch()
    }

    /**
     * 2019-04-03 runBlocking() 함수를 사용하지 않고 메인 쓰레드에 종속되는 GlobalScope 코루틴 사용
     * 탭을 전환 할때 마다 발생하던 balanceRenewalLaunch() 호출에 대한 에러는 사라짐
     */
    private fun balanceRenewalLaunch() = CoroutineScope(Dispatchers.IO)
        .launch {
            val url = Request.Builder().url(UserInfoList.URL)
            val body = FormBody.Builder().apply {
                add("user_email", "${user?.email}")
            }.build()

            val request = url.post(body).build()
            OkHttpClient().newCall(request).enqueue(BalanceRenewalCallback())
        }

    // 잔액 갱신에 대한 응답
    inner class BalanceRenewalCallback : Callback {
        override fun onFailure(call: Call, e: IOException) {
            CoroutineScope(Dispatchers.Main).launch {
                toast("잔액 갱신에 실패").show()
            }
        }

        override fun onResponse(call: Call, response: Response) {
            val items: ArrayList<UserInfoList> = UserInfoList.parseJSON(response)
            (activity as MenuActivity).sharedUserBalance = items.first().userCash
            runOnUiThread { profileBalT.text = (activity as MenuActivity).sharedUserBalance }
        }
    }
}
