package com.capstone.hanzo.bluebsystem


import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.icu.util.MeasureUnit
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.*
import org.jetbrains.anko.AlertBuilder
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.find
import org.jetbrains.anko.info
import org.jetbrains.anko.support.v4.runOnUiThread
import org.jetbrains.anko.support.v4.toast
import org.w3c.dom.Element
import org.w3c.dom.Text
import java.io.IOException
import java.net.URL
import javax.xml.parsers.DocumentBuilderFactory


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 */

class PlatformInfoFragment : Fragment(), AnkoLogger, SwipeRefreshLayout.OnRefreshListener {
    private lateinit var PI_ListView: ListView
    private lateinit var PI_listPlatName: TextView
    private lateinit var PI_swipe: SwipeRefreshLayout
    private val listAdapter = PlatformArvlInfoListAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_platform_info2, container, false).apply {
            PI_ListView = find(R.id.PI_ListView2)
            PI_listPlatName = find(R.id.PI_listPlatName2)
            PI_swipe = find(R.id.listSwipe)
        }

        val ctrl = activity as MenuActivity
        platformInfoListInit()

        PI_swipe.apply {
            setOnRefreshListener(this@PlatformInfoFragment)
        }
        PI_listPlatName.apply {
            text = ctrl.sharedPlatformName
        }

        PI_ListView.apply {
            onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
                val v = parent?.getItemAtPosition(position) as PlatformArvlInfoList2
            }
        }
        return view
    }


    // 스와이프 시 새로고침이 되도록 한다
    override fun onRefresh() {
        PI_ListView.run {
            postDelayed({
                toast("새로고침").show()
//                PlatFormInfoThread().start()
                platformInfoListInit()
            }, 500)
            PI_swipe.isRefreshing = false
        }
    }



    fun platformInfoListInit() = GlobalScope.launch {
        val request = makeRequest("http://openapi.tago.go.kr/openapi/service/ArvlInfoInqireService/getSttnAcctoArvlPrearngeInfoList?" +
                    "ServiceKey=SQYXLo2JYmzB7pvVorqfLma6NF38tdCUkcJ0Pn0pXJC0G4fPu%2F7xt%2Bqpoq%2F1qkiBw1krMnqNMNqxcLs0H3B7%2Bw%3D%3D" +
                    "&cityCode=22" +
                    "&nodeId=${(activity as MenuActivity).sharedPlatformId}" +
                    "&numOfRows=20")

        OkHttpClient().newCall(request).enqueue(PlatformInfoCallback())
    }

    inner class PlatformInfoCallback : Callback {
        override fun onFailure(call: Call, e: IOException) = runOnUiThread { toast("연결 실패").show() }

        override fun onResponse(call: Call, response: Response) {
            val stream = response.body()?.byteStream()
            val factory = DocumentBuilderFactory.newInstance()
            val builder = factory.newDocumentBuilder()
            val doc = builder.parse(stream)
            val root = doc.documentElement

            val itemList = root.getElementsByTagName("item")

            // 어댑터 내의 내용을 모두 제거한다.(리스트뷰에 새로운 내용을 갱신하기 위해)
            listAdapter.clear()

            for (idx in 0 until itemList.length) {
                val node = itemList.item(idx) as Element
                val time = (((node.getElementsByTagName("arrtime").item(0) as Element).textContent).toInt() / 60).toString()
                val busNum = (node.getElementsByTagName("routeno").item(0) as Element).textContent
                val type = (node.getElementsByTagName("vehicletp").item(0) as Element).textContent
                listAdapter.addItem(busNum, time, type)
            }

            runOnUiThread { PI_ListView.adapter = listAdapter }
        }
    }

}


//// 정류장의 도착 정보를 리스트뷰에 표시하기 위한 쓰레드
//inner class PlatFormInfoThread : Thread() {
//    override fun run() {
//        val stream = URL(
//            "http://openapi.tago.go.kr/openapi/service/ArvlInfoInqireService/getSttnAcctoArvlPrearngeInfoList?" +
//                    "ServiceKey=SQYXLo2JYmzB7pvVorqfLma6NF38tdCUkcJ0Pn0pXJC0G4fPu%2F7xt%2Bqpoq%2F1qkiBw1krMnqNMNqxcLs0H3B7%2Bw%3D%3D" +
//                    "&cityCode=22" +
//                    "&nodeId=${(activity as MenuActivity).sharedPlatformId}" +
//                    "&numOfRows=20"
//        ).openStream()
//
//        val factory = DocumentBuilderFactory.newInstance()
//        val builder = factory.newDocumentBuilder()
//        val doc = builder.parse(stream)
//        val root = doc.documentElement
//
//        val itemList = root.getElementsByTagName("item")
//
//        // 어댑터 내의 내용을 모두 제거한다.(리스트뷰에 새로운 내용을 갱신하기 위해)
//        listAdapter.clear()
//
//        for (idx in 0 until itemList.length) {
//            val node = itemList.item(idx) as Element
//            val time =
//                (((node.getElementsByTagName("arrtime").item(0) as Element).textContent).toInt() / 60).toString()
//            val busNum = (node.getElementsByTagName("routeno").item(0) as Element).textContent
//            val type = (node.getElementsByTagName("vehicletp").item(0) as Element).textContent
//            listAdapter.addItem(busNum, time, type)
//        }
//
//        runOnUiThread {
//            PI_ListView.adapter = listAdapter
//        }
//    }
//
//}

//    // 사용자의 예약 상태를 갱신하는 쓰레드
//    inner class UserBookingStateThread(val number: String) : Thread() {
//        override fun run() {
//            val url = Request.Builder().url("http://13.125.170.17/userBookingStateRenewal.php")
//            val body = FormBody.Builder().apply {
//                add("user_email", FirebaseAuth.getInstance().currentUser?.email!!)
//                add("balance_account", (activity as MenuActivity).sharedUserBalance)
//                add("busNumber", number)
//            }.build()
//            val request = url.post(body).build()
//            OkHttpClient().newCall(request).enqueue(UserBookingStateCallback())
//        }
//    }
//
//    inner class UserBookingStateCallback : Callback {
//        override fun onFailure(call: Call, e: IOException) {
//
//        }
//
//        override fun onResponse(call: Call, response: Response) {
//            val result = response.body()?.string()
//
//            when (result) {
//                "SUCCESS" -> {
//
//                }
//                "FAILED" -> {
//
//                }
//            }
//        }
//    }
//
//AlertDialog.Builder(ctrl).apply {
//    setTitle("${v.number}번 버스 예약하기")
//    setView(ctrl.layoutInflater.inflate(R.layout.reservation_alert, null).apply {
//        val expense: TextView = find(R.id.expenseAlert)
//        val balance: TextView = find(R.id.balanceAlert)
//        val msg: TextView = find(R.id.msgAlert)
//
//        expense.run {
//            when (v.number.contains("급행")) {
//                true -> text = 1650.toString()
//                false -> text = 1250.toString()
//            }
//        }
//
//        balance.run {
//            text = ctrl.sharedUserBalance
//        }
//
//        msg.run {
//            when (expense.text.toString().toInt() > balance.text.toString().toInt()) {
//                true -> {
//                    setTextColor(Color.RED)
//                    text = "잔액이 부족합니다"
//                }
//
//                false -> {
//                    setTextColor(Color.BLUE)
//                    text = "예약이 가능합니다"
//                }
//            }
//        }
//    })
//
//    val lisentner = DialogInterface.OnClickListener { dialog, which ->
//        //                        val alert = dialog as AlertDialog
////                        val text: TextView? = alert.findViewById(R.id.msgAlert)
//
//        when (which) {
//            DialogInterface.BUTTON_POSITIVE -> {
//                UserBookingStateThread(v.number).start()
//            }
//            DialogInterface.BUTTON_NEGATIVE -> {
//            }
//        }
//    }
//    setPositiveButton("예약", lisentner)
//    setNegativeButton("취소", lisentner)
//}.show()