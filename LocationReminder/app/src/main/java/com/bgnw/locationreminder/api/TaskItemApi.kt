import com.bgnw.locationreminder.data.ItemOpportunity
import com.bgnw.locationreminder.data.TaskItem
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

    @GET("prod-taski/opps/{item_id}")
    fun getItemOpps(
        @Path("item_id") itemId: Int,
        @Query("format") format: String,
    ): Call<MutableList<ItemOpportunity>>

    @POST("prod-taski/opps/create/")
    fun createItemOpp(
        @Body body: ItemOpportunity
    ): Call<MutableList<ItemOpportunity>>
}