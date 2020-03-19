package com.example.kotlinmessenger.registerlogin

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.util.Patterns
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.kotlinmessenger.R
import com.example.kotlinmessenger.models.User
import com.example.kotlinmessenger.Utils.toast
import com.example.kotlinmessenger.messages.LatestMessagesActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.camera_or_gallery.view.*
import java.util.*

class RegisterActivity : AppCompatActivity()
{

    private lateinit var mAuth : FirebaseAuth
    var REQUEST_IMAGE_CAPTURE = 100
    var selectedPhotoUri : Uri? = null
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        mAuth = FirebaseAuth.getInstance()

        login_btn.setOnClickListener {

            validateRegister()
        }

        already_have_an_acc.setOnClickListener {
            Log.d("MainAcitivty","Try to show login activity")

            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)

        }

        // This should change to a dialog, where we can choose to take picture or Gallery
        select_photo_button.setOnClickListener{

            galleryOrCamera()
            //getphoto()
        }


    }

    // This function will let user decide whether to take a picture or take a picture from gallery
    private fun galleryOrCamera()
    {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.camera_or_gallery,null)
        dialog.setContentView(view)
        dialog.show()

        // When user choose Camera
        view.camerabtn.setOnClickListener {

            takePictureIntent()
            dialog.dismiss()
        }

        // When user choose Gallery
        view.gallerybtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent,0)
            dialog.dismiss()
        }



    }

    private fun takePictureIntent()
    {
        // This intent will open the camera
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also {
                pictureIntent ->
            pictureIntent.resolveActivity(this.packageManager!!)?.also {
                startActivityForResult(pictureIntent,REQUEST_IMAGE_CAPTURE)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == 0 && resultCode == Activity.RESULT_OK && data != null)
        {
            //Proceed and check what the selected image was
            Log.d("RegisterActivity","Photo was selected")

            selectedPhotoUri = data.data

            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver,selectedPhotoUri)

            select_image.setImageBitmap(bitmap)

            select_photo_button.alpha = 0f
        }

    }

    private fun validateRegister()
    {

        Log.d("RegisterActivity","Registering now!!!!!")
        val email = loginPw.text.toString().trim()
        val password = password_edittext.text.toString().trim()


        if(email.isEmpty())
        {
            loginPw.error = "Email required"
            loginPw.requestFocus()
            return
        }
        if( !Patterns.EMAIL_ADDRESS.matcher(email).matches())
        {
            loginPw.error = "Valid Email required"
            loginPw.requestFocus()
            return
        }
        if(password.isEmpty() || password.length < 6)
        {
            password_edittext.error="6 Char Password required"
            password_edittext.requestFocus()
            return
        }

        registerUser(email, password)

    }

    private fun registerUser(email:String,password:String)
    {
        progress_bar.visibility = View.VISIBLE
        mAuth.createUserWithEmailAndPassword(email,password)
            //If the task is completed, it will call onCompleteListener
            .addOnCompleteListener(this){ Task ->
                progress_bar.visibility = View.GONE
                if(Task.isSuccessful)
                {
                    //Log.d("","Successfully created user with uid: ${Task.result?.user?.uid}")
                    val intent = Intent(this, LoginActivity::class.java)
                    toast("Register Successfully!")
                    uploadImageToFirebaseStorage()
                }
                else
                {
                    Task.exception?.message?.let {
                        toast(it)
                    }

                }
            }
    }


    private fun uploadImageToFirebaseStorage()
    {
        if (selectedPhotoUri == null) return


        // Unique ID generator to generate random unique ID and convert it to String
        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/${filename}")

        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                Log.d("Register","Successfully uploaded image: ${it.metadata?.path}")

                ref.downloadUrl.addOnSuccessListener {
                    it.toString()
                    Log.d("RegisterActivity","File location : $it")

                    saveUserToFiredatabase(it.toString())
                }
            }
            .addOnFailureListener {
                //do some logging here
            }
    }

    private fun saveUserToFiredatabase(profileImageUrl: String)
    {
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

        val user = User(
            uid,
            loginEmail.text.toString(),
            profileImageUrl
        )

        ref.setValue(user)
            .addOnSuccessListener {

                val intent = Intent(this, LatestMessagesActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                Log.d("RegisterActivity","Finally we saved the user to Firebase Database")
            }
            .addOnFailureListener {
                Log.d("","Failed to set value to database : ${it.message}")
            }

    }


}


