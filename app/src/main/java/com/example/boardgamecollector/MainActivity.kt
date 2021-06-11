package com.example.boardgamecollector

import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.boardgamecollector.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    companion object{
        const val REQUEST_CODE = 10000
        private const val PER_PAGE = 8
        private class MainActivityViewModel(private val dbHelper: MyDBHelper): ViewModel(){
            companion object{
                private val sortOrderStrings = arrayOf("${GamesCollector.GamesEntry.COLUMN_TITLE_ORIGINAL} ASC",
                    "${GamesCollector.GamesEntry.COLUMN_TITLE_ORIGINAL} DESC",
                    "${GamesCollector.GamesEntry.COLUMN_CURRENT_RANK} ASC",
                    "${GamesCollector.GamesEntry.COLUMN_CURRENT_RANK} DESC",
                    "${GamesCollector.GamesEntry.COLUMN_RELEASE_YEAR} ASC",
                    "${GamesCollector.GamesEntry.COLUMN_RELEASE_YEAR} DESC")
            }

            private val _page: MutableLiveData<Int> = MutableLiveData()
            val page: LiveData<Int> get() = _page

            var sortOrder: MutableLiveData<Int> = MutableLiveData<Int>().also { it.postValue(0) }

            private val _games: MutableLiveData<Array<GameData>> = MutableLiveData()
            val games: LiveData<Array<GameData>> get() = _games

            private val _maxPages: MutableLiveData<Int> = MutableLiveData()
            val maxPages: LiveData<Int> get() = _maxPages

            private val _cursor: MutableLiveData<Cursor> = MutableLiveData()

            private fun loadPage(cursor: Cursor){
                val begin = page.value?.times(PER_PAGE)?:0
                cursor.moveToPosition(begin)

                val arrList: ArrayList<GameData> = ArrayList()

                for (i in 0 until PER_PAGE){
                    if(cursor.isAfterLast)
                        break
                    arrList.add(
                        GameData(
                            gameId = cursor.getLong(cursor.getColumnIndex(GamesCollector.GamesEntry.GAME_ID)),
                            ranks = Array(1){
                                Rank(rank = cursor.getInt(cursor.getColumnIndex(GamesCollector.GamesEntry.COLUMN_CURRENT_RANK)))
                            },
                            hasImg = cursor.getInt(cursor.getColumnIndex(GamesCollector.GamesEntry.COLUMN_HAS_IMG)).let {
                                return@let it == 1
                            },
                            img = cursor.getBlob(cursor.getColumnIndex(GamesCollector.GamesEntry.COLUMN_IMG)).let{
                                val op = BitmapFactory.Options()
                                if (cursor.getInt(cursor.getColumnIndex(GamesCollector.GamesEntry.COLUMN_HAS_IMG)) == 1)
                                    return@let BitmapFactory.decodeByteArray(it, 0, it.size, op)
                                else null
                            },
                            description = cursor.getString(cursor.getColumnIndex(GamesCollector.GamesEntry.COLUMN_DESCRIPTION)),
                            yearPublished = cursor.getInt(cursor.getColumnIndex(GamesCollector.GamesEntry.COLUMN_RELEASE_YEAR)),
                            originalTitle = cursor.getString(cursor.getColumnIndex(GamesCollector.GamesEntry.COLUMN_TITLE_ORIGINAL))
                        )
                    )
                    cursor.moveToNext()
                }

                _games.postValue(Array<GameData>(arrList.size){
                    arrList[it]
                })
            }

            fun refresh(){
                _page.postValue(0)
                _page.value = 0
                if(_cursor.value != null){
                    _cursor.value!!.close()
                }
                viewModelScope.launch {
                    val db = dbHelper.readableDatabase
                    val projection = arrayOf(GamesCollector.GamesEntry.GAME_ID,
                        GamesCollector.GamesEntry.COLUMN_TITLE_ORIGINAL,
                        GamesCollector.GamesEntry.COLUMN_CURRENT_RANK,
                        GamesCollector.GamesEntry.COLUMN_RELEASE_YEAR,
                        GamesCollector.GamesEntry.COLUMN_DESCRIPTION,
                        GamesCollector.GamesEntry.COLUMN_HAS_IMG,
                        GamesCollector.GamesEntry.COLUMN_IMG
                    )
                    val cursor = db.query(
                        GamesCollector.GamesEntry.TABLE_NAME,
                        projection,
                        null,
                        null,
                        null,
                        null,
                        sortOrderStrings[sortOrder.value?:0]
                    )
                    _cursor.postValue(cursor)
                    var max = cursor.count/ PER_PAGE
                    if(cursor.count > max * PER_PAGE)
                        max++
                    _maxPages.postValue(max)
                    loadPage(cursor)
                }
            }

            fun nextPage(){
                val next = _page.value?.plus(1)?:0
                _page.value = next
                _page.postValue(next)

                viewModelScope.launch {
                    loadPage(_cursor.value!!)
                }
            }

            fun prevPage(){
                val prev = _page.value?.minus(1)?:0
                _page.value = prev
                _page.postValue(prev)
                viewModelScope.launch {
                    loadPage(_cursor.value!!)
                }
            }
        }
    }
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainActivityViewModel
    private lateinit var loadingDialog: LoadingDialog

    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.refresh()
            loadingDialog.startLoadingDialog()
            loadingDialog.setInfo(getString(R.string.refreshing))
        }
    }


    override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
        viewModel.sortOrder.value = pos
        viewModel.refresh()
    }

    override fun onNothingSelected(parent: AdapterView<*>) {
        // Another interface callback
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadingDialog = LoadingDialog(this)

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

        spinner.onItemSelectedListener = this

        binding.btnLBggAct.setOnClickListener {
            startForResult.launch(Intent(this, BGGActivity::class.java))
        }

        binding.btnNext.setOnClickListener {
            loadingDialog.startLoadingDialog()
            viewModel.nextPage()
        }

        binding.btnPrev.setOnClickListener {
            loadingDialog.startLoadingDialog()
            viewModel.prevPage()
        }

        viewModel.page.observe(this){
            if(it == 0){
                binding.btnPrev.visibility = View.INVISIBLE
            }else{
                binding.btnPrev.visibility = View.VISIBLE
            }

            if(it >= (viewModel.maxPages.value?.minus(1)?:0)){
                binding.btnNext.visibility = View.INVISIBLE
            }else{
                binding.btnNext.visibility = View.VISIBLE
            }
        }

        viewModel.maxPages.observe(this){
            if(viewModel.page.value!! >= (viewModel.maxPages.value?.minus(1)?:0)){
                binding.btnNext.visibility = View.INVISIBLE
            }else{
                binding.btnNext.visibility = View.VISIBLE
            }
        }

        val tableGames: TableLayout = binding.tableGames

        viewModel.games.observe(this){
            tableGames.removeAllViews()
            binding.scrollView2.fullScroll(ScrollView.FOCUS_UP)
            it.iterator().forEach { gameData ->
                val inflated = LayoutInflater.from(this).inflate(R.layout.layout_table_games_row, null,false)
                val tvRank = inflated.findViewById<TextView>(R.id.tvRank)
                tvRank.text = gameData.ranks?.get(0)?.rank.toString()
                val img = inflated.findViewById<ImageView>(R.id.trImage)
                if(gameData.hasImg == true){
                    if(gameData.img!=null)
                        img.setImageBitmap(gameData.img)
                }else{
                    img.visibility = View.INVISIBLE
                }
                val tvTitle = inflated.findViewById<TextView>(R.id.tvTitle)
                tvTitle.text = gameData.originalTitle?:""
                val tvDescription = inflated.findViewById<TextView>(R.id.tvDescription)
                tvDescription.text = gameData.description?:""
                val tvYear = inflated.findViewById<TextView>(R.id.tvReleaseYear)
                tvYear.text = gameData.yearPublished.toString()

                val tr = TableRow(this)
                tr.addView(inflated)

                tr.setOnClickListener {
                    val intent = Intent(this, GameInfoActivity::class.java).apply {
                        Log.i("XD", gameData.gameId.toString())
                        putExtra("gameId", gameData.gameId)
                    }
                    startForResult.launch(intent)
                }

                tableGames.addView(tr, TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT))
            }
            loadingDialog.dissmisDialog()
        }
        viewModel.refresh()

        binding.btnLocations.setOnClickListener {
            startActivity(Intent(this, LocationsActivity::class.java))
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