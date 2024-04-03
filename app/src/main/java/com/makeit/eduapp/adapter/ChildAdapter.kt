package com.makeit.eduapp.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.makeit.eduapp.R
import com.makeit.eduapp.model.Child
import org.json.JSONException
import org.json.JSONObject


class ChildAdapter(private val childList: List<Child>) : RecyclerView.Adapter<ChildAdapter.ChildItemHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChildItemHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.child_item_holder, parent, false)
        return ChildItemHolder(itemView)
    }

    override fun onBindViewHolder(holder: ChildItemHolder, position: Int) {
        val currentChild = childList[position]
        holder.bind(currentChild)
    }

    override fun getItemCount() = childList.size

    inner class ChildItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val FCM_API = "https://fcm.googleapis.com/fcm/send"
        private val serverKey = "key=" + "AAAAs8IkiZU:APA91bEMSJI6V5kVLpv2SlkUQYLTqMc46Sh-J1MR5kb0X20rxinkGFL1OGD7jRQFhFDivo1qUnFn3kicpZaxJxwnlSXcjh9ERqUXINbu4K9y7bPfCX3iuS5EBaa8yGuQrg5lKjZmeYKe"
        private val contentType = "application/json"

        private val tvChildName: TextView = itemView.findViewById(R.id.tv_name)
        private val tvChildScreentime: TextView = itemView.findViewById(R.id.tv_screentime)
        private val btnNotify: Button = itemView.findViewById(R.id.btn_notify)
        private val MAX_TIME_LIMIT_IN_MINUTES = 20
        fun bind(child: Child) {
            tvChildName.text = child.name
            tvChildScreentime.text = "Screen time: ${child.screenTime.toString()} mins"

            btnNotify.setOnClickListener {
                val topic = "/topics/${child.id}" //topic has to match what the receiver subscribed to

                val notification = JSONObject()
                val notifcationBody = JSONObject()

                try {
                    if (child.screenTime > MAX_TIME_LIMIT_IN_MINUTES){
                        notifcationBody.put("title", "Your usage has exceeded limit.")
                        notifcationBody.put("message", "Please be mindful with your screen time !")   //Enter your notification message
                        notification.put("to", topic)
                        notification.put("data", notifcationBody)
                        Log.e("TAG", "try")
                    }
                } catch (e: JSONException) {
                    Log.e("TAG", "onCreate: " + e.message)
                }

                val requestQueue = Volley.newRequestQueue(itemView.context)
                sendNotification(requestQueue, notification)
            }

        }
        fun sendNotification(requestQueue: RequestQueue, notification: JSONObject) {
            Log.e("TAG", "sendNotification")
            val jsonObjectRequest = object : JsonObjectRequest(FCM_API, notification,
                com.android.volley.Response.Listener<JSONObject> { response ->
                    Log.i("TAG", "onResponse: $response")

                },
                com.android.volley.Response.ErrorListener {
                    Toast.makeText(itemView.context, "Request error", Toast.LENGTH_LONG).show()
                    Log.i("TAG", "onErrorResponse: Didn't work")
                    Log.i("TAG", "onErrorResponse2: ${it.toString()}")

                }) {

                override fun getHeaders(): Map<String, String> {
                    val params = HashMap<String, String>()
                    params["Authorization"] = serverKey
                    params["Content-Type"] = contentType
                    return params
                }
            }
            requestQueue.add(jsonObjectRequest)
        }
    }
}