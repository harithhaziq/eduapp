package com.makeit.eduapp.ui.home

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.makeit.eduapp.R
import com.makeit.eduapp.adapter.ChildAdapter
import com.makeit.eduapp.databinding.FragmentHomeBinding
import com.makeit.eduapp.model.Child
import com.makeit.eduapp.model.User
import com.makeit.eduapp.ui.SplashScreenActivity
import com.makeit.eduapp.util.MyFirebaseMessagingService
import com.makeit.eduapp.util.ScreenTimeForegroundService
import com.makeit.eduapp.util.ScreenTimeTracker
import com.makeit.eduapp.util.SnackbarDisplayListener
import java.io.IOException
import java.util.Locale

class HomeFragment : Fragment(){

    companion object{
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val TAG = HomeFragment::class.java.simpleName
    private val homeViewModel: HomeViewModel by viewModels()
    private lateinit var user: User
    private lateinit var childAdapter: ChildAdapter
    private lateinit var fusedLocationClient: FusedLocationProviderClient


    private val alarmManager: AlarmManager by lazy {
        requireActivity().getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }

    private val serviceIntent: Intent by lazy {
        Intent(requireContext(), ScreenTimeForegroundService::class.java)
    }

    private val pendingIntent: PendingIntent by lazy {
        PendingIntent.getService(
            requireContext(),
            0,
            serviceIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

//        val tvWelcome: TextView = binding.tvWelcome
//        val tvUserCategory: TextView = binding.tvUserCategory
//        val tvCountOfChild: TextView = binding.tvCountOfChild
//        homeViewModel.text.observe(viewLifecycleOwner) {
//            Log.d(TAG, "textObserver: ")
//            textView.text = it
//        }

//        homeViewModel.username.observe(viewLifecycleOwner){
//            tvWelcome.text = "Welcome, $it !"
//        }
//
//        homeViewModel.countOfChild.observe(viewLifecycleOwner){
//            tvCountOfChild.text = "You have $it dependent under you"
//        }
//
//        homeViewModel.userCategory.observe(viewLifecycleOwner){
//            tvUserCategory.text = it
//        }
        return root
    }

//    private fun userObservable() : Observer<User> = Observer {
//
//        if (it != null)
//        {
//            Log.d(TAG, "onCreateView: userObserver")
//            binding.tvWelcome.text = "Welcome, ${it.name}"
//
//            if (user.isParent){
//                binding.tvUserCategory.text = "Parent"
//            } else {
//                binding.tvUserCategory.text = "Dependent"
//            }
//        }
//    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnAddChild = binding.btnAddChild
        val etNewChildEmail = binding.etNewChildEmail

        val db = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()
        val usersCollection = db.collection("users")
        val parentsCollection = db.collection("parent")
        val childList = mutableListOf<Child>()
        val parent : User

        val myFcm = MyFirebaseMessagingService()
//        screenTimeForegroundService = ScreenTimeForegroundService()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        homeViewModel.address.observe(viewLifecycleOwner) { address ->
            Log.d(TAG, "onViewCreated: address $address")
            binding.tvAddress.text = "Address: $address"

            val userData = hashMapOf(
                "address" to address
            )

            auth.currentUser?.let {
                db.collection("users").document(it.uid)
                    .set(userData, SetOptions.merge())
                    .addOnSuccessListener {
                        Log.d(TAG, "User data added successfully")
                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Error adding user data", e)
                    }
            }
        }

        homeViewModel.screenTime.observe(viewLifecycleOwner) { time ->
            Log.d(TAG, "onViewCreated: screentime 1")
            binding.tvScreentime.text = "Today's screentime: ${time.toString()} minutes"

            val screenTime = time
            val userData = hashMapOf(
                "screenTime" to screenTime
            )

            auth.currentUser?.let {
                db.collection("users").document(it.uid)
                    .set(userData, SetOptions.merge())
                    .addOnSuccessListener {
                        Log.d(TAG, "User data added successfully")
                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Error adding user data", e)
                    }
            }

        }

        homeViewModel.isChildTo.observe(viewLifecycleOwner) { parentEmail ->
            binding.tvCountOfChild.text = "Parent's email: $parentEmail"
        }


        homeViewModel.user.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                Log.d(TAG, "onCreateView: userObserver")

                subscribeUserPushTopic(user)

                binding.tvWelcome.text = "Welcome, ${user.name}"
                binding.tvUserCategory.text = if (user.isParent) "Parent" else "Dependent"
                if (user.isParent){
                    binding.ivUser.setImageResource(R.drawable.ic_profile_parent)
                    binding.btnAddChild.visibility = View.VISIBLE
                } else {
                    binding.ivUser.setImageResource(R.drawable.ic_profile_dependent)
                    binding.cvAddChild.visibility = View.GONE
                    binding.cvChild.visibility = View.GONE
                }
            }
        }

//        homeViewModel.getUserObservable().observe(viewLifecycleOwner, userObservable())

        homeViewModel.childList.observe(viewLifecycleOwner){
            Log.d(TAG, "onCreateView: childObserver")
            binding.tvCountOfChild.text = "You have ${it.size} dependent under you"

            childAdapter = ChildAdapter(it) // Pass an empty list initially
            binding.rvChildList.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = childAdapter
            }
        }

        val totalScreenTime = ScreenTimeTracker.getTotalScreenTime(requireContext())
        homeViewModel.setScreenTime(totalScreenTime)

        // Check for location permission
        if (ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request location permission
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            // Permission already granted, get location
            getLocation()
        }

        val currentUser = auth.currentUser

        if (currentUser != null) {
            // User is signed in
            val uid = currentUser.uid
            val email = currentUser.email
            val displayName = currentUser.displayName

            // You can use the user details as needed
            Log.d(TAG, "UID: $uid")
            Log.d(TAG, "Email: $email")
            Log.d(TAG, "Display Name: $displayName")

                    // Example: Query all users
            usersCollection.get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        Log.d(TAG, "${document.id} => ${document.data}")
                        // Access the data in the document
                        val email = document.getString("email")
                        val name = document.getString("name")
                        val category = document.getString("category")
                        val token = document.getString("token")
                        val id = document.id
                        val isParent: Boolean = category=="parent"

                        if (email == currentUser.email){
                            if (email != null && name != null && category != null){
                                user = User(id, email, name, isParent, token?:"",mutableListOf())
                                homeViewModel.setUsername(user.name)
                                homeViewModel.setUser(user)

                                if (user != null){
                                    if (user.isParent){
                                        parentsCollection.get()
                                            .addOnSuccessListener { documents ->
                                                for (document in documents) {
                                                    Log.d(TAG, "${document.id} => ${document.data}")
                                                    //Access the data in the document
                                                    val parentEmail = document.getString("parentEmail")
                                                    val childEmail = document.getString("childEmail")

                                                    if (parentEmail != null && childEmail != null) {

                                                        if (user.email == parentEmail){

                                                            usersCollection.get()
                                                                .addOnSuccessListener { documents ->
                                                                    for (document in documents) {
                                                                        Log.d(
                                                                            TAG,
                                                                            "${document.id} => ${document.data}"
                                                                        )
                                                                        // Access the data in the document
                                                                        val email =
                                                                            document.getString("email")
                                                                        val name =
                                                                            document.getString("name")
                                                                        val category =
                                                                            document.getString("category")
                                                                        val token =
                                                                            document.getString("token")
                                                                        val screenTime =
                                                                            document.getLong("screenTime")
                                                                        val id = document.id

                                                                        if (email == childEmail && name != null && category != null) {
                                                                            childList.add(
                                                                                Child(
                                                                                    id,
                                                                                    childEmail,
                                                                                    name,
                                                                                    parentEmail,
                                                                                    category,
                                                                                    token?:"",
                                                                                    screenTime?:0
                                                                                )
                                                                            )
                                                                            homeViewModel.setChildList(
                                                                                childList
                                                                            )
                                                                        }
                                                                    }
                                                                }
                                                        }
                                                    }
                                                }
                                            }
                                    } else if (!user.isParent){
                                        parentsCollection.get()
                                            .addOnSuccessListener { documents ->
                                                for (document in documents) {
                                                    Log.d(TAG, "${document.id} => ${document.data}")
                                                    //Access the data in the document
                                                    val parentEmail =
                                                        document.getString("parentEmail")
                                                    val childEmail =
                                                        document.getString("childEmail")

                                                    if (parentEmail != null && childEmail != null && childEmail == user.email) {
                                                        homeViewModel.isChildTo(parentEmail)
                                                    }
                                                }
                                            }
                                    }
                                }
                                break
                            }
                            Log.d(TAG, "Name: $name, email: $email, category:$category")
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "Error getting documents: ", exception)
                }

        } else {
            // No user is signed in
            Log.d(TAG, "No user signed in")
        }
//        // Example: Query all users
//        usersCollection.get()
//            .addOnSuccessListener { documents ->
//                for (document in documents) {
//                    Log.d(TAG, "${document.id} => ${document.data}")
//                    // Access the data in the document
//                    val name = document.getString("name")
//                    val age = document.getLong("age")
//
//                    Log.d(TAG, "Name: $name, Age: $age")
//                }
//            }
//            .addOnFailureListener { exception ->
//                Log.w(TAG, "Error getting documents: ", exception)
//            }

        btnAddChild.setOnClickListener {

            var isChildExistInList = false
            for (child in childList){
                if (child.email == etNewChildEmail.text.toString()){
                    isChildExistInList = true
                }
            }

            if (isChildExistInList){
                Snackbar.make(view, "Child already exists.", Snackbar.LENGTH_SHORT).show()
            } else {
                //FIND CHILD EMAIL IF NOT EXIST
                usersCollection.get()
                    .addOnSuccessListener { documents ->
                        var isChildFound = false
                        for (document in documents) {
                            Log.d(TAG, "${document.id} => ${document.data}")
                            // Access the data in the document
                            val email = document.getString("email")

                            if (email == etNewChildEmail.text.toString()){
                                val newParentData = hashMapOf(
                                    "childEmail" to email,
                                    "parentEmail" to user.email
                                )

                                parentsCollection
                                    .add(newParentData)
                                    .addOnSuccessListener {
                                        Snackbar.make(view, "Child has been successfully saved.", Snackbar.LENGTH_SHORT).show()

                                        parentsCollection.get()
                                            .addOnSuccessListener { documents ->
                                                for (document in documents) {
                                                    Log.d(TAG, "${document.id} => ${document.data}")
                                                    //Access the data in the document
                                                    val parentEmail = document.getString("parentEmail")
                                                    val childEmail = document.getString("childEmail")

                                                    if (parentEmail != null && childEmail != null) {

                                                        if (user.email == parentEmail){

                                                            usersCollection.get()
                                                                .addOnSuccessListener { documents ->
                                                                    for (document in documents) {
                                                                        Log.d(
                                                                            TAG,
                                                                            "${document.id} => ${document.data}"
                                                                        )
                                                                        // Access the data in the document
                                                                        val email =
                                                                            document.getString("email")
                                                                        val name =
                                                                            document.getString("name")
                                                                        val category =
                                                                            document.getString("category")
                                                                        val token =
                                                                            document.getString("token")
                                                                        val screenTime =
                                                                            document.getLong("screenTime")
                                                                        val id =
                                                                            document.id

                                                                        if (email == childEmail && name != null && category != null) {
                                                                            childList.add(
                                                                                Child(
                                                                                    id,
                                                                                    childEmail,
                                                                                    name,
                                                                                    parentEmail,
                                                                                    category,
                                                                                    token?:"",
                                                                                    screenTime?:0
                                                                                )
                                                                            )
                                                                            homeViewModel.setChildList(
                                                                                childList
                                                                            )
                                                                        }
                                                                    }
                                                                }
                                                        }
                                                    }
                                                }
                                            }

                                    }
                                    .addOnFailureListener {
                                        Snackbar.make(view, "Child found but failed to register.", Snackbar.LENGTH_SHORT).show()
                                        Log.d(TAG, "onFailure: ${it.toString()}")

                                    }
                                isChildFound = true
                                break
                            }
                        }
                        if (!isChildFound){
                            Snackbar.make(view, "Child not found.", Snackbar.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.w(TAG, "Error getting documents: ", exception)
                        Snackbar.make(view, "Error getting documents.", Snackbar.LENGTH_SHORT).show()
                    }
                }
            }

        binding.btnLogout.setOnClickListener {

            FirebaseMessaging.getInstance().unsubscribeFromTopic(user.id).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "unsubscribed from topic: ${user.id}")
                    Toast.makeText(requireContext(), "Unsubscribed from topic: ${user.id}", Toast.LENGTH_SHORT).show()

                    FirebaseAuth.getInstance().signOut()

                    context?.stopService(Intent(context, ScreenTimeForegroundService::class.java))

                    context?.startActivity(Intent(context, SplashScreenActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    })
                } else {
                    Log.d(TAG, "Fail to unsubscribe from topic: ${task.result.toString()}")
                    Toast.makeText(requireContext(), "Fail to unsubscribe: ${user.id}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        startScreenTimeForegroundService()

    }

    private fun subscribeUserPushTopic(user: User){
        if (user.token.isNotEmpty()){
            FirebaseMessaging.getInstance().subscribeToTopic(user.email)
        } else {
            Log.d(TAG, "subscribeUserPushTopic: user.token empty")
        }
    }

    private fun startScreenTimeForegroundService() {

        val serviceIntent = Intent(requireContext(), ScreenTimeForegroundService::class.java)
        requireActivity().startService(serviceIntent)

        Log.d(TAG, "startScreenTimeForegroundService: in homefragment")
        // Schedule the service to run
        val intervalMillis = 60 * 1000L //
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + intervalMillis,
            intervalMillis,
            pendingIntent
        )
    }

    @SuppressLint("MissingPermission")
    private fun getLocation() {
        // Get last known location
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    // Got location, now perform reverse geocoding
                    getAddressFromLocation(it.latitude, it.longitude)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    requireContext(),
                    "Failed to get location: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun getAddressFromLocation(latitude: Double, longitude: Double) {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (addresses != null) {
                if (addresses.isNotEmpty()) {
                    val address = addresses?.get(0)
                    Log.d(TAG, "getAddressFromLocation: addresses.get ${addresses.toString()}")
                    val addressText = address?.getAddressLine(0)
                    Log.d(TAG, "getAddressFromLocation: address.getAddressLine ${address.toString()}")

                    Toast.makeText(requireContext(), "Address: $addressText", Toast.LENGTH_LONG).show()
                    if (addressText != null) {
                        homeViewModel.setAddress(addressText)
                    }
                } else {
                    Toast.makeText(requireContext(), "No address found", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (ioException: IOException) {
            Toast.makeText(
                requireContext(),
                "Failed to get address: ${ioException.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}