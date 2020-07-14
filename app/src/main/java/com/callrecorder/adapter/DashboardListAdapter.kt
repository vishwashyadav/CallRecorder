package com.callrecorder.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.callrecorder.CommonMethods
import com.callrecorder.R
import com.callrecorder.Utils
import com.callrecorder.bean.UserCallDetailsList
import com.callrecorder.dialog.StickyDialog


class DashboardListAdapter(private val mContext: Context, private val dataList: List<UserCallDetailsList>, private val listener: Listener) : RecyclerView.Adapter<DashboardListAdapter.ViewHolder>() {
    private val layoutInflater: LayoutInflater = LayoutInflater.from(mContext)
    private var userType = 0


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = layoutInflater.inflate(R.layout.itemview_dashboard_list, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val bean = dataList[position]
        val n = bean.toNum
        val name = CommonMethods().getContactName(n, mContext)
        var callerName:String = name;
        if(callerName==null||callerName=="") {
            callerName = n;
        }

        holder.tvTitle.text = callerName;
        holder.tvDesc.text = bean.callDate;
        holder.txtDuration.text = Utils.getTimeSpan(bean.callDuration);


        if(bean.callType=="OUTGOING")
            holder.imgCallType.setImageResource(R.drawable.outgoingcall);
        else
            holder.imgCallType.setImageResource(R.drawable.incomingcall);

        if (!bean.IsUploaded)
            {
                holder.txtStatus.text = "";

        } else if( bean.IsDeleted)
        {
            holder.txtStatus.text="DELETED";
        }
        else if(bean.IsUploaded && !bean.IsFileUploaded){

            holder.txtStatus.text= "PENDING";
        }
        else if(bean.IsUploaded && bean.IsFileUploaded)
        {
            holder.txtStatus.text= "UPLOADED";
        }

        if (!bean.IsFileUploaded) {
            holder.tvRecordingFileUpload.visibility = View.GONE;
        } else {

            holder.tvRecordingFileUpload.visibility = View.VISIBLE
        }

        holder.tvRecordingFileUpload.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                StickyDialog(mContext, "Go Back", "Do you want delete this file", "Yes", StickyDialog.Okay {
                    listener.deleteItem(dataList[position]);
                }, true, "No", StickyDialog.Cancel {

                }).show()
            }
        })


    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvTitle: TextView = itemView.findViewById(R.id.txtContactName)
        var tvDesc: TextView = itemView.findViewById(R.id.txtCallDate)
        var txtDuration: TextView = itemView.findViewById(R.id.txtDuration)
        var tvRecordingFileUpload: ImageView = itemView.findViewById(R.id.imgFileUpload)
        var txtStatus: TextView = itemView.findViewById(R.id.txtStatus)
        var imgCallType: ImageView = itemView.findViewById(R.id.imgCallType)

        var tvLayout: RelativeLayout = itemView.findViewById(R.id.layout)

    }

    interface Listener {
        fun deleteItem(year: UserCallDetailsList)
    }

}
