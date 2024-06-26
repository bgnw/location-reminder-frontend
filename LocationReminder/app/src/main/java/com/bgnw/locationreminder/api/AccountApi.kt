import com.bgnw.locationreminder.api.AuthResponse
import com.bgnw.locationreminder.data.Account
import com.bgnw.locationreminder.data.AccountPartialForLocation
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface AccountApi {
    @GET("prod-account/{username}")
    fun getAccount(
        @Path("username") username: String?,
        @Query("format") format: String?,
    ): Call<Account>

    @POST("prod-account/create/")
    fun createAccount(
        @Body body: Account
    ): Call<Account>

    @GET("prod-account/auth/{username}/{password}")
    fun authenticate(
        @Path("username") username: String?,
        @Path("password") password: String?,
        @Query("format") format: String?,
    ): Call<AuthResponse>

    @PATCH("prod-account/update/{username}")
    fun updateAccountLocation(
        @Path("username") username: String?,
        @Body body: AccountPartialForLocation,
        @Query("format") format: String = "json"
    ): Call<AccountPartialForLocation>
}