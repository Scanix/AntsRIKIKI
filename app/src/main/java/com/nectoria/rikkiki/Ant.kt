package com.nectoria.rikkiki

import android.graphics.*
import android.util.Log
import java.util.*

class Ant : Obstacle {
    var state: State
    var stepsFromNest: Int
    var stepsFromFood: Int
    var erase: Boolean
    var prevPosition: Point
    var dyingTime: Int = 2

    constructor(world: World, x: Int, y: Int, w: Int, h: Int) : super(world, x, y, w, h) {
        this.type = Type.ANT
        this.state = State.SCAVENGER_MODE
        this.stepsFromNest = 0
        this.stepsFromFood = -1
        this.erase = false
        this.prevPosition = Point(-1, -1)
    }

    override fun update() {
        if (this.state == State.DYING_MODE) {
            if (dyingTime == 0)
                this.state = State.DEAD_MODE
            else
                dyingTime--

            return
        }

        if (world.grid[this.position.x][this.position.y].type == Type.NEST)
            this.reachedNest()
        if (world.grid[this.position.x][this.position.y].type == Type.FOOD)
            this.reachedFood()

        (world.grid[this.position.x][this.position.y] as Cell).stepOnCell()

        this.stepsFromNest++
        if (this.stepsFromFood >= 0) this.stepsFromFood++

        var newPos = this.getMinDistanceFood()
        if (this.state == State.SCAVENGER_MODE) {
            if (this.closeToNest() || newPos.equals(-3, -3) || isSamePosition(
                    newPos,
                    this.prevPosition
                )
            ) {
                do {
                    newPos = this.randomWalk()
                } while (world.adjPos[this.position.x][this.position.y].count() != 1 && isSamePosition(
                        newPos,
                        this.prevPosition
                    )
                )
            }
        }
        // DELIVERY_MODE
        else {
            newPos = this.getMinNestDistanceCell()
            if (this.erase) (world.grid[this.position.x][this.position.y] as Cell).eraseFoodTrail()
        }

        if (this.isDiagonal(newPos)) {
            this.stepsFromNest++
            if (this.stepsFromFood >= 0) this.stepsFromFood++
        }

        this.saveOldPosition()
        this.updatePosition(newPos)

        if (this.state == State.SCAVENGER_MODE) this.updateNestDistance()
        // DELIVERY_MODE
        else if (this.stepsFromFood != -1)
            (world.grid[this.position.x][this.position.y] as Cell).setFoodDistanceComplexe(this.stepsFromFood)
    }

    private fun getMinDistanceFood(): Point {
        val cell = (world.grid[this.position.x][this.position.y] as Cell)
        val initialDistance = if (cell.foodDistance == -1) Int.MAX_VALUE else cell.foodDistance
        val newPos = world.adjPos[this.position.x][this.position.y].reduce { foodPos, nextPos ->
            if ((world.grid[nextPos.x][nextPos.y] as Cell).foodDistance == -1)
                return@reduce foodPos

            if ((world.grid[foodPos.x][foodPos.y] as Cell).foodDistance == -1)
                return@reduce nextPos

            if (
                (world.grid[foodPos.x][foodPos.y] as Cell).foodDistance != -1 &&
                (world.grid[nextPos.x][nextPos.y] as Cell).foodDistance <
                (world.grid[foodPos.x][foodPos.y] as Cell).foodDistance
            )
                return@reduce nextPos
            return@reduce foodPos
        }

        if ((world.grid[newPos.x][newPos.y] as Cell).foodDistance >= initialDistance)
            return Point(-3, -3)

        if ((world.grid[newPos.x][newPos.y] as Cell).foodDistance != -1)
            return newPos

        return Point(-3, -3)
    }

    private fun getMinNestDistanceCell(): Point {
        val nextPos = world.adjPos[this.position.x][this.position.y].reduce { minPos, nextPos ->
            if (
                (world.grid[nextPos.x][nextPos.y] as Cell).nestDistance <
                (world.grid[minPos.x][minPos.y] as Cell).nestDistance
            )
                return@reduce nextPos
            return@reduce minPos
        }

        return nextPos
    }

    private fun randomWalk(): Point {
        return world.adjPos[this.position.x][this.position.y].random()
    }

    private fun closeToNest(): Boolean {
        return isSamePosition(this.position, world.nest.position)
    }

    private fun startEraseFoodTrail() {
        this.erase = true
    }

    private fun saveOldPosition() {
        this.prevPosition = Point(this.position)
    }

    private fun updatePosition(newPos: Point) {
        this.position = Point(newPos)
    }

    private fun updateNestDistance() {
        if (this.stepsFromNest > (world.grid[this.position.x][this.position.y] as Cell).nestDistance)
        // Update ant distance to cell stored value
            this.stepsFromNest =
                (world.grid[this.position.x][this.position.y] as Cell).nestDistance
        // Update cells with ant's new closest distance
        else
            (world.grid[this.position.x][this.position.y] as Cell).setCellsNestDistance(this.stepsFromNest)
    }

    private fun isDiagonal(newPos: Point): Boolean {
        return !(this.position.x == newPos.x || this.position.y == newPos.y)
    }

    private fun reachedFood() {
        this.stepsFromFood = 0
        (world.grid[this.position.x][this.position.y] as Food).eatFood()
        if ((world.grid[this.position.x][this.position.y] as Food).foodLeft <= 0)
            this.startEraseFoodTrail()
        this.state = State.DELIVERY_MODE
    }

    private fun reachedNest() {
        this.stepsFromNest = 0
        this.stepsFromFood = -1
        this.resetPrevPosition()
        this.state = State.SCAVENGER_MODE
        this.erase = false
    }

    private fun resetPrevPosition() {
        this.prevPosition.x = world.nest.position.x
        this.prevPosition.y = world.nest.position.y
    }

    override fun render(canvas: Canvas) {
        val paint = Paint()
        paint.alpha = 255

        if (this.state == State.DYING_MODE)
            paint.color = Color.RED
        else
            paint.color = Color.WHITE

        paint.strokeWidth = 3f

        canvas.drawOval(
            RectF(
                (this.position.x * this.size.x).toFloat(),
                (this.position.y * this.size.y).toFloat(),
                (this.position.x * this.size.x + this.size.x).toFloat(),
                (this.position.y * this.size.y + this.size.y).toFloat(),
            ), paint
        )
    }

    fun kill() {
        this.state = State.DYING_MODE
    }
}

enum class State {
    SCAVENGER_MODE,
    DELIVERY_MODE,
    SPAWN_MODE,
    DYING_MODE,
    DEAD_MODE,
}

fun isSamePosition(pos1: Point, pos2: Point): Boolean {
    return pos1.x == pos2.x && pos1.y == pos2.y
}