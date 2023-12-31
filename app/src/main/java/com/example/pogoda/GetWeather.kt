import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query


interface WttrService {
    @GET("/")
    suspend fun getWeather(@Query("format") format: String): String
}


class GetWeather {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://wttr.in/")
        .addConverterFactory(ScalarsConverterFactory.create())
        .build()

    private val wttrService = retrofit.create(WttrService::class.java)

    suspend fun fetchWeather(location: String): String {
        val format = "$location?format=%c+%t+%w+%h+%P"  //Wzrór zapytania: https://wttr.in/Wroclaw?format=%c+%t+%w+%h+%P
        return wttrService.getWeather(format)
    }
}
