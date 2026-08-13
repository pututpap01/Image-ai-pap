package com.example.data.repository

import com.example.data.local.GeneratedImageDao
import com.example.data.local.GeneratedImageEntity
import kotlinx.coroutines.flow.Flow

class ImageRepository(private val dao: GeneratedImageDao) {

    val allImages: Flow<List<GeneratedImageEntity>> = dao.getAllImages()
    val favoriteImages: Flow<List<GeneratedImageEntity>> = dao.getFavoriteImages()

    suspend fun saveImage(image: GeneratedImageEntity): Long {
        return dao.insertImage(image)
    }

    suspend fun toggleFavorite(image: GeneratedImageEntity) {
        dao.updateImage(image.copy(isFavorite = !image.isFavorite))
    }

    suspend fun deleteImage(image: GeneratedImageEntity) {
        dao.deleteImage(image)
    }

    suspend fun deleteImageById(id: Long) {
        dao.deleteImageById(id)
    }

    suspend fun clearAll() {
        dao.deleteAllImages()
    }
}
