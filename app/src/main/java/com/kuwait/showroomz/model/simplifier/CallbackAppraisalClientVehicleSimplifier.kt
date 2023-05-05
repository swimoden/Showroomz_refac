package com.kuwait.showroomz.model.simplifier

import android.os.Parcelable
import com.kuwait.showroomz.extras.BASE_URL
import com.kuwait.showroomz.extras.MyApplication
import com.kuwait.showroomz.extras.Shared
import com.kuwait.showroomz.model.data.*
import com.kuwait.showroomz.model.local.LocalRepo
import kotlinx.android.parcel.Parcelize

@Parcelize
class CallbackAppraisalClientVehicleSimplifier(val callback: CallbackAppraisalClientVehicle):Parcelable {
    private val local=LocalRepo()
     var model = callback.modelData?.id?.let { local.getOne<Model>(it) }

    val modelData =
        model?.let {
            ModelSimplifier(
                it
            )
        }
var price = model?.price()
    val clientVehicle = callback.clientVehicleId?.let {

            val car = local.getOne<ClientVehicle>(it)
            car?.let { it2 ->
                ClientVehicleSimplifier(it2)
            }

    }
    val requests = callback.requests
    val name =  clientVehicle?.dealerName +" "+ clientVehicle?.modelName
}

class ClientVehicleSimplifier(val clientVehicle: ClientVehicle) {

    var options = clientVehicle.options
    val dealerName = clientVehicle.dealerName
    val modelName = clientVehicle.modelName
    val gallery = clientVehicle.imageGallery
    val cylinders = clientVehicle.cylinder
    val year = clientVehicle.year
    val mileage = clientVehicle.mileage
    val engine = clientVehicle.engine
    val condition = clientVehicle.condition
    val description = clientVehicle.description
    val firstImage: String? = if (gallery.isNotEmpty()) gallery[0]?.let { it.file } else " "
    val name = "$dealerName - $modelName"
}

class RequestSimplifier(request: AppraisalRequest) {
    var isEnglish = Shared.prefs.getString(MyApplication.LANG, "EN") == "EN"
    private var lang: Lang?=request.appraisal.let {
        it?.translations?.let { trans ->
            if (isEnglish) trans.en else trans.ar
        }
    }
    var name =lang?.name

    var price =if (request.price!=null)request.price else "Status Pending"

}