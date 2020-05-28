package com.callrecorder.login

import android.support.annotation.StringRes

/**
 * Base interface that any class that wants to act as a View in the MVP (Model View Presenter)
 * pattern must implement. Generally this interface will be extended by a more specific interface
 * that then usually will be implemented by an Activity or Fragment.
 */
interface IBaseView {

    fun showLoading()

    fun hideLoading()

    fun showError(error: String)

    fun showError(@StringRes error: Int)

}
