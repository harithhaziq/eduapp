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
    private val _screenTime = MutableLiveData<Long>()
    private val _address = MutableLiveData<String>()
    private val _isChildTo = MutableLiveData<String>()

    // Expose LiveData objects to the UI
    val username: LiveData<String> = _username
    val text: LiveData<String> = _text
    val childList: LiveData<List<Child>> = _childList
    val user: LiveData<User> = _user
    val screenTime: LiveData<Long> = _screenTime
    val address: LiveData<String> = _address
    val isChildTo: LiveData<String> = _isChildTo
    // Method to update username


    fun isChildTo(parentName: String){
        _isChildTo.value = parentName
    }

    fun setAddress(address: String){
        _address.value = address
    }
    fun setScreenTime(time: Long){
        _screenTime.value = time
    }
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
}