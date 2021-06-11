package com.example.boardgamecollector

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.ClipDescription
import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.icu.text.SimpleDateFormat
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.boardgamecollector.databinding.ActivityGameInfoBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

class GameInfoActivity : AppCompatActivity() {
    companion object{
        private class GameInfoViewModel(private val dbHelper: MyDBHelper): ViewModel(){
            private val _artistsAll: MutableLiveData<ArrayList<Person>> by lazy {
                MutableLiveData<ArrayList<Person>>().also { it.postValue(ArrayList())}
            }
            val artistsAll: LiveData<ArrayList<Person>> get() = _artistsAll

            private val _artistsChosen: MutableLiveData<ArrayList<Person>> by lazy {
                MutableLiveData<ArrayList<Person>>().also { it.postValue(ArrayList())}
            }
            val artistsChosen: LiveData<ArrayList<Person>> get() = _artistsChosen

            private val _designersAll: MutableLiveData<ArrayList<Person>> by lazy {
                MutableLiveData<ArrayList<Person>>().also { it.postValue(ArrayList())}
            }
            val designersAll: LiveData<ArrayList<Person>> get() = _designersAll

            private val _designersChosen: MutableLiveData<ArrayList<Person>> by lazy {
                MutableLiveData<ArrayList<Person>>().also { it.postValue(ArrayList()) }
            }
            val designersChosen: LiveData<ArrayList<Person>> get() = _designersChosen

            private val _locations: MutableLiveData<ArrayList<Location>> by lazy {
                MutableLiveData<ArrayList<Location>>().also { it.postValue(ArrayList())}
            }
            val locations: LiveData<ArrayList<Location>> get() = _locations

            private val _location: MutableLiveData<Location>by lazy {
                MutableLiveData<Location>().also { it.postValue(Location(0,""))}
            }
            val location: LiveData<Location> get() = _location

            private val _img: MutableLiveData<Bitmap> = MutableLiveData()
            val img: LiveData<Bitmap> get() = _img

            private val _hasImg: MutableLiveData<Boolean> = MutableLiveData()
            val hasImg: LiveData<Boolean> get() = _hasImg

            fun choseArtist(person: Person){
                if(_artistsChosen.value?.contains(person) == false) {
                    _artistsChosen.value?.add(person)
                    _artistsChosen.postValue(_artistsChosen.value)
                }
            }

            fun removeArtist(idx: Int){
                _artistsChosen.value?.removeAt(idx)
                _artistsChosen.postValue(_artistsChosen.value)
            }

            fun changeArtist(idx: Int, person: Person){
                _artistsChosen.value?.set(idx, person)
                _artistsChosen.postValue(_artistsChosen.value)
            }

            private val _loading: MutableLiveData<Boolean> = MutableLiveData()
            val loading: LiveData<Boolean> get() = _loading

            private val _saving: MutableLiveData<Int> = MutableLiveData()
            val saving: LiveData<Int> get() = _saving

            private val _game: MutableLiveData<GameData> = MutableLiveData()
            val game: LiveData<GameData> get() = _game

            private fun loadArtists(){
                val db = dbHelper.readableDatabase
                val projection = arrayOf(GamesCollector.ArtistsEntry.COLUMN_ARTIST_ID, GamesCollector.ArtistsEntry.COLUMN_ARTIST_NAME)
                val cursor = db.query(
                    GamesCollector.ArtistsEntry.TABLE_NAME,
                    projection,
                    null,
                    null,
                    null,
                    null,
                    null
                )

                val arr = ArrayList<Person>()

                with(cursor){
                    while (moveToNext()){
                        val person = Person(getLong(getColumnIndex(GamesCollector.ArtistsEntry.COLUMN_ARTIST_ID)), getString(getColumnIndex(GamesCollector.ArtistsEntry.COLUMN_ARTIST_NAME)))
                        arr.add(person)
                    }
                }

                _artistsAll.postValue(arr)
            }

            fun choseDesigner(person: Person){
                if(_designersChosen.value?.contains(person) == false) {
                    _designersChosen.value?.add(person)
                    _designersChosen.postValue(_designersChosen.value)
                }
            }

            fun removeDesigner(idx: Int){
                _designersChosen.value?.removeAt(idx)
                _designersChosen.postValue(_designersChosen.value)
            }

            fun changeDesigner(idx: Int, person: Person){
                _designersChosen.value?.set(idx, person)
                _designersChosen.postValue(_designersChosen.value)
            }

            private fun loadDesigners(){
                val db = dbHelper.readableDatabase
                val projection = arrayOf(GamesCollector.DesignersEntry.COLUMN_DESIGNER_ID, GamesCollector.DesignersEntry.COLUMN_DESIGNER_NAME)
                val cursor = db.query(
                    GamesCollector.DesignersEntry.TABLE_NAME,
                    projection,
                    null,
                    null,
                    null,
                    null,
                    null
                )
                val arr = ArrayList<Person>()

                with(cursor){
                    while (moveToNext()){
                        val person = Person(getLong(getColumnIndex(GamesCollector.DesignersEntry.COLUMN_DESIGNER_ID)), getString(getColumnIndex(GamesCollector.DesignersEntry.COLUMN_DESIGNER_NAME)))
                        arr.add(person)
                    }
                }

                _designersAll.postValue(arr)
            }

            private fun loadLocations(){
                val db = dbHelper.readableDatabase
                val projection = arrayOf(GamesCollector.LocationEntry.COLUMN_LOCATION_ID, GamesCollector.LocationEntry.COLUMN_LOCATION)
                val cursor = db.query(
                    GamesCollector.LocationEntry.TABLE_NAME,
                    projection,
                    null,
                    null,
                    null,
                    null,
                    null
                )

                val arr = ArrayList<Location>()

                with(cursor){
                    while (moveToNext()){
                        val person = Location(getLong(getColumnIndex(GamesCollector.LocationEntry.COLUMN_LOCATION_ID)), getString(getColumnIndex(GamesCollector.LocationEntry.COLUMN_LOCATION)))
                        arr.add(person)
                    }
                }

                _locations.postValue(arr)
            }

            private fun loadGame(gameId: Long){
                val db = dbHelper.readableDatabase
                val projection = arrayOf(
                    GamesCollector.GamesEntry.COLUMN_TITLE,
                    GamesCollector.GamesEntry.COLUMN_TITLE_ORIGINAL,
                    GamesCollector.GamesEntry.COLUMN_RELEASE_YEAR,
                    GamesCollector.GamesEntry.COLUMN_DESCRIPTION,
                    GamesCollector.GamesEntry.COLUMN_ORDER_DATE,
                    GamesCollector.GamesEntry.COLUMN_DELIVERY_DATE,
                    GamesCollector.GamesEntry.COLUMN_PAID_PRICE,
                    GamesCollector.GamesEntry.COLUMN_SUGGESTED_PRICE,
                    GamesCollector.GamesEntry.COLUMN_EAN_CODE,
                    GamesCollector.GamesEntry.COLUMN_BGG_ID,
                    GamesCollector.GamesEntry.COLUMN_PRODUCTION_CODE,
                    GamesCollector.GamesEntry.COLUMN_CURRENT_RANK,
                    GamesCollector.GamesEntry.COLUMN_TYPE,
                    GamesCollector.GamesEntry.COLUMN_COMMENT,
                    GamesCollector.GamesEntry.COLUMN_IMG,
                    GamesCollector.GamesEntry.COLUMN_HAS_IMG
                )
                var cursor = db.query(
                    GamesCollector.GamesEntry.TABLE_NAME,
                    projection,
                    "${GamesCollector.GamesEntry.GAME_ID} = ?",
                    arrayOf(gameId.toString()),
                    null,
                    null,
                    null
                )
                cursor.moveToNext()
                val format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                val hImg = cursor.getInt(cursor.getColumnIndex(GamesCollector.GamesEntry.COLUMN_HAS_IMG)).let { return@let it == 1 }
                val game = GameData(
                    gameId = gameId,
                    title = cursor.getString(cursor.getColumnIndex( GamesCollector.GamesEntry.COLUMN_TITLE)),
                    originalTitle = cursor.getString(cursor.getColumnIndex( GamesCollector.GamesEntry.COLUMN_TITLE_ORIGINAL)),
                    yearPublished = cursor.getInt(cursor.getColumnIndex(GamesCollector.GamesEntry.COLUMN_RELEASE_YEAR)),
                    description = cursor.getString(cursor.getColumnIndex(GamesCollector.GamesEntry.COLUMN_DESCRIPTION)),
                    ordered = Calendar.getInstance().let {
                        val ld = LocalDate.parse(cursor.getString(cursor.getColumnIndex(GamesCollector.GamesEntry.COLUMN_ORDER_DATE)), format)
                        it.set(ld.year, ld.monthValue-1, ld.dayOfMonth)
                        it.time
                    },
                    delivered = Calendar.getInstance().let {
                        val ld = LocalDate.parse(cursor.getString(cursor.getColumnIndex(GamesCollector.GamesEntry.COLUMN_DELIVERY_DATE)), format)
                        it.set(ld.year, ld.monthValue-1, ld.dayOfMonth)
                        it.time
                    },
                    paidPrice = cursor.getString(cursor.getColumnIndex(GamesCollector.GamesEntry.COLUMN_PAID_PRICE)),
                    suggestedPrice = cursor.getString(cursor.getColumnIndex(GamesCollector.GamesEntry.COLUMN_SUGGESTED_PRICE)),
                    eanCode = cursor.getInt(cursor.getColumnIndex(GamesCollector.GamesEntry.COLUMN_EAN_CODE)),
                    bggId = cursor.getInt(cursor.getColumnIndex(GamesCollector.GamesEntry.COLUMN_BGG_ID)),
                    productionCode = cursor.getString(cursor.getColumnIndex(GamesCollector.GamesEntry.COLUMN_PRODUCTION_CODE)),
                    currentRank = cursor.getInt(cursor.getColumnIndex(GamesCollector.GamesEntry.COLUMN_CURRENT_RANK)),
                    type = cursor.getInt(cursor.getColumnIndex(GamesCollector.GamesEntry.COLUMN_TYPE)),
                    comment = cursor.getString(cursor.getColumnIndex(GamesCollector.GamesEntry.COLUMN_COMMENT)),
                    hasImg = hImg,
                    img = if(hImg) cursor.getBlob(cursor.getColumnIndex(GamesCollector.GamesEntry.COLUMN_IMG)).let {
                        val op = BitmapFactory.Options()
                            BitmapFactory.decodeByteArray(it, 0, it.size, op)
                    } else null
                )
                _hasImg.postValue(hImg)
                cursor.close()

                cursor = db.query(
                        GamesCollector.GameArtistsEntry.TABLE_NAME,
                        arrayOf(GamesCollector.GameArtistsEntry.COLUMN_ARTIST_ID),
                        "${GamesCollector.GameArtistsEntry.COLUMN_GAME_ID} = ?",
                        arrayOf(gameId.toString()),
                        null,
                        null,
                        null
                )

                val arrArtId = Array(cursor.count){
                    cursor.moveToNext()
                    cursor.getLong(cursor.getColumnIndex(GamesCollector.GameArtistsEntry.COLUMN_ARTIST_ID))
                }

                val arrListArt = ArrayList<Person>()
                arrArtId.iterator().forEach {
                    cursor.close()
                    cursor = db.query(
                        GamesCollector.ArtistsEntry.TABLE_NAME,
                        arrayOf(GamesCollector.ArtistsEntry.COLUMN_ARTIST_NAME),
                        "${GamesCollector.ArtistsEntry.COLUMN_ARTIST_ID} = ?",
                        arrayOf(it.toString()),
                        null,
                        null,
                        null
                    )
                    cursor.moveToNext()
                    arrListArt.add(Person(it, cursor.getString(cursor.getColumnIndex(GamesCollector.ArtistsEntry.COLUMN_ARTIST_NAME))))
                }
                cursor.close()

                _artistsChosen.postValue(arrListArt)

                cursor = db.query(
                        GamesCollector.GameDesignersEntry.TABLE_NAME,
                        arrayOf(GamesCollector.GameDesignersEntry.COLUMN_DESIGNER_ID),
                        "${GamesCollector.GameDesignersEntry.COLUMN_GAME_ID} = ?",
                        arrayOf(gameId.toString()),
                        null,
                        null,
                        null
                )

                val arrDesId = Array(cursor.count){
                    cursor.moveToNext()
                    cursor.getLong(cursor.getColumnIndex(GamesCollector.GameDesignersEntry.COLUMN_DESIGNER_ID))
                }

                val arrListDes = ArrayList<Person>()
                arrDesId.iterator().forEach {
                    cursor.close()
                    cursor = db.query(
                        GamesCollector.DesignersEntry.TABLE_NAME,
                        arrayOf(GamesCollector.DesignersEntry.COLUMN_DESIGNER_NAME),
                        "${GamesCollector.DesignersEntry.COLUMN_DESIGNER_ID} = ?",
                        arrayOf(it.toString()),
                        null,
                        null,
                        null
                    )
                    cursor.moveToNext()
                    arrListDes.add(Person(it, cursor.getString(cursor.getColumnIndex(GamesCollector.DesignersEntry.COLUMN_DESIGNER_NAME))))
                }
                cursor.close()

                _designersChosen.postValue(arrListDes)

                cursor = db.query(
                        GamesCollector.GamesLocationsEntry.TABLE_NAME,
                        arrayOf(GamesCollector.GamesLocationsEntry.COLUMN_LOCATION_ID, GamesCollector.GamesLocationsEntry.COLUMN_COMMENT),
                        "${GamesCollector.GamesLocationsEntry.COLUMN_GAME_ID} = ?",
                        arrayOf(gameId.toString()),
                        null,
                        null,
                        null
                )

                cursor.moveToNext()
                if(!cursor.isAfterLast) {
                    val locationId = cursor.getLong(cursor.getColumnIndex(GamesCollector.GamesLocationsEntry.COLUMN_LOCATION_ID))
                    game.locationComment = cursor.getString(cursor.getColumnIndex(GamesCollector.GamesLocationsEntry.COLUMN_COMMENT))
                    cursor.close()

                    cursor = db.query(
                            GamesCollector.LocationEntry.TABLE_NAME,
                            arrayOf(GamesCollector.LocationEntry.COLUMN_LOCATION),
                            "${GamesCollector.LocationEntry.COLUMN_LOCATION_ID} = ?",
                            arrayOf(locationId.toString()),
                            null,
                            null,
                            null
                    )

                    cursor.moveToNext()
                    _location.postValue(Location(locationId, cursor.getString(cursor.getColumnIndex(GamesCollector.LocationEntry.COLUMN_LOCATION))))
                }
                cursor.close()

                cursor = db.query(
                        GamesCollector.RanksEntry.TABLE_NAME,
                        arrayOf(GamesCollector.RanksEntry.COLUMN_RANK, GamesCollector.RanksEntry.COLUMN_DATE),
                        "${GamesCollector.RanksEntry.COLUMN_GAME_ID} = ?",
                        arrayOf(gameId.toString()),
                        null,
                        null,
                        "${GamesCollector.RanksEntry.COLUMN_DATE} DESC"
                )

                game.ranks = Array<Rank>(cursor.count){
                    cursor.moveToNext()
                    Rank(cursor.getInt(cursor.getColumnIndex(GamesCollector.RanksEntry.COLUMN_RANK)),
                            Calendar.getInstance().let{
                                val ld = LocalDate.parse(cursor.getString(cursor.getColumnIndex(GamesCollector.RanksEntry.COLUMN_DATE)), format)
                                it.set(ld.year, ld.monthValue, ld.dayOfMonth)
                                it.time
                            }
                    )
                }
                cursor.close()
                _game.postValue(game)
            }

            fun loadStartingData(gameId: Long){
                viewModelScope.launch(Dispatchers.IO){
                    _loading.postValue(true)
                    loadArtists()
                    loadDesigners()
                    loadLocations()
                    loadGame(gameId)
                    _loading.postValue(false)
                }
            }

            fun loadBitmapFromUrl(src: String){
                viewModelScope.launch(Dispatchers.IO){
                    val bitmap = getBitmapFromURL(src)
                    if(bitmap!=null) {
                        _img.postValue(bitmap)
                        _hasImg.postValue(true)
                    }else{
                        _hasImg.postValue(false)
                    }
                }
            }

            fun setBitmap(bitmap: Bitmap){
                _img.postValue(bitmap)
                _hasImg.postValue(true)
            }

            fun save(title: String,
                     originalTitle: String,
                     published: Int,
                     description: String,
                     ordered: Date,
                     delivered: Date,
                     paid: String,
                     suggested: String,
                     ean: Int,
                     bgg: Int,
                     production: String,
                     type: Int,
                     comment: String,
                     hasImg: Boolean,
                     img: Bitmap?,
                     locationID: Long,
                     locationComment: String
            ){
                viewModelScope.launch {
                    val game = game.value!!
                    game.title = title
                    game.originalTitle = originalTitle
                    game.yearPublished = published
                    game.description = description
                    game.ordered = ordered
                    game.delivered = delivered
                    game.paidPrice = paid
                    game.suggestedPrice = suggested
                    game.eanCode = ean
                    game.bggId = bgg
                    game.productionCode = production
                    game.type = type
                    game.comment = comment
                    game.hasImg = hasImg
                    game.img = img

                    updateGame(dbHelper.writableDatabase, game)

                    if(locationInDB(dbHelper.readableDatabase, locationID))
                        putGameLocation(dbHelper.writableDatabase, game.gameId!! , locationID, locationComment)
                    changeGameArtists(dbHelper.writableDatabase, game.gameId!!, artistsChosen.value!!)
                    changeGameDesigners(dbHelper.writableDatabase, game.gameId!!, designersChosen.value!!)

                    _saving.postValue(1)
                }
            }
        }
    }
    private lateinit var binding: ActivityGameInfoBinding
    private lateinit var viewModel: GameInfoViewModel

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            val bitmap = decodeUri(uri, contentResolver)
            if(bitmap != null)
                viewModel.setBitmap(bitmap)
            else
                Toast.makeText(this, R.string.error_occ, Toast.LENGTH_SHORT).show()
        }else{
            Toast.makeText(this, R.string.error_occ, Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameInfoBinding.inflate(layoutInflater)

        setContentView(binding.root)
        val dbHelper = MyDBHelper.getInstance(this)
        viewModel = GameInfoViewModel(dbHelper)
        val loadingDialog = LoadingDialog(this)

        val gameID = intent.getLongExtra("gameId",0)

        var gameLocationID = 0.toLong()

        binding.btnReturn.setOnClickListener {
            this.finish()
        }

        binding.btnChangeGame.setOnClickListener {
            setResult(Activity.RESULT_OK)
            loadingDialog.startLoadingDialog()

            viewModel.save(
                    title = binding.txtInChangeGameTitle.text.toString(),
                    originalTitle = binding.txtInChangeGameOriginalTitle.text.toString(),
                    published = binding.txtInChangeGameYear.text.toString().toInt(),
                    description = binding.txtInGameChangeDescription.text.toString(),
                    ordered = Calendar.getInstance().let {
                         it.set(
                                binding.dpChangeGameOrdered.year,
                                binding.dpChangeGameOrdered.month,
                                binding.dpChangeGameOrdered.dayOfMonth
                         )
                        it.time
                    },
                    delivered = Calendar.getInstance().let {
                         it.set(
                                binding.dpChangeGameReceived.year,
                                binding.dpChangeGameReceived.month,
                                binding.dpChangeGameReceived.dayOfMonth
                         )
                        it.time
                    },
                    paid = binding.txtInChangeGamePaid.text.toString(),
                    suggested = binding.txtInChangeGameSuggestedPrice.text.toString(),
                    ean = binding.txtInChangeGameEan.text.toString().toInt(),
                    bgg = binding.txtInChangeGameBGGId.text.toString().toInt(),
                    production = binding.txtInChangeGameProductionCode.text.toString(),
                    type = binding.spinnerChangeGameType.selectedItemPosition,
                    comment = binding.txtInChangeGameComment.text.toString(),
                    hasImg = binding.cbChangeGameHasImage.isChecked,
                    img = if (binding.cbChangeGameHasImage.isChecked) viewModel.game.value!!.img else null,
                    locationID =  gameLocationID,
                    locationComment = binding.txtInChangeGameLocationComment.text.toString()
            )

            viewModel.saving.observe(this){
                loadingDialog.dissmisDialog()
                this.finish()
            }
        }

        binding.btnDeleteGame.setOnClickListener {
            deleteGame(dbHelper.writableDatabase, gameID)
            setResult(Activity.RESULT_OK)
            this.finish()
        }

        binding.tvChangeGameLocation.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle(R.string.change_location)

            val lvLocations =  ListView(this);

            ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, Array<String>(viewModel.locations.value!!.size){
                viewModel.locations.value!![it].name
            }).also {
                lvLocations.adapter = it
            }


            builder.setView(lvLocations)
            val d = builder.create()

            lvLocations.setOnItemClickListener { _, _, position, _ ->
                gameLocationID = viewModel.locations.value!![position].id
                binding.tvChangeGameLocation.text = getString(R.string.location) + ": "+viewModel.locations.value!![position].name
                d.dismiss()
            }

            d.show()
        }

        val spinner: Spinner = binding.spinnerChangeGameType


        binding.tvCurrentRank.setOnClickListener {
            val builder = AlertDialog.Builder(this)

            val lvRanks =  ListView(this);

            ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, Array<String>(viewModel.game.value!!.ranks!!.size){
                viewModel.game.value!!.ranks!![it].rank.toString() +" "+ viewModel.game.value!!.ranks!![it].date.toString()
            }).also {
                lvRanks.adapter = it
            }


            builder.setView(lvRanks)
            builder.show()
        }

        ArrayAdapter.createFromResource(
                this,
                R.array.type_options,
                android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }
        viewModel.location.observe(this){
            gameLocationID = it.id
            binding.tvChangeGameLocation.text = getString(R.string.location) + ": " + it.name
        }

        viewModel.artistsChosen.observe(this){ value ->
            value.let {
                val artists: Array<String> = Array(it.size){ idx ->
                    "${it[idx].name}, ${getString(R.string.id)}: ${it[idx].id}"
                }

                ArrayAdapter(this, android.R.layout.simple_list_item_1, artists).also { adapter ->
                    binding.lvChangeGameArtists.adapter = adapter
                }

                setListViewHeightBasedOnChildren(binding.lvChangeGameArtists)
            }
        }

        viewModel.designersChosen.observe(this){ value ->
            value.let {
                val designers: Array<String> = Array(it.size){ idx ->
                    "${it[idx].name}, ${getString(R.string.id)}: ${it[idx].id}"
                }

                ArrayAdapter(this, android.R.layout.simple_list_item_1, designers).also { adapter ->
                    binding.lvChageGameDesigners.adapter = adapter
                }

                setListViewHeightBasedOnChildren(binding.lvChageGameDesigners)
            }
        }

        viewModel.loading.observe(this){ value ->
            value.let {
                if(it){
                    loadingDialog.startLoadingDialog()
                }else{
                    loadingDialog.dissmisDialog()
                }
            }
        }

        viewModel.hasImg.observe(this){
            binding.cbChangeGameHasImage.isChecked = it
        }

        viewModel.img.observe(this){
            if(viewModel.img.value!=null) {
                binding.cbChangeGameHasImage.isChecked = true
                binding.ivChangeGameIMG.setImageBitmap(viewModel.img.value)
            }
        }

        binding.cbChangeGameHasImage.setOnCheckedChangeListener { _, isChecked ->
            if(!isChecked){
                binding.btnChangeGameLoadFromURL.visibility = View.GONE
                //binding.btnChangeGameLoadImage.visibility = View.GONE
                binding.urlHolder.visibility = View.GONE
                binding.ivChangeGameIMG.visibility = View.GONE
            }else{
                binding.btnChangeGameLoadFromURL.visibility = View.VISIBLE
                //binding.btnChangeGameLoadImage.visibility = View.VISIBLE
                binding.ivChangeGameIMG.visibility = View.VISIBLE
                binding.urlHolder.visibility = View.VISIBLE
            }
        }

        binding.btnChangeGameLoadFromURL.setOnClickListener {
            val src = binding.txtInChangeGameIMGURL.text?.toString()
            if(src != null)
                viewModel.loadBitmapFromUrl(src)
            else
                Toast.makeText(this, R.string.error_occ, Toast.LENGTH_SHORT).show()
        }

        binding.btnChangeGameAddArtist.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle(getString(R.string.title_add_artist))

            val inflated = LayoutInflater.from(this).inflate(R.layout.person_dialog, null,false)
            val inID: AutoCompleteTextView = inflated.findViewById(R.id.inID)
            val inName: AutoCompleteTextView = inflated.findViewById(R.id.inName)

            val artists: Array<Person> = viewModel.artistsAll.value?.let { it1 ->
                Array<Person>(it1.size){
                    it1[it]
                }
            }?: Array(0){ Person(0,"") }

            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, Array(artists.size){
                artists[it].id
            }).also {
                inID.setAdapter(it)
            }

            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, Array(artists.size){
                artists[it].name
            }).also {
                inName.setAdapter(it)
            }

            inID.onItemClickListener = AdapterView.OnItemClickListener{ _, _, position, _ ->
                inName.setSelection(position)
            }

            inName.onItemClickListener = AdapterView.OnItemClickListener{ _, _, position, _ ->
                inID.setSelection(position)
            }

            builder.setView(inflated)

            builder.setPositiveButton(android.R.string.ok) { dialogInterface: DialogInterface, _ ->
                dialogInterface.dismiss()
                viewModel.choseArtist(Person(inID.text.toString().toLong(), inName.text.toString()))
            }

            builder.setNegativeButton(android.R.string.cancel) { dialogInterface: DialogInterface, _ ->
                dialogInterface.cancel()
            }

            builder.create().show()
        }

        binding.lvChangeGameArtists.setOnItemClickListener { _, _, position, _ ->
            val builder = AlertDialog.Builder(this)
            builder.setTitle(getString(R.string.title_change_artist))

            val inflated = LayoutInflater.from(this).inflate(R.layout.person_dialog, null,false)
            val inID: AutoCompleteTextView = inflated.findViewById(R.id.inID)
            val inName: AutoCompleteTextView = inflated.findViewById(R.id.inName)

            val artists: Array<Person> = viewModel.artistsAll.value?.let { it1 ->
                Array<Person>(it1.size){
                    it1[it]
                }
            }?: Array(0){ Person(0,"") }

            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, Array(artists.size){
                artists[it].id
            }).also {
                inID.setAdapter(it)
            }

            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, Array(artists.size){
                artists[it].name
            }).also {
                inName.setAdapter(it)
            }

            inID.onItemClickListener = AdapterView.OnItemClickListener{ _, _, position, _ ->
                inName.setSelection(position)
            }

            inID.setText(viewModel.artistsChosen.value?.get(position)?.id.toString())
            if(inID.text.toString() == "null" ){
                inID.setText("")
            }

            inName.onItemClickListener = AdapterView.OnItemClickListener{ _, _, position, _ ->
                inID.setSelection(position)
            }

            inName.setText(viewModel.artistsChosen.value?.get(position)?.name ?: "")

            builder.setView(inflated)

            builder.setPositiveButton(android.R.string.ok) { dialogInterface: DialogInterface, _ ->
                viewModel.changeArtist(position, Person(inID.text.toString().toLong(), inName.text.toString()))
                dialogInterface.dismiss()
            }

            builder.setNegativeButton(R.string.remove) { dialogInterface: DialogInterface, _ ->
                viewModel.removeArtist(position)
                dialogInterface.cancel()
            }

            builder.create().show()
        }

        binding.lvChageGameDesigners.setOnItemClickListener { _, _, position, _ ->
            val builder = AlertDialog.Builder(this)
            builder.setTitle(getString(R.string.title_change_designer))

            val inflated = LayoutInflater.from(this).inflate(R.layout.person_dialog, null,false)
            val inID: AutoCompleteTextView = inflated.findViewById(R.id.inID)
            val inName: AutoCompleteTextView = inflated.findViewById(R.id.inName)

            val designer: Array<Person> = viewModel.designersAll.value?.let { it1 ->
                Array<Person>(it1.size){
                    it1[it]
                }
            }?: Array(0){ Person(0,"") }

            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, Array(designer.size){
                designer[it].id
            }).also {
                inID.setAdapter(it)
            }

            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, Array(designer.size){
                designer[it].name
            }).also {
                inName.setAdapter(it)
            }

            inID.onItemClickListener = AdapterView.OnItemClickListener{ _, _, position, _ ->
                inName.setSelection(position)
            }
            inID.setText(viewModel.designersChosen.value?.get(position)?.id.toString())
            if(inID.text.toString() == "null" ){
                inID.setText("")
            }

            inName.onItemClickListener = AdapterView.OnItemClickListener{ _, _, position, _ ->
                inID.setSelection(position)
            }
            inName.setText(viewModel.designersChosen.value?.get(position)?.name ?: "")

            builder.setView(inflated)

            builder.setPositiveButton(android.R.string.ok) { dialogInterface: DialogInterface, _ ->
                viewModel.changeDesigner(position, Person(inID.text.toString().toLong(), inName.text.toString()))
                dialogInterface.dismiss()
            }

            builder.setNegativeButton(R.string.remove) { dialogInterface: DialogInterface, _ ->
                viewModel.removeDesigner(position)
                dialogInterface.cancel()
            }

            builder.create().show()
        }

        binding.btnChangeGameAddDesigner.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle(getString(R.string.title_add_designer))

            val inflated = LayoutInflater.from(this).inflate(R.layout.person_dialog, null,false)
            val inID: AutoCompleteTextView = inflated.findViewById(R.id.inID)
            val inName: AutoCompleteTextView = inflated.findViewById(R.id.inName)

            val designer: Array<Person> = viewModel.designersAll.value?.let { it1 ->
                Array<Person>(it1.size){
                    it1[it]
                }
            }?: Array(0){ Person(0,"") }

            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, Array(designer.size){
                designer[it].id
            }).also {
                inID.setAdapter(it)
            }

            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, Array(designer.size){
                designer[it].name
            }).also {
                inName.setAdapter(it)
            }

            inID.onItemClickListener = AdapterView.OnItemClickListener{ _, _, position, _ ->
                inName.setSelection(position)
            }

            inName.onItemClickListener = AdapterView.OnItemClickListener{ _, _, position, _ ->
                inID.setSelection(position)
            }

            builder.setView(inflated)

            builder.setPositiveButton(android.R.string.ok) { dialogInterface: DialogInterface, _ ->
                dialogInterface.dismiss()
                viewModel.choseDesigner(Person(inID.text.toString().toLong(), inName.text.toString()))
            }

            builder.setNegativeButton(android.R.string.cancel) { dialogInterface: DialogInterface, _ ->
                dialogInterface.cancel()
            }

            builder.create().show()
        }

        viewModel.game.observe(this){ game ->
            binding.txtInChangeGameTitle.setText(game.title)
            binding.txtInChangeGameOriginalTitle.setText(game.originalTitle)
            binding.txtInChangeGameYear.setText(game.yearPublished.toString())
            binding.txtInGameChangeDescription.setText(game.description)
            val cal = Calendar.getInstance().also { it.time = game.ordered?: Date()}
            binding.dpChangeGameOrdered.updateDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
            cal.time = game.delivered?: Date()
            binding.dpChangeGameReceived.updateDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
            binding.txtInChangeGamePaid.setText(game.paidPrice.toString())
            binding.txtInChangeGameSuggestedPrice.setText(game.suggestedPrice.toString())
            binding.txtInChangeGameEan.setText(game.eanCode.toString())
            binding.txtInChangeGameBGGId.setText(game.bggId.toString())
            binding.txtInChangeGameProductionCode.setText(game.productionCode)
            binding.tvCurrentRank.text = "${getText(R.string.rank)}: ${game.currentRank}"
            binding.spinnerChangeGameType.setSelection(game.type?:0)
            binding.txtInChangeGameComment.setText(game.comment)
            if(game.hasImg==true){
                binding.ivChangeGameIMG.setImageBitmap(game.img)
            }
            binding.txtInChangeGameLocationComment.setText(game.locationComment)
        }

        viewModel.loadStartingData(gameID)
    }
}