package com.capstone.hanzo.bluebsystem


import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.jetbrains.anko.*
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.DialogInterface
import android.support.v7.app.AlertDialog
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.*
import org.jetbrains.anko.sdk27.coroutines.onItemClick
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.support.v4.runOnUiThread
import org.jetbrains.anko.support.v4.toast
import java.io.IOException


// TODO: 앱 설치 직후에는 리스트뷰에 정보가 표시되지 않음(2019.04.04) 해결 중..
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 *
 */
class ReservationBusFragment : Fragment(), AnkoLogger {
    private lateinit var RB_searchST: EditText
    private lateinit var RB_numSearch: TextView
    private lateinit var RB_stSearch: TextView
    private lateinit var RB_listView: ListView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_reservation_bus, container, false).apply {
            RB_searchST = find(R.id.RB_searchST)
            RB_numSearch = find(R.id.RB_numSearch)
            RB_stSearch = find(R.id.RB_stSearch)
            RB_listView = find(R.id.RB_listView)
        }

        // 액티비티 컨트롤러
        val controller = activity as MenuActivity

        // 가상 키보드 정보 불러오기
        val imm = with(controller) { getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager }

        RB_searchST.hint = "노선번호 검색"
        RB_listView.adapter = controller.busAdapter

        RB_numSearch.run {
            setTextColor(Color.parseColor("#FFFFFF"))
            setBackgroundColor(Color.parseColor("#000E2B"))
            setOnClickListener {

                RB_searchST.run {
                    setText("")
                    hint = "노선번호 검색"
                    setOnKeyListener { v, keyCode, event ->
                        when (keyCode) {
                            KeyEvent.KEYCODE_ENTER -> imm.hideSoftInputFromWindow(v.windowToken, 0)
                            else -> false
                        }
                    }
                    addTextChangedListener(object : TextWatcher {
                        override fun afterTextChanged(s: Editable?) {
                            val filterText = s.toString()
                            if (filterText.isEmpty()) {
                                RB_listView.clearTextFilter()
                            } else {
                                RB_listView.setFilterText(filterText)
                            }
                        }

                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        }

                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                        }
                    })
                }

                RB_numSearch.run {
                    setTextColor(Color.parseColor("#FFFFFF"))
                    setBackgroundColor(Color.parseColor("#000E2B"))
                }

                RB_stSearch.run {
                    setTextColor(Color.parseColor("#000E2B"))
                    setBackgroundColor(Color.parseColor("#FFFFFF"))
                }

                RB_listView.run {
                    adapter = controller.busAdapter
                    onItemClickListener = object : AdapterView.OnItemClickListener {
                        override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                            val v = parent?.getItemAtPosition(position) as BusNoList
                        }
                    }
                }
            }
        }

        RB_stSearch.run {
            setOnClickListener {
                RB_searchST.run {
                    setText("")
                    hint = "정류장 검색"
                    setOnKeyListener { v, keyCode, event ->
                        when (keyCode) {
                            KeyEvent.KEYCODE_ENTER -> imm.hideSoftInputFromWindow(v.windowToken, 0)
                            else -> false
                        }
                    }
                    addTextChangedListener(object : TextWatcher {
                        override fun afterTextChanged(s: Editable?) {
                            val filterText = s.toString()
                            if (filterText.isEmpty()) {
                                RB_listView.clearTextFilter()
                            } else {
                                RB_listView.setFilterText(filterText)
                            }
                        }

                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                        }

                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

                        }
                    })
                }

                RB_stSearch.run {
                    setTextColor(Color.parseColor("#FFFFFF"))
                    setBackgroundColor(Color.parseColor("#000E2B"))
                }

                RB_numSearch.run {
                    setTextColor(Color.parseColor("#000E2B"))
                    setBackgroundColor(Color.parseColor("#FFFFFF"))
                }

                RB_listView.run {
                    adapter = controller.platAdapter
                    onItemClickListener = object : AdapterView.OnItemClickListener {
                        override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                            val v = parent?.getItemAtPosition(position) as PlatformArvlInfoList

                            with(controller) {
                                sharedPlatformId = v.platId
                                sharedPlatformName = v.platName
                            }

                            val trans = controller.supportFragmentManager.beginTransaction().apply {
                                add(R.id.mainContainer, PlatformInfoFragment())
                                addToBackStack(null)
                            }.commit()
                        }
                    }
                }
            }
        }

        RB_listView.apply {
            isTextFilterEnabled = true
        }

        return view
    }

}
