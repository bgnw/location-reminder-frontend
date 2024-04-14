import com.bgnw.locationreminder.data.Log
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface LogApi {
    @POST("logging/add/")
    fun sendLog(
        @Body body: Log
    ): Call<Log>
}