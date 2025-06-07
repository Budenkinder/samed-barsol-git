package com.budenkinder.shisha.data

import com.google.firebase.database.PropertyName
import java.io.Serializable

data class MenuMain(
    @PropertyName("menue") val menue: List<Item>?,
    @PropertyName("title") val title: String?,
    @PropertyName("category") val category: String?
):Serializable
{
    constructor() : this(null, "","")
}