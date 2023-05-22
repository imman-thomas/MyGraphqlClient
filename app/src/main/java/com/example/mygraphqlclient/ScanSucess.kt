package com.example.mygraphqlclient

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity



interface ScanSuccess {

    fun onSuccess(c: Context, alert: AlertDialog) {
       // val myDialog : AlertDialog.Builder = AlertDialog.Builder(c).setView(v)
        alert.create()
        alert.show()
    }

    fun onClose(alert: AlertDialog){
        alert.dismiss()
    }

}