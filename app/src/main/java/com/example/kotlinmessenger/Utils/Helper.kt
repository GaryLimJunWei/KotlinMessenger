package com.example.kotlinmessenger.Utils

import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.kotlinmessenger.registerlogin.RegisterActivity
import com.example.kotlinmessenger.messages.LatestMessagesActivity

fun Context.toast(message:String) =
    Toast.makeText(this,message, Toast.LENGTH_SHORT).show()

fun Context.login()
{
    val intent = Intent(this, LatestMessagesActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        //IF YOU don't do this, when user press back button user will see the register again
    }
    startActivity(intent)
}

fun Context.logout()
{
    val intent = Intent(this, RegisterActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        //IF YOU don't do this, when user press back button user will see the register again
    }
    startActivity(intent)
}