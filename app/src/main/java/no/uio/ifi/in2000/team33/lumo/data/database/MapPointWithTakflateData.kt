package no.uio.ifi.in2000.team33.lumo.data.database

import androidx.room.Embedded
import androidx.room.Relation


// Defines connection between mapPoint and takflater, one to many relationship
data class MapPointWithTakflateData(
    @Embedded val mapPoint: MapPointEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "mapPointId"
    )
    val takflateData: List<TakflateDataEntity>
)