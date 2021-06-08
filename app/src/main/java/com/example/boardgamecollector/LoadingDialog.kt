package com.example.boardgamecollector

import android.app.Activity
import android.app.AlertDialog
import android.widget.TextView

class LoadingDialog(var activity: Activity) {
    private var dialog: AlertDialog? = null
    private var info: TextView? = null

    fun startLoadingDialog(){
        val builder = AlertDialog.Builder(activity);

        val inflater = activity.layoutInflater
        val inflated = inflater.inflate(R.layout.loading_dialog, null)

        info = inflated.findViewById(R.id.tvLoadingInfo)

        builder.setView(inflated)
        builder.setCancelable(false)
        dialog = builder.create()
        dialog?.show()
    }

    fun dissmisDialog(){
        dialog?.setCancelable(true)
        dialog?.dismiss()
    }

    fun setInfo(text: String){
        info?.text = text
    }
}