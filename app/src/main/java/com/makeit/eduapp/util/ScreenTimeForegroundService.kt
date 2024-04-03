package com.makeit.eduapp.util

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class ScreenTimeForegroundService : Service() {

    var context: Context = this
    var handler: Handler? = null
    var runnable: Runnable? = null

    private var snackbarDisplayListener: SnackbarDisplayListener? = null

    companion object{
        val TAG = ScreenTimeForegroundService::class.java.simpleName
    }

    fun setSnackbarDisplayListener(listener: SnackbarDisplayListener) {
        this.snackbarDisplayListener = listener
    }
//    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        // Obtain screen time
//        val screenTime = ScreenTimeTracker.getTotalScreenTime(applicationContext)
//        Log.d(TAG, "onStartCommand: test")
//
////        snackbarDisplayListener?.showSnackbar("Your message here")
//
//        // Update Firestore with screen time
////        updateFirestore(screenTime)
//
//        // Return START_STICKY to ensure the service restarts if it's killed by the system
//        return START_STICKY
//    }

    override fun onCreate() {
        Toast.makeText(this, "Service created!", Toast.LENGTH_LONG).show()

        handler = Handler()
        runnable = Runnable {
            Toast.makeText(context, "Service is still running", Toast.LENGTH_LONG).show()
            Log.d(TAG, "onCreate: Service is still running")
            val screenTime = ScreenTimeTracker.getTotalScreenTime(applicationContext)
            val address =
            updateFirestore(screenTime = screenTime)
            handler!!.postDelayed(runnable!!, 5 * 60 * 1000L)
        }

        handler!!.postDelayed(runnable!!, 15000)
    }

    override fun onDestroy() {
        /* IF YOU WANT THIS SERVICE KILLED WITH THE APP THEN UNCOMMENT THE FOLLOWING LINE */
        //handler.removeCallbacks(runnable);
        Toast.makeText(this, "Service stopped", Toast.LENGTH_LONG).show()
    }

    override fun onStart(intent: Intent?, startid: Int) {
        Toast.makeText(this, "Service started by user.", Toast.LENGTH_LONG).show()
    }

    private fun updateFirestore(screenTime: Long) {
        // Update Firestore with the obtained screen time
        // Example code to write data to Firestore
        val db = FirebaseFirestore.getInstance()
        val user = FirebaseAuth.getInstance().currentUser

        user?.let {
            db.collection("users").document(user.uid)
                .update("screenTime", screenTime)
                .addOnSuccessListener {
                    Log.d(TAG, "Screen time updated successfully.")
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error updating screen time", e)
                }
        }
    }

    // Function to stop the runnable and handler
    private fun stopHandler() {
        runnable?.let { handler?.removeCallbacks(it) }
        Toast.makeText(this, "Service stopped", Toast.LENGTH_LONG).show()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}