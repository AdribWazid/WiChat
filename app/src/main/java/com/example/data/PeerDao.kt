package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.model.PeerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PeerDao {
    @Query("SELECT * FROM peers ORDER BY isOnline DESC, lastSeen DESC")
    fun getAllPeers(): Flow<List<PeerEntity>>

    @Query("SELECT * FROM peers WHERE isOnline = 1 ORDER BY lastSeen DESC")
    fun getOnlinePeers(): Flow<List<PeerEntity>>

    @Query("SELECT * FROM peers WHERE userId = :userId LIMIT 1")
    suspend fun getPeerById(userId: String): PeerEntity?

    @Query("SELECT * FROM peers WHERE userId = :userId LIMIT 1")
    fun getPeerFlowById(userId: String): Flow<PeerEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(peer: PeerEntity)

    @Query("UPDATE peers SET isOnline = :isOnline, lastSeen = :lastSeen WHERE userId = :userId")
    suspend fun updateStatus(userId: String, isOnline: Boolean, lastSeen: Long)

    @Query("UPDATE peers SET ipAddress = :ipAddress, port = :port, isOnline = 1, lastSeen = :lastSeen WHERE userId = :userId")
    suspend fun updateAddressAndOnline(userId: String, ipAddress: String, port: Int, lastSeen: Long)

    @Query("UPDATE peers SET isOnline = 0")
    suspend fun markAllOffline()

    @Query("DELETE FROM peers WHERE userId = :userId")
    suspend fun deletePeer(userId: String)
}
