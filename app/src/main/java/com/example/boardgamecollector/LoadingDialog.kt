package com.example.boardgamecollector

import android.app.Activity
import android.app.AlertDialog

class LoadingDialog(var activity: Activity) {
    private var dialog: AlertDialog? = null

    fun startLoadingDialog(){
        val builder = AlertDialog.Builder(activity);

        val inflater = activity.layoutInflater
        builder.setView(inflater.inflate(R.layout.loading_dialog, null))
        builder.setCancelable(true)

        dialog = builder.create()
        dialog?.show()
    }

    fun dissmisDialog(){
        dialog?.dismiss()
    }
}