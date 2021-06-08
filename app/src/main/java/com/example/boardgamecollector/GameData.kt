package com.example.boardgamecollector

import android.graphics.Bitmap
import java.io.FileDescriptor
import java.util.*

data class GameData(
        var gameId: Int? = null,
        var title: String? = null,
        var originalTitle: String? = null,
        var yearPublished: Int? = null,
        var description: String? = null,
        var ordered: Date? = null,
        var delivered: Date? = null,
        var paidPrice: String? = null,
        var suggestedPrice: String? = null,
        var eanCode: Int? = null,
        var bggId: Int? = null,
        var productionCode: String? = null,
        var currentRank: Int? = null,
        var type: Int? = null,
        var comment: String? = null,
        var img: Bitmap? = null,
        var hasImg: Boolean? = null,
        var artists: Array<Person>? = null,
        var designers: Array<Person>? = null,
        var ranks: Array<Rank>? = null,
        var location: Location? = null,
        var locationComment: String? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GameData

        if (gameId != other.gameId) return false
        if (title != other.title) return false
        if (originalTitle != other.originalTitle) return false
        if (yearPublished != other.yearPublished) return false
        if (description != other.description) return false
        if (ordered != other.ordered) return false
        if (delivered != other.delivered) return false
        if (paidPrice != other.paidPrice) return false
        if (suggestedPrice != other.suggestedPrice) return false
        if (eanCode != other.eanCode) return false
        if (bggId != other.bggId) return false
        if (productionCode != other.productionCode) return false
        if (currentRank != other.currentRank) return false
        if (type != other.type) return false
        if (comment != other.comment) return false
        if (img != other.img) return false
        if (hasImg != other.hasImg) return false
        if (artists != null) {
            if (other.artists == null) return false
            if (!artists.contentEquals(other.artists)) return false
        } else if (other.artists != null) return false
        if (designers != null) {
            if (other.designers == null) return false
            if (!designers.contentEquals(other.designers)) return false
        } else if (other.designers != null) return false
        if (ranks != null) {
            if (other.ranks == null) return false
            if (!ranks.contentEquals(other.ranks)) return false
        } else if (other.ranks != null) return false
        if (location != other.location) return false
        if (locationComment != other.locationComment) return false

        return true
    }

    override fun hashCode(): Int {
        var result = gameId ?: 0
        result = 31 * result + (title?.hashCode() ?: 0)
        result = 31 * result + (originalTitle?.hashCode() ?: 0)
        result = 31 * result + (yearPublished ?: 0)
        result = 31 * result + (description?.hashCode() ?: 0)
        result = 31 * result + (ordered?.hashCode() ?: 0)
        result = 31 * result + (delivered?.hashCode() ?: 0)
        result = 31 * result + (paidPrice?.hashCode() ?: 0)
        result = 31 * result + (suggestedPrice?.hashCode() ?: 0)
        result = 31 * result + (eanCode ?: 0)
        result = 31 * result + (bggId ?: 0)
        result = 31 * result + (productionCode?.hashCode() ?: 0)
        result = 31 * result + (currentRank ?: 0)
        result = 31 * result + (type ?: 0)
        result = 31 * result + (comment?.hashCode() ?: 0)
        result = 31 * result + (img?.hashCode() ?: 0)
        result = 31 * result + (hasImg?.hashCode() ?: 0)
        result = 31 * result + (artists?.contentHashCode() ?: 0)
        result = 31 * result + (designers?.contentHashCode() ?: 0)
        result = 31 * result + (ranks?.contentHashCode() ?: 0)
        result = 31 * result + (location?.hashCode() ?: 0)
        result = 31 * result + (locationComment?.hashCode() ?: 0)
        return result
    }
}
