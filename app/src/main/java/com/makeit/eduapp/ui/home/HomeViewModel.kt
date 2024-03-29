package com.makeit.eduapp.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.makeit.eduapp.model.Child
import com.makeit.eduapp.model.User

class HomeViewModel : ViewModel() {
    companion object{
        private val TAG = HomeViewModel::class.java.simpleName
    }

    // Example LiveData objects
    private val _username = MutableLiveData<String>()
    private val _text = MutableLiveData<String>()
    private val _childList = MutableLiveData<List<Child>>()
    private val _user = MutableLiveData<User>()

    // Expose LiveData objects to the UI
    val username: LiveData<String> = _username
    val text: LiveData<String> = _text
    val childList: LiveData<List<Child>> = _childList
    val user: LiveData<User> = _user

    // Method to update username
    fun setUsername(name: String) {
        _username.value = name
    }

    // Method to update text
    fun setText(newText: String) {
        _text.value = newText
    }

    // Method to update child list
    fun setChildList(newList: List<Child>) {
        _childList.value = newList
    }

    // Method to update user
    fun setUser(newUser: User) {
        _user.value = newUser
    }

    fun getUserObservable() : LiveData<User>
    {
        return _user
    }
}