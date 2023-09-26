package com.kuwait.showroomz.model.local

import android.util.Log
import com.kuwait.showroomz.model.data.AppraisalInfo
import io.realm.*
import io.realm.kotlin.where


class LocalRepo(val realm: Realm = Realm.getDefaultInstance()) {

    inline fun <reified T : RealmModel> getAll(index: Int = 0): MutableList<T>? =
        realm.copyFromRealm(realm.where<T>().equalTo("index", index).findAll())
    inline fun <reified T : RealmModel> getAllParents(index: String = "0"): MutableList<T>? =
        realm.copyFromRealm(realm.where<T>().equalTo("parent", index.toString()).findAll())

    inline fun <reified T : RealmModel> getAll(): MutableList<T>? =
        realm.copyFromRealm(realm.where<T>().findAll())

    inline fun <reified T : RealmModel> getAllByString(
        key: String,
        value: String = ""
    ): MutableList<T>? =
        realm.copyFromRealm(realm.where<T>().equalTo(key, value).findAll())

    inline fun <reified T : RealmModel> getAllByInt(
        key: String,
        value: Int = 0
    ): MutableList<T>? =
        realm.copyFromRealm(realm.where<T>().equalTo(key, value).findAll())

    inline fun <reified T : RealmModel> getOne(id: String): T? =
        realm.where<T>().equalTo("id", id).findFirst()

    inline fun <reified T : RealmModel> getOneWithSlug(id: String): T? =
        realm.where<T>().equalTo("slug", id).findFirst()

    inline fun <reified T : RealmModel> getParent(id: String): T? =
        realm.where<T>().equalTo("id", id).findFirst()

    inline fun <reified T : RealmModel> getOneWithPredicate(id: String, field: String): T? =
        realm.where<T>().equalTo(field, id).findFirst()

    inline fun <reified T : RealmModel> save(
        list: List<T>,
        crossinline action: (done: Boolean) -> (Unit)
    ) {
        // var path = realm.path
        realm.executeTransactionAsync(
            {
                it.copyToRealmOrUpdate(list)
            },
            {
                action.invoke(true)
            },
            {
                action.invoke(false)
            })
    }

    inline fun saveObject(realmObject: RealmObject, crossinline action: (done: Boolean) -> (Unit)) {

        realm.executeTransactionAsync(
            {
                it.insertOrUpdate(realmObject)
            },
            {
                action.invoke(true)
            },
            {
                action.invoke(false)
            })
    }

    fun <T> delete(clazz: Class<T>, index: Int, array:ArrayList<Int>,  action: (newIndex: Int) -> (Unit)) where T : RealmObject? {
        realm.executeTransactionAsync({
            val result =
                it.where(clazz).equalTo("id", "${array[index]}").findAllAsync()
            result.deleteAllFromRealm()
           it.close()
        },
            {
                if (index < array.size -1){
                    action.invoke(index + 1)
                }
            },
            {
                if (index < array.size -1){
                    action.invoke(index + 1)
                }
            })


    }
    fun <T>delete(clazz: Class<T>, index: Int, array:ArrayList<Int>) where T : RealmObject? {
        if (array.size > 0){
        delete(clazz, index, array) {
            delete(clazz, it, array)
        }
    }
    }

    inline fun <reified T : RealmObject> deleteList(
        list: RealmList<T>,
        crossinline action: (done: Boolean) -> (Unit)
    ) {
        //var result = realm.where(T::class.java).findAll()
        realm.executeTransactionAsync({
            list.deleteAllFromRealm()
        },
            {
                action.invoke(true)
            },
            {
                it.message?.let { it1 -> Log.e("deleteList", it1) }
                action.invoke(false)
            })
    }

    fun deleteAppraisalObjects() {
        realm.executeTransactionAsync({
            it.delete(AppraisalInfo::class.java)
        },
            {
                print("succes")
            },
            {
                it.message?.let { it1 -> Log.e("deleted", it1) }
            })


    }

    inline fun <reified T : RealmObject> deleteObj(
        list: RealmObject,
        crossinline action: (done: Boolean) -> (Unit)
    ) {
        //  var result = realm.where(T::class.java).findFirst()
        realm.executeTransactionAsync({
            list.deleteFromRealm()
        },
            {
                action.invoke(true)
            },
            {
                it.message?.let { it1 -> Log.e("deleted", it1) }
                action.invoke(false)
            })
    }

    fun removeDB() {
        realm.executeTransaction {
            it.deleteAll()
        }
    }
}

