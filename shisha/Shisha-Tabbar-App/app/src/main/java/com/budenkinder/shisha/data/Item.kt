package com.budenkinder.shisha.data

import com.google.firebase.database.PropertyName
import java.io.Serializable

data class Item(
    @PropertyName("description") var description: String? = "",
    @PropertyName("header") var header: Boolean = false,
    @PropertyName("image_url") var image_url: String? = "",
    @PropertyName("name") var name: String? = "",
    @PropertyName("price") var price: String? = "",
    @PropertyName("tab_title") var tab_title: String? = "",
    @PropertyName("title1") var title1: String? = "",
    @PropertyName("block") var block: Boolean? = false

) : Serializable {
    constructor() : this("", false, "", "", "", "", "", false)
}