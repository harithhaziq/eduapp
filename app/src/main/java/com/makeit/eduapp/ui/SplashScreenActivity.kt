package com.makeit.eduapp.ui

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.snackbar.Snackbar
import com.makeit.eduapp.EduApp
import com.makeit.eduapp.R
import com.makeit.eduapp.databinding.ActivitySplashScreenBinding

class SplashScreenActivity : AppCompatActivity() {
    val TAG = SplashScreenActivity::class.java.simpleName

    private lateinit var binding: ActivitySplashScreenBinding
//    private val prefs = EduApp.prefs!!

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)

        val videoView = binding.vvSplashScreenBg
        val videoUri = Uri.parse("android.resource://" + packageName + "/" + R.raw.splash_screen_bg)

        videoView.setVideoURI(videoUri)
        videoView.setOnPreparedListener {
            it.isLooping = true
            videoView.start()
        }

        videoView.setOnCompletionListener {
            Snackbar.make(view, "Loading Finished", Snackbar.LENGTH_SHORT).show()
        }
    }
}