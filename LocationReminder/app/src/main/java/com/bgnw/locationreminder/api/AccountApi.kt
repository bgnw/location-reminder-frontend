import com.bgnw.locationreminder.api.Account_ApiStruct
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface AccountApi {
    @GET("prod-account/{username}")
    fun getAccount(
        @Path("username") username: String?,
        @Query("format") format: String?,
    ): Call<Account_ApiStruct>

    @POST("prod-account/create/")
    fun createAccount(
        @Body body: Account_ApiStruct
    ): Call<Account_ApiStruct>
}