import com.bgnw.locationreminder.api.TaskList
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface TaskListApi {
    @GET("prod-taskl/{list_id}")
    fun getList(
        @Path("list_id") listId: Int,
        @Query("format") format: String,
    ): Call<TaskList>

    @POST("prod-taskl/create/")
    fun createList(
        @Body body: TaskList
    ): Call<TaskList>

    @GET("prod-taskl/from-user/{username}")
    fun getOwnedLists(
        @Path("username") username: String,
        @Query("format") format: String,
    ): Call<List<TaskList>>
}