package codestitcher.calcpro.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class CalculationHistoryItem(
    val id: String = UUID.randomUUID().toString(),
    val expression: String,
    val result: String,
    val timestamp: Long = System.currentTimeMillis()
)

class HistoryRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("calcpro_history_prefs", Context.MODE_PRIVATE)

    fun loadHistory(): List<CalculationHistoryItem> {
        val jsonStr = prefs.getString("history_items", null) ?: return emptyList()
        val list = mutableListOf<CalculationHistoryItem>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    CalculationHistoryItem(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        expression = obj.optString("expression", ""),
                        result = obj.optString("result", ""),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                    )
                )
            }
        } catch (_: Exception) {
            // Ignore parse errors
        }
        return list
    }

    fun saveHistory(items: List<CalculationHistoryItem>) {
        val array = JSONArray()
        items.take(200).forEach { item ->
            val obj = JSONObject().apply {
                put("id", item.id)
                put("expression", item.expression)
                put("result", item.result)
                put("timestamp", item.timestamp)
            }
            array.put(obj)
        }
        prefs.edit().putString("history_items", array.toString()).apply()
    }

    fun clearHistory() {
        prefs.edit().remove("history_items").apply()
    }
}
