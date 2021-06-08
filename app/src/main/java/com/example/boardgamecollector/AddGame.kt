package com.example.boardgamecollector

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.BaseColumns
import android.util.Log
import android.view.LayoutInflater
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.boardgamecollector.databinding.ActivityAddGameBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList

class AddGame : AppCompatActivity() {
    companion object{

        private class AddGameViewModel(private val dbHelper: MyDBHelper): ViewModel() {
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

            private val _img: MutableLiveData<Bitmap> = MutableLiveData()
            val img: LiveData<Bitmap> get() = _img

            private val _hasImg: MutableLiveData<Boolean> = MutableLiveData()
            val hasImg: LiveData<Boolean> get() = _hasImg

            var basicGameData: BasicGameData? = null

            fun choseArtist(person: Person){
                if(_artistsChosen.value?.contains(person) == false) {
                    _artistsChosen.value?.add(person)
                    _artistsChosen.postValue(_artistsChosen.value)
                }
            }

            fun removeArtist(idx: Int){
                _artistsChosen.value?.removeAt(idx)
            }

            fun changeArtist(idx: Int, person: Person){
                _artistsChosen.value?.set(idx, person)
            }

            private val _loading: MutableLiveData<Boolean> = MutableLiveData()
            val loading: LiveData<Boolean> get() = _loading

            private val _saving: MutableLiveData<Int> = MutableLiveData()
            val saving: LiveData<Int> get() = _saving

            val rowId: MutableLiveData<Long> = MutableLiveData()

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
                        val person = Person(getInt(getColumnIndex(GamesCollector.ArtistsEntry.COLUMN_ARTIST_ID)), getString(getColumnIndex(GamesCollector.ArtistsEntry.COLUMN_ARTIST_NAME)))
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
            }

            fun changeDesigner(idx: Int, person: Person){
                _designersChosen.value?.set(idx, person)
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
                        val person = Person(getInt(getColumnIndex(GamesCollector.DesignersEntry.COLUMN_DESIGNER_ID)), getString(getColumnIndex(GamesCollector.DesignersEntry.COLUMN_DESIGNER_NAME)))
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
                        val person = Location(getInt(getColumnIndex(GamesCollector.LocationEntry.COLUMN_LOCATION_ID)), getString(getColumnIndex(GamesCollector.LocationEntry.COLUMN_LOCATION)))
                        arr.add(person)
                    }
                }

                _locations.postValue(arr)
            }

            fun loadStartingData(){
                viewModelScope.launch(Dispatchers.IO){
                    _loading.postValue(true)
                    loadArtists()
                    loadDesigners()
                    loadLocations()
                    _loading.postValue(false)
                }
            }

            fun clearOnSearch(){
                _artistsChosen.postValue(ArrayList())
                _designersChosen.postValue(ArrayList())
                basicGameData = null
                _img.postValue(null)
                _hasImg.postValue(false)
            }

            fun loadBitmapFromUrl(src: String){
                viewModelScope.launch(Dispatchers.IO){
                    val bitmap = getBitmapFromURL(src)
                    if(bitmap!=null) {
                        _img.postValue(bitmap)
                        _hasImg.postValue(true)
                    }
                }
            }

            fun getLocationId(name: String): Int{
                for(i in 0 until locations.value!!.size){
                    if(locations.value!![i].name == name){
                        return locations.value!![i].id
                    }
                }
                return locations.value!!.size
            }

            fun setBitmap(bitmap: Bitmap){
                _img.postValue(bitmap)
                _hasImg.postValue(true)
            }

            fun addGame(gameData: GameData){
                viewModelScope.launch(Dispatchers.IO) {
                    _saving.postValue(1)
                    try{

                        val db = dbHelper.writableDatabase
                        rowId.postValue(putGame(db, gameData))



                        _saving.postValue(0)
                    }catch (e: Exception){
                        Log.i("addGame", e.stackTraceToString())
                        _saving.postValue(-1)
                    }
                }
            }
        }
    }
    private lateinit var binding: ActivityAddGameBinding
    private lateinit var viewModel: AddGameViewModel
    private lateinit var xmlParser: XMLParser


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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = AddGameViewModel(MyDBHelper.getInstance(this))

        binding = ActivityAddGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val spinner: Spinner = binding.spinnerType

        ArrayAdapter.createFromResource(
                this,
                R.array.type_options,
                android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }

        viewModel.locations.observe(this){ value ->
            value.let{
                val locations: Array<String> = Array(it.size){ idx ->
                    it[idx].name
                }

                ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, locations).also { adapter ->
                    binding.txtInLocation.setAdapter(adapter)
                }
            }
        }

        viewModel.artistsChosen.observe(this){ value ->
            value.let {
                val artists: Array<String> = Array(it.size){ idx ->
                    "${it[idx].name}, ${getString(R.string.id)}: ${it[idx].id}"
                }

                ArrayAdapter(this, android.R.layout.simple_list_item_1, artists).also { adapter ->
                    binding.ArtistsList.adapter = adapter
                }

                setListViewHeightBasedOnChildren(binding.ArtistsList)
            }
        }

        viewModel.designersChosen.observe(this){ value ->
            value.let {
                val designers: Array<String> = Array(it.size){ idx ->
                    "${it[idx].name}, ${getString(R.string.id)}: ${it[idx].id}"
                }

                ArrayAdapter(this, android.R.layout.simple_list_item_1, designers).also { adapter ->
                    binding.DesignersList.adapter = adapter
                }

                setListViewHeightBasedOnChildren(binding.DesignersList)
            }
        }

        val loadingDialog = LoadingDialog(this)

        viewModel.loading.observe(this){ value ->
            value.let {
                if(it){
                    loadingDialog.startLoadingDialog()
                }else{
                    loadingDialog.dissmisDialog()
                }
            }
        }

        viewModel.img.observe(this){
            if(viewModel.img.value!=null)
                binding.imgLoadedImage.setImageBitmap(viewModel.img.value)
        }

        binding.btnAddArtist.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle(getString(R.string.title_add_artist))

            val inflated = LayoutInflater.from(this).inflate(R.layout.person_dialog, null,false)
            val inID: AutoCompleteTextView = inflated.findViewById(R.id.inID)
            val inName: AutoCompleteTextView = inflated.findViewById(R.id.inName)

            val artists: Array<Person> = viewModel.artistsAll.value!!.toTypedArray()

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
                viewModel.choseArtist(Person(inID.text.toString().toInt(), inName.text.toString()))
            }

            builder.setNegativeButton(android.R.string.cancel) { dialogInterface: DialogInterface, _ ->
                dialogInterface.cancel()
            }

            builder.create().show()
        }

        binding.btnAddDesigner.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle(getString(R.string.title_add_designer))

            val inflated = LayoutInflater.from(this).inflate(R.layout.person_dialog, null,false)
            val inID: AutoCompleteTextView = inflated.findViewById(R.id.inID)
            val inName: AutoCompleteTextView = inflated.findViewById(R.id.inName)

            val designer: Array<Person> = viewModel.designersAll.value!!.toTypedArray()

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
                viewModel.choseDesigner(Person(inID.text.toString().toInt(), inName.text.toString()))
            }

            builder.setNegativeButton(android.R.string.cancel) { dialogInterface: DialogInterface, _ ->
                dialogInterface.cancel()
            }

            builder.create().show()
        }

        viewModel.loadStartingData()

        xmlParser = XMLParser(this.applicationInfo.dataDir)

        xmlParser.processingForName.observe(this){ value ->
            value.let{
                if(it){
                    loadingDialog.setInfo("${getText(R.string.processing_data)}")
                }else{
                    if (xmlParser.loaded.value == 200){
                        loadingDialog.dissmisDialog()
                        val builder = AlertDialog.Builder(this)
                        builder.setTitle(getString(R.string.choose_game))

                        val inflated = LayoutInflater.from(this).inflate(R.layout.choose_game, null,false)
                        val listView: ListView = inflated.findViewById(R.id.lvGamesToChoose)
                        val arr = xmlParser.gamesBasic.value!!
                        ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, Array(arr.size){ i ->
                            "${getText(R.string.game_id)} ${arr[i].id}\n" +
                                    "${getText(R.string.game_name)} ${arr[i].name}\n" +
                                    "${getText(R.string.release_year)}: ${arr[i].yearPublished}"
                        }).also { adapter ->
                            listView.adapter = adapter
                        }

                        builder.setView(inflated)
                        val d = builder.create()

                        listView.setOnItemClickListener { _, _, position, _ ->
                            xmlParser.getGameByID(arr[position].id)
                            viewModel.basicGameData = arr[position]
                            d.dismiss()
                        }
                        d.show()
                    }
                    else if(xmlParser.loaded.value == -2){

                    }
                    else{
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

        xmlParser.processingForId.observe(this){value ->
            value.let{
                if(it){
                    loadingDialog.setInfo("${getText(R.string.processing_data)}")
                }else{
                    if (xmlParser.loaded.value == 200){
                        loadingDialog.dissmisDialog()
                        val gameData = xmlParser.game.value!!
                        binding.txtInGameTitle.setText(viewModel.basicGameData!!.name)
                        binding.txtInOriginalTitle.setText(gameData.originalTitle)
                        binding.txtInReleaseYear.setText(gameData.yearPublished.toString())
                        for (i in gameData.artists!!.indices){
                            viewModel.choseArtist(gameData.artists!![i])
                        }
                        for (i in gameData.designers!!.indices){
                            viewModel.choseDesigner(gameData.designers!![i])
                        }
                        binding.txtInDescription.setText(gameData.description)
                        binding.txtInBGGId.setText(gameData.bggId.toString())
                        binding.txtInRank.setText(gameData.ranks!![0].rank.toString())
                        if(gameData.img!=null) {
                            viewModel.setBitmap(gameData.img!!)
                        }
                    }
                    else if(xmlParser.loaded.value == -2){

                    }
                    else{
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

        xmlParser.retryingIn.observe(this){ value ->
            if(xmlParser.processingForName.value == false && xmlParser.processingForId.value == false)
                loadingDialog.setInfo("${getText(R.string.waiting_for_data)}\n${getText(R.string.retrying_in)} ${value}s")
        }

        binding.btnSearchBGG.setOnClickListener {
            xmlParser.getGamesByName(binding.txtInGameTitle.text.toString())
            viewModel.clearOnSearch()
            loadingDialog.startLoadingDialog()
            loadingDialog.setInfo("${getText(R.string.loading_data)}")
        }

        binding.btnLoadImage.setOnClickListener {
            getContent.launch("image/*")
        }

        binding.btnLoadImageURL.setOnClickListener {
            val src = binding.txtInImgUrl.text?.toString()
            if(src != null)
                viewModel.loadBitmapFromUrl(src)
            else
                Toast.makeText(this, R.string.error_occ, Toast.LENGTH_SHORT).show()
        }

        binding.btnConfirm.setOnClickListener {
            viewModel.addGame(GameData(title = binding.txtInGameTitle.text.toString(),
                    originalTitle = binding.txtInOriginalTitle.text.toString(),
                    yearPublished = binding.txtInReleaseYear.text.toString().toInt(),
                    description = binding.txtInDescription.text.toString(),
                    ordered = Calendar.getInstance().also { it.set(binding.dpOrdered.year, binding.dpOrdered.month, binding.dpOrdered.dayOfMonth) }.time ,
                    delivered = Calendar.getInstance().also { it.set(binding.dpReceived.year, binding.dpReceived.month, binding.dpReceived.dayOfMonth) }.time,
                    paidPrice = binding.txtInPaid.text.toString(),
                    suggestedPrice = binding.txtInSuggestedPrice.text.toString(),
                    eanCode = binding.txtInEAN.text.toString().toInt(),
                    bggId = binding.txtInBGGId.text.toString().toInt(),
                    productionCode = binding.txtInProductionCode.text.toString(),
                    currentRank = binding.txtInRank.text.toString().toInt(),
                    type = binding.spinnerType.selectedItemPosition,
                    comment = binding.txtInComment.text.toString(),
                    img = viewModel.img.value,
                    hasImg = viewModel.hasImg.value,
                    artists = Array(viewModel.artistsChosen.value!!.size){
                                                                         viewModel.artistsChosen.value!![it]
                    },
                    designers = Array(viewModel.designersChosen.value!!.size){
                                                                         viewModel.designersChosen.value!![it]
                    },
                    ranks = Array(1){
                                    Rank(binding.txtInRank.text.toString().toInt())
                    },
                    location = Location(viewModel.getLocationId(binding.txtInLocation.text.toString()),binding.txtInLocation.text.toString()),
                    locationComment = binding.txtInLocationComment.text.toString()
            ))
        }

        viewModel.saving.observe(this){
            when(it){
                1 ->{
                    loadingDialog.startLoadingDialog()
                    loadingDialog.setInfo(getString(R.string.adding))
                }
                0 ->{
                    loadingDialog.dissmisDialog()
                    val data = Intent()
                    data.putExtra("newGameId", viewModel.rowId.value)
                    setResult(Activity.RESULT_OK, data)
                    this.finish()
                }
                -1 ->{
                    loadingDialog.dissmisDialog()
                    Toast.makeText(this, R.string.error_occ, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}