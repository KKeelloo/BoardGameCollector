package com.example.boardgamecollector

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.boardgamecollector.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    companion object{
        const val REQUEST_CODE = 10000
        const val PER_PAGE = 8
        private class MainActivityViewModel(private val dbHelper: MyDBHelper): ViewModel(){

            private val _page: MutableLiveData<Int> = MutableLiveData()
            val page: LiveData<Int> get() = _page

            private val _games: MutableLiveData<Array<GameData>> = MutableLiveData()
            val games: LiveData<Array<GameData>> get() = _games

            fun refresh(){

            }

            fun nextPage(){

            }

            fun prevPage(){

            }
        }
    }
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainActivityViewModel

    val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.refresh()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val btnAddGame: Button = binding.btnAddGame
        viewModel = MainActivityViewModel(MyDBHelper.getInstance(this))

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

        binding.btnLBggAct.setOnClickListener {
            startForResult.launch(Intent(this, BGGActivity::class.java))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if((requestCode == REQUEST_CODE) && (resultCode == RESULT_OK)){
            if(data != null){
                if(data.hasExtra("newGameId")){
                    val gameId = data.extras?.getLong("newGameId")?: (-1).toLong()
                    if(gameId != (-1).toLong()) {
                        viewModel.refresh()
                    }
                }
            }
        }
    }
}