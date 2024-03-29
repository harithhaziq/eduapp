package com.makeit.eduapp.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.makeit.eduapp.adapter.ChildAdapter
import com.makeit.eduapp.databinding.FragmentHomeBinding
import com.makeit.eduapp.model.Child
import com.makeit.eduapp.model.User

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val TAG = HomeFragment::class.java.simpleName
    private val homeViewModel: HomeViewModel by viewModels()
    private lateinit var user: User
    private lateinit var childAdapter: ChildAdapter


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

    private fun userObservable() : Observer<User> = Observer {

        if (it != null)
        {
            Log.d(TAG, "onCreateView: userObserver")
            binding.tvWelcome.text = "Welcome, ${it.name}"

            if (user.isParent){
                binding.tvUserCategory.text = "Parent"
            } else {
                binding.tvUserCategory.text = "Dependent"
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()
        val usersCollection = db.collection("users")
        val parentsCollection = db.collection("parent")
        val childList = mutableListOf<Child>()
        val parent : User

        homeViewModel.user.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                Log.d(TAG, "onCreateView: userObserver")
                binding.tvWelcome.text = "Welcome, ${user.name}"
                binding.tvUserCategory.text = if (user.isParent) "Parent" else "Dependent"
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
                        val isParent: Boolean = category=="parent"

                        if (email == currentUser.email){
                            if (email != null && name != null && category != null){
                                user = User(email, name, isParent, mutableListOf())
                                homeViewModel.setUsername(user.name)
                                homeViewModel.setUser(user)

                                if (user != null){
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

                                                                if (email == childEmail && name != null && category != null) {
                                                                    childList.add(
                                                                        Child(
                                                                            childEmail,
                                                                            name,
                                                                            parentEmail,
                                                                            category
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




    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}