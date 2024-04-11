package com.devtoochi.blood_donation.backend.models

import org.json.JSONObject

data class BloodGroup(
    val id: String,
    val name: String,
    var isSelected: Boolean = false
) {
    fun toJsonObject(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("name", name)
            put("isSelected", isSelected)
        }
    }
}

fun fromJsonObject(jsonObject: JSONObject): BloodGroup {
    return BloodGroup(
        jsonObject.getString("id"),
        jsonObject.getString("name"),
        jsonObject.getBoolean("isSelected")
    )
}
