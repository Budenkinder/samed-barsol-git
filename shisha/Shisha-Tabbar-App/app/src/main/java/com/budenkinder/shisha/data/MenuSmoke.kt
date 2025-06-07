package com.budenkinder.shisha.data

import com.google.firebase.database.PropertyName

data class MenuSmoke(
    @PropertyName("menue") val menue: List<Item>?,
    @PropertyName("title") val title: String?
)
{
    constructor() : this(null, "")
}