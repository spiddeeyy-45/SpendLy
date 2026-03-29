package Model.Register

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RegisterApiInstance {

    private const val BASE_URL = "http://10.0.2.2:3000/"
    val api: RegisterApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RegisterApiService::class.java)
    }
}