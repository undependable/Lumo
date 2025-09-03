package no.uio.ifi.in2000.team33.lumo.data.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for power consumption operations*,
 * made for a single user system with fixed userId
 */

@Dao
interface PowerConsumptionDao {
    @Upsert
    suspend fun upsertPowerConsumption(powerConsumption: PowerConsumptionEntity)

    @Query("SELECT * FROM power_consumption WHERE userId = 1")
    fun getPowerConsumption(): Flow<PowerConsumptionEntity?>
}