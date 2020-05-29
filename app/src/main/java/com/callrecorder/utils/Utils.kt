package com.callrecorder;

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.graphics.Color
import android.provider.Settings
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class Utils {
//Commited to git
    companion object {

        private var pDialog: ProgressDialog? = null
        private var toast: Toast? = null

        fun showDialog(context: Activity, msg: String, title: String) {
            if (pDialog == null) {
                pDialog = ProgressDialog(context)
                pDialog?.setMessage(msg)
                pDialog?.setTitle(title)
                pDialog?.setCancelable(false)
                if (pDialog?.isShowing == false && !(context as AppCompatActivity).isFinishing) {
                    pDialog?.show()
                }
            }
        }

        fun showDialog(context: Context, msg: String, title: String) {
            if (pDialog == null) {
                pDialog = ProgressDialog(context)
                pDialog?.setMessage(msg)
                pDialog?.setTitle(title)
                pDialog?.setCancelable(false)
                if (pDialog?.isShowing == false && !(context as AppCompatActivity).isFinishing) {
                    pDialog?.show()
                }
            }
        }

        fun showDialog(context: Context, msg: String) {
            if (pDialog == null) {
                pDialog = ProgressDialog(context)
                pDialog?.setMessage(msg)
                pDialog?.setTitle("")
                pDialog?.setCancelable(false)
                if (pDialog?.isShowing == false && !(context as AppCompatActivity).isFinishing) {
                    pDialog?.show()
                }
            }
        }

        fun hideDialog() {
            if (pDialog != null) {
                if (pDialog?.isShowing == true) {
                    pDialog?.dismiss()
                    pDialog = null
                }
            }
        }


        fun toast(message: String, context: Context?) {
            val v = LayoutInflater.from(context).inflate(R.layout.layout_custom_toastview, null)
            val textView = v.findViewById<TextView>(R.id.tvToast)
            textView.text = message
            toast = if (toast == null) {
                Toast(context)
            } else {
                toast?.cancel()
                Toast(context)
            }
            toast?.view = v
            toast?.duration = Toast.LENGTH_LONG
            toast?.show()
        }


        private fun getRandomColor(): Int {
            val rand = Random()
            return Color.argb(100, rand.nextInt(256), rand.nextInt(256), rand.nextInt(256))
        }

        fun getReadableFileSize(size: Long): String {
            if (size <= 0) {
                return "0"
            }
            val units = arrayOf("B", "KB", "MB", "GB", "TB")
            val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
            return DecimalFormat("#,##0.#").format(size / Math.pow(1024.0, digitGroups.toDouble())) + " " + units[digitGroups]
        }

        fun getUniqueId(context: Activity): String {
            var androidId = Settings.Secure.getString(context.getContentResolver(),
                    Settings.Secure.ANDROID_ID)
            return androidId
        }

        fun getTimeSpan(seconds:Int):String
        {
            var minutes: Int
            var hours: Int


            hours = seconds / 3600;
            minutes = seconds % 3600 / 60;
            val seconds_output = seconds % 3600 % 60
            return (minutes.toString()+":"+minutes+":"+seconds_output);
        }

        fun DateToString(date:Date):String
        {
            val pattern = "yyyy-MM-dd HH:mm:ss";
            val simpleDateFormat = SimpleDateFormat(pattern);

            val dateStr = simpleDateFormat.format(date);
            return dateStr;
        }
    }


}