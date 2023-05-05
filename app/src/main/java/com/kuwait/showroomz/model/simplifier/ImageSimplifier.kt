package com.kuwait.showroomz.model.simplifier

import com.kuwait.showroomz.extras.BASE_URL
import com.kuwait.showroomz.model.data.Image

class ImageSimplifier(var image: Image) {
    var imageUrl = image.file ?: ""
    var id = image.id
}