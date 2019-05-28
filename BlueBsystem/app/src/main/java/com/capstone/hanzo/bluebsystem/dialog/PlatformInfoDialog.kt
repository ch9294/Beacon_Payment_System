package com.capstone.hanzo.bluebsystem.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.WindowManager
import com.capstone.hanzo.bluebsystem.PlatformArvlInfoListAdapter
import com.capstone.hanzo.bluebsystem.R
import com.capstone.hanzo.bluebsystem.makeRequest
import kotlinx.android.synthetic.main.dialog_plaform_info.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Response
import org.jetbrains.anko.runOnUiThread
import org.jetbrains.anko.toast
import org.w3c.dom.Element
import java.io.IOException
import javax.xml.parsers.DocumentBuilderFactory

class PlatformInfoDialog(context: Context, val name: String, val id: String) : Dialog(context) {

    val adapter = PlatformArvlInfoListAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_plaform_info)
        dialogTitlePlat.text = name
        platformInfoListInit()

        // 현재 어플리케이션이 작동하고 있는 스마트폰의 디스플레이 실제 크기 정보
        val dm = context.resources.displayMetrics

        // 다이얼로그의 크기를 설정함
        window?.attributes = WindowManager.LayoutParams().apply {
            copyFrom(this@PlatformInfoDialog.window?.attributes)
            width = dm.widthPixels / 2 + 200
            height = dm.heightPixels / 2 + 500
        }

        dialogRefreshBtn.setOnClickListener {
            platformInfoListInit()
        }
    }


    private fun platformInfoListInit() = CoroutineScope(Dispatchers.IO).launch {
        val request = makeRequest(
            "http://openapi.tago.go.kr/openapi/service/ArvlInfoInqireService/getSttnAcctoArvlPrearngeInfoList?" +
                    "ServiceKey=SQYXLo2JYmzB7pvVorqfLma6NF38tdCUkcJ0Pn0pXJC0G4fPu%2F7xt%2Bqpoq%2F1qkiBw1krMnqNMNqxcLs0H3B7%2Bw%3D%3D" +
                    "&cityCode=22" +
                    "&nodeId=${id.trim()}" +
                    "&numOfRows=20"
        )
        OkHttpClient().newCall(request).enqueue(PlatformInfoCallback())
    }

    inner class PlatformInfoCallback : Callback {

        val PREV_CNT = "arrprevstationcnt"
        val ARR_TIME = "arrtime"
        val ROUTE_NO = "routeno"
        val VEHICLE_TYPE = "vehicletp"
        val ITEM = "item"

        private fun getTextContentByTagName(tag: String, node: Element, idx: Int = 0): String {
            val item = tag.run(node::getElementsByTagName).item(idx) as Element
            return item.textContent
        }

        override fun onFailure(call: Call, e: IOException) = context.runOnUiThread { toast("연결 실패") }

        override fun onResponse(call: Call, response: Response) {
            val stream = response.body()?.byteStream()
            val factory = DocumentBuilderFactory.newInstance()
            val builder = factory.newDocumentBuilder()
            val doc = builder.parse(stream)
            val root = doc.documentElement

            val itemList = ITEM.run(root::getElementsByTagName)

            // 어댑터 내의 내용을 모두 제거한다.(리스트뷰에 새로운 내용을 갱신하기 위해)
            adapter.clear()

            for (idx in 0 until itemList.length) {
                val node = itemList.item(idx) as Element
                val time = getTextContentByTagName(ARR_TIME, node).toInt().div(60).toString()
                val busNum = getTextContentByTagName(ROUTE_NO, node)
                val type = getTextContentByTagName(VEHICLE_TYPE, node)
                val prevCnt = getTextContentByTagName(PREV_CNT, node)
                adapter.addItem(busNum, time, type, prevCnt)
            }
            CoroutineScope(Dispatchers.Main).launch { dialogListPlat.adapter = adapter }
        }
    }
}
