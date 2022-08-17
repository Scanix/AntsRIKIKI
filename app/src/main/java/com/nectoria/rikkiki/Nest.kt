package com.nectoria.rikkiki

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import java.util.*

class Nest : Cell {
    constructor(world: World, x: Int, y: Int,  w: Int, h: Int,) : super(world, x, y, w, h) {
        this.type = Type.NEST
        this.nestDistance = 0
    }

    override fun stepOnCell() {
    }

    override fun update() {
    }

    override fun render(canvas: Canvas) {
        val paint = Paint()
        paint.alpha = 255
        paint.setARGB(255, 46, 28, 4)

        paint.strokeWidth = 3f

        canvas.drawRect(
            (this.position.x * this.size.x).toFloat(), (this.position.y * this.size.y).toFloat(), (this.position.x * this.size.x + this.size.x).toFloat(),
            (this.position.y * this.size.y + this.size.y).toFloat(), paint
        )
    }
}