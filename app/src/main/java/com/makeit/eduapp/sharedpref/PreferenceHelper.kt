package com.makeit.eduapp.sharedpref

import android.content.Context
import android.content.SharedPreferences

class PreferenceHelper(context : Context) {

    val prefs: SharedPreferences = context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)

}