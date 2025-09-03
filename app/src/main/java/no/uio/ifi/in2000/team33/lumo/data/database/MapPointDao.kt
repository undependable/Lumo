package no.uio.ifi.in2000.team33.lumo.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
/**
 * Data Access Object (DAO) for map points and associated roof surface operations
 */

@Dao
interface MapPointDao {

    // inserts or updates map point in database
    @Upsert
    suspend fun upsertMapPoint(mapPoint: MapPointEntity) : Long // returns id for new entry

    // deletes map point from database
    @Delete
    suspend fun deleteMapPoint(mapPoint: MapPointEntity)

    // inserts or updates list of roof surface data
    @Upsert
    suspend fun upsertTakflateDataList(takflateDataList: List<TakflateDataEntity>)

    // deletes all roof surface data for a given map point
    @Query("DELETE FROM takflate_data WHERE mapPointId = :mapPointId")
    suspend fun deleteTakflateDataForMapPoint(mapPointId: Int)

    // finds map point by name and coordinates
    @Query("SELECT * FROM map_points WHERE name = :name AND lat = :lat AND lon = :lon")
    suspend fun getMapPointByNameAndCoordinates(name: String, lat: Double, lon: Double): List<MapPointEntity>

    // finds map point by name
    @Query("SELECT * FROM map_points WHERE name = :name")
    suspend fun getMapPointByName(name: String): List<MapPointEntity>

    // gets all map points with associated roof surface data as an observable flow
    // uses room's @Transaction annotation to ensure consistent retrieval of related data
    @Transaction
    @Query("SELECT * FROM map_points")
    fun getMapPointsWithTakflateData(): Flow<List<MapPointWithTakflateData>>

}