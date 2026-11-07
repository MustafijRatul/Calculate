package codestitcher.calcpro.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class CurrencyItem(
    val code: String,
    val name: String,
    val country: String,
    val flag: String,
    val symbol: String,
    val defaultRate: Double
)

object CurrencyRepository {

    private const val PREFS_NAME = "calcpro_currency_prefs"
    private const val KEY_CACHED_RATES = "cached_rates_json"
    private const val KEY_LAST_SYNC = "last_sync_timestamp"
    private const val ONE_DAY_MS = 24 * 60 * 60 * 1000L // 24 hours

    // Comprehensive list of countries sorted alphabetically
    val ALL_CURRENCIES = listOf(
        CurrencyItem("AFN", "Afghan Afghani", "Afghanistan", "🇦🇫", "؋", 68.50),
        CurrencyItem("ALL", "Albanian Lek", "Albania", "🇦🇱", "L", 92.40),
        CurrencyItem("DZD", "Algerian Dinar", "Algeria", "🇩🇿", "DA", 133.50),
        CurrencyItem("ARS", "Argentine Peso", "Argentina", "🇦🇷", "$", 995.00),
        CurrencyItem("AMD", "Armenian Dram", "Armenia", "🇦🇲", "֏", 387.00),
        CurrencyItem("AUD", "Australian Dollar", "Australia", "🇦🇺", "A$", 1.52),
        CurrencyItem("AZN", "Azerbaijani Manat", "Azerbaijan", "🇦🇿", "₼", 1.70),

        CurrencyItem("BHD", "Bahraini Dinar", "Bahrain", "🇧🇭", "BD", 0.38),
        CurrencyItem("BDT", "Bangladeshi Taka", "Bangladesh", "🇧🇩", "৳", 121.50),
        CurrencyItem("BYN", "Belarusian Ruble", "Belarus", "🇧🇾", "Br", 3.28),
        CurrencyItem("BOB", "Bolivian Boliviano", "Bolivia", "🇧🇴", "Bs", 6.93),
        CurrencyItem("BAM", "Bosnia Convertible Mark", "Bosnia", "🇧🇦", "KM", 1.82),
        CurrencyItem("BRL", "Brazilian Real", "Brazil", "🇧🇷", "R$", 5.75),
        CurrencyItem("BND", "Brunei Dollar", "Brunei", "🇧🇳", "B$", 1.32),
        CurrencyItem("BGN", "Bulgarian Lev", "Bulgaria", "🇧🇬", "лв", 1.82),

        CurrencyItem("KHR", "Cambodian Riel", "Cambodia", "🇰🇭", "៛", 4050.0),
        CurrencyItem("CAD", "Canadian Dollar", "Canada", "🇨🇦", "C$", 1.39),
        CurrencyItem("CLP", "Chilean Peso", "Chile", "🇨🇱", "CLP$", 960.0),
        CurrencyItem("CNY", "Chinese Yuan", "China", "🇨🇳", "¥", 7.18),
        CurrencyItem("COP", "Colombian Peso", "Colombia", "🇨🇴", "COL$", 4410.0),
        CurrencyItem("CRC", "Costa Rican Colon", "Costa Rica", "🇨🇷", "₡", 512.0),
        CurrencyItem("CZK", "Czech Koruna", "Czech Republic", "🇨🇿", "Kč", 23.40),

        CurrencyItem("DKK", "Danish Krone", "Denmark", "🇩🇰", "kr", 6.95),
        CurrencyItem("DOP", "Dominican Peso", "Dominican Republic", "🇩🇴", "RD$", 60.20),

        CurrencyItem("EGP", "Egyptian Pound", "Egypt", "🇪🇬", "E£", 49.30),
        CurrencyItem("ETB", "Ethiopian Birr", "Ethiopia", "🇪🇹", "Br", 122.0),
        CurrencyItem("EUR", "Euro", "European Union", "🇪🇺", "€", 0.93),

        CurrencyItem("FJD", "Fijian Dollar", "Fiji", "🇫🇯", "FJ$", 2.25),

        CurrencyItem("GEL", "Georgian Lari", "Georgia", "🇬🇪", "₾", 2.72),
        CurrencyItem("GHS", "Ghanaian Cedi", "Ghana", "🇬🇭", "GH₵", 16.30),

        CurrencyItem("HKD", "Hong Kong Dollar", "Hong Kong", "🇭🇰", "HK$", 7.77),
        CurrencyItem("HUF", "Hungarian Forint", "Hungary", "🇭🇺", "Ft", 380.0),

        CurrencyItem("ISK", "Icelandic Krona", "Iceland", "🇮🇸", "kr", 138.0),
        CurrencyItem("INR", "Indian Rupee", "India", "🇮🇳", "₹", 84.10),
        CurrencyItem("IDR", "Indonesian Rupiah", "Indonesia", "🇮🇩", "Rp", 15650.0),
        CurrencyItem("IQD", "Iraqi Dinar", "Iraq", "🇮🇶", "IQD", 1310.0),
        CurrencyItem("ILS", "Israeli Shekel", "Israel", "🇮🇱", "₪", 3.73),

        CurrencyItem("JMD", "Jamaican Dollar", "Jamaica", "🇯🇲", "J$", 158.0),
        CurrencyItem("JPY", "Japanese Yen", "Japan", "🇯🇵", "¥", 153.20),
        CurrencyItem("JOD", "Jordanian Dinar", "Jordan", "🇯🇴", "JD", 0.71),

        CurrencyItem("KZT", "Kazakhstani Tenge", "Kazakhstan", "🇰🇿", "₸", 492.0),
        CurrencyItem("KES", "Kenyan Shilling", "Kenya", "🇰🇪", "KSh", 129.0),
        CurrencyItem("KWD", "Kuwaiti Dinar", "Kuwait", "🇰🇼", "KD", 0.31),

        CurrencyItem("LBP", "Lebanese Pound", "Lebanon", "🇱🇧", "L£", 89500.0),

        CurrencyItem("MYR", "Malaysian Ringgit", "Malaysia", "🇲🇾", "RM", 4.38),
        CurrencyItem("MVR", "Maldivian Rufiyaa", "Maldives", "🇲🇻", "Rf", 15.45),
        CurrencyItem("MXN", "Mexican Peso", "Mexico", "🇲🇽", "Mex$", 20.10),
        CurrencyItem("MAD", "Moroccan Dirham", "Morocco", "🇲🇦", "MAD", 9.90),

        CurrencyItem("NPR", "Nepalese Rupee", "Nepal", "🇳🇵", "Rs", 134.5),
        CurrencyItem("NZD", "New Zealand Dollar", "New Zealand", "🇳🇿", "NZ$", 1.68),
        CurrencyItem("NGN", "Nigerian Naira", "Nigeria", "🇳🇬", "₦", 1680.0),
        CurrencyItem("NOK", "Norwegian Krone", "Norway", "🇳🇴", "kr", 11.05),

        CurrencyItem("OMR", "Omani Rial", "Oman", "🇴🇲", "OMR", 0.38),

        CurrencyItem("PKR", "Pakistani Rupee", "Pakistan", "🇵🇰", "₨", 278.50),
        CurrencyItem("PEN", "Peruvian Sol", "Peru", "🇵🇪", "S/.", 3.77),
        CurrencyItem("PHP", "Philippine Peso", "Philippines", "🇵🇭", "₱", 58.60),
        CurrencyItem("PLN", "Polish Zloty", "Poland", "🇵🇱", "zł", 4.05),

        CurrencyItem("QAR", "Qatari Riyal", "Qatar", "🇶🇦", "QR", 3.64),

        CurrencyItem("RON", "Romanian Leu", "Romania", "🇷🇴", "lei", 4.65),
        CurrencyItem("RUB", "Russian Ruble", "Russia", "🇷🇺", "₽", 97.50),

        CurrencyItem("SAR", "Saudi Riyal", "Saudi Arabia", "🇸🇦", "﷼", 3.75),
        CurrencyItem("SGD", "Singapore Dollar", "Singapore", "🇸🇬", "S$", 1.32),
        CurrencyItem("ZAR", "South African Rand", "South Africa", "🇿🇦", "R", 17.60),
        CurrencyItem("KRW", "South Korean Won", "South Korea", "🇰🇷", "₩", 1385.0),
        CurrencyItem("LKR", "Sri Lankan Rupee", "Sri Lanka", "🇱🇰", "Rs", 293.0),
        CurrencyItem("SEK", "Swedish Krona", "Sweden", "🇸🇪", "kr", 10.80),
        CurrencyItem("CHF", "Swiss Franc", "Switzerland", "🇨🇭", "CHF", 0.88),

        CurrencyItem("TWD", "New Taiwan Dollar", "Taiwan", "🇹🇼", "NT$", 32.10),
        CurrencyItem("THB", "Thai Baht", "Thailand", "🇹🇭", "฿", 34.10),
        CurrencyItem("TND", "Tunisian Dinar", "Tunisia", "🇹🇳", "DT", 3.10),
        CurrencyItem("TRY", "Turkish Lira", "Turkey", "🇹🇷", "₺", 34.30),

        CurrencyItem("UGX", "Ugandan Shilling", "Uganda", "🇺🇬", "USh", 3670.0),
        CurrencyItem("UAH", "Ukrainian Hryvnia", "Ukraine", "🇺🇦", "₴", 41.30),
        CurrencyItem("AED", "UAE Dirham", "United Arab Emirates", "🇦🇪", "د.إ", 3.67),
        CurrencyItem("GBP", "British Pound", "United Kingdom", "🇬🇧", "£", 0.79),
        CurrencyItem("USD", "US Dollar", "United States", "🇺🇸", "$", 1.00),
        CurrencyItem("UYU", "Uruguayan Peso", "Uruguay", "🇺🇾", "\$U", 41.50),
        CurrencyItem("UZS", "Uzbekistani Som", "Uzbekistan", "🇺🇿", "UZS", 12800.0),

        CurrencyItem("VND", "Vietnamese Dong", "Vietnam", "🇻🇳", "₫", 25300.0)
    ).sortedBy { it.country }

    /**
     * Get rates map (relative to USD = 1.0).
     */
    fun getSavedRates(context: Context): Map<String, Double> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonStr = prefs.getString(KEY_CACHED_RATES, null)
        if (!jsonStr.isNullOrEmpty()) {
            try {
                val json = JSONObject(jsonStr)
                val map = mutableMapOf<String, Double>()
                val keys = json.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    map[key] = json.getDouble(key)
                }
                return map
            } catch (_: Exception) {}
        }
        return ALL_CURRENCIES.associate { it.code to it.defaultRate }
    }

    /**
     * Get formatted last sync time string.
     */
    fun getLastSyncFormatted(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastSync = prefs.getLong(KEY_LAST_SYNC, 0L)
        if (lastSync == 0L) return "Default Rates (Offline)"

        val diff = System.currentTimeMillis() - lastSync
        val sdf = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
        val dateStr = sdf.format(Date(lastSync))

        return if (diff < ONE_DAY_MS) {
            "Updated Today ($dateStr)"
        } else {
            "Updated on $dateStr"
        }
    }

    /**
     * Fetch latest rates from open public exchange rate API if more than 24h passed,
     * or if force == true.
     */
    suspend fun syncRatesIfNeeded(context: Context, force: Boolean = false): Map<String, Double> = withContext(Dispatchers.IO) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastSync = prefs.getLong(KEY_LAST_SYNC, 0L)
        val now = System.currentTimeMillis()

        if (!force && (now - lastSync) < ONE_DAY_MS && lastSync != 0L) {
            return@withContext getSavedRates(context)
        }

        try {
            val url = URL("https://open.er-api.com/v6/latest/USD")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 8000
                readTimeout = 8000
                requestMethod = "GET"
                setRequestProperty("Accept", "application/json")
            }

            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                val response = reader.use { it.readText() }
                val rootJson = JSONObject(response)

                if (rootJson.optString("result") == "success" && rootJson.has("rates")) {
                    val ratesObj = rootJson.getJSONObject("rates")
                    val updatedMap = mutableMapOf<String, Double>()

                    val keys = ratesObj.keys()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        updatedMap[key] = ratesObj.getDouble(key)
                    }

                    prefs.edit()
                        .putString(KEY_CACHED_RATES, ratesObj.toString())
                        .putLong(KEY_LAST_SYNC, now)
                        .apply()

                    return@withContext updatedMap
                }
            }
        } catch (_: Exception) {}

        return@withContext getSavedRates(context)
    }
}
