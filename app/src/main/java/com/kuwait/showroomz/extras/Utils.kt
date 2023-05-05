package com.kuwait.showroomz.extras

import android.content.Context
import android.content.res.Configuration
import android.database.Cursor
import android.database.CursorIndexOutOfBoundsException
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.OpenableColumns
import android.text.TextUtils
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.kuwait.showroomz.model.data.Advertisement
import com.kuwait.showroomz.model.simplifier.NotificationData
import com.kuwait.showroomz.model.simplifier.Section
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern
import java.util.stream.Collectors


class Utils {
    private object HOLDER {
        val INSTANCE = Utils()
    }

    private val randomGenerator = Random()

    companion object {
        val instance: Utils by lazy { HOLDER.INSTANCE }
    }

    fun isNullOrEmpty(word: String): Boolean {
        return word.isNullOrEmpty()
    }

    fun setLanguage(context: Context, language: String?) {
        val prefs =
            PreferenceManager.getDefaultSharedPreferences(context)
        prefs.edit().putString("language", language).apply()
        val myLocale = Locale(language)
        Locale.setDefault(myLocale)
        val res = context.resources
        val dm = res.displayMetrics
        val conf = res.configuration
        conf.locale = myLocale
        conf.setLayoutDirection(myLocale)
        res.updateConfiguration(conf, dm)
    }


    fun setLocale(context: Context, language: String?) {
        val locale = Locale(language)
        val config = Configuration(context.resources.configuration)
        Locale.setDefault(locale)
        config.setLocale(locale)
        context.resources.updateConfiguration(
            config,
            context.resources.displayMetrics
        )
    }

    fun removeUnitFromPriceString(s: String): String {
        return if (isEnglish) s.replace(" KWD", "").replace(",", "")
            .replace(" ", "") else s.replace(" د.ك ", "").replace(",", "").replace(" ", "")
    }

    fun getRandomAds(list: List<Advertisement>): Advertisement {
        if (list.size == 1) {
            return list.get(0)
        }
        val x: Int = randomGenerator.nextInt(list.size)
        return list.get(x)
    }

    fun stringToDate(stringDate: String, formatString: String): Date? {
        if ( stringDate.isBlank() || stringDate.isNullOrEmpty()){
            return Date()
        }
        val dateFormatter = SimpleDateFormat(formatString, Locale.ENGLISH)
        dateFormatter.timeZone = TimeZone.getTimeZone("UTC")
        val dt = dateFormatter.parse(stringDate)
        return dt

    }

    fun dateToString(date: Date, formatString: String): String? {
        val dateFormatter = SimpleDateFormat(formatString, Locale.ENGLISH)
        dateFormatter.timeZone = TimeZone.getTimeZone("UTC")
        val dt = dateFormatter.format(date)
        return dt

    }
    fun dateToStringGMT(date: Date, formatString: String): String? {
        val dateFormatter = SimpleDateFormat(formatString, Locale.ENGLISH)
        dateFormatter.timeZone = TimeZone.getDefault()
        val dt = dateFormatter.format(date)
        return dt

    }
    fun dateToStringLocalized(date: Date, formatString: String): String? {
        val dateFormatter = SimpleDateFormat(formatString, Locale(if (isEnglish) "en" else "ar"))
        dateFormatter.timeZone = TimeZone.getTimeZone("UTC")
        val dt = dateFormatter.format(date)
        return dt

    }




    fun isValidEmail(target: CharSequence?): Boolean {
        val EMAIL_ADDRESS_PATTERN: Pattern = Pattern.compile(
            "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                    "\\@" +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                    "(" +
                    "\\." +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,4}" +
                    ")+"
        )

        return !TextUtils.isEmpty(target) && EMAIL_ADDRESS_PATTERN.matcher(target).matches()
    }

    fun getNameFromContentUri(
        context: Context,
        contentUri: Uri?
    ): String? {
        return try {
            val returnCursor: Cursor? =
                context.contentResolver.query(contentUri!!, null, null, null, null)
            val nameColumnIndex: Int = returnCursor!!.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            returnCursor.moveToFirst()
            returnCursor.getString(nameColumnIndex)
        } catch (e: CursorIndexOutOfBoundsException) {
            val uriPathHelper = URIPathHelper()
            contentUri?.let {
                uriPathHelper.getPath(context, it)
            }
        }

    }

    fun getDates(startDate: String, endDate: String): List<Date>? {
        val dates = ArrayList<Date>()
        val df1: DateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        var date1: Date? = null
        var date2: Date? = null
        try {
            date1 = df1.parse(startDate)
            date2 = df1.parse(endDate)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        val cal1 = Calendar.getInstance()
        cal1.time = date1
        val cal2 = Calendar.getInstance()
        cal2.time = date2
        while (!cal1.after(cal2)) {
            dates.add(cal1.time)
            cal1.add(Calendar.DATE, 1)
        }
        return dates
    }

    fun addDaysToDate(date: Date, nbDays: Int): Date? {
        val c = Calendar.getInstance()
        c.time = date
        c.add(Calendar.DATE, nbDays)
        return c.time
    }

    fun getDayOfWeek(date: Date): Int {
        val d = date.toString()
        val c = Calendar.getInstance()
        //c.firstDayOfWeek = Calendar.SATURDAY
        c.time = date

        val day = c.get(Calendar.DAY_OF_WEEK) - 1
        if (day == 0) return 7
        return day
    }

    fun getDayOfWeekBooking(date: Date): Int {
        val d = date.toString()
        val c = Calendar.getInstance()
        //c.firstDayOfWeek = Calendar.SATURDAY
        c.time = date

        val day = c.get(Calendar.DAY_OF_WEEK)
        if (day == 0) return 7
        return day
    }

    fun convertToArabic(value: String): String {
        return (value)
            .replace("1".toRegex(), "١").replace("2".toRegex(), "٢")
            .replace("3".toRegex(), "٣").replace("4".toRegex(), "٤")
            .replace("5".toRegex(), "٥").replace("6".toRegex(), "٦")
            .replace("7".toRegex(), "٧").replace("8".toRegex(), "٨")
            .replace("9".toRegex(), "٩").replace("0".toRegex(), "٠")
    }

    fun convertToEnglish(value: String): String {
        return (value)
            .replace("١".toRegex(), "1").replace("٢".toRegex(), "2")
            .replace("٣".toRegex(), "3").replace("٤".toRegex(), "4")
            .replace("٥".toRegex(), "5").replace("٦".toRegex(), "6")
            .replace("٧".toRegex(), "7").replace("٨".toRegex(), "8")
            .replace("٩".toRegex(), "9").replace("٠".toRegex(), "0")
            .replace("٫", ".").replace(",", ".")

    }

    fun isProbablyArabic(s: String): Boolean {
        for (charac in s.toCharArray()) {
            if (Character.UnicodeBlock.of(charac) === java.lang.Character.UnicodeBlock.ARABIC) {
                return true
            }
        }
        return false
        /* var i = 0
         while (i < s.length) {
             val c = s.codePointAt(i)
             if (c in 0x0600..0x06E0) return true
             i += Character.charCount(c)
         }
         return false*/
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun <T> toMap(set: Set<T>): Map<T, T?>? {
        val map: Map<T, T> = HashMap()

        val set = map.entries
        return set.stream().collect(
            Collectors.toMap({ obj: Map.Entry<T, T> -> obj.key },
                { obj: Map.Entry<T, T> -> obj.value },
                { a, b -> b })
        )

    }


    fun buildNotificationData(properties: Bundle?): NotificationData? {
        val result = NotificationData()

        for (key in properties?.keySet()!!) {
            try {
                when (key) {

                    "modelData" -> result.modelData = properties[key].toString()
                    "locationInApp" -> result.locationInApp = properties[key].toString()
                    "bank" -> result.bank = properties[key].toString()
                    "body" -> result.body = properties[key].toString()
                    "from" -> result.from = properties[key].toString()
                    "title" -> result.title = properties[key].toString()
                    "dealerData" -> result.dealerData = properties[key].toString()
                    "callback_id" -> result.callback_id = properties[key].toString()
                    "bank_id" -> result.bank_id = properties[key].toString()
                    "link" -> result.link = properties[key].toString()
                    "navigateToLink" -> result.navigateToLink = properties[key].toString()
                    "section" -> {
                        Log.e("UTILS", "properties[key]: =${properties[key].toString()} ")
                        val myJsonObj = properties[key]
                        val parser = JsonParser()
                        val json: JsonObject = parser.parse(myJsonObj.toString()).asJsonObject
                        Log.e("UTILS", "properties[key]: =${json["parent_id"].asString}} ")
                        Log.e("UTILS", "properties[key]: =${json["id"].asString}} ")
                        result.section = Section(json["parent_id"].asString, json["id"].asString)
                    }
                }


            } catch (e: Exception) {
                Log.e("buildNotificationData", e.localizedMessage)
            }
        }
        return result
    }

    @Throws(java.lang.Exception::class)
    fun mapToBundle(data: Map<String?, String?>): Bundle? {
        val bundle = Bundle()
        for ((key, value) in data) {
            if (value is String) bundle.putString(key, value as String?)
        }
        return bundle
    }

    fun getDaysBetween(date1: Date, date2: Date): Int {
        val diff: Long = date2.time - date1.time
        return (diff / (1000 * 60 * 60 * 24)).toInt()
    }

}