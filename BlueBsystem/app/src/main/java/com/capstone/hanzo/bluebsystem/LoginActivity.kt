package com.capstone.hanzo.bluebsystem

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_login.*
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.toast

class LoginActivity : AppCompatActivity(), GoogleApiClient.OnConnectionFailedListener {

    override fun onConnectionFailed(p0: ConnectionResult) {

    }

    companion object {
        const val RC_SIGN_IN = 1000
    }

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mGoogleApiClient: GoogleApiClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("331962744301-8ep1ufkrdc4668fe0c57t95bsps5p8o0.apps.googleusercontent.com")
            .requestEmail()
            .build()

        mGoogleApiClient = GoogleApiClient.Builder(this)
            .enableAutoManage(this, this)
            .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
            .build()

        mAuth = FirebaseAuth.getInstance()

        btnLGoogleLogin.setOnClickListener {
            val signIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient)
            startActivityForResult(
                signIntent,
                RC_SIGN_IN
            )
        }
    }

    override fun onStart() {
        super.onStart()
        val user = FirebaseAuth.getInstance().currentUser

        user?.let {
            startActivity(intentFor<MenuActivity>())
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            RC_SIGN_IN -> {
                val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
                when (result.isSuccess) {
                    true -> {
                        val account = result.signInAccount
                        firebaseAuthWithGoogle(account!!)
                    }
                    false -> {
                        toast("로그인 실패").show()
                    }
                }
            }
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        mAuth.signInWithCredential(credential).addOnCompleteListener {
            when (!it.isSuccessful) {
                true -> {

                }
                false -> {
                    LoginThread().start()
                    startActivity(intentFor<MenuActivity>())
                }
            }
        }
    }

   inner class LoginThread : Thread() {

        // 현재 어플리케이션에 로그인 된 사용자 객체
        private val currentUser = FirebaseAuth.getInstance().currentUser

        override fun run() {
            val url = Request.Builder().url("http://13.125.170.17/googleUserInsert.php")
            val body = FormBody.Builder().apply {
                add("user_email", currentUser?.email!!)
                add("user_name", currentUser.displayName!!)
            }.build()

            // http 요청 객체
            val request = url.post(body).build()
            // http 요청
            OkHttpClient().newCall(request).execute()
        }
    }
}
