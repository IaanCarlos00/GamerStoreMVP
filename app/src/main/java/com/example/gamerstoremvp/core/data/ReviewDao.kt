package com.example.gamerstoremvp.core.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ReviewDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: ReviewEntity)

    @Query("SELECT * FROM reviews WHERE productCode = :productCode ORDER BY id DESC")
    fun getReviewsForProduct(productCode: String): Flow<List<ReviewEntity>>
}
