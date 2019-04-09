package com.capstone.hanzo.bluebsystem


import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import kr.co.bootpay.*
import kr.co.bootpay.enums.PG
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.find
import org.jetbrains.anko.sdk27.coroutines.textChangedListener
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.yesButton

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 *
 */
class PaymentManagementFragment : Fragment() {

    private lateinit var btnCharge: Button
    private lateinit var editExpense: EditText
    private lateinit var textExpense: TextView
    private val user = FirebaseAuth.getInstance().currentUser

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_payment_management, container, false).apply {
            btnCharge = find(R.id.BtnCharge)
            editExpense = find(R.id.editExpense)
            textExpense = find(R.id.textExpense)
        }
        editExpense.addTextChangedListener(TextChangedListener())
        btnCharge.setOnClickListener(ChargeListener())

        return view
    }

    inner class TextChangedListener : TextWatcher {
        private fun <T> check(string: T) {

            textExpense.apply {

                if (string.toString().isEmpty()) {
                    textExpense.text = "1,000원 단위로 충전 가능합니다"
                    setTextColor(Color.parseColor("#000E2B"))
                } else if (string.toString().toInt() < 1000) {
                    text = "최소 충전 금액은 1000원입니다"
                    setTextColor(Color.RED)

                } else {
                    text = "충전 가능한 금액입니다"
                    setTextColor(Color.BLUE)
                }
            }
        }

        override fun afterTextChanged(s: Editable?) {
            check(s)
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            check(s)
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            check(s)
        }
    }

    inner class ChargeListener : View.OnClickListener {
        override fun onClick(v: View?) {
            val data = editExpense.text.toString().toInt()

            if (data < 1000) {
                alert {
                    message = "1000원 이상 입력해주세요"
                    yesButton {
                        title = "확인"
                    }
                }.show()
            } else {
                Bootpay.init((activity as MenuActivity))
                    .setApplicationId("5c6e1824b6d49c7cbc505f9c") // 해당 안드로이드 프로젝트의 어플리케이션 id 값
                    .setPG(PG.KAKAO) // 결제할 PG사
                    .setUserPhone(user?.phoneNumber) // 구매자 전화번호
                    .setMethod("easy") // 결제 수단
                    .setName("지갑 충전") // 결제할 상품명
                    .setOrderId("70006929") // 고유 주문번호로, 생성하신 값을 보내주셔야 합니다
                    .setPrice(editExpense.text.toString().toInt()) // 결제할 금액
                    .addItem("지갑 충전", 1, "ITEM_CODE_CASH", editExpense.text.toString().toInt())
                    .isShowAgree(true) // 결제 동의창 생성
                    .onConfirm(object : ConfirmListener {
                        override fun onConfirm(message: String?) {
                            Bootpay.confirm(message) // 재고가 있을 경우
                            Log.d("confirm", message)
                        }
                    })
                    .onDone(object : DoneListener { // 결제완료시 호출, 아이템 지급 등 데이터 동기화 로직을 수행합니다
                        override fun onDone(message: String?) {
                            ChargeThread().start()
                            Log.d(":done", message)
                        }
                    })
                    .onReady(object : ReadyListener { // 가상계좌 입금 계좌번호가 발급되면 호출되는 부분
                        override fun onReady(message: String?) {
                            Log.d("ready", message)
                        }
                    })
                    .onCancel(object : CancelListener { // 결제 취소시 호출되는 부분
                        override fun onCancel(message: String?) {
                            Log.d("cancle", message)
                        }
                    })
                    .onError(object : ErrorListener { // 에러가 났을때 호출되는 부분
                        override fun onError(message: String?) {
                            Log.d("error", message)
                        }
                    })
                    .onClose(object : CloseListener { // 결체창이 닫힐때 실행되는 부분
                        override fun onClose(message: String?) {
                            Log.d("close", "close")
                        }
                    })
                    .show()
            }
        }
    }

    inner class ChargeThread : Thread() {
        override fun run() {
            val url = Request.Builder().url("http://13.125.170.17/CashRecharge.php")

            val body = FormBody.Builder().apply {
                add("user_email", FirebaseAuth.getInstance().currentUser?.email!!)
                add("user_cash", editExpense.text.toString())
            }.build()

            val request = url.post(body).build()
            OkHttpClient().newCall(request).execute()
        }
    }
}
