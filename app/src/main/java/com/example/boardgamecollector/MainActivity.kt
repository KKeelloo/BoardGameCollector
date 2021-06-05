package com.example.boardgamecollector

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import com.example.boardgamecollector.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    companion object{
        val REQUEST_CODE = 10000
    }
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val btnAddGame: Button = binding.btnAddGame

        btnAddGame.setOnClickListener {
            val intent = Intent(this, AddGame::class.java)
            startActivityForResult(intent, REQUEST_CODE)
        }

        val spinner: Spinner = binding.spinnerSort

        ArrayAdapter.createFromResource(
            this,
            R.array.sort_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if((requestCode == REQUEST_CODE) && (resultCode == RESULT_OK)){
            if(data != null){
                if(data.hasExtra("newGameId")){
                    val gameId = data.extras?.getInt("newGameId")
                    if(gameId != -1) {
                        //TODO add new game to table view
                    }
                }
            }
        }
    }
}