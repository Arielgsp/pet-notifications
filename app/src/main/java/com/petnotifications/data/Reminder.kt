package com.petnotifications.data

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.Update
import com.petnotifications.Repeat
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.ZoneId

@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val note: String = "",
    val timeMillis: Long,
    val repeat: String = Repeat.NONE.name,
    /** Ya se avisó (solo aplica a los que no se repiten). */
    val fired: Boolean = false,
    /** Sin interrumpir: no aparece en pantalla, la mascota deja una carta sin interrumpir a sus pies. */
    @ColumnInfo(defaultValue = "0") val quiet: Boolean = false,
)

/**
 * Un aviso pospuesto (recordatorio que se repite o evento del calendario). Guardado para que
 * sobreviva a que se cierre la app o se reinicie el teléfono; se borra al volver a avisar.
 */
@Entity(tableName = "snoozes")
data class Snooze(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val atMillis: Long,
    val alertKey: String,
    val title: String,
    val body: String,
    val source: String,
    val reminderId: Long? = null,
    val eventId: Long? = null,
    val eventBegin: Long? = null,
    val repeating: Boolean = false,
)

/**
 * Registro local de cada aviso entregado, para la pantalla de diagnóstico: cuándo tenía que sonar,
 * cuándo sonó de verdad y quién lo mostró. No sale del teléfono.
 */
@Entity(tableName = "deliveries")
data class Delivery(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val alertKey: String,
    val source: String,
    val scheduledAt: Long,
    val deliveredAt: Long,
    /** true: lo dijo la mascota; false: solo la notificación (mascota oculta o pantalla apagada). */
    val byPet: Boolean,
    /** Si en ese momento la app tenía permiso de alarmas exactas. */
    val exactAlarms: Boolean,
)

/** Un aviso marcado como "Hecho" (solo en la app: nunca se escribe en el calendario del usuario). */
@Entity(tableName = "completions")
data class Completion(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val alertKey: String,
    val source: String,
    val reminderId: Long? = null,
    val eventId: Long? = null,
    val doneAt: Long,
)

@Dao
interface CompletionDao {
    @Insert
    suspend fun insert(c: Completion)

    @Query("SELECT * FROM completions ORDER BY doneAt DESC")
    suspend fun all(): List<Completion>

    @Query("SELECT COUNT(*) FROM completions WHERE alertKey = :key")
    suspend fun countForKey(key: String): Int
}

@Dao
interface DeliveryDao {
    @Insert
    suspend fun insert(d: Delivery)

    @Query("SELECT * FROM deliveries ORDER BY deliveredAt DESC")
    suspend fun all(): List<Delivery>

    /** Se guardan solo los últimos 90 días. */
    @Query("DELETE FROM deliveries WHERE deliveredAt < :before")
    suspend fun deleteOlderThan(before: Long)
}

@Dao
interface SnoozeDao {
    @Query("SELECT * FROM snoozes")
    suspend fun all(): List<Snooze>

    @Query("SELECT * FROM snoozes WHERE id = :id")
    suspend fun get(id: Long): Snooze?

    @Query("SELECT * FROM snoozes WHERE reminderId = :reminderId")
    suspend fun forReminder(reminderId: Long): List<Snooze>

    @Insert
    suspend fun insert(s: Snooze): Long

    @Query("DELETE FROM snoozes WHERE id = :id")
    suspend fun delete(id: Long)
}

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders ORDER BY fired ASC, timeMillis ASC")
    fun observeAll(): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders WHERE fired = 0 AND timeMillis <= :now ORDER BY timeMillis")
    suspend fun due(now: Long): List<Reminder>

    /** Los que todavía tienen que sonar (incluye los que vencieron con el teléfono apagado). */
    @Query("SELECT * FROM reminders WHERE fired = 0")
    suspend fun pending(): List<Reminder>

    @Query("SELECT * FROM reminders WHERE id = :id")
    suspend fun get(id: Long): Reminder?

    @Insert
    suspend fun insert(r: Reminder): Long

    @Update
    suspend fun update(r: Reminder)

    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun delete(id: Long)
}

@Database(entities = [Reminder::class, Snooze::class, Delivery::class, Completion::class], version = 5, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun reminders(): ReminderDao
    abstract fun snoozes(): SnoozeDao
    abstract fun deliveries(): DeliveryDao
    abstract fun completions(): CompletionDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "pets.db")
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                .build().also { instance = it }
        }

        /** v2: tabla de avisos pospuestos. Los recordatorios existentes quedan intactos. */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `snoozes` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`atMillis` INTEGER NOT NULL, `alertKey` TEXT NOT NULL, `title` TEXT NOT NULL, " +
                        "`body` TEXT NOT NULL, `source` TEXT NOT NULL, `reminderId` INTEGER, `eventId` INTEGER, " +
                        "`eventBegin` INTEGER, `repeating` INTEGER NOT NULL)",
                )
            }
        }

        /** v4: avisos marcados como "Hecho" (para las rachas). */
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `completions` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`alertKey` TEXT NOT NULL, `source` TEXT NOT NULL, `reminderId` INTEGER, `eventId` INTEGER, " +
                        "`doneAt` INTEGER NOT NULL)",
                )
            }
        }

        /** v5: recordatorios "sin interrumpir" (carta sin interrumpir). */
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `reminders` ADD COLUMN `quiet` INTEGER NOT NULL DEFAULT 0")
            }
        }

        /** v3: registro de entregas para el diagnóstico. */
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `deliveries` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`alertKey` TEXT NOT NULL, `source` TEXT NOT NULL, `scheduledAt` INTEGER NOT NULL, " +
                        "`deliveredAt` INTEGER NOT NULL, `byPet` INTEGER NOT NULL, `exactAlarms` INTEGER NOT NULL)",
                )
            }
        }
    }
}

/** Próxima ocurrencia estrictamente posterior a [now] de un recordatorio que se repite. */
fun nextOccurrence(timeMillis: Long, repeat: Repeat, now: Long, zone: ZoneId = ZoneId.systemDefault()): Long {
    if (repeat == Repeat.NONE) return timeMillis
    var t = Instant.ofEpochMilli(timeMillis).atZone(zone)
    while (t.toInstant().toEpochMilli() <= now) {
        t = if (repeat == Repeat.DAILY) t.plusDays(1) else t.plusWeeks(1)
    }
    return t.toInstant().toEpochMilli()
}
