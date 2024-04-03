package com.makeit.eduapp.ui

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.makeit.eduapp.MainActivity
import com.makeit.eduapp.R
import com.makeit.eduapp.databinding.ActivitySplashScreenBinding


class SplashScreenActivity : AppCompatActivity() {
    val TAG = SplashScreenActivity::class.java.simpleName

    private lateinit var binding: ActivitySplashScreenBinding
//    private val prefs = EduApp.prefs!!

    companion object{
        const val MY_PERMISSIONS_REQUEST_PACKAGE_USAGE_STATS = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)

        val etUsername = binding.usernameEditText
        val etPassword = binding.passwordEditText
        val etFName = binding.fNameEditText
        val btnLogin = binding.loginButton
        val btnRegister = binding.registerButton
        val videoView = binding.vvSplashScreenBg
        val videoUri = Uri.parse("android.resource://" + packageName + "/" + R.raw.splash_screen_bg)
        var categorySelection = ""

        videoView.setVideoURI(videoUri)
        videoView.setOnPreparedListener {
            it.isLooping = true
            videoView.start()
        }

//        videoView.setOnCompletionListener {
//            Snackbar.make(view, "Loading Finished", Snackbar.LENGTH_SHORT).show()
//
//            val intent = Intent(this, MainActivity::class.java)
//            startActivity(intent)
//        }

//        btnLogin.setOnClickListener {
//            if (isValidCredentials((etUsername.text).toString(), (etPassword.text).toString())){
//                Snackbar.make(view, "Wrong username/password", Snackbar.LENGTH_SHORT).show()
//            } else {
//                showIncompleteCredential(view)
//            }
//        }
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()

        val dropdownTextView: Spinner = binding.categoryDropdown

        val items = listOf("parent", "child")

        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, items)
        dropdownTextView.setAdapter(adapter)

        dropdownTextView.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                // Handle item selection
                val selectedItem = items[position]
                categorySelection = selectedItem
                Toast.makeText(
                    this@SplashScreenActivity,
                    "Selected: $selectedItem",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Handle no item selected
            }
        }

        btnLogin.setOnClickListener {
            if (isValidCredentials((etUsername.text).toString(), (etPassword.text).toString())){
                auth.signInWithEmailAndPassword(etUsername.text.toString(), etPassword.text.toString())
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            val uid = user?.uid

                            if (uid != null) {
                                FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        val token = task.result
                                        val userData = hashMapOf(
                                            "email" to user.email,
                                            "token" to token
                                        )

                                        db.collection("users").document(uid)
                                            .set(userData, SetOptions.merge())
                                            .addOnSuccessListener {
                                                Log.d(TAG, "User data added successfully")
                                            }
                                            .addOnFailureListener { e ->
                                                Log.w(TAG, "Error adding user data", e)
                                            }
                                        Log.d(TAG, "onCreate: token: ${token.toString()}")
                                    } else {
                                        Log.d(TAG, "onCreate: task: ${task.result.toString()}")
                                    }
                                }



                                val intent = Intent(this, MainActivity::class.java)
                                intent.putExtra("user", user)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                            }
                        } else {
                            showWrongCredential(view)
                            Log.w(TAG, "Authentication failed", task.exception)
                        }
                    }
            } else {
                showIncompleteCredential(view)
            }
        }


        btnRegister.setOnClickListener {

            if (etFName.visibility == View.GONE || dropdownTextView.visibility == View.GONE){
                etFName.visibility = View.VISIBLE
                dropdownTextView.visibility = View.VISIBLE
            } else {
                if ((!isValidCredentials(etUsername.text.toString(), etPassword.text.toString())) || etFName.text?.isNotEmpty() == false || !categorySelection.isNotBlank()){
                    showIncompleteCredential(view)
                } else {
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(etUsername.text.toString(), etPassword.text.toString())
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Registration successful, handle new user
                            val user = FirebaseAuth.getInstance().currentUser
                            if (user != null) {
                                FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        val token = task.result
                                        val userData = hashMapOf(
                                            "email" to user.email,
                                            "token" to token,
                                            "name" to binding.fNameEditText.text.toString(),
                                            "category" to categorySelection
                                        )

                                        db.collection("users").document(user.uid)
                                            .set(userData)
                                            .addOnSuccessListener {
                                                Log.d(TAG, "User data added successfully")
                                                Snackbar.make(view, "Registration Success. Please login", Snackbar.LENGTH_SHORT).show()

                                                binding.fNameEditText.visibility = View.GONE
                                                binding.categoryDropdown.visibility = View.GONE
                                            }
                                            .addOnFailureListener { e ->
                                                Log.w(TAG, "Error adding user data", e)
                                            }
                                        Log.d(TAG, "onCreate: token: ${token.toString()}")
                                    } else {
                                        Log.d(TAG, "onCreate: task: ${task.result.toString()}")
                                    }
                                }
                            }
                        } else {
                            // Registration failed, handle error
                            Toast.makeText(this, "Registration failed", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
//            if (isValidCredentials(username = etUsername.text.toString(), password = etPassword.text.toString())){
//
//                FirebaseAuth.getInstance().createUserWithEmailAndPassword(etUsername.text.toString(), etPassword.text.toString())
//                    .addOnCompleteListener { task ->
//                        if (task.isSuccessful) {
//                            // Registration successful, handle new user
//                            val user = FirebaseAuth.getInstance().currentUser
//                            if (user != null) {
//                                // Store additional user data in Firestore
//                                val userData = hashMapOf(
//                                    "email" to user.email,
//                                    "name" to "John Doe",
//                                    "age" to 30
//                                    // Add more user data as needed
//                                )
//
//                                val db = FirebaseFirestore.getInstance()
//                                db.collection("users").document(user.uid)
//                                    .set(userData)
//                                    .addOnSuccessListener {
//                                        Log.d(TAG, "User data added successfully")
//                                        // Navigate to the main activity or perform other actions
//                                    }
//                                    .addOnFailureListener { e ->
//                                        Log.w(TAG, "Error adding user data", e)
//                                        // Handle error
//                                    }
//                            }
//                        } else {
//                            // Registration failed, handle error
//                            Toast.makeText(this, "Registration failed", Toast.LENGTH_SHORT).show()
//                        }
//                    }
//
//            } else {
//                showIncompleteCredential(view)
//            }
        }

        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(), packageName
        )

        if (mode == AppOpsManager.MODE_ALLOWED){

        } else {
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            startActivityForResult(intent, MY_PERMISSIONS_REQUEST_PACKAGE_USAGE_STATS)
        }
    }


    private fun showIncompleteCredential(view: View) {
        Snackbar.make(view, "Incomplete credential", Snackbar.LENGTH_SHORT).show()
    }

    private fun showWrongCredential(view: View) {
        Snackbar.make(view, "Wrong username/passsword", Snackbar.LENGTH_SHORT).show()
    }

    private fun isValidCredentials(username: String, password: String) : Boolean{
        return username.isNotEmpty() && password.isNotEmpty()
    }
}