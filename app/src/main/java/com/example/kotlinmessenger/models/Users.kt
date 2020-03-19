package com.example.kotlinmessenger.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/*
    In order to extends to Parcelable and not implementing all the tedious members,
    in the gradle file add the extension experimental = true
    and use this annotation to suppress the error
 */

@Parcelize
class User(val uid:String,val username:String,val profileImageUrl:String) : Parcelable
{
    constructor() : this("","","")
}