package com.my.mapupassessment.service

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.my.mapupassessment.utils.Constants
import com.my.mapupassessment.utils.prefs.SharedPrefManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class BaseApplication: Application(){


    @Inject
    lateinit var sharedPrefManager: SharedPrefManager

    override fun onCreate() {
        super.onCreate()

        val mode = sharedPrefManager.getInt(
            Constants.THEME_TOGGLE,
            AppCompatDelegate.MODE_NIGHT_NO
        )

        AppCompatDelegate.setDefaultNightMode(mode)

    }



}