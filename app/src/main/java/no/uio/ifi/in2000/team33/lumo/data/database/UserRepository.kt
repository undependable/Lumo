package no.uio.ifi.in2000.team33.lumo.data.database

import kotlinx.coroutines.flow.Flow
import no.uio.ifi.in2000.team33.lumo.ui.utility.data.UserInfo
import javax.inject.Inject

/**
 * Repository class for user information and power consumption data
 * ViewModel access to the local database
 */
class UserRepository @Inject constructor(
    private val userInfoDao: UserInfoDao,
    private val powerConsumptionDao: PowerConsumptionDao
) {

    // gets user info as an observable flow
    fun getUserInfo(): Flow<UserInfo?> {
        return userInfoDao.getUserInfo()
    }

    // updates or inserts user info
    suspend fun updateUserInfo(userInfo: UserInfo) {
        userInfoDao.upsertUserInfo(userInfo)
    }

    // gets the power consumption as an observable flow
    fun getPowerConsumption(): Flow<PowerConsumptionEntity?> {
        return powerConsumptionDao.getPowerConsumption()
    }

    // updates or inserts power consumption data
    suspend fun updatePowerConsumption(powerConsumption: PowerConsumptionEntity) {
        powerConsumptionDao.upsertPowerConsumption(powerConsumption)
    }
}