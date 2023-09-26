package com.kuwait.showroomz.model.local

import android.os.Handler
import android.os.Looper
import io.realm.Realm
import io.realm.RealmAsyncTask
import io.realm.RealmObject
import io.realm.RealmResults

class AsyncDelete {

    private val realm: Realm by lazy { Realm.getDefaultInstance() }
    private val handler = Handler(Looper.getMainLooper()) // Handler associated with the UI thread
    private var transaction: RealmAsyncTask? = null

    fun <T>delete(MyModel: Class<T>, array:ArrayList<Int>) where T : RealmObject? {
        array.forEach{ id ->
            transaction = realm.executeTransactionAsync({ bgRealm ->
                val objectToDelete = bgRealm.where(MyModel).equalTo("id", "$id").findFirst()
                objectToDelete?.deleteFromRealm()
            }, {
                print("success")
            }, { error ->
                val x  = error.message
                print(x)
            })
        }
    }

    fun cleanup() {
        transaction?.let {
            if (!it.isCancelled) {
                it.cancel()
            }
        }
        realm.close()
    }
}
