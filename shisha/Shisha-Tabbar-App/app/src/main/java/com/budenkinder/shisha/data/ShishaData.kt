package com.budenkinder.shisha.data

import com.google.firebase.database.PropertyName
import java.io.Serializable

data class Dao(
    @PropertyName("shisha_bar") var shisha_bar: ShishaData?
) : Serializable {
    constructor() : this(null)
}

data class ShishaData(
    @PropertyName("version") var version: Long = -1,
    @PropertyName("tabs") var tabs: ArrayList<ArrayList<Item>>? = null
) : Serializable {
    constructor() : this(0, null)
}