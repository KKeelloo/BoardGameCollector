package com.example.boardgamecollector

import android.app.Activity
import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.boardgamecollector.databinding.ActivityBGGBinding
class BGGActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBGGBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBGGBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val dbHelper = MyDBHelper.getInstance(this)
        val xmlParser = XMLParser(this.applicationInfo.dataDir)

        val loadingDialog = LoadingDialog(this)


        binding.btnImportByUsername.setOnClickListener {
            val builder = AlertDialog.Builder(this)

            val inUsername = EditText(this)
            inUsername.hint = getText(R.string.username)

            builder.setView(inUsername)
            builder.setNegativeButton(android.R.string.cancel){ dialogInterface: DialogInterface, _ ->
                dialogInterface.cancel()
            }
            builder.setPositiveButton(android.R.string.ok){dialogInterface: DialogInterface, _ ->
                xmlParser.findGamesByUsername(inUsername.text.toString(), dbHelper)
                loadingDialog.startLoadingDialog()
                dialogInterface.dismiss()
            }
            builder.show()
        }

        binding.btnRefreshStats.setOnClickListener {
            loadingDialog.startLoadingDialog()
            xmlParser.refreshRanks(dbHelper)
        }

        xmlParser.retryingIn.observe(this){ value ->
            if(xmlParser.processingForBGG.value == false )
                loadingDialog.setInfo("${getText(R.string.waiting_for_data)}\n${getText(R.string.retrying_in)} ${value}s")
        }

        xmlParser.numLoaded.observe(this){
            if(xmlParser.processingForBGG.value == true )
                loadingDialog.setInfo("${getString(R.string.adding)}\n${it}..${xmlParser.numToLoad.value}")
        }

        xmlParser.processingForBGG.observe(this){ value ->
            if(value){
                loadingDialog.setInfo(getString(R.string.processing_data))
            }
            else {
                if (xmlParser.loaded.value == 200){
                    loadingDialog.dissmisDialog()
                    Toast.makeText(this, R.string.loaded,Toast.LENGTH_SHORT).show()
                    setResult(Activity.RESULT_OK)
                }
                else if(xmlParser.loaded.value != -2){
                    loadingDialog.dissmisDialog()
                    val builder = AlertDialog.Builder(this)

                    val out = TextView(this)
                    out.text = getText(R.string.error_occ)

                    builder.setView(out)
                    builder.setNegativeButton("Cancel"){ dialogInterface: DialogInterface, _ ->
                        dialogInterface.cancel()
                    }
                    builder.show()
                }
            }
        }
    }
}