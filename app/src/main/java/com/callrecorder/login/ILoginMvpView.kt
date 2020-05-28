package com.callrecorder.login


import android.os.Bundle
import android.support.annotation.StringRes


interface ILoginMvpView : IBaseView {

    fun openMainActivity(bundle: Bundle?)

    fun showError(errorString: String, inputField: String)

    fun viewFocusChange(isFocus: Boolean, inputFieldType: String)

    fun attachPresenter(presenter: LoginPresenter<*>)

    fun onSuccess(response: LoginResponseBean, userName: String, userNumber: String)

    fun onError(error: String)

    fun onError(@StringRes error: Int)


}
