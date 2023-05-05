package com.kuwait.showroomz.model.simplifier

import com.kuwait.showroomz.extras.BASE_URL
import com.kuwait.showroomz.extras.VideoUtils
import com.kuwait.showroomz.model.data.Image


class VideoSimplifier(val video: Image) {
    val url = /*BASE_URL +*/ video.file
    val thumbnail = url?.let { VideoUtils.retrieveVideoFrameFromVideo(it) }

    //val duration = video.duration?.replace('.',':')
   /* val duration = video.duration?.indexOf('.')
        ?.let { video.duration?.substring(0, it) } + ':' + video.duration?.indexOf('.') ?.let {
        video.duration?.indexOf('.')?.let { it1 -> video.duration?.substring(it+1, it1+3) }
    }*/

    val x = video.duration?.split(".")
    val y = x?.let{
        if (it.isNotEmpty()){
           it.first().let{
               if (it == ""){
                   0
               }else {
                   it.toInt()
               }
           }
        }else{
            0
        }
    }?: run{
        0
    }

    val duration = if (y == 0){
        "00:01"
    }else{
        val minutes = (y / 60) % 60
        val seconds = y % 60
        var finalMinutes = "$minutes"
        var finalSeconds = "$seconds"
        if (seconds < 10){
            finalSeconds =  "0$seconds"
        }
        if (minutes < 10){
            finalMinutes =  "0$minutes"
        }
        "$finalMinutes:$finalSeconds"

        //duration = String(format: "%d:%d",  minutes, seconds)
    }

}