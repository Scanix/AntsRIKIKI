package com.nectoria.rikkiki

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import java.util.*

open class Obstacle {
    val world: World
    var position: Point
    val size: Point
    var type: Type

    constructor(world: World, x: Int, y: Int, w: Int, h: Int) {
        this.world = world
        this.position = Point(x, y)
        this.size = Point(w, h)
        this.type = Type.OBSTACLE
    }

    fun createObstacle(world: World) {
        if (!(this.position.x == world.nest.position.x && this.position.y == world.nest.position.y))
            world.grid[this.position.x][this.position.y] =
                Obstacle(world, this.position.x, this.position.y, this.size.x, this.size.y)
    }

    open fun update() {}

    open fun render(canvas: Canvas) {
        val paint = Paint()
        paint.alpha = 255
        paint.setARGB(255, 10, 10, 10)

        paint.strokeWidth = 3f

        canvas.drawRect(
            (this.position.x * this.size.x).toFloat(), (this.position.y * this.size.y).toFloat(), (this.position.x * this.size.x + this.size.x).toFloat(),
            (this.position.y * this.size.y + this.size.y).toFloat(), paint
        )
    }
}