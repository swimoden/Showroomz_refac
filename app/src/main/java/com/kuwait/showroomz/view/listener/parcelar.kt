package com.kuwait.showroomz.view.listener

import android.os.Parcel
import android.os.Parcelable
import com.kuwait.showroomz.extras.readRealmList
import com.kuwait.showroomz.extras.writeRealmList
import io.realm.RealmList
import io.realm.RealmModel
import kotlinx.android.parcel.Parceler

interface RealmListParceler<T>: Parceler<RealmList<T>?> where T: RealmModel, T: Parcelable {
    override fun create(parcel: Parcel): RealmList<T>? = parcel.readRealmList(clazz)

    override fun RealmList<T>?.write(parcel: Parcel, flags: Int) {
        parcel.writeRealmList(this, clazz)
    }

    val clazz : Class<T>
}