import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// Definicja interfejsu dla Retrofit
interface WttrService {
    @GET("/")
    suspend fun getWeather(@Query("format") format: String): String
}

// Klasa GetWeather
class GetWeather {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://wttr.in/")
        .addConverterFactory(ScalarsConverterFactory.create())
        .build()

    private val wttrService = retrofit.create(WttrService::class.java)

    suspend fun fetchWeather(location: String): String {
        val format = "$location?format=%c+%t+%w"
        return wttrService.getWeather(format)
    }
}
