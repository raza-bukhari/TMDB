package com.example.tmdb.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "movie_remote_keys")
data class MovieRemoteKeysEntity(
    @PrimaryKey val id: Long,
    val category: String,
    val prevPage: Int?,
    val nextPage: Int?
)
