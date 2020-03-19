package com.example.kotlinmessenger.registerlogin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.EditText
import com.example.kotlinmessenger.R
import com.example.kotlinmessenger.Utils.login
import com.example.kotlinmessenger.Utils.toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity()
{

    private lateinit var mAuth : FirebaseAuth
    lateinit var loginEmail : EditText
    lateinit var loginPw : EditText
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        mAuth = FirebaseAuth.getInstance()
        loginEmail = findViewById(R.id.loginEmail)
        loginPw = findViewById(R.id.loginPw)

        back_to_register.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        login_btn.setOnClickListener { checklogin() }
    }

    private fun checklogin()
    {
        val email = loginEmail.text.toString().trim()
        val pw = loginPw.text.toString().trim()
        if(email.isNullOrEmpty())
        {
            loginEmail.error = "Email Required"
            loginEmail.requestFocus()
            return
        }

        if( !Patterns.EMAIL_ADDRESS.matcher(email).matches())
        {
            loginEmail.error = "Invalid Email"
            loginEmail.requestFocus()
            return
        }

        if(pw.isNullOrEmpty())
        {
            loginPw.error = "Required Field"
            loginPw.requestFocus()
            return
        }

        loginUser(email,pw)
    }

    private fun loginUser(email:String,pw:String)
    {
        progress_bar.visibility = View.VISIBLE
        mAuth.signInWithEmailAndPassword(email,pw)
            .addOnCompleteListener(this) { Task ->
                if (Task.isSuccessful)
                {
                    login()
                }
                else
                {
                    Task.exception?.message?.let {
                        toast(it)
                    }
                }

                progress_bar.visibility = View.GONE
            }
    }

    override fun onStart()
    {
        super.onStart()
        mAuth.currentUser?.let {
            login()
        }
    }
}
