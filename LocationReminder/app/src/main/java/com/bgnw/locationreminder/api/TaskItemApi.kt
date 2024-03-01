import com.bgnw.locationreminder.api.TaskItem
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface TaskItemApi {
    @GET("prod-taski/{item_id}")
    fun getItem(
        @Path("item_id") listId: Int,
        @Query("format") format: String,
    ): Call<TaskItem>

    @GET("prod-taski/from-list/{list_id}")
    fun getListItems(
        @Path("list_id") listId: Int,
        @Query("format") format: String,
    ): Call<List<TaskItem>>

    @POST("prod-taski/create/")
    fun createItem(
        @Body body: TaskItem
    ): Call<TaskItem>
}