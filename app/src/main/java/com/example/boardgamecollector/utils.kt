package com.example.boardgamecollector

import android.content.ContentResolver
import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.widget.ListAdapter
import android.widget.ListView
import androidx.core.content.contentValuesOf
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.lang.Exception
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import javax.net.ssl.HttpsURLConnection

//https://stackoverflow.com/a/3495908/14595589

fun setListViewHeightBasedOnChildren(listView: ListView) {
    val listAdapter: ListAdapter = listView.adapter

    var totalHeight = listView.paddingTop + listView.paddingBottom

    for (i in 0 until listAdapter.count) {
        val listItem = listAdapter.getView(i, null, listView);
        if (listItem is ViewGroup) {
            listItem.setLayoutParams(LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
         }

         listItem.measure(0, 0);
         totalHeight += listItem.measuredHeight;
    }

    val params = listView.layoutParams;
    params.height = totalHeight + (listView.dividerHeight * (listAdapter.count - 1));
    listView.layoutParams = params;
}

// https://stackoverflow.com/a/5086706/14595589

fun decodeUri(selectedImage: Uri, contentResolver: ContentResolver):Bitmap? {
    return try {
        // Decode image size
        val o: BitmapFactory.Options = BitmapFactory.Options()
        o.inJustDecodeBounds = true
        BitmapFactory.decodeStream(contentResolver.openInputStream(selectedImage), null, o)

        // The new size we want to scale to
        val REQUIRED_SIZE = 100

        // Find the correct scale value. It should be the power of 2.
        var widthTmp = o.outWidth
        var heightTmp = o.outHeight
        var scale = 1
        while (true) {
            if (widthTmp / 2 < REQUIRED_SIZE
                    || heightTmp / 2 < REQUIRED_SIZE) {
                break
            }
            widthTmp /= 2
            heightTmp /= 2
            scale *= 2
        }

        // Decode with inSampleSize
        val o2 = BitmapFactory.Options()
        o2.inSampleSize = scale
        BitmapFactory.decodeStream(contentResolver.openInputStream(selectedImage), null, o2)
    }catch (e: Exception){
        Log.i("decodeUri",e.stackTraceToString())
        null
    }
}

fun  getBitmapFromURL(src: String): Bitmap?{
    return try {
        val url =  URL(src)
        val connection = url.openConnection() as HttpsURLConnection
        connection.doInput = true
        connection.connect()
        val input: InputStream = connection.inputStream
        Bitmap.createScaledBitmap(BitmapFactory.decodeStream(input),100,100,true)
    } catch (e: IOException) {
        Log.i("getBitmap",e.stackTraceToString())
        null
    }
}

fun putGame(db: SQLiteDatabase, game: GameData): Long{
    val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    val values = ContentValues().apply {
        put(GamesCollector.GamesEntry.COLUMN_TITLE, game.title)
        put(GamesCollector.GamesEntry.COLUMN_TITLE_ORIGINAL, game.originalTitle)
        put(GamesCollector.GamesEntry.COLUMN_RELEASE_YEAR, game.yearPublished)
        put(GamesCollector.GamesEntry.COLUMN_DESCRIPTION, game.description)
        put(GamesCollector.GamesEntry.COLUMN_ORDER_DATE, format.format(game.ordered?: Date()))
        put(GamesCollector.GamesEntry.COLUMN_DELIVERY_DATE, format.format(game.delivered?: Date()))
        put(GamesCollector.GamesEntry.COLUMN_PAID_PRICE, game.paidPrice)
        put(GamesCollector.GamesEntry.COLUMN_SUGGESTED_PRICE, game.suggestedPrice)
        put(GamesCollector.GamesEntry.COLUMN_EAN_CODE, game.eanCode)
        put(GamesCollector.GamesEntry.COLUMN_BGG_ID, game.bggId)
        put(GamesCollector.GamesEntry.COLUMN_PRODUCTION_CODE, game.productionCode)
        put(GamesCollector.GamesEntry.COLUMN_CURRENT_RANK, game.ranks?.get(0)?.rank?:0)
        put(GamesCollector.GamesEntry.COLUMN_TYPE, game.type)
        put(GamesCollector.GamesEntry.COLUMN_COMMENT, game.comment)
        put(GamesCollector.GamesEntry.COLUMN_IMG, game.img.let { bitmap ->
            val stream = ByteArrayOutputStream()
            bitmap?.compress(Bitmap.CompressFormat.PNG, 100, stream)?: return@let null
            val bar = stream.toByteArray()
            bitmap.recycle()
            bar
        })
        put(GamesCollector.GamesEntry.COLUMN_HAS_IMG, game.hasImg.let {
            if(it == true)
                return@let 1
            else
                0
        })
    }
    return db.insert(GamesCollector.GamesEntry.TABLE_NAME, null, values)
}

fun updateGameRank(db: SQLiteDatabase, gameId: Long, rank: Rank){
    putRank(db, rank, gameId)
    val values = ContentValues().apply {
        put(GamesCollector.GamesEntry.COLUMN_CURRENT_RANK, rank.rank)
    }
    val selection = "${GamesCollector.GamesEntry.GAME_ID} = ?"
    val selectionArgs = arrayOf(gameId.toString())
    db.update(
        GamesCollector.GamesEntry.TABLE_NAME,
        values,
        selection,
        selectionArgs
    )
}

fun updateGame(db: SQLiteDatabase, game: GameData){
    val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    val values = ContentValues().apply {
        put(GamesCollector.GamesEntry.COLUMN_TITLE, game.title)
        put(GamesCollector.GamesEntry.COLUMN_TITLE_ORIGINAL, game.originalTitle)
        put(GamesCollector.GamesEntry.COLUMN_RELEASE_YEAR, game.yearPublished)
        put(GamesCollector.GamesEntry.COLUMN_DESCRIPTION, game.description)
        put(GamesCollector.GamesEntry.COLUMN_ORDER_DATE, format.format(game.ordered?: Date()))
        put(GamesCollector.GamesEntry.COLUMN_DELIVERY_DATE, format.format(game.delivered?: Date()))
        put(GamesCollector.GamesEntry.COLUMN_PAID_PRICE, game.paidPrice)
        put(GamesCollector.GamesEntry.COLUMN_SUGGESTED_PRICE, game.suggestedPrice)
        put(GamesCollector.GamesEntry.COLUMN_EAN_CODE, game.eanCode)
        put(GamesCollector.GamesEntry.COLUMN_BGG_ID, game.bggId)
        put(GamesCollector.GamesEntry.COLUMN_PRODUCTION_CODE, game.productionCode)
        put(GamesCollector.GamesEntry.COLUMN_CURRENT_RANK, game.ranks?.get(0)?.rank?:0)
        put(GamesCollector.GamesEntry.COLUMN_TYPE, game.type)
        put(GamesCollector.GamesEntry.COLUMN_COMMENT, game.comment)
        put(GamesCollector.GamesEntry.COLUMN_IMG, game.img.let { bitmap ->
            val stream = ByteArrayOutputStream()
            bitmap?.compress(Bitmap.CompressFormat.PNG, 100, stream)?: return@let null
            val bar = stream.toByteArray()
            bitmap.recycle()
            bar
        })
        put(GamesCollector.GamesEntry.COLUMN_HAS_IMG, game.hasImg.let {
            if(it == true)
                return@let 1
            else
                0
        })
    }
    val selection = "${GamesCollector.GamesEntry.GAME_ID} = ?"
    val selectionArgs = arrayOf(game.gameId.toString())
    db.update(
        GamesCollector.GamesEntry.TABLE_NAME,
        values,
        selection,
        selectionArgs
    )
}

fun deleteGame(db:SQLiteDatabase, gameId: Long){
    val selection = "${GamesCollector.GamesEntry.GAME_ID} = ?"
    val selectionArgs = arrayOf(gameId.toString())
    db.delete(GamesCollector.GamesEntry.TABLE_NAME, selection, selectionArgs)
    db.delete(GamesCollector.GamesLocationsEntry.TABLE_NAME, "${GamesCollector.GamesLocationsEntry.COLUMN_GAME_ID} = ?", arrayOf(gameId.toString()))
    db.delete(GamesCollector.GameArtistsEntry.TABLE_NAME, "${GamesCollector.GameArtistsEntry.COLUMN_GAME_ID} = ?", arrayOf(gameId.toString()))
    db.delete(GamesCollector.GameDesignersEntry.TABLE_NAME, "${GamesCollector.GameDesignersEntry.COLUMN_GAME_ID} = ?", arrayOf(gameId.toString()))
}

fun putArtist(db: SQLiteDatabase, artist: Person): Long{
    val values = ContentValues().apply {
        put(GamesCollector.ArtistsEntry.COLUMN_ARTIST_ID, artist.id)
        put(GamesCollector.ArtistsEntry.COLUMN_ARTIST_NAME, artist.name)
    }
    return db.insert(GamesCollector.ArtistsEntry.TABLE_NAME, null, values)
}

fun putGameArtist(db: SQLiteDatabase, gameId: Long, artistId: Long): Long{
    val values = ContentValues().apply {
        put(GamesCollector.GameArtistsEntry.COLUMN_GAME_ID, gameId)
        put(GamesCollector.GameArtistsEntry.COLUMN_ARTIST_ID, artistId)
    }
    return db.insert(GamesCollector.GameArtistsEntry.TABLE_NAME, null, values)
}

fun changeGameArtists(db: SQLiteDatabase, gameId: Long, artists: ArrayList<Person>){
    val selection = "${GamesCollector.GameArtistsEntry.COLUMN_GAME_ID} = ?"
    val selectionArgs = arrayOf(gameId.toString())
    db.delete(GamesCollector.GameArtistsEntry.TABLE_NAME, selection, selectionArgs)
    artists.iterator().forEach {
        if(!artistInDB(db, it.id))
            putArtist(db, it)
        putGameArtist(db, gameId, it.id)
    }
}

fun putDesigner(db: SQLiteDatabase, designer: Person): Long{
    val values = ContentValues().apply {
        put(GamesCollector.DesignersEntry.COLUMN_DESIGNER_ID, designer.id)
        put(GamesCollector.DesignersEntry.COLUMN_DESIGNER_NAME, designer.name)
    }
    return db.insert(GamesCollector.DesignersEntry.TABLE_NAME, null, values)
}

fun putGameDesigner(db: SQLiteDatabase, gameId: Long, designerId: Long): Long{
    val values = ContentValues().apply {
        put(GamesCollector.GameDesignersEntry.COLUMN_GAME_ID, gameId)
        put(GamesCollector.GameDesignersEntry.COLUMN_DESIGNER_ID, designerId)
    }
    return db.insert(GamesCollector.GameDesignersEntry.TABLE_NAME, null, values)
}

fun changeGameDesigners(db: SQLiteDatabase, gameId: Long, designers: ArrayList<Person>){
    val selection = "${GamesCollector.GameDesignersEntry.COLUMN_GAME_ID} = ?"
    val selectionArgs = arrayOf(gameId.toString())
    db.delete(GamesCollector.GameDesignersEntry.TABLE_NAME, selection, selectionArgs)
    designers.iterator().forEach {
        if(!designerInDB(db, it.id))
            putDesigner(db, it)
        putGameDesigner(db, gameId, it.id)
    }
}
fun putLocation(db: SQLiteDatabase, location: Location): Long{
    val values = ContentValues().apply {
        put(GamesCollector.LocationEntry.COLUMN_LOCATION_ID, location.id)
        put(GamesCollector.LocationEntry.COLUMN_LOCATION, location.name)
    }
    return db.insert(GamesCollector.LocationEntry.TABLE_NAME, null, values)
}

fun newLocation(db: SQLiteDatabase, location: String): Long{
    val values = ContentValues().apply {
        put(GamesCollector.LocationEntry.COLUMN_LOCATION, location)
    }
    return db.insert(GamesCollector.LocationEntry.TABLE_NAME, null, values)
}

fun updateLocation(db: SQLiteDatabase, locationId: Long, location: String){
    db.update(
        GamesCollector.LocationEntry.TABLE_NAME,
        ContentValues().apply {
            put(GamesCollector.LocationEntry.COLUMN_LOCATION, location)
        },
        "${GamesCollector.LocationEntry.COLUMN_LOCATION_ID} = ?",
        arrayOf(locationId.toString())
    )
}

fun locationInDB(db: SQLiteDatabase, locationId: Long):Boolean{
    val cursor = db.query(GamesCollector.LocationEntry.TABLE_NAME, arrayOf(GamesCollector.LocationEntry.COLUMN_LOCATION_ID), "${GamesCollector.LocationEntry.COLUMN_LOCATION_ID} = ?", arrayOf(locationId.toString()), null, null, null)
    cursor.moveToNext()
    if(cursor.isAfterLast) {
        cursor.close()
        return false
    }
    cursor.close()
    return true
}

fun deleteLocation(db: SQLiteDatabase, locationId: Long){
    db.delete(GamesCollector.GamesLocationsEntry.TABLE_NAME, "${GamesCollector.GamesLocationsEntry.COLUMN_LOCATION_ID} = ?", arrayOf(locationId.toString()))
    val selection = "${GamesCollector.LocationEntry.COLUMN_LOCATION_ID} = ?"
    val selectionArgs = arrayOf(locationId.toString())
    db.delete(GamesCollector.LocationEntry.TABLE_NAME, selection, selectionArgs)
}

fun putGameLocation(db: SQLiteDatabase, gameId: Long, locationId: Long, comment: String): Long{
    db.delete(GamesCollector.GamesLocationsEntry.TABLE_NAME,
            "${GamesCollector.GamesLocationsEntry.COLUMN_GAME_ID} = ?",
            arrayOf(gameId.toString()))
    val values = ContentValues().apply {
        put(GamesCollector.GamesLocationsEntry.COLUMN_LOCATION_ID, locationId)
        put(GamesCollector.GamesLocationsEntry.COLUMN_GAME_ID, gameId)
        put(GamesCollector.GamesLocationsEntry.COLUMN_COMMENT, comment)
    }
    return db.insert(GamesCollector.GamesLocationsEntry.TABLE_NAME, null, values)
}

fun putRank(db: SQLiteDatabase, rank: Rank, gameId: Long): Long{
    val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    val values = ContentValues().apply {
        put(GamesCollector.RanksEntry.COLUMN_RANK, rank.rank)
        put(GamesCollector.RanksEntry.COLUMN_GAME_ID, gameId)
        put(GamesCollector.RanksEntry.COLUMN_DATE, format.format(rank.date))
    }
    return db.insert(GamesCollector.RanksEntry.TABLE_NAME, null, values)
}

fun gameInDB(db: SQLiteDatabase, bggId: Long): Boolean{
    val cursor = db.query(GamesCollector.GamesEntry.TABLE_NAME, arrayOf(GamesCollector.GamesEntry.GAME_ID), "${GamesCollector.GamesEntry.COLUMN_BGG_ID} = ?", arrayOf(bggId.toString()), null, null, null)
    cursor.moveToNext()
    if(cursor.isAfterLast) {
        cursor.close()
        return false
    }
    cursor.close()
    return true
}

fun artistInDB(db: SQLiteDatabase, id: Long): Boolean{
    val cursor = db.query(GamesCollector.ArtistsEntry.TABLE_NAME, arrayOf(GamesCollector.ArtistsEntry.COLUMN_ARTIST_ID), "${GamesCollector.ArtistsEntry.COLUMN_ARTIST_ID} = ?", arrayOf(id.toString()), null, null, null)
    cursor.moveToNext()
    if(cursor.isAfterLast) {
        cursor.close()
        return false
    }
    cursor.close()
    return true
}

fun designerInDB(db: SQLiteDatabase, id: Long): Boolean{
    val cursor = db.query(GamesCollector.DesignersEntry.TABLE_NAME, arrayOf(GamesCollector.DesignersEntry.COLUMN_DESIGNER_ID), "${GamesCollector.DesignersEntry.COLUMN_DESIGNER_ID} = ?", arrayOf(id.toString()), null, null, null)
    cursor.moveToNext()
    if(cursor.isAfterLast) {
        cursor.close()
        return false
    }
    cursor.close()
    return true
}
