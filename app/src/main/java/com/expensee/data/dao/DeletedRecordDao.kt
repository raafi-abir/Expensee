package com.expensee.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.expensee.data.model.DeletedRecordEntity

@Dao
interface DeletedRecordDao {
    @Query("SELECT * FROM deleted_records")
    suspend fun getAllDeleted(): List<DeletedRecordEntity>

    @Query("SELECT syncId FROM deleted_records")
    suspend fun getAllDeletedSyncIds(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun recordDeleted(deleted: DeletedRecordEntity)

    @Query("DELETE FROM deleted_records WHERE syncId = :syncId")
    suspend fun removeDeleted(syncId: String)

    @Query("DELETE FROM deleted_records")
    suspend fun clearAll()
}
