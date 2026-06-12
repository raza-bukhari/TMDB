package com.example.tmdb.feature.videoplayer.di

import com.example.tmdb.feature.videoplayer.VideoPlayerViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val featureVideoPlayerModule = module {
    viewModelOf(::VideoPlayerViewModel)
}
