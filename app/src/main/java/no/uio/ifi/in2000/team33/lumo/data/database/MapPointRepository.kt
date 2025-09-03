package no.uio.ifi.in2000.team33.lumo.data.database

import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import no.uio.ifi.in2000.team33.lumo.ui.home.ViewModel.RoofData
import no.uio.ifi.in2000.team33.lumo.ui.utility.data.MapPoint
import javax.inject.Inject

class MapPointRepository @Inject constructor(private val mapPointDao: MapPointDao) {

    // Adds or updates mappoint in database
    @Transaction
    suspend fun saveMapPointWithTakflate(mapPoint: MapPoint): Int {
        // Try to find existing entity first
        val existingEntities = mapPointDao.getMapPointByNameAndCoordinates(
            mapPoint.name,
            mapPoint.lat,
            mapPoint.lon
        )

        val mapPointEntity = toMapPointEntity(mapPoint)

        // If we found an existing entity, preserve its ID
        if (existingEntities.isNotEmpty()) {
            val existingId = existingEntities.first().id
            val updatedEntity = mapPointEntity.copy(id = existingId)
            mapPointDao.upsertMapPoint(updatedEntity)

            // Clear existing takflate data
            mapPointDao.deleteTakflateDataForMapPoint(existingId)

            // Add new takflate data
            if (mapPoint.registeredRoofs.isNotEmpty()) {
                val takflateEntities = mapPoint.registeredRoofs.map { takflate ->
                    TakflateDataEntity(
                        navn = takflate.navn,
                        area = takflate.area.toDoubleOrNull() ?: 0.0,
                        vinkel = takflate.vinkel.toIntOrNull() ?: 0,
                        retning = takflate.retning,
                        mapPointId = existingId
                    )
                }
                mapPointDao.upsertTakflateDataList(takflateEntities)
            }

            return existingId
        } else {
            // No existing entity, proceed with normal insert
            val id = mapPointDao.upsertMapPoint(mapPointEntity).toInt()

            if (mapPoint.registeredRoofs.isNotEmpty()) {
                val takflateEntities = mapPoint.registeredRoofs.map { takflate ->
                    TakflateDataEntity(
                        navn = takflate.navn,
                        area = takflate.area.toDoubleOrNull() ?: 0.0,
                        vinkel = takflate.vinkel.toIntOrNull() ?: 0,
                        retning = takflate.retning,
                        mapPointId = id
                    )
                }
                mapPointDao.upsertTakflateDataList(takflateEntities)
            }

            return id
        }
    }


    // deletes mappoint from database
    @Transaction
    suspend fun deleteMapPoint(mapPoint: MapPoint) {
        // Find the entity in database first to ensure we have the correct ID
        val existingPoints = mapPointDao.getMapPointByNameAndCoordinates(
            mapPoint.name,
            mapPoint.lat,
            mapPoint.lon
        )

        if (existingPoints.isNotEmpty()) {
            // Delete the found entity (or entities if there are duplicates)
            existingPoints.forEach { entity ->
                mapPointDao.deleteMapPoint(entity)
            }
        } else {
            // If not found by coordinates, try by name as fallback
            val entitiesByName = mapPointDao.getMapPointByName(mapPoint.name)
            entitiesByName.forEach { entity ->
                mapPointDao.deleteMapPoint(entity)
            }
        }
    }


    // loadPoints() function, loads from db
    fun getMapPointsWithTakflateData(): Flow<List<MapPoint>> {
        return mapPointDao.getMapPointsWithTakflateData().map { mapPointWithDataList ->
            mapPointWithDataList.map { mapPointWithData ->
                toMapPoint(mapPointWithData.mapPoint, mapPointWithData.takflateData)
            }
        }
    }


    // data conversion functions
    // convert MapPoint to MapPointEntity
    private fun toMapPointEntity(mapPoint: MapPoint): MapPointEntity {
        return MapPointEntity(
            name = mapPoint.name,
            lat = mapPoint.lat,
            lon = mapPoint.lon,
            isFavorite = mapPoint.isFavorite,
            erHus = mapPoint.isHouse,
            postnummer = mapPoint.areaCode,
            poststed = mapPoint.areaPlace,
            region = mapPoint.region,
            closestStationID = mapPoint.closestStationID,
        )
    }

    // converts mapPointEntity to MapPoint, including takflate data
    private fun toMapPoint(
        entity: MapPointEntity,
        takflateList: List<TakflateDataEntity>
    ): MapPoint {
        return MapPoint(
            name = entity.name,
            lat = entity.lat,
            lon = entity.lon,
            isFavorite = entity.isFavorite,
            isHouse = entity.erHus,
            areaCode = entity.postnummer,
            areaPlace = entity.poststed,
            region = entity.region,
            closestStationID = entity.closestStationID,
            temperature = 10000.0, // default
            priceThisHour = "", // default
            annualEstimate = 0.0, // default
            registeredRoofs = takflateList.map { entity ->
                RoofData(
                    navn = entity.navn,
                    area = entity.area.toString(),
                    vinkel = entity.vinkel.toString(),
                    retning = entity.retning
                )
            }.toMutableList(),

            )
    }
}