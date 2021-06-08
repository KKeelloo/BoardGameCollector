package com.example.boardgamecollector

import android.database.sqlite.SQLiteDatabase
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import javax.net.ssl.HttpsURLConnection
import javax.xml.parsers.DocumentBuilderFactory

class XMLParser(private val dataDir: String): ViewModel(){

    private val _retryingIn: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>().also { it.postValue(0) }
    }
    val retryingIn: LiveData<Int> get() = _retryingIn

    private val _processingForName: MutableLiveData<Boolean> = MutableLiveData<Boolean>().also { it.value=false }
    val processingForName: LiveData<Boolean> get() = _processingForName

    private val _processingForId: MutableLiveData<Boolean> = MutableLiveData<Boolean>().also { it.value=false }
    val processingForId: LiveData<Boolean> get() = _processingForId

    private val _processingForUsername: MutableLiveData<Boolean> = MutableLiveData<Boolean>().also { it.value=false }
    val processingForUsername: LiveData<Boolean> get() = _processingForUsername

    private val _loaded: MutableLiveData<Int> = MutableLiveData<Int>().also { it.value=-2 }
    val loaded: LiveData<Int> get() = _loaded

    private val _numToLoad: MutableLiveData<Int> = MutableLiveData<Int>().also { it.value = 0 }
    val numToLoad: LiveData<Int> get() = _numToLoad

    private val _numLoaded: MutableLiveData<Int> = MutableLiveData<Int>().also { it.value = 0 }
    val numLoaded: LiveData<Int> get() = _numLoaded

    private val _gamesBasic: MutableLiveData<Array<BasicGameData>> by lazy {
        MutableLiveData<Array<BasicGameData>>().also { it.postValue(Array(0){BasicGameData("","","")}) }
    }
    val gamesBasic: LiveData<Array<BasicGameData>> get() = _gamesBasic

    private val _games: MutableLiveData<Array<GameData>> by lazy {
        MutableLiveData<Array<GameData>>().also { it.postValue(Array(0){ GameData() }) }
    }
    val games: LiveData<Array<GameData>> get() = _games

    private val _game: MutableLiveData<GameData> by lazy {
        MutableLiveData<GameData>().also { it.postValue(GameData())}
    }
    val game: LiveData<GameData> get() = _game

    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend fun getRequestToXMLFile(request: String, processing: MutableLiveData<Boolean>): Int{
        try {
            val url = URL(request)
            val con = url.openConnection() as HttpsURLConnection

            when (con.responseCode) {
                200 -> {
                    val inStream = con.inputStream

                    processing.postValue(true)

                    val dir = File("$dataDir/tmp")
                    if (!dir.exists()) dir.mkdir()

                    val fos = FileOutputStream("$dir/tmp.xml")
                    val data = ByteArray(1024)

                    var count = inStream.read(data)
                    while (count!=-1){
                        fos.write(data, 0, count)
                        count = inStream.read(data)
                    }

                    inStream.close()
                    fos.close()
                    return 200
                }

                202, 429 -> {
                    for (i in 0..9) {
                        _retryingIn.postValue(10 - i)
                        delay(1000)
                    }
                    return getRequestToXMLFile(request, processing)
                }

                else -> return con.responseCode

            }
        }catch (e:Exception){
            Log.i("getRequest", e.stackTraceToString())
            return -1
        }
    }

    fun getGamesByName(name: String){
        viewModelScope.launch(Dispatchers.IO){
            val request = "https://www.boardgamegeek.com/xmlapi2/search?query=$name&type=boardgame,boardgameexpansion"
            when(val resp = getRequestToXMLFile(request, _processingForName)){
                200 -> {
                    val file = File("$dataDir/tmp/tmp.xml")
                    val xmlDoc: Document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file)
                    xmlDoc.documentElement.normalize()
                    val items: NodeList = xmlDoc.getElementsByTagName("item")
                    val arr: ArrayList<BasicGameData> = ArrayList<BasicGameData>().also {
                        for (i in 0 until items.length){
                            val elem = items.item(i)
                            val id = elem.attributes.getNamedItem("id").textContent
                            var name = ""
                            var year = ""
                            val children = elem.childNodes
                            for(j in 0 until children.length){
                                when(children.item(j).nodeName){
                                    "name" -> name = children.item(j).attributes.getNamedItem("value").textContent
                                    "yearpublished" -> year = children.item(j).attributes.getNamedItem("value").textContent
                                }
                            }

                            val tmp = BasicGameData(id,name,year)
                            if(!it.contains(tmp))
                                it.add(tmp)
                        }
                    }

                    val arrToPost: Array<BasicGameData> = Array(arr.size){ i ->
                        arr[i]
                    }
                    _gamesBasic.postValue(arrToPost)
                    _loaded.postValue(200)
                    delay(300)
                    _processingForName.postValue(false)

                    file.delete()
                }
                else -> {
                    _loaded.postValue(resp)
                    delay(300)
                    _processingForName.postValue(false)
                }
            }
        }
    }

    private suspend fun _getGameByID(id: String):GameData?{
        val request = "https://www.boardgamegeek.com/xmlapi2/thing?id=$id&stats=1"
        when(val resp = getRequestToXMLFile(request, _processingForId)){
            200 -> {
                val file = File("$dataDir/tmp/tmp.xml")
                val xmlDoc: Document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file)
                xmlDoc.documentElement.normalize()

                val gameData = GameData()

                gameData.description = xmlDoc.getElementsByTagName("description").item(0).textContent
                gameData.originalTitle = xmlDoc.getElementsByTagName("name").item(0).attributes.getNamedItem("value").textContent
                gameData.yearPublished = xmlDoc.getElementsByTagName("yearpublished").item(0).attributes.getNamedItem("value").textContent.toInt()
                gameData.bggId = id.toInt()
                if(xmlDoc.getElementsByTagName("thumbnail").length>0){
                    gameData.hasImg = true
                    gameData.img = getBitmapFromURL(xmlDoc.getElementsByTagName("thumbnail").item(0).textContent)
                }

                val rank = Rank()
                val ranks = xmlDoc.getElementsByTagName("rank")
                for(i in 0 until ranks.length){
                    val item = ranks.item(i)
                    val atr = item.attributes
                    val rankName = atr.getNamedItem("name")
                    if(rankName.textContent == "boardgame"){
                        if(atr.getNamedItem("value").textContent != "Not Ranked")
                            rank.rank = atr.getNamedItem("value").textContent.toInt()
                        break
                    }
                }
                gameData.ranks = Array(1){rank}

                val links = xmlDoc.getElementsByTagName("link")

                val arrListArtists: ArrayList<Person> = ArrayList()

                val arrListDesigners: ArrayList<Person> = ArrayList<Person>().also {
                    for (i in 0 until links.length){
                        val link = links.item(i)
                        val atr = link.attributes
                        val type = atr.getNamedItem("type")
                        when(type.textContent){
                            "boardgamedesigner" -> {
                                it.add(Person(atr.getNamedItem("id").textContent.toLong()
                                        ,atr.getNamedItem("value").textContent))
                            }
                            "boardgameartist" -> {
                                arrListArtists.add(Person(atr.getNamedItem("id").textContent.toLong()
                                        ,atr.getNamedItem("value").textContent))
                            }
                        }
                    }
                }

                gameData.artists = Array(arrListArtists.size){ idx ->
                    arrListArtists[idx]
                }

                gameData.designers = Array(arrListDesigners.size){ idx ->
                    arrListDesigners[idx]
                }
                file.delete()
                _loaded.postValue(200)
                return gameData
            }
            else -> {
                _loaded.postValue(resp)
                return null
            }
        }
    }

    fun getGameByID(id: String){
        viewModelScope.launch(Dispatchers.IO){
            val gameData = _getGameByID(id)
            if(gameData!=null){
                _game.postValue(gameData)
                delay(300)
                _processingForId.postValue(false)
            }
            else{
                delay(300)
                _processingForId.postValue(false)
            }
        }
    }




    fun findGamesByUsername(username: String, db: MyDBHelper){
        viewModelScope.launch(Dispatchers.IO) {
            val request = "https://www.boardgamegeek.com/xmlapi2/collection?username=$username&own=1"
            val res = getRequestToXMLFile(request, _processingForUsername)
            if( res == 200){
                val file = File("$dataDir/tmp/tmp.xml")
                val xmlDoc: Document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file)
                xmlDoc.documentElement.normalize()
                var item: Element? = xmlDoc.getElementsByTagName("message").item(0) as Element?

                if( item == null) {
                    item = xmlDoc.getElementsByTagName("items").item(0) as Element?
                    if(item == null) {
                        _loaded.postValue(-1)
                        delay(300)
                        _processingForUsername.postValue(false)
                        return@launch
                    }

                    val numIter = xmlDoc.getElementsByTagName("items").item(0).attributes.getNamedItem("totalitems").textContent.toInt()
                    _numLoaded.postValue(0)
                    _numToLoad.postValue(numIter)
                    val items = xmlDoc.getElementsByTagName("item")
                    file.delete()
                    for (i in 0 until numIter) {
                        delay(1500)
                        val itemAtr = items.item(i).attributes
                        val id = itemAtr.getNamedItem("objectid").textContent
                        if(!gameInDB(db.readableDatabase, id.toLong())) {
                            val game = _getGameByID(id)
                            if(game!=null){
                                game.gameId = putGame(db.writableDatabase, game)
                                game.artists?.iterator()?.forEach { artist ->
                                    if(!artistInDB(db.readableDatabase, artist.id))
                                        putArtist(db.writableDatabase, artist)
                                    putGameArtist(db.writableDatabase, game.gameId!!, artist.id)
                                }
                                game.designers?.iterator()?.forEach { designer ->
                                    if(!designerInDB(db.readableDatabase, designer.id))
                                        putDesigner(db.writableDatabase, designer)
                                    putGameDesigner(db.writableDatabase, game.gameId!!, designer.id)
                                }
                                putRank(db.writableDatabase, game.ranks?.get(0) ?: Rank(), game.gameId!!)
                            }
                        }
                        _numLoaded.postValue(i+1)
                    }
                    _loaded.postValue(200)
                    delay(300)
                    _processingForUsername.postValue(false)
                    _numLoaded.postValue(0)
                }else if(item.textContent == "Invalid username specified"){
                    _loaded.postValue(-3)
                    delay(300)
                    _processingForUsername.postValue(false)
                }
            }else{
                _loaded.postValue(res)
                delay(300)
                _processingForUsername.postValue(false)
            }
        }
    }



}