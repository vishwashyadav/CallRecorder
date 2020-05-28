package com.callrecorder.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import com.callrecorder.MainActivity
import com.callrecorder.PreferenceManager
import com.callrecorder.R
import com.callrecorder.Utils
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity(), ILoginMvpView {

    override fun showError(error: Int) {
        Utils.toast(getString(error), this)
    }

    override fun showLoading() {
        Utils.showDialog(mContext!!, "Please wait..", "Login")
    }

    override fun hideLoading() {
        Utils.hideDialog()
    }

    override fun showError(error: String) {
        Utils.toast(error, this)
    }


    override fun openMainActivity(bundle: Bundle?) {
        val intent = Intent(mContext, MainActivity::class.java)
        if (bundle != null)
            intent.putExtra("bundle", bundle)
        startActivity(intent)
        finish()
    }

    private lateinit var mPresenter: LoginPresenter<*>
    private var mContext: Context? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mContext = this
        setContentView(R.layout.activity_login)

        LoginPresenter<ILoginMvpView>(this@LoginActivity)


        etUsername.onFocusChangeListener = mPresenter.addFocusChangeEvent(etUsername, "text")
        etContactNumber.onFocusChangeListener = mPresenter.addFocusChangeEvent(etContactNumber, "number")


        btnLogin.setOnClickListener {
            mPresenter.onSubmitLogin(etContactNumber, etUsername, Utils.getUniqueId(this@LoginActivity))
//            startActivity(Intent(this, DashboardActivity::class.java))
//            finish()
        }


    }


    override fun showError(errorString: String, inputField: String) {
        if (TextUtils.isEmpty(errorString))
            return
        if (inputField.equals("number", ignoreCase = true)) {
            inputUserNumberLayout.isErrorEnabled = true
            inputUserNumberLayout.error = errorString

        } else if (inputField.equals("text", ignoreCase = true)) {
            inputUserNameLayout.isErrorEnabled = true
            inputUserNameLayout.error = errorString

        }
    }

    override fun viewFocusChange(isFocus: Boolean, inputFieldType: String) {
        when (inputFieldType) {
            "number" -> if (isFocus) {
                inputUserNameLayout.isErrorEnabled = false
                inputUserNameLayout.error = null
            }
            "text" -> if (isFocus) {
                inputUserNumberLayout.isErrorEnabled = false
                inputUserNumberLayout.error = null
            }
        }
    }

    override fun attachPresenter(presenter: LoginPresenter<*>) {
        mPresenter = presenter
    }


    override fun onSuccess(response: LoginResponseBean, userName: String, userNumber: String) {

        PreferenceManager.instance(mContext).set(PreferenceManager.USER_ID, response.userId)

        PreferenceManager.instance(mContext).set(PreferenceManager.USER_NAME, userName)
        PreferenceManager.instance(mContext).set(PreferenceManager.USER_MOBILE_NUMBER, userNumber)
        PreferenceManager.instance(mContext).set(PreferenceManager.LOGIN_STATUS, true)
        PreferenceManager.instance(mContext).set(PreferenceManager.switchOn, true)

        startActivity(Intent(mContext, MainActivity::class.java))
        finish()


    }

    override fun onError(error: String) {
        Utils.toast(error, mContext)
    }

    override fun onError(error: Int) {
        Utils.toast(getString(error), mContext)
    }


}
