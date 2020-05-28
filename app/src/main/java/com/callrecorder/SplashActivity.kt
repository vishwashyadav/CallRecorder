package com.callrecorder

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.callrecorder.login.LoginActivity


class SplashActivity : AppCompatActivity() {

    private var mContext: Context? = null
    private var arguments: Bundle? = null
    private var provider: WebServiceProvider? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mContext = this
        setContentView(R.layout.activity_splash)
        arguments = intent.getBundleExtra("bundle")
        provider = WebServiceProvider.retrofit.create(WebServiceProvider::class.java)

        redirectToActivity()

    }


    fun redirectToActivity() {
        val userLoginStatus = PreferenceManager.instance(mContext).get(PreferenceManager.LOGIN_STATUS, false)
        if (userLoginStatus) {
            startActivity(Intent(mContext, MainActivity::class.java))
        } else
            startActivity(Intent(mContext, LoginActivity::class.java))

        finish()
    }


}