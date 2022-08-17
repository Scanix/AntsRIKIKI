package com.nectoria.rikkiki

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import java.util.*
import kotlin.math.max

open class Cell : Obstacle {
    var nestDistance: Int
    var foodDistance: Int
    var fDuration: Double
    var steps: Int
    var stepDuration: Int

    constructor(world: World, x: Int, y: Int, w: Int, h: Int, steps: Int = 0) : super(
        world,
        x,
        y,
        w,
        h
    ) {
        this.type = Type.CELL
        this.nestDistance = Int.MAX_VALUE
        this.foodDistance = -1
        this.fDuration = 0.0
        this.steps = steps
        this.stepDuration = Cell.stepDuration
    }

    fun setCellsNestDistance(stepsFromNest: Int) {
        this.setCellsDistance(stepsFromNest, Type.NEST)
    }

    fun setFoodDistanceComplexe(stepsFromFood: Int) {
        this.setDistance(stepsFromFood, Type.FOOD)
        this.fDuration = foodMaxD
        // Never set nest value
        world.nest.foodDistance = -1
    }

    private fun setCellsDistance(steps: Int, property: Type) {
        this.setDistance(steps, property)
        world.adjPos[this.position.x][this.position.y].forEach { position ->
            if (position.x == this.position.x || position.y == this.position.y)
                (world.grid[position.x][position.y] as Cell).setDistance(
                    steps + 1,
                    property
                )
            // Diagonal values, increase by +2
            else
                (world.grid[position.x][position.y] as Cell).setDistance(
                    steps + 2,
                    property
                )
        }
    }

    private fun setDistance(steps: Int, property: Type) {
        if (property == Type.NEST) {
            if (this.nestDistance > steps) this.nestDistance = steps
        } else {
            // property == "food"
            if (this.foodDistance == -1) this.foodDistance = steps
            else if (this.foodDistance > steps) this.foodDistance = steps
        }
    }

    fun eraseFoodTrail() {
        this.fDuration = 0.0
    }

    open fun stepOnCell() {
        this.addStep()
    }

    private fun addStep(n: Int = 1) {
        this.stepDuration = Cell.stepDuration
        this.steps += n
    }

    private fun decreaseSteps() {
        this.steps--
    }

    override fun update() {
        this.updateSteps()
        this.fDuration = max(--this.fDuration, 0.0)
        if (this.fDuration.toInt() == 0) this.foodDistance = -1
    }

    private fun updateSteps() {
        this.stepDuration--
        if (this.stepDuration < 0) this.decreaseSteps()
    }

    override fun render(canvas: Canvas) {
        /*val paint = Paint()
        paint.alpha = 255
        if (this.fDuration.toInt() == 0) paint.setARGB(255, 48, 2, max(98 - this.steps * 2, 20))
        else paint.setARGB(50, 100, 100, 255)

        paint.strokeWidth = 3f

        canvas.drawRect(
            (this.position.x * this.size.x).toFloat(),
            (this.position.y * this.size.y).toFloat(),
            (this.position.x * this.size.x + this.size.x).toFloat(),
            (this.position.y * this.size.y + this.size.y).toFloat(),
            paint
        )*/
    }

    companion object {
        var stepDuration = max(Constants.GRID_W, Constants.GRID_H) * 10
        var foodMaxD = max(Constants.GRID_W, Constants.GRID_H) * 1.5
    }
}