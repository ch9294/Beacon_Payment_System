package com.capstone.hanzo.bluebsystem

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
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
            startActivity(intentFor<LoginActivity>())
            finish()
        }, 700)
    }

    override fun onBackPressed() {

    }

    private fun infoInit() = CoroutineScope(Dispatchers.IO).launch {
        val cntBus = database.busDao().getCount()
        val cntPlat = database.platformDao().getCount()
        queryInfoLaunch(BusNoList.URL)
        queryInfoLaunch(PlatformArvlInfoList.URL)
//        if (cntBus == 0) {
//            queryInfoLaunch(BusNoList.URL)
//        }
//        if (cntPlat == 0) {
//            queryInfoLaunch(PlatformArvlInfoList.URL)
//        }
    }

    private fun queryInfoLaunch(url: String) = CoroutineScope(Dispatchers.IO).launch {
        val request = makeRequest(url)
        if (url == BusNoList.URL) {
            OkHttpClient().newCall(request).enqueue(BusInitCallback())
        } else {
            OkHttpClient().newCall(request).enqueue(PlatformInitCallback())
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


    override fun onDestroy() {
        InfoDB.destroyInstance()
        super.onDestroy()
    }
}
