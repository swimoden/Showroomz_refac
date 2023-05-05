package com.kuwait.showroomz.extras.customViews

import android.view.animation.Animation
import android.view.animation.Transformation

class ProgressBarAnimation(
    private var progressBar: CircleProgressBar?,
    private var from: Float,
    private var to: Float
) : Animation() {

    override fun applyTransformation(
        interpolatedTime: Float,
        t: Transformation?
    ) {
        super.applyTransformation(interpolatedTime, t)
        val value = from + (to - from) * interpolatedTime
//        progressBar!!.setProgress(value)
    }

}