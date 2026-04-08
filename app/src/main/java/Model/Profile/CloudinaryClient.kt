package Model.Profile

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object CloudinaryClient {
    private val BASE_URL="https://api.cloudinary.com/"
    val api:CloudinaryRetrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CloudinaryRetrofit::class.java)
    }
}