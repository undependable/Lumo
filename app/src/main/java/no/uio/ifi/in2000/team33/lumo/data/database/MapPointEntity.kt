package no.uio.ifi.in2000.team33.lumo.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity class for mappoints
 */

@Entity(tableName = "map_points")
data class MapPointEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val lat: Double,
    val lon: Double,
    var isFavorite: Boolean = false,
    val erHus: Boolean,
    val postnummer: String,
    val poststed: String,
    val region: String,
    val closestStationID: String = ""
)
