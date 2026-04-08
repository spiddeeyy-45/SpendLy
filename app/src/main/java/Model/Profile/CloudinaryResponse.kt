package Model.Profile

import com.google.gson.annotations.SerializedName

data class CloudinaryResponse(
    @SerializedName("secure_url")
    val secure_url:String
)