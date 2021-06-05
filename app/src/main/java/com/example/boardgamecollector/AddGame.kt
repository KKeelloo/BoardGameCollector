package com.example.boardgamecollector

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.BaseColumns
import android.view.LayoutInflater
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Spinner
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.boardgamecollector.databinding.ActivityAddGameBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddGame : AppCompatActivity() {
    companion object{
        private class AddGameViewModel(private val dbHelper: MyDBHelper): ViewModel() {
            private val _artistsAll: MutableLiveData<ArrayList<Person>> by lazy {
                MutableLiveData<ArrayList<Person>>().also { it.value = ArrayList() }
            }
            val artistsAll: LiveData<ArrayList<Person>> get() = _artistsAll

            private val _artistsChosen: MutableLiveData<ArrayList<Person>> by lazy {
                MutableLiveData<ArrayList<Person>>().also { it.value = ArrayList() }
            }
            val artistsChosen: LiveData<ArrayList<Person>> get() = _artistsChosen

            private val _designersAll: MutableLiveData<ArrayList<Person>> by lazy {
                MutableLiveData<ArrayList<Person>>().also { it.value = ArrayList() }
            }
            val designersAll: LiveData<ArrayList<Person>> get() = _designersAll

            private val _designersChosen: MutableLiveData<ArrayList<Person>> by lazy {
                MutableLiveData<ArrayList<Person>>().also { it.value = ArrayList() }
            }
            val designersChosen: LiveData<ArrayList<Person>> get() = _designersChosen

            private val _locations: MutableLiveData<ArrayList<Location>> by lazy {
                MutableLiveData<ArrayList<Location>>().also { it.value = ArrayList() }
            }
            val locations: LiveData<ArrayList<Location>> get() = _locations


            fun choseArtist(person: Person){
                if(_artistsChosen.value?.contains(person) == false)
                    _artistsChosen.value?.add(person)
            }

            fun removeArtist(idx: Int){
                _artistsChosen.value?.removeAt(idx)
            }

            fun changeArtist(idx: Int, person: Person){
                _artistsChosen.value?.set(idx, person)
            }

            private val _loading: MutableLiveData<Boolean> = MutableLiveData()
            val loading: LiveData<Boolean> get() = _loading

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
                if(_designersChosen.value?.contains(person) == false)
                    _designersChosen.value?.add(person)
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
                val projection = arrayOf(BaseColumns._ID, GamesCollector.LocationEntry.COLUMN_LOCATION)
                val cursor = db.query(
                        GamesCollector.DesignersEntry.TABLE_NAME,
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
                        val person = Location(getInt(getColumnIndex(BaseColumns._ID)), getString(getColumnIndex(GamesCollector.LocationEntry.COLUMN_LOCATION)))
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
        }
    }
    private lateinit var binding: ActivityAddGameBinding
    private lateinit var viewModel: AddGameViewModel

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

        binding.btnAddArtist.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle(getString(R.string.title_add_artist))

            val inflated = LayoutInflater.from(this).inflate(R.layout.person_dialog, null,false)
            val inID: AutoCompleteTextView = inflated.findViewById(R.id.inID)
            val inName: AutoCompleteTextView = inflated.findViewById(R.id.inName)

            val artists: Array<Person> = viewModel.artistsChosen.value!!.toTypedArray()

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
        }

        binding.btnAddDesigner.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle(getString(R.string.title_add_designer))

            val inflated = LayoutInflater.from(this).inflate(R.layout.person_dialog, null,false)
            val inID: AutoCompleteTextView = inflated.findViewById(R.id.inID)
            val inName: AutoCompleteTextView = inflated.findViewById(R.id.inName)

            val designer: Array<Person> = viewModel.designersChosen.value!!.toTypedArray()

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
        }

        viewModel.loadStartingData()


    }
}