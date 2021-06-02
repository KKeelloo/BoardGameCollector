package com.example.boardgamecollector

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class MyDBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION){
    companion object{
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "GamesCollector.db"
        private var instance: MyDBHelper? = null

        @Synchronized
        fun getInstance(context: Context) : MyDBHelper{
            if(instance == null){
                instance = MyDBHelper(context.applicationContext)
            }
            return instance!!;
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(GamesCollector.SQL_CREATE_TABLE_GAMES)
        db.execSQL(GamesCollector.SQL_CREATE_TABLE_RANKS)
        db.execSQL(GamesCollector.SQL_CREATE_TABLE_ARTISTS)
        db.execSQL(GamesCollector.SQL_CREATE_TABLE_GAME_ARTISTS)
        db.execSQL(GamesCollector.SQL_CREATE_TABLE_DESIGNERS)
        db.execSQL(GamesCollector.SQL_CREATE_TABLE_GAME_DESIGNERS)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(GamesCollector.SQL_DELETE_TABLE_GAME_DESIGNERS)
        db.execSQL(GamesCollector.SQL_DELETE_TABLE_DESIGNERS)
        db.execSQL(GamesCollector.SQL_DELETE_TABLE_GAME_ARTISTS)
        db.execSQL(GamesCollector.SQL_DELETE_TABLE_ARTISTS)
        db.execSQL(GamesCollector.SQL_DELETE_TABLE_RANKS)
        db.execSQL(GamesCollector.SQL_DELETE_TABLE_GAMES)
        onCreate(db)
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }
}