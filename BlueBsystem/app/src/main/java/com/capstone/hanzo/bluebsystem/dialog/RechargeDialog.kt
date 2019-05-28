package com.capstone.hanzo.bluebsystem.dialog

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.capstone.hanzo.bluebsystem.MenuActivity
import com.capstone.hanzo.bluebsystem.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.dialog_recharge.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kr.co.bootpay.*
import kr.co.bootpay.enums.PG
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jetbrains.anko.activityManager
import org.jetbrains.anko.alert
import org.jetbrains.anko.yesButton
import org.w3c.dom.Text
import java.lang.Exception

class RechargeDialog(context: Context, val activity: Activity) : Dialog(context) {
    private lateinit var rechargeBtn: Button
    private lateinit var editText: EditText
    private lateinit var textAlert: TextView

    val user = FirebaseAuth.getInstance().currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_recharge)
        val dm = context.resources.displayMetrics

        window?.attributes = WindowManager.LayoutParams().apply {
            copyFrom(this@RechargeDialog.window?.attributes)
            width = dm.widthPixels / 2
            height = WindowManager.LayoutParams.WRAP_CONTENT
        }

        editText = findViewById(R.id.dialogEditMoney)
        rechargeBtn = findViewById(R.id.dialogBtnRecharge)
        textAlert = findViewById(R.id.dialogTextAlert)

        editText.addTextChangedListener(object : TextWatcher {

            private fun <T> check(string: T) {
                textAlert.apply {
                    if (string.toString().isEmpty()) {
                        text = "입력 값이 없습니다"
                    } else if (string.toString().toInt() < 1000) {
                        text = "1000원 이상 충전 가능합니다"
                        setTextColor(Color.RED)
                    } else {
                        text = "충전이 가능합니다"
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
        })
        rechargeBtn.setOnClickListener {
            val money = dialogEditMoney.toString().toInt()

            if (money < 1000) {
                context.alert {
                    message = "1000원 이상 입력해주세요"
                    yesButton { title = "확인" }
                }
            } else {
                Bootpay.init(activity)
                    .setApplicationId("5c6e1824b6d49c7cbc505f9c")
                    .setPG(PG.KAKAO)
                    .setUserPhone(user?.phoneNumber)
                    .setMethod("easy")
                    .setName("지갑 충전")
                    .setOrderId("70006929")
                    .setPrice(money)
                    .addItem("지갑 충전", 1, "ITEM_CODE_CASH", money)
                    .isShowAgree(true)
                    .onConfirm(object : ConfirmListener {
                        override fun onConfirm(message: String?) {
                            Bootpay.confirm(message)
                        }
                    })
                    .onDone(object : DoneListener {
                        override fun onDone(message: String?) {
                            recharge()
                        }
                    })
                    .onCancel(object : CancelListener {
                        override fun onCancel(message: String?) {}
                    })
                    .onError(object : ErrorListener {
                        override fun onError(message: String?) {}
                    })
                    .onClose(object : CloseListener {
                        override fun onClose(message: String?) {}
                    })
                    .show()
            }
        }
    }


    private fun recharge() = CoroutineScope(Dispatchers.IO).launch {
        val url = Request.Builder().url("http://13.125.170.17/CashRecharge.php")

        val body = FormBody.Builder().apply {
            add("user_email", user?.email!!)
            add("user_cash", dialogEditMoney.text.toString())
        }.build()

        val request = url.post(body).build()
        OkHttpClient().newCall(request).execute()
    }
}
