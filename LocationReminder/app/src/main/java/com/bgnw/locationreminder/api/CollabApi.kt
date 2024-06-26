import com.bgnw.locationreminder.data.Collab
import com.bgnw.locationreminder.data.CollabReq
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface CollabApi {
    @POST("prod-collab/rq/add/")
    fun addRequest(
        @Body body: CollabReq
    ): Call<CollabReq>

    @DELETE("prod-collab/rq/delete/{pk}")
    fun deleteRequest(
        @Path("pk") pk: Int
    ): Call<CollabReq>

    @GET("prod-collab/rq/get-sent/{sender}")
    fun getSentRequests(
        @Path("sender") usernameSender: String
    ): Call<List<CollabReq>?>

    @GET("prod-collab/rq/get-received/{recipient}")
    fun getReceivedRequests(
        @Path("recipient") usernameRecipient: String
    ): Call<List<CollabReq>?>

    @POST("prod-collab/add/")
    fun addCollab(
        @Body body: Collab
    ): Call<Collab>

    @DELETE("prod-collab/delete/{pk}")
    fun deleteCollab(
        @Path("pk") pk: Int
    ): Call<Collab>

    @GET("prod-collab/get-user/{user}")
    fun getCollabsForUser(
        @Path("user") username: String
    ): Call<List<Collab>?>
}