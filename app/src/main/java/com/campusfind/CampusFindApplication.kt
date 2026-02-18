package com.campusfind

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * FILE: app/src/main/java/com/campusfind/CampusFindApplication.kt
 *
 * @HiltAndroidApp triggers Hilt code generation for the entire app.
 * Must be registered in AndroidManifest.xml via android:name=".CampusFindApplication"
 *
 * See: DEC-022 (Hilt), TASK-100
 */
@HiltAndroidApp
class CampusFindApplication : Application()