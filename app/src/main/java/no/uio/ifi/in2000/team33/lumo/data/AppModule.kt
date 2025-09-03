package no.uio.ifi.in2000.team33.lumo.data

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import no.uio.ifi.in2000.team33.lumo.data.address.AddressRepository
import no.uio.ifi.in2000.team33.lumo.data.database.MapPointDao
import no.uio.ifi.in2000.team33.lumo.data.database.MapPointRepository
import no.uio.ifi.in2000.team33.lumo.data.database.PowerConsumptionDao
import no.uio.ifi.in2000.team33.lumo.data.database.UserDatabase
import no.uio.ifi.in2000.team33.lumo.data.database.UserInfoDao
import no.uio.ifi.in2000.team33.lumo.data.database.UserRepository
import no.uio.ifi.in2000.team33.lumo.data.electricity.ElectricityPriceRepository
import no.uio.ifi.in2000.team33.lumo.data.frost.FrostRepository
import no.uio.ifi.in2000.team33.lumo.data.pvgis.PvgisRepository
import javax.inject.Singleton

/**
 * Hilt module that provides dependencies for the app
 * defines dependencies that need to exist for the lifttime of the application,
 * like database access objects, repositories, etc.
 *
 */

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * Provides the Room database instance for user data
     *
     * @param context The application context
     * @return The Room database instance
     */

    @Provides
    @Singleton
    fun provideUserDatabase(
        @ApplicationContext context: Context
    ): UserDatabase {
        return Room.databaseBuilder(
            context,
            UserDatabase::class.java,
            "userDatabase"
        )
            .fallbackToDestructiveMigration(true) // Make sure app doesn't crash if db migration fails
            .build()
    }

    @Provides
    @Singleton
    fun provideUserInfoDao(db: UserDatabase) = db.getUserInfoDao()

    @Provides
    @Singleton
    fun provideMapPointDao(db: UserDatabase) = db.getMapPointDao()

    @Provides
    @Singleton
    fun provideMapPointRepository(mapPointDao: MapPointDao): MapPointRepository {
        return MapPointRepository(mapPointDao)
    }

    @Provides
    @Singleton
    fun providePowerConsumptionDao(database: UserDatabase): PowerConsumptionDao {
        return database.getPowerConsumptionDao()
    }

    @Provides
    @Singleton
    fun provideUserRepository(
        userInfoDao: UserInfoDao,
        powerConsumptionDao: PowerConsumptionDao
    ): UserRepository {
        return UserRepository(userInfoDao, powerConsumptionDao)
    }

    @Provides
    @Singleton
    fun provideAddressRepository() = AddressRepository()

    @Provides
    @Singleton
    fun provideElectricityPriceRepository() = ElectricityPriceRepository()

    @Provides
    @Singleton
    fun provideFrostRepository() = FrostRepository()

    @Provides
    @Singleton
    fun providePvgisRepository() = PvgisRepository()
}