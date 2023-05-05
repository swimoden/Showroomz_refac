package com.kuwait.showroomz.extras

import android.app.Application
import android.content.Context
import com.kuwait.showroomz.BuildConfig
import com.kuwait.showroomz.extras.localDb.MyMigration
import com.yariksoffice.lingver.Lingver
import io.realm.Realm
import io.realm.RealmConfiguration


class MyApplication : Application() {
    companion object Key {
        const val APP_VERSION = "APP_VERSION"
        const val APP_VERSION_NAME = "APP_VERSION_NAME"
        const val IS_UP_TO_DATE = "IS_UP_TO_DATE"

        const val LANGUAGE_SELECTED= "IS_FIRST_TIME"
        const val LANG = "LANG"
        lateinit var context:Context
    }

    override fun onCreate() {
        super.onCreate()
        context=this
        Realm.init(this)

        LanguageManager.instance.getLanguage(this)?.let { Lingver.init(this, it) }
        if (BuildConfig.IS_PROD){
            val config = RealmConfiguration.Builder()
                .name(BuildConfig.DB_NAME)
                //.migration(MyMigration())
                .deleteRealmIfMigrationNeeded()
                .schemaVersion(BuildConfig.DB_SHEMA_VERSION)
                .allowWritesOnUiThread(true)
                .allowQueriesOnUiThread(true)
                .build()
            Realm.setDefaultConfiguration(config)
        }else{
            val config = RealmConfiguration.Builder()
                .name(BuildConfig.DB_NAME)
                .migration(MyMigration())
                .deleteRealmIfMigrationNeeded()
                .schemaVersion(BuildConfig.DB_SHEMA_VERSION)
                .allowWritesOnUiThread(true)
                .allowQueriesOnUiThread(true)
                .build()
            Realm.setDefaultConfiguration(config)
        }

    }


}