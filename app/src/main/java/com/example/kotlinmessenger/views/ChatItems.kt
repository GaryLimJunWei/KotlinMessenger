package com.example.kotlinmessenger.views

import com.example.kotlinmessenger.R
import com.example.kotlinmessenger.models.User
import com.squareup.picasso.Picasso
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.chat_from_row.view.*
import kotlinx.android.synthetic.main.chat_to_row.view.*

class ChatFromItem(val text:String,val user: User) : Item<ViewHolder>()
{

    override fun bind(viewHolder: ViewHolder, position: Int)
    {
        viewHolder.itemView.textViewFromView.text = text
        val targetImageView = viewHolder.itemView.imageView
        Picasso.get().load(user.profileImageUrl).into(targetImageView)
    }

    override fun getLayout(): Int
    {
        return R.layout.chat_from_row
    }

}

class ChatToItem(val text:String,val user: User) : Item<ViewHolder>()
{

    override fun bind(viewHolder: ViewHolder, position: Int)
    {
        viewHolder.itemView.textViewToView.text = text
        val targetImageView = viewHolder.itemView.imageViewToView
        Picasso.get().load(user.profileImageUrl).into(targetImageView)
    }

    override fun getLayout(): Int
    {
        return R.layout.chat_to_row
    }

}