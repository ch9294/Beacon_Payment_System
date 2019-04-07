package com.capstone.hanzo.bluebsystem


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.*
import org.jetbrains.anko.*
import org.jetbrains.anko.support.v4.intentFor
import org.jetbrains.anko.support.v4.runOnUiThread
import org.jetbrains.anko.support.v4.toast
import java.io.IOException


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 *
 */
class UserManagementFragment : Fragment(), AnkoLogger {

    private lateinit var profileNm: TextView
    private lateinit var profileEm: TextView
    private lateinit var profileBalT: TextView
    private lateinit var btnSetAlarm: Button
    private lateinit var btnLogout: Button
    private val user = FirebaseAuth.getInstance().currentUser

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_user_management, container, false).apply {
            profileBalT = findViewById(R.id.profileBalT)
            profileEm = findViewById(R.id.profileEm)
            profileNm = findViewById(R.id.profileNm)
            btnLogout = findViewById(R.id.btnLogout)
            btnSetAlarm = findViewById(R.id.btnSetAlarm)
        }

        user?.let {
            profileNm.text = it.displayName
            profileEm.text = it.email
        }

        btnLogout.run {
            setOnClickListener {
                FirebaseAuth.getInstance().signOut()
                startActivity(intentFor<LoginActivity>().clearTask().newTask().clearTop())
            }
        }
        return view
    }


    override fun onResume() {
        super.onResume()
        balanceRenewalLaunch()
    }

    /**
     * 2019-04-03 runBlocking() 함수를 사용하지 않고 메인 쓰레드에 종속되는 GlobalScope 코루틴 사용
     * 탭을 전환 할때 마다 발생하던 balanceRenewalLaunch() 호출에 대한 에러는 사라짐
     */
    private fun balanceRenewalLaunch() = GlobalScope.launch {
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
            runOnUiThread { toast("잔액 갱신에 실패").show() }
        }

        override fun onResponse(call: Call, response: Response) {
            val items: ArrayList<UserInfoList> = UserInfoList.parseJSON(response)
            (activity as MenuActivity).sharedUserBalance = items.first().userCash
            runOnUiThread { profileBalT.text = (activity as MenuActivity).sharedUserBalance }
        }
    }
}
