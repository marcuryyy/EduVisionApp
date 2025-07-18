import android.content.Context
import androidx.core.content.edit

object AppData {
    private const val PREFS_NAME = "app_data"
    private lateinit var prefs: android.content.SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    var folderId: Int
        get() = prefs.getInt("folder_id", -1)
        set(value) = prefs.edit { putInt("folder_id", value) }
}