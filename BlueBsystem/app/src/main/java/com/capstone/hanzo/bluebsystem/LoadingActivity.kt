package com.capstone.hanzo.bluebsystem

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Window
import com.capstone.hanzo.bluebsystem.room.InfoDB
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Response
import org.jetbrains.anko.*
import java.io.IOException

class LoadingActivity : AppCompatActivity(), AnkoLogger {

    private lateinit var database: InfoDB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_loading)

        database = InfoDB.getInstance(this)!!
        loading()
    }

    private fun loading() {
        Handler().postDelayed({
            infoInit()
            startActivity(intentFor<LoginActivity>().clearTop())
            finish()
        }, 500)
    }

    override fun onBackPressed() {
        TODO("로딩 화면에서는 뒤로가기 버튼의 작동을 막는다.")
    }

    private fun infoInit() = GlobalScope.launch {
        val cntBus = database.busDao().getCount()
        val cntPlat = database.platformDao().getCount()

        if (cntBus == 0) {
            launch {
                val request = makeRequest(BusNoList.URL)
                OkHttpClient().newCall(request).enqueue(BusInitCallback())
            }
        }
        if (cntPlat == 0) {
            launch {
                val request = makeRequest(PlatformArvlInfoList.URL)
                OkHttpClient().newCall(request).enqueue(PlatformInitCallback())
            }
        }

        launch {
            val request = makeRequest(UserInfoList.URL)
            OkHttpClient().newCall(request).enqueue(UserInitCallback())
        }
    }

    inner class BusInitCallback : Callback {
        override fun onFailure(call: Call, e: IOException) {
            CoroutineScope(Dispatchers.Main).launch {
                toast("인터넷 연결 실패").show()
            }
        }

        override fun onResponse(call: Call, response: Response) {
            val list = BusNoList.parseJSON(response)
            list.forEach {
                database.busDao().insert(it)
            }
        }
    }

    inner class PlatformInitCallback : Callback {
        override fun onFailure(call: Call, e: IOException) {
            CoroutineScope(Dispatchers.Main).launch {
                toast("인터넷 연결 실패").show()
            }
        }

        override fun onResponse(call: Call, response: Response) {
            val list = PlatformArvlInfoList.parseJSON(response)
            list.forEach {
                database.platformDao().insert(it)
            }
        }
    }

    inner class UserInitCallback : Callback {
        override fun onFailure(call: Call, e: IOException) {
            CoroutineScope(Dispatchers.Main).launch {
                toast("인터넷 연결 실패").show()
            }
        }

        override fun onResponse(call: Call, response: Response) {
            val list = UserInfoList.parseJSON(response)
            list.forEach {
                database.userDao().insertUser(it)
            }
        }
    }

    override fun onDestroy() {
        InfoDB.destroyInstance()
        super.onDestroy()
    }
}
