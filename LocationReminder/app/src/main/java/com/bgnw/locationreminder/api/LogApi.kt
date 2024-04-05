import com.bgnw.locationreminder.api.CustomBody
import com.bgnw.locationreminder.data.ItemOpportunity
import com.bgnw.locationreminder.data.Log
import com.bgnw.locationreminder.data.TaskItem
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface LogApi {
    @POST("logging/add/")
    fun sendLog(
        @Body body: Log
    ): Call<Log>
}