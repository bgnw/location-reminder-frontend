import com.bgnw.locationreminder.api.TaskItem_ApiStruct
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
    ): Call<TaskItem_ApiStruct>

    @GET("prod-taski/from-list/{list_id}")
    fun getListItems(
        @Path("list_id") listId: Int,
        @Query("format") format: String,
    ): Call<List<TaskItem_ApiStruct>>

    @POST("prod-taski/create/")
    fun createItem(
        @Body body: TaskItem_ApiStruct
    ): Call<TaskItem_ApiStruct>
}