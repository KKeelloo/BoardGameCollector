package com.example.boardgamecollector

import android.provider.BaseColumns

object GamesCollector {
    object GamesEntry : BaseColumns {
        const val TABLE_NAME = "games"
        const val GAME_ID = "game_id"
        const val COLUMN_TITLE = "title"
        const val COLUMN_TITLE_ORIGINAL = "original_title"
        const val COLUMN_RELEASE_YEAR = "release_date"
        const val COLUMN_DESCRIPTION = "description"
        const val COLUMN_ORDER_DATE = "order_date"
        const val COLUMN_DELIVERY_DATE = "deliver_date"
        const val COLUMN_PAID_PRICE = "paid_price"
        const val COLUMN_SUGGESTED_PRICE = "suggested_price"
        const val COLUMN_EAN_CODE = "ean_code"
        const val COLUMN_BGG_ID = "bgg_id"
        const val COLUMN_PRODUCTION_CODE = "production_code"
        const val COLUMN_CURRENT_RANK =  "current_rank"
        const val COLUMN_TYPE = "type"
        const val COLUMN_COMMENT = "comment"
        const val COLUMN_IMG = "img"
        const val COLUMN_HAS_IMG = "has_img"
    }
    
    const val SQL_CREATE_TABLE_GAMES =
            "CREATE_TABLE ${GamesEntry.TABLE_NAME} (" +
                    "${GamesEntry.GAME_ID} INTEGER PRIMARY KEY," +
                    "${GamesEntry.COLUMN_TITLE} TEXT," +
                    "${GamesEntry.COLUMN_TITLE_ORIGINAL} TEXT," +
                    "${GamesEntry.COLUMN_RELEASE_YEAR} INTEGER," +
                    "${GamesEntry.COLUMN_DESCRIPTION} TEXT," +
                    "${GamesEntry.COLUMN_ORDER_DATE} DATE," +
                    "${GamesEntry.COLUMN_DELIVERY_DATE} DATE," +
                    "${GamesEntry.COLUMN_PAID_PRICE} TEXT," +
                    "${GamesEntry.COLUMN_SUGGESTED_PRICE} TEXT," +
                    "${GamesEntry.COLUMN_EAN_CODE} INTEGER," +
                    "${GamesEntry.COLUMN_BGG_ID} INTEGER," +
                    "${GamesEntry.COLUMN_PRODUCTION_CODE} TEXT," +
                    "${GamesEntry.COLUMN_CURRENT_RANK} INTEGER," +
                    "${GamesEntry.COLUMN_TYPE} INTEGER," +
                    "${GamesEntry.COLUMN_COMMENT} TEXT," +
                    "${GamesEntry.COLUMN_HAS_IMG} INTEGER," +
                    "${GamesEntry.COLUMN_IMG} BLOB)"

    const val SQL_DELETE_TABLE_GAMES =
            "DROP TABLE IF EXISTS ${GamesEntry.TABLE_NAME}"

    object RanksEntry: BaseColumns{
        const val TABLE_NAME = "ranks"
        const val COLUMN_RANK = "rank"
        const val COLUMN_DATE = "date"
        const val COLUMN_GAME_ID = GamesEntry.GAME_ID
    }

    const val SQL_CREATE_TABLE_RANKS =
            "CREATE TABLE ${RanksEntry.TABLE_NAME} (" +
                    "${RanksEntry.COLUMN_RANK} INTEGER," +
                    "${RanksEntry.COLUMN_DATE} DATE," +
                    "${RanksEntry.COLUMN_GAME_ID} INTEGER," +
                    "FOREIGN KEY(${RanksEntry.COLUMN_GAME_ID}) " +
                            "REFERENCES ${GamesEntry.TABLE_NAME}(${GamesEntry.GAME_ID}) " +
                            "ON UPDATE CASCADE ON DELETE CASCADE)"

    const val SQL_DELETE_TABLE_RANKS =
            "DROP TABLE IF EXISTS ${RanksEntry.TABLE_NAME}"

    object DesignersEntry : BaseColumns{
        const val TABLE_NAME = "designers"
        const val COLUMN_DESIGNER_ID = "designer_id"
        const val COLUMN_DESIGNER_NAME = "designer_name"
    }

    const val SQL_CREATE_TABLE_DESIGNERS =
            "CREATE TABLE ${DesignersEntry.TABLE_NAME} (" +
                    "${DesignersEntry.COLUMN_DESIGNER_ID} INTEGER PRIMARY KEY," +
                    "${DesignersEntry.COLUMN_DESIGNER_NAME} TEXT)"

    const val SQL_DELETE_TABLE_DESIGNERS =
            "DROP TABLE IF EXISTS ${DesignersEntry.TABLE_NAME}"

    object GameDesignersEntry : BaseColumns{
        const val TABLE_NAME = "game_designers"
        const val COLUMN_DESIGNER_ID = DesignersEntry.COLUMN_DESIGNER_ID
        const val COLUMN_GAME_ID = GamesEntry.GAME_ID
    }

    const val SQL_CREATE_TABLE_GAME_DESIGNERS =
            "CREATE TABLE ${GameDesignersEntry.TABLE_NAME} (" +
                    "${GameDesignersEntry.COLUMN_DESIGNER_ID} INTEGER," +
                    "${GameDesignersEntry.COLUMN_GAME_ID} INTEGER," +
                    "FOREIGN KEY(${GameDesignersEntry.COLUMN_DESIGNER_ID}) REFERENCES " +
                            "${DesignersEntry.TABLE_NAME}(${DesignersEntry.COLUMN_DESIGNER_ID})" +
                            " ON UPDATE CASCADE ON DELETE CASCADE," +
                    "FOREIGN KEY(${GameDesignersEntry.COLUMN_GAME_ID}) REFERENCES " +
                            "${GamesEntry.TABLE_NAME}(${GamesEntry.GAME_ID})" +
                            " ON UPDATE CASCADE ON DELETE CASCADE)"

    const val SQL_DELETE_TABLE_GAME_DESIGNERS =
            "DROP TABLE IF EXISTS ${GameDesignersEntry.TABLE_NAME}"

    object ArtistsEntry : BaseColumns{
        const val TABLE_NAME = "artists"
        const val COLUMN_ARTIST_ID = "artist_id"
        const val COLUMN_ARTIST_NAME = "artist_name"
    }

    const val SQL_CREATE_TABLE_ARTISTS =
            "CREATE TABLE ${ArtistsEntry.TABLE_NAME} (" +
                    "${ArtistsEntry.COLUMN_ARTIST_ID} INTEGER PRIMARY KEY," +
                    "${ArtistsEntry.COLUMN_ARTIST_NAME} TEXT)"

    const val SQL_DELETE_TABLE_ARTISTS =
            "DROP TABLE IF EXISTS ${ArtistsEntry.TABLE_NAME}"

    object GameArtistsEntry : BaseColumns{
        const val TABLE_NAME = "game_artists"
        const val COLUMN_ARTIST_ID = ArtistsEntry.COLUMN_ARTIST_ID
        const val COLUMN_GAME_ID = GamesEntry.GAME_ID
    }

    const val SQL_CREATE_TABLE_GAME_ARTISTS =
            "CREATE TABLE ${GameArtistsEntry.TABLE_NAME} (" +
                    "${GameArtistsEntry.COLUMN_ARTIST_ID} INTEGER," +
                    "${GameArtistsEntry.COLUMN_GAME_ID} INTEGER," +
                    "FOREIGN KEY(${GameArtistsEntry.COLUMN_ARTIST_ID}) REFERENCES " +
                            "${ArtistsEntry.TABLE_NAME}(${ArtistsEntry.COLUMN_ARTIST_ID})" +
                            " ON UPDATE CASCADE ON DELETE CASCADE," +
                    "FOREIGN KEY(${GameArtistsEntry.COLUMN_GAME_ID}) REFERENCES " +
                            "${GamesEntry.TABLE_NAME}(${GamesEntry.GAME_ID})" +
                            " ON UPDATE CASCADE ON DELETE CASCADE)"

    const val SQL_DELETE_TABLE_GAME_ARTISTS =
            "DROP TABLE IF EXISTS ${GameArtistsEntry.TABLE_NAME}"

    object LocationEntry : BaseColumns{
        const val TABLE_NAME = "locations"
        const val COLUMN_LOCATION = "location"
    }

    const val SQL_CREATE_TABLE_LOCATIONS =
            "CREATE TABLE ${LocationEntry.TABLE_NAME} (" +
                    "${BaseColumns._ID} INTEGER PRIMARY KEY," +
                    "${LocationEntry.COLUMN_LOCATION} TEXT)"

    const val SQL_DELETE_TABLE_LOCATIONS =
            "DROP TABLE IF EXISTS ${LocationEntry.TABLE_NAME}"

    object GamesLocationsEntry : BaseColumns{
        const val TABLE_NAME = "games_locations"
        const val COLUMN_COMMENT = "comment"
        const val COLUMN_GAME_ID = GamesEntry.GAME_ID
        const val COLUMN_LOCATION_ID = BaseColumns._ID
    }

    const val SQL_CREATE_TABLE_GAMES_LOCATIONS =
            "CREATE TABLE ${GamesLocationsEntry.TABLE_NAME} (" +
                    "${GamesLocationsEntry.COLUMN_GAME_ID} INTEGER," +
                    "${GamesLocationsEntry.COLUMN_LOCATION_ID} INTEGER," +
                    "${GamesLocationsEntry.COLUMN_COMMENT} TEXT," +
                    "FOREIGN KEY(${GamesLocationsEntry.COLUMN_GAME_ID}) REFERENCES " +
                            "${GamesEntry.TABLE_NAME}(${GamesEntry.GAME_ID})" +
                            " ON UPDATE CASCADE ON DELETE CASCADE," +
                    "FOREIGN KEY(${GamesLocationsEntry.COLUMN_LOCATION_ID} REFERENCES " +
                            "${LocationEntry.TABLE_NAME}(${BaseColumns._ID})" +
                            " ON UPDATE CASCADE ON DELETE CASCADE)"

    const val SQL_DELETE_TABLE_GAMES_LOCATIONS =
            "DROP TABLE IF EXISTS ${GamesLocationsEntry.TABLE_NAME}"
}