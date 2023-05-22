package com.example.mygraphqlclient

import CountriesQuery
import CountryQuery
import android.R.layout
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.CodeScannerView
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import com.apollographql.apollo3.ApolloClient


class MainActivity : AppCompatActivity() ,ScanSuccess{
    private lateinit var codeScanner: CodeScanner
    private var dialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_second)
        val scannerView = findViewById<CodeScannerView>(R.id.scanner_view)
        codeScanner = CodeScanner(this, scannerView)

        // Parameters (default values)
        codeScanner.camera = CodeScanner.CAMERA_BACK // or CAMERA_FRONT or specific camera id
        codeScanner.formats = CodeScanner.ALL_FORMATS // list of type BarcodeFormat,
        // ex. listOf(BarcodeFormat.QR_CODE)
        codeScanner.autoFocusMode = AutoFocusMode.SAFE // or CONTINUOUS
        codeScanner.scanMode = ScanMode.SINGLE // or CONTINUOUS or PREVIEW
        codeScanner.isAutoFocusEnabled = true // Whether to enable auto focus or not
        codeScanner.isFlashEnabled = false // Whether to enable flash or not

        // Callbacks
        var macReceived = false ; var eanReceived = false
        val mac_str : String = "MAC:"
        var mac: String =""
        var ean: String = ""
        codeScanner.decodeCallback = DecodeCallback {

            runOnUiThread {
               // Toast.makeText(this, "Scan result: ${it.text}", Toast.LENGTH_SHORT).show()
                val inflater = layoutInflater
                val dialoglayout: View = inflater.inflate(R.layout.scan_result_success, null)
                val myDialog : AlertDialog.Builder = AlertDialog.Builder(this).setView(dialoglayout)
                val alert : AlertDialog = myDialog.create()
                val macId : TextView = dialoglayout.findViewById(R.id.mac_id)
                val eanId : TextView = dialoglayout.findViewById(R.id.ean_id)
                var sendToServer : Button = dialoglayout.findViewById<Button>(R.id.send_to_server)
                fun setMacEan(macEanvalue: String){
                    if (macEanvalue == it.text && macEanvalue != "") {
                        Toast.makeText(
                            this,
                            "Scanned Successfully!!",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else if (macEanvalue != it.text && macEanvalue != "") {
                        Toast.makeText(this, "Value changed from $macEanvalue to ${it.text} ", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this, "Scan result: ${it.text}", Toast.LENGTH_SHORT).show()
                    }
                }
                if(mac_str in it.text) {
                    setMacEan(mac)
                    mac = it.text
                    macReceived = true
                }else{
                    setMacEan(ean)
                    ean = it.text
                    eanReceived = true
                }
                if(macReceived && eanReceived){
                    macId.text = mac.replace(mac_str,"")
                    eanId.text = ean
                    sendToServer.setOnClickListener{
                        Toast.makeText(this,"Sending to Server",Toast.LENGTH_LONG).show()
                        mac=""
                        ean=""
                        macReceived = false
                        eanReceived = false
                        codeScanner.startPreview()
                        fetchDataFromServer()
                        onClose(alert)
                    }
                    onSuccess(this,alert)
                }
                //codeScanner.startPreview()
            }

        }
        codeScanner.errorCallback = ErrorCallback { // or ErrorCallback.SUPPRESS
            runOnUiThread {
                Toast.makeText(this, "Camera initialization error: ${it.message}",
                    Toast.LENGTH_SHORT).show()

            }
        }

        scannerView.setOnClickListener {
            codeScanner.startPreview()
        }

        val requestCamera = registerForActivityResult(ActivityResultContracts.RequestPermission(),{
            if(it)
            {
                Toast.makeText(applicationContext,"Camera Permission Granted",Toast.LENGTH_SHORT).show()
            }
            else{
                Toast.makeText(applicationContext,"Camera Permission Not Granted",Toast.LENGTH_SHORT).show()
            }
        })

        requestCamera.launch(android.Manifest.permission.CAMERA)
    }

    override fun onResume() {
        super.onResume()
        codeScanner.startPreview()
    }

    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()
    }

    fun fetchDataFromServer(){
        val apolloClient = ApolloClient.Builder()
            .serverUrl("https://countries.trevorblades.com")
            .build()
        GlobalScope.launch {
            suspend fun  response()  = apolloClient
                .query(CountriesQuery())
                .execute()
                .data
                ?.countries
            val res = response()
            res?.map {
                    it -> Log.d("Country",it.name)
            }

            
        }
    }



}



