package com.capstone.hanzo.bluebsystem.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import com.capstone.hanzo.bluebsystem.R
import com.capstone.hanzo.bluebsystem.RouteInfoAdapter
import kotlinx.android.synthetic.main.list_bus_route_info.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.*
import org.jetbrains.anko.runOnUiThread
import org.jetbrains.anko.toast
import org.w3c.dom.Element
import java.io.IOException
import javax.xml.parsers.DocumentBuilderFactory

class RouteDialog(context: Context, val number: String, val id: String) : Dialog(context) {

    private val listAdapter = RouteInfoAdapter()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.list_bus_route_info)
        routeInfoInit()
        val dm = context.resources.displayMetrics

        window?.attributes = WindowManager.LayoutParams().apply {
            copyFrom(this@RouteDialog.window?.attributes)
            width = dm.widthPixels / 2 + 200
            height = dm.heightPixels / 2 + 300
        }

        dialogTitleBus.text = "${number}"
    }

    private fun routeInfoInit() = CoroutineScope(Dispatchers.IO).launch {
        val client = OkHttpClient()
        val address =
            "http://openapi.tago.go.kr/openapi/service/BusRouteInfoInqireService/getRouteAcctoThrghSttnList?" +
                    "ServiceKey=SQYXLo2JYmzB7pvVorqfLma6NF38tdCUkcJ0Pn0pXJC0G4fPu%2F7xt%2Bqpoq%2F1qkiBw1krMnqNMNqxcLs0H3B7%2Bw%3D%3D" +
                    "&cityCode=22" +
                    "&routeId=${id.trim()}" +
                    "&numOfRows=100"
        val request = Request.Builder().url(address).build()
        client.newCall(request).enqueue(RouteInfoCallback())
    }


    inner class RouteInfoCallback : Callback {
        private val NODE_ID = "nodeid"
        private val NODE_NAME = "nodenm"

        override fun onFailure(call: Call, e: IOException) {
            context.runOnUiThread { toast("노선 정보를 불러올 수 없습니다.") }
        }

        private fun getTextContentByTagName(tag: String, node: Element, idx: Int = 0): String {
            val item = tag.run(node::getElementsByTagName).item(idx) as Element
            return item.textContent
        }

        override fun onResponse(call: Call, response: Response) {

            val stream = response.body()?.byteStream()
            val factory = DocumentBuilderFactory.newInstance()
            val builder = factory.newDocumentBuilder()
            val doc = builder.parse(stream)
            val root = doc.documentElement

            val itemList = root.getElementsByTagName("item")

            listAdapter.clear()


            for (idx in 0 until itemList.length) {
                val node = itemList.item(idx) as Element
                val id = getTextContentByTagName(NODE_ID, node)
                val name = getTextContentByTagName(NODE_NAME, node)
                listAdapter.addItem(id, name)
            }


            CoroutineScope(Dispatchers.Main).launch { dialogListBus.adapter = listAdapter }
        }

    }
}