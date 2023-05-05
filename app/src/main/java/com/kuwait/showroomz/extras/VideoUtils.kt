package com.kuwait.showroomz.extras

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever


object VideoUtils {
    fun retrieveVideoFrameFromVideo(videoPath: String) : Bitmap?{
        var bitmap: Bitmap? = null
        Thread {
            var mediaMetadataRetriever: MediaMetadataRetriever? = null
            try {
                mediaMetadataRetriever = MediaMetadataRetriever()
                mediaMetadataRetriever.setDataSource(videoPath, HashMap())
                //   mediaMetadataRetriever.setDataSource(videoPath);
                bitmap = mediaMetadataRetriever.frameAtTime
            } catch (e: Exception) {
                e.printStackTrace()
                //throw Throwable("Exception in retriveVideoFrameFromVideo(String videoPath)" + e.message)
            } finally {
                mediaMetadataRetriever?.release()
            }
        }.start()

        return bitmap
    }
}