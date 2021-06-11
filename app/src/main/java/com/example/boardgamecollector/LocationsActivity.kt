package com.example.boardgamecollector

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.ListView
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.boardgamecollector.databinding.ActivityLocationsBinding
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LocationsActivity : AppCompatActivity() {
    companion object{
        private class LocationsViewModel(private val dbHelper: MyDBHelper): ViewModel(){
            private val _locations: MutableLiveData<Array<Location>> = MutableLiveData()
            val locations: LiveData<Array<Location>> get() =  _locations

            private val _games: MutableLiveData<Array<String>> = MutableLiveData()
            val games: LiveData<Array<String>> get() =  _games

            fun loadLocations(){
                viewModelScope.launch(Dispatchers.IO) {
                    val projection = arrayOf(GamesCollector.LocationEntry.COLUMN_LOCATION_ID, GamesCollector.LocationEntry.COLUMN_LOCATION)
                    val cursor = dbHelper.readableDatabase.query(GamesCollector.LocationEntry.TABLE_NAME, projection, null, null, null, null, null)
                    _locations.postValue(Array<Location>(cursor.count){
                        cursor.moveToNext()
                        Location(cursor.getLong(cursor.getColumnIndex(GamesCollector.LocationEntry.COLUMN_LOCATION_ID)),
                            cursor.getString(cursor.getColumnIndex(GamesCollector.LocationEntry.COLUMN_LOCATION))
                        )
                    })
                    cursor.close()
                }
            }

            fun loadGamesForLocation(id: Long){
                viewModelScope.launch(Dispatchers.IO){
                    val projection = arrayOf(GamesCollector.GamesLocationsEntry.COLUMN_GAME_ID)
                    var cursor = dbHelper.readableDatabase.query(GamesCollector.GamesLocationsEntry.TABLE_NAME,
                        projection,
                        "${GamesCollector.GamesLocationsEntry.COLUMN_LOCATION_ID} = ?",
                        arrayOf(id.toString()),
                        null,
                        null,
                        null
                    )
                    val arr = Array<Long>(cursor.count){
                        cursor.moveToNext()
                        cursor.getLong(cursor.getColumnIndex(GamesCollector.GamesLocationsEntry.COLUMN_GAME_ID))
                    }
                    Log.i("[Hmm]", arr.size.toString())
                    _games.postValue(Array<String>(arr.size){
                        cursor.close()
                        cursor = dbHelper.readableDatabase.query(GamesCollector.GamesEntry.TABLE_NAME,
                            arrayOf(GamesCollector.GamesEntry.COLUMN_TITLE_ORIGINAL),
                            "${GamesCollector.GamesLocationsEntry.COLUMN_GAME_ID} = ?",
                            arrayOf(arr[it].toString()),
                            null,
                            null,
                            null
                        )
                        cursor.moveToNext()

                        cursor.getString(cursor.getColumnIndex(GamesCollector.GamesEntry.COLUMN_TITLE_ORIGINAL))
                    })
                    cursor.close()
                }
            }
            fun deleteLocation(location: Long, pos: Int){
                 val tmp = ArrayList<Location>()
                _locations.value?.iterator()?.forEach {
                    tmp.add(it)
                }
                tmp.removeAt(pos)

                _locations.postValue(Array<Location>(tmp.size){
                    tmp[it]
                })
                deleteLocation(dbHelper.writableDatabase, location)
            }

            fun updateLocation(location: Long, txt: String, pos:Int){
                updateLocation(dbHelper.writableDatabase, location, txt)
                _locations.value!![pos] = Location(location, txt)
                _locations.postValue(_locations.value)
            }
        }
    }
    private lateinit var binding: ActivityLocationsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var location = 0.toLong()
        var pos = 0.toInt()
        binding = ActivityLocationsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val loadingDialog = LoadingDialog(this)
        val dbHelper = MyDBHelper.getInstance(this)
        val viewModel = LocationsViewModel(dbHelper)

        viewModel.locations.observe(this){ locations ->
            ArrayAdapter(this, android.R.layout.simple_list_item_1, Array(locations.size){ locations[it].name }).let {
                binding.lvLocations.adapter = it
            }
            loadingDialog.dissmisDialog()
        }

        binding.lvLocations.setOnItemClickListener { _, _, position, _ ->
            loadingDialog.startLoadingDialog()
            pos = position
            viewModel.locations.value?.get(position)?.let {
                location = it.id
                viewModel.loadGamesForLocation(it.id) }
        }
        loadingDialog.startLoadingDialog()
        viewModel.loadLocations()

        viewModel.games.observe(this){
            loadingDialog.dissmisDialog()
            val builder = AlertDialog.Builder(this)

            val inflated = LayoutInflater.from(this).inflate(R.layout.locations_data, null,false)
            val lvGames = inflated.findViewById<ListView>(R.id.lvGames)
            val txtIn = inflated.findViewById<TextInputEditText>(R.id.txtInLocation)

            txtIn.setText(viewModel.locations.value?.get(pos)?.name)

            ArrayAdapter(this, android.R.layout.simple_list_item_1, it).also { adapter ->
                lvGames.adapter = adapter
            }
            builder.setView(inflated)

            builder.setPositiveButton(android.R.string.ok) { dialogInterface: DialogInterface, _ ->
                dialogInterface.dismiss()
                viewModel.updateLocation(location, txtIn.text.toString(), pos)
            }

            if(it.isEmpty())
                builder.setNegativeButton(R.string.remove) { dialogInterface: DialogInterface, _ ->
                    dialogInterface.cancel()
                    viewModel.deleteLocation(location, pos)
                }

            builder.create().show()
        }

        binding.btnAddLocation.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Title")

            // Set up the input
            val input = EditText(this)
            builder.setView(input)

            // Set up the buttons
            builder.setPositiveButton(android.R.string.ok) { dialogInterface: DialogInterface, _ ->
                newLocation(dbHelper.writableDatabase, input.text.toString())
                dialogInterface.dismiss()
                loadingDialog.startLoadingDialog()
                viewModel.loadLocations()
            }

            builder.setNegativeButton(android.R.string.cancel) {dialogInterface: DialogInterface, _ ->
                dialogInterface.cancel()
            }

            builder.show()
        }
    }
}