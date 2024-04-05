import com.bgnw.locationreminder.data.ItemOpportunity
import com.bgnw.locationreminder.data.TaskItem
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface TaskItemApi {
    @GET("prod-taski/{item_id}")
    fun getItem(
        @Path("item_id") itemId: Int,
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

    @GET("prod-taski/filters-foruser/{username}")
    fun getFiltersForUser(
        @Path("username") username: String,
        @Query("format") format: String,
    ): Call<List<Map<String, String>>?>

    @GET("prod-taski/filters-foritem/{item_id}")
    fun getFiltersForItem(
        @Path("item_id") item_id: Int,
        @Query("format") format: String,
    ): Call<List<Map<String, String>>?>

    @PATCH("prod-taski/update/{item_id}")
    fun updateItem(
        @Path("item_id") item_id: Int,
        @Query("format") format: String = "json",
        @Body body: TaskItem
    ): Call<Void>

    @DELETE("prod-taski/delete/{item_id}")
    fun deleteItem(
        @Path("item_id") itemId: Int,
        @Query("format") format: String,
    ): Call<Void>
}