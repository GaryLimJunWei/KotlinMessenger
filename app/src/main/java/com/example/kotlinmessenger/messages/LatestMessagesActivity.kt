package com.example.kotlinmessenger.messages

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.kotlinmessenger.R
import com.example.kotlinmessenger.Utils.logout
import com.example.kotlinmessenger.models.ChatMessage
import com.example.kotlinmessenger.models.User
import com.example.kotlinmessenger.registerlogin.RegisterActivity
import com.example.kotlinmessenger.views.LatestMessageRow
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_latest_messages.*
import kotlinx.android.synthetic.main.latest_message_row.view.*

class LatestMessagesActivity : AppCompatActivity()
{

    companion object{
        var currentUser : User? = null
        val TAG = "LatestMessages"

    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_latest_messages)

//        setupDummyRows()
        recyclerview_latest_messages.adapter = adapter

        // This is to add the horizontal line in the recyclerView
        recyclerview_latest_messages.addItemDecoration((DividerItemDecoration(this,
            DividerItemDecoration.VERTICAL)))

        // Set item click listener on your adapter
        adapter.setOnItemClickListener { item, view ->
            Log.d(TAG,"123")
            val intent = Intent(this,ChatLogActivity::class.java)

            val row = item as LatestMessageRow
            row.chatPartnerUser
            intent.putExtra(NewMessageActivity.USER_KEY,row.chatPartnerUser)
            startActivity(intent)
        }

        listenForLatestMessages()

        fetchCurrentUser()

        verifyUserIsLoggedIn()

    }


    val latestMessagesMap = HashMap<String,ChatMessage>()

    fun refreshRecyclerViewMessages() {

        adapter.clear()
        latestMessagesMap.values.forEach{
            adapter.add(LatestMessageRow(it))
        }
    }

    private fun listenForLatestMessages() {
        val fromId = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId")
        ref.addChildEventListener(object: ChildEventListener {

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java) ?: return
                latestMessagesMap[p0.key!!] = chatMessage
                refreshRecyclerViewMessages()

            }

            override fun onCancelled(p0: DatabaseError) {}
            override fun onChildRemoved(p0: DataSnapshot) {}
            override fun onChildMoved(p0: DataSnapshot, p1: String?) {}
            override fun onChildChanged(p0: DataSnapshot, p1: String?) {

                val chatMessage = p0.getValue(ChatMessage::class.java) ?: return
                latestMessagesMap[p0.key!!] = chatMessage
                refreshRecyclerViewMessages()
            }
        })
    }
    val adapter = GroupAdapter<ViewHolder>()

//    private fun setupDummyRows(){
//
//
//        adapter.add(LatestNessageRow())
//        adapter.add(LatestNessageRow())
//        adapter.add(LatestNessageRow())
//
//
//    }

    private fun fetchCurrentUser()
    {
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
               currentUser = p0.getValue(User::class.java)
                Log.d("LatestMessage","Current user ${currentUser?.username}")
            }

        })
    }


    private fun verifyUserIsLoggedIn()
    {
        /*
            Checking if the user is already logged in,
            if not, it will direct user to the registration page
         */
        val uid = FirebaseAuth.getInstance().uid
        if(uid == null)
        {
            val intent = Intent(this, RegisterActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    /*
        This is to create option menu on the navigation bar
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean
    {
        menuInflater.inflate(R.menu.nav_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean
    {

        when(item?.itemId)
        {
            R.id.menu_new_message ->
            {
                val intent = Intent(this, NewMessageActivity::class.java)
                startActivity(intent)
            }

            R.id.menu_sign_out ->
            {
                AlertDialog.Builder(this).apply {
                    setTitle("Are you sure you want to logout?")
                    //What are the 2 parameters?
                    setPositiveButton("Yes"){_,_ ->
                        FirebaseAuth.getInstance().signOut()
                        logout()
                    }
                    setNegativeButton("Cancel"){_,_ ->

                    }
                } .create().show()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
