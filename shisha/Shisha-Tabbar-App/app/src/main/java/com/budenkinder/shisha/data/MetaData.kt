package com.budenkinder.shisha.data

import com.google.firebase.database.PropertyName
import java.io.Serializable

data class MetaData(
    @PropertyName("app_version_code") val app_version_code: String?,
    @PropertyName("restaurant_menu_version") val restaurant_menu_version: String?
):Serializable{
    constructor() : this("","")
}