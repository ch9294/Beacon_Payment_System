package com.capstone.hanzo.bluebsystem

import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.*
import org.jetbrains.anko.find
import org.jetbrains.anko.layoutInflater

class PlatformListAdapter() : BaseAdapter(), Filterable {
    // 리스트뷰에 표현할 원본데이터
    private val itemList: ArrayList<PlatformArvlInfoList> = arrayListOf()
    // 필터링 된 데이터
    private var filteredItemList = itemList

    private lateinit var id: TextView
    private lateinit var number: TextView
    private lateinit var name: TextView
    private var listFilter: Filter? = null

    override fun getCount() = filteredItemList.size

    override fun getItem(position: Int) = filteredItemList[position]

    override fun getItemId(position: Int) = position.toLong()

    fun addItem(list: List<PlatformArvlInfoList>) = itemList.addAll(list)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view = convertView
        val context = parent?.context
        val item = filteredItemList[position]

        if (view == null) view = context?.layoutInflater?.inflate(R.layout.list_platform_reservation, parent, false)


        view?.let {
            id = it.find(R.id.listPlatId)
            number = it.find(R.id.listPlatNo)
            name = it.find(R.id.listPlatName)
        }

        id.text = item.platId
        number.text = item.platNo
        name.text = item.platName

        return view!!
    }

    override fun getFilter() = listFilter.takeIf { it == null }?.apply { ListFilter() } ?: ListFilter()

    private inner class ListFilter : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val result = FilterResults()

            if (constraint.isNullOrEmpty()) {
                result.values = itemList
                result.count = itemList.size
            } else {
                val itemList2 = ArrayList<PlatformArvlInfoList>()

                itemList.forEach {
                    if (constraint.toString().toUpperCase() in it.platName.toUpperCase() || constraint.toString().toUpperCase() in it.platNo.toUpperCase())
                        itemList2.add(it)
                }
                result.values = itemList2
                result.count = itemList2.size
            }
            return result
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            filteredItemList = results?.values as ArrayList<PlatformArvlInfoList>
            if (results.count > 0) notifyDataSetChanged() else notifyDataSetInvalidated()
        }
    }
}

class NumberListAdapter : BaseAdapter(), Filterable {
    // 리스트뷰에 표현할 원본데이터
    private val itemList: ArrayList<BusNoList> = arrayListOf()
    // 필터링 된 데이터
    private var filteredItemList = itemList

    private lateinit var number: TextView
    private lateinit var start: TextView
    private lateinit var end: TextView
    private var listFilter: Filter? = null

    override fun getCount() = filteredItemList.size

    override fun getItem(position: Int) = filteredItemList[position]

    override fun getItemId(position: Int) = position.toLong()

    fun addItem(list: List<BusNoList>) = itemList.addAll(list)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view = convertView
        val context = parent?.context

        if (view == null) view = context?.layoutInflater?.inflate(R.layout.list_bus_reservation, parent, false)

        view?.let {
            number = it.find(R.id.listBusNum)
            start = it.find(R.id.listBusStart)
            end = it.find(R.id.listBusEnd)
        }

        number.text = filteredItemList[position].busNo
        start.text = filteredItemList[position].start
        end.text = filteredItemList[position].end

        return view!!
    }

    override fun getFilter() = listFilter.takeIf { it == null }?.apply { ListFilter() } ?: ListFilter()


    private inner class ListFilter : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val result = FilterResults()

            if (constraint.isNullOrEmpty()) {
                result.values = itemList
                result.count = itemList.size
            } else {
                val itemList2 = ArrayList<BusNoList>()
                itemList.forEach { if (constraint.toString().toUpperCase() in it.busNo.toUpperCase()) itemList2.add(it) }
                result.values = itemList2
                result.count = itemList2.size
            }
            return result
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            filteredItemList = results?.values as ArrayList<BusNoList>
            if (results.count > 0) notifyDataSetChanged() else notifyDataSetInvalidated()
        }
    }
}

class PlatformArvlInfoListAdapter : BaseAdapter() {
    private val itemList = ArrayList<PlatformArvlInfoList2>()
    private lateinit var number: TextView
    private lateinit var time: TextView
    private lateinit var type: TextView
    private lateinit var image: ImageView
    private lateinit var count: TextView

    override fun getCount() = itemList.size

    override fun getItem(position: Int) = itemList[position]

    override fun getItemId(position: Int) = position.toLong()

    fun addItem(number: String, time: String, type: String, prevCnt: String) =
        PlatformArvlInfoList2(number, time, type, prevCnt).run(itemList::add)

    fun clear() = itemList.clear()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view = convertView
        val arrtime = itemList[position].time.toInt()
        val context = parent?.context

        if (view == null) view = context?.layoutInflater?.inflate(R.layout.bus_arrive_info, parent, false)

        view?.let {
            image = it.find(R.id.busImg)
            number = it.find(R.id.busNum)
            time = it.find(R.id.arrtime)
            type = it.find(R.id.type)
            count = it.find(R.id.prevCount)
        }

        type.text = itemList[position].type

        number.text = itemList[position].number

        if (number.text.contains("급행")) {
            image.setImageResource(R.mipmap.express_bus_image)
        } else {
            image.setImageResource(R.mipmap.normal_bus_image)
        }

        time.apply {
            text = "${arrtime}분"
            if (arrtime < 5) setTextColor(Color.RED) else setTextColor(Color.BLUE)
        }

        count.run {
            text = itemList[position].prevCnt
            val cnt = text.toString().toInt()

            if (cnt > 10) {
                text = "10 정거장 이상 남음"
            } else if (cnt == 2) {
                text = "전전"
            } else if (cnt == 1) {
                text = "전"
            }
        }

        return view!!
    }


}

//        if (listFilter == null) listFilter = ListFilter()
//        return listFilter!!