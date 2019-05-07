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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
        val view = inflater
            .inflate(R.layout.fragment_platform_info2, container, false)
            .apply {
                PI_ListView = this.find(R.id.PI_ListView2)
                PI_listPlatName = find(R.id.PI_listPlatName2)
                PI_swipe = find(R.id.listSwipe)
            }

        val ctrl = activity as MenuActivity
        platformInfoListInit()

        PI_swipe.setOnRefreshListener(this)
        PI_listPlatName.text = ctrl.sharedPlatformName
        PI_ListView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            parent?.getItemAtPosition(position) as PlatformArvlInfoList2
        }

        return view
    }


    // 스와이프 시 새로고침이 되도록 한다
    override fun onRefresh() {
        PI_ListView.run {
            postDelayed({
                toast("새로고침").show()
                platformInfoListInit()
            }, 500)
            PI_swipe.isRefreshing = false
        }
    }


    fun platformInfoListInit() = CoroutineScope(Dispatchers.IO).launch {
        val request = makeRequest(
            "http://openapi.tago.go.kr/openapi/service/ArvlInfoInqireService/getSttnAcctoArvlPrearngeInfoList?" +
                    "ServiceKey=SQYXLo2JYmzB7pvVorqfLma6NF38tdCUkcJ0Pn0pXJC0G4fPu%2F7xt%2Bqpoq%2F1qkiBw1krMnqNMNqxcLs0H3B7%2Bw%3D%3D" +
                    "&cityCode=22" +
                    "&nodeId=${(activity as MenuActivity).sharedPlatformId}" +
                    "&numOfRows=20"
        )

        OkHttpClient().newCall(request).enqueue(PlatformInfoCallback())
    }

    inner class PlatformInfoCallback : Callback {

        val ARR_TIME = "arrtime"
        val ROUTE_NO = "routeno"
        val VEHICLE_TYPE = "vehicletp"
        val ITEM = "item"

        private fun getTextContentByTagName(tag: String, node: Element, idx: Int = 0): String {
            val item = tag.run(node::getElementsByTagName).item(idx) as Element
            return item.textContent
        }

        override fun onFailure(call: Call, e: IOException) = runOnUiThread { toast("연결 실패").show() }

        override fun onResponse(call: Call, response: Response) {
            val stream = response.body()?.byteStream()
            val factory = DocumentBuilderFactory.newInstance()
            val builder = factory.newDocumentBuilder()
            val doc = builder.parse(stream)
            val root = doc.documentElement

            val itemList = ITEM.run(root::getElementsByTagName)

            // 어댑터 내의 내용을 모두 제거한다.(리스트뷰에 새로운 내용을 갱신하기 위해)
            listAdapter.clear()

            for (idx in 0 until itemList.length) {
                val node = itemList.item(idx) as Element
                val time = getTextContentByTagName(ARR_TIME, node).toInt().div(60).toString()
                val busNum = getTextContentByTagName(ROUTE_NO, node)
                val type = getTextContentByTagName(VEHICLE_TYPE, node)
                listAdapter.addItem(busNum, time, type)
            }
            CoroutineScope(Dispatchers.Main).launch { PI_ListView.adapter = listAdapter }
        }
    }

}