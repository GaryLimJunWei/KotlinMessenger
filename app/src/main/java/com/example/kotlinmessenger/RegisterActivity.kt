package com.example.kotlinmessenger

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.util.Patterns
import android.view.View
import androidx.appcompat.app.AppCompatActivity
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

        register_btn.setOnClickListener {

            validateRegister()
        }

        already_have_an_acc.setOnClickListener {
            Log.d("MainAcitivty","Try to show login activity")

            val intent = Intent(this,LoginActivity::class.java)
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
            pictureIntent.resolveActivity(this?.packageManager!!)?.also {
                startActivityForResult(pictureIntent,REQUEST_IMAGE_CAPTURE)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == 0 && resultCode == Activity.RESULT_OK && data != null)
        {
            Log.d("RegisterActivity","Photo was selected")

            selectedPhotoUri = data.data

            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver,selectedPhotoUri)

            select_image.setImageBitmap(bitmap)

            select_photo_button.alpha = 0f
        }

    }

//    private fun uploadImageAndSaveUri(bitmap: Bitmap)
//    {
//        val baos = ByteArrayOutputStream()
//        val storageRef = FirebaseStorage.getInstance().reference
//            .child("pics/${FirebaseAuth.getInstance().currentUser?.uid}")
//        bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos)
//        val image = baos.toByteArray()
//
//        val upload = storageRef.putBytes(image)
//
//        progress_bar.visibility = View.VISIBLE
//        upload.addOnCompleteListener { uploadTask ->
//            progress_bar.visibility = View.INVISIBLE
//            if (uploadTask.isSuccessful)
//            {
//                storageRef.downloadUrl.addOnCompleteListener { urlTask ->
//                    //Using the let operator and only when the value is not NULL then
//                    // the statement will be executed
//                    urlTask.result?.let {
//                        imageUri = it
//                        this?.toast(imageUri.toString())
//
//                        select_photo_button.setImageBitmap(bitmap)
//                    }
//                }
//            }
//            else
//            {
//                uploadTask.exception?.let {
//                    this?.toast(it.message!!)
//                }
//            }
//        }
//    }

    private fun validateRegister()
    {

        val email = email_edittext.text.toString().trim()
        val password = password_edittext.text.toString().trim()


        if(email.isEmpty())
        {
            email_edittext.error = "Email required"
            email_edittext.requestFocus()
            return
        }
        if( !Patterns.EMAIL_ADDRESS.matcher(email).matches())
        {
            email_edittext.error = "Valid Email required"
            email_edittext.requestFocus()
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

                    login()
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


    // This is from kotlin messenger youtube
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
//    {
//        super.onActivityResult(requestCode, resultCode, data)
//
//        if(requestCode==0 && resultCode == Activity.RESULT_OK && data != null)
//        {
//            //proceed and check what the selected image was  ....
//
//            Log.d("RegisterActivity","Photo was selected")
//
//            // The uri is the location where the image is stored
//            selectedPhotoUri = data.data
//
//            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver,selectedPhotoUri)
//
//            val bitmapDrawable = BitmapDrawable(bitmap)
//            select_photo_button.setBackgroundDrawable(bitmapDrawable)
//        }
//    }





    private fun uploadImageToFirebaseStorage()
    {
        if (selectedPhotoUri == null) return


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

        val user = User(uid,username_edittext.text.toString(),profileImageUrl)

        ref.setValue(user)
            .addOnSuccessListener {
                Log.d("RegisterActivity","Finally we saved the user to Firebase Database")
            }

    }


}

class User(val uid:String,val username:String,val profileImageUrl:String)
