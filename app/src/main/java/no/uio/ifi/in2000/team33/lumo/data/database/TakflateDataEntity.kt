package no.uio.ifi.in2000.team33.lumo.data.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index
import androidx.room.PrimaryKey
/**
 * Room entity class for roof surface data
 *
 * Each roof surface data entry is associated with a map point
 *
 * The entity has a many to one relationship with the map point
 */

@Entity(
    tableName = "takflate_data",
    foreignKeys = [ForeignKey(
        entity = MapPointEntity::class,
        parentColumns = ["id"],
        childColumns = ["mapPointId"],
        onDelete = CASCADE
    )],
    indices = [Index("mapPointId")]
)

data class TakflateDataEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val navn: String,
    val area: Double,
    val vinkel: Int,
    val retning: String,
    val mapPointId: Int
)