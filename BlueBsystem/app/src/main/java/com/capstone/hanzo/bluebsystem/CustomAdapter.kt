package com.capstone.hanzo.bluebsystem

import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
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

    override fun getCount(): Int {
        return filteredItemList.size
    }

    override fun getItem(position: Int): Any {
        return filteredItemList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view = convertView
        val context = parent?.context

        if (view == null) {
            view = context?.layoutInflater?.inflate(R.layout.list_platform_reservation, parent, false)
        }

        view?.let {
            id = it.find(R.id.listPlatId)
            number = it.find(R.id.listPlatNo)
            name = it.find(R.id.listPlatName)
        }

        val item = filteredItemList[position]

        id.text = item.platId
        number.text = item.platNo
        name.text = item.platName

        return view!!
    }

    fun addItem(list:List<PlatformArvlInfoList>) {
        itemList.addAll(list)
    }

    override fun getFilter(): Filter {
        if (listFilter == null) {
            listFilter = ListFilter()
        }
        return listFilter!!
    }

    private inner class ListFilter : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val result = FilterResults()

            if (constraint == null || constraint.isEmpty()) {
                result.values = itemList
                result.count = itemList.size
            } else {
                val itemList2 = ArrayList<PlatformArvlInfoList>()

                for (item in itemList) {
                    if (item.platName.toUpperCase().contains(constraint.toString().toUpperCase()) || item.platNo.toUpperCase().contains(
                            constraint.toString().toUpperCase()
                        )
                    ) {
                        itemList2.add(item)
                    }
                }
                result.values = itemList2
                result.count = itemList2.size
            }
            return result
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            filteredItemList = results?.values as ArrayList<PlatformArvlInfoList>

            if (results.count > 0) {
                notifyDataSetChanged()
            } else {
                notifyDataSetInvalidated()
            }
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

    override fun getCount(): Int {
        return filteredItemList.size
    }

    override fun getItem(position: Int): Any {
        return filteredItemList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view = convertView
        val context = parent?.context

        if (view == null) {
            view = context?.layoutInflater?.inflate(R.layout.list_bus_reservation, parent, false)
        }

        view?.let {
            with(it) {
                number = find(R.id.listBusNum)
                start = find(R.id.listBusStart)
                end = find(R.id.listBusEnd)
            }
        }
        number.text = filteredItemList[position].busNo
        start.text = filteredItemList[position].start
        end.text = filteredItemList[position].end

        return view!!
    }

    fun addItem(list:List<BusNoList>) {
        itemList.addAll(list)
    }

    override fun getFilter(): Filter {
        if (listFilter == null) {
            listFilter = ListFilter()
        }
        return listFilter!!
    }

    private inner class ListFilter : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val result = FilterResults()

            if (constraint == null || constraint.isEmpty()) {
                result.values = itemList
                result.count = itemList.size
            } else {
                val itemList2 = ArrayList<BusNoList>()
                for (item in itemList) {
                    if (item.busNo.toUpperCase().contains(constraint.toString().toUpperCase())) {
                        itemList2.add(item)
                    }
                }
                result.values = itemList2
                result.count = itemList2.size
            }
            return result
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            filteredItemList = results?.values as ArrayList<BusNoList>

            if (results.count > 0) {
                notifyDataSetChanged()
            } else {
                notifyDataSetInvalidated()
            }
        }
    }
}

class PlatformArvlInfoListAdapter : BaseAdapter() {
    private val itemList = ArrayList<PlatformArvlInfoList2>()
    private lateinit var number: TextView
    private lateinit var time: TextView
    private lateinit var type: TextView

    override fun getCount(): Int {
        return itemList.size
    }

    override fun getItem(position: Int): Any {
        return itemList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view = convertView
        val context = parent?.context

        if (view == null) {
            view = context?.layoutInflater?.inflate(R.layout.list_platform_infomation, parent, false)
        }

        view?.let {
            with(it) {
                number = find(R.id.PI_listBusNum)
                time = find(R.id.PI_listArvlTime)
                type = find(R.id.PI_listBusType)
            }
        }

        number.text = itemList[position].number

        val arrtime = (itemList[position].time).toInt()

        time.apply {
            text = "${arrtime}분"
            if (arrtime <= 5) {
                setTextColor(Color.RED)
            } else {
                setTextColor(Color.BLUE)
            }
        }

        type.text = itemList[position].type

        return view!!
    }

    fun addItem(number: String, time: String, type: String) {
        val item = PlatformArvlInfoList2(number, time, type)
        itemList.add(item)
    }

    fun clear() {
        itemList.clear()
    }
}