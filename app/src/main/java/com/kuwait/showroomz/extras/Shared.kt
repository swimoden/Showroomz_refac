package com.kuwait.showroomz.extras

import android.content.Context
import android.content.SharedPreferences
import android.os.Parcelable
import android.view.Display
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.kuwait.showroomz.model.data.Brand
import com.kuwait.showroomz.model.data.Model
import com.kuwait.showroomz.model.local.LocalRepo
import com.kuwait.showroomz.model.simplifier.BrandSimplifier
import com.kuwait.showroomz.model.simplifier.ModelSimplifier
import io.realm.RealmObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.Reader
import java.lang.reflect.Type


class Shared {


    //val prefs = PreferenceManager.getDefaultSharedPreferences(context)

    companion object {

        private const val PREF_TIME = "Pref time"
        lateinit var prefs: SharedPreferences

        @Volatile
        private var instance: Shared? = null
        private val LOCK = Any()
        private val local = LocalRepo()

        operator fun invoke(context: Context): Shared = instance ?: synchronized(LOCK) {
            instance ?: buildHelper(context).also {
                instance = it
            }
        }

        private fun buildHelper(context: Context): Shared {
            prefs = PreferenceManager.getDefaultSharedPreferences(context)
            return Shared()
        }
        var selectedIndex = 0
        var oldIndex = 0
        var modelsList:ArrayList<Model> = arrayListOf()
        var filtredList:ArrayList<Model> = arrayListOf()
        var brands : ArrayList<Brand> =  arrayListOf()

        var modelsSimplifierList:ArrayList<ModelSimplifier> = arrayListOf()
        var brandsSimplifierList:ArrayList<BrandSimplifier> = arrayListOf()
    }


    fun bool(key: String): Boolean {
        return prefs.getBoolean(key, false)
    }

    fun int(key: String): Int {
        return prefs.getInt(key, 0)
    }

    fun string(key: String): String? {
        return prefs.getString(key, "")
    }

  inline fun <reified T : Parcelable> setParcelable(key: String, obj: T) {
        prefs.edit(commit = true) { putString(key, Gson().toJson(obj)).apply() }
    }
    inline fun <reified T : Parcelable> getParcelable(key: String) :Parcelable {
        val parcelableObject= prefs.getString(key, "")
      return Gson().fromJson(parcelableObject, T::class.java)
    }
    fun setString(key: String, value: String) {
        prefs.edit(commit = true) { putString(key, value) }

    }

    fun setInt(key: String, value: Int) {
        prefs.edit(commit = true) { putInt(key, value) }

    }

    fun setBool(key: String, value: Boolean) {
        prefs.edit(commit = true) { putBoolean(key, value) }

    }


    fun removeKey(key: String) {
        val editor = prefs.edit()
        val x = prefs.all
        for (entry in x.entries) {
            if (entry.key == key) {
                editor.remove(entry.key)
                break
            }
        }
        editor.apply()
    }

    fun existKey(key: String?): Boolean {
        return prefs.contains(key)
    }

    fun <T : RealmObject> setList(key: String?, list: List<T>?) {
        try {
            val gson = Gson()

            val json = gson.toJson(list)
            key?.let { setString(it, json) }
        } catch (e: StackOverflowError) {

        }

    }

    inline fun <reified T : RealmObject> getList(key: String?): ArrayList<T> {
        val arrayItems: ArrayList<T> = arrayListOf()
        val serializedObject = prefs.getString(key, "")
        if (serializedObject != null) {

            val gson = Gson()
            val jsonArray = JsonParser().parse(serializedObject).asJsonArray
            val type: Type = object : TypeToken<T>() {}.type
            jsonArray
                .forEach {
                    arrayItems.add(gson.fromJson(it, T::class.java))
                }
        }
        return arrayItems
    }
    fun removeAll(){
        prefs.edit().clear().apply()
    }

}

