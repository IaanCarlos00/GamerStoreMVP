package com.example.gamerstoremvp.core.data

import kotlinx.coroutines.flow.Flow

class ReviewRepository(private val reviewDao: ReviewDao) {

    fun getReviewsForProduct(productCode: String): Flow<List<ReviewEntity>> {
        return reviewDao.getReviewsForProduct(productCode)
    }

    suspend fun insertReview(review: ReviewEntity) {
        reviewDao.insertReview(review)
    }
}
