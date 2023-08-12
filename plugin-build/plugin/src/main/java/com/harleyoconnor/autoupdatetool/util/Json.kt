package com.harleyoconnor.autoupdatetool.util

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

val MOSHI = Moshi.Builder()
    .add(ModVersionInfo::class.java, ModVersionInfo.Adapter().indent("  "))
    .addLast(KotlinJsonAdapterFactory())
    .build()
