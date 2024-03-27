package com.makeit.eduapp

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import com.makeit.eduapp.sharedpref.PreferenceHelper

class EduApp : MultiDexApplication() {

    companion object {
        var prefs: PreferenceHelper? = null
    }
    override fun onCreate() {
        super.onCreate()

        prefs = PreferenceHelper(applicationContext)

    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }
}