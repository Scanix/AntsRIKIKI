package com.nectoria.rikkiki

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import java.util.*

class Food : Cell {
    var foodLeft: Int

    constructor(world: World, x: Int, y: Int, w: Int, h: Int) : super(world, x, y, w, h) {
        this.type = Type.FOOD
        this.foodDistance = 0
        this.foodLeft = Constants.FOOD_STOCK
    }

    fun eatFood() {
        this.foodLeft--
    }

    override fun update() {
        if (foodLeft <= 0) world.grid[this.position.x][this.position.y] = Cell(
            world,
            this.position.x,
            this.position.y,
            this.size.x,
            this.size.y
        )
    }

    override fun render(canvas: Canvas) {
        val paint = Paint()
        paint.alpha = 255
        paint.setARGB(255,69, 62, 1)

        paint.strokeWidth = 3f

        canvas.drawRect(
            (this.position.x * this.size.x).toFloat(), (this.position.y * this.size.y).toFloat(), (this.position.x * this.size.x + this.size.x).toFloat(),
            (this.position.y * this.size.y + this.size.y).toFloat(), paint
        )
    }

}
