package com.kuwait.showroomz.extras.localDb

import com.kuwait.showroomz.BuildConfig
import com.kuwait.showroomz.extras.Shared
import com.kuwait.showroomz.extras.TOKEN
import com.kuwait.showroomz.extras.USER_ID
import io.realm.DynamicRealm
import io.realm.RealmMigration

class MyMigration : RealmMigration {
    override fun migrate(realm: DynamicRealm, oldVersion: Long, newVersion: Long) {
       // if (BuildConfig.IS_PROD){
            var oldVersion = oldVersion
            val schema = realm.schema
            val prefs = Shared()

            // Migrate to version 1: Add a new class.
            // Example:
            // open class Person(
            //     var name: String = "",
            //     var age: Int = 0,
            // ): RealmObject()
           /* if (oldVersion < newVersion) {
                schema.create("Person")
                    .addField("name", String::class.java)
                    .addField("age", Int::class.javaPrimitiveType)
                oldVersion++
            }*/

        if (oldVersion < newVersion){
            prefs.removeKey(TOKEN)
            prefs.removeKey(USER_ID)
            val category = schema.get("Category")
            if (category?.hasField("showAdsOnScroll") != true) {
                category?.addField("showAdsOnScroll", Boolean::class.java)
            }
            //- Property 'Advertisement.forMen' has been added.
            //    - Property 'Advertisement.forWomen' has been added.
            val ads = schema.get("Advertisement")
            if (ads?.hasField("forMen") != true) {
                ads?.addField("forMen", Boolean::class.java)?.setNullable("forMen", true)
            }
            if (ads?.hasField("forWomen") != true) {
                ads?.addField("forWomen", Boolean::class.java)?.setNullable("forWomen", true)
            }
            val settings = schema.get("Setting")
            if (settings?.hasField("frequencyMan") != true) {
                settings?.addField("frequencyMan", Int::class.java)?.setNullable("frequencyMan", true)
            }
            if (settings?.hasField("frequencyWomen") != true) {
                settings?.addField("frequencyWomen", Int::class.java)?.setNullable("frequencyWomen", true)
            }
            if (settings?.hasField("phoneNumber") != true) {
                settings?.addField("phoneNumber", String::class.java)//?.setNullable("phoneNumber", true)
            }
            // - Property 'Image.createdAt' has been added.
            schema.get("Image")?.let { image ->
                if (!image.hasField("createdAt")){
                    image.addField("createdAt", String::class.java)//?.setNullable("createdAt", true)
                }
            }
            schema.get("Model")?.let { model ->
                if (!model.hasField("link")){
                    model.addField("link", String::class.java)//?.setNullable("createdAt", true)
                }
            }
            oldVersion += 1
        }

    }
}