package com.nectoria.rikkiki

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Point
import android.util.Log
import java.util.*
import kotlin.math.floor

class World(w: Int, h: Int) {
    private val gridX: Int = Constants.GRID_W
    private val gridY: Int = Constants.GRID_H
    val grid: Array<Array<Obstacle>>
    val nest: Nest
    var adjPos: Array<Array<MutableList<Point>>>
    private val ants: MutableList<Ant>
    val size: Point = Point(w, h)

    init {
        this.grid = this.initGrid()
        this.addCell()
        this.nest = this.initNest(Constants.NEST_X, Constants.NEST_Y)
        this.adjPos = this.getAdjPositions()
        this.addObstacles(Constants.OBSTACLE_COUNT)
        if (Constants.FOOD != 0) this.initFood(Constants.FOOD)
        this.adjPos = this.getAdjPositions()
        this.ants = this.initAnts(Constants.ANTS)
        //this.renderAllOnce(canvas)
    }

    private fun initGrid(): Array<Array<Obstacle>> {
        val grid = Array(gridX) { x ->
            Array(gridY) { y ->
                Obstacle(this, x, y, size.x, size.y)
            }
        }

        return grid
    }

    private fun addCell() {
        for (x in 0 until gridX)
            for (y in 0 until gridY)
                this.grid[x][y] = Cell(this, x, y, size.x, size.y)
    }

    private fun addObstacles(quantity: Int) {
        for (i in 0 until quantity) {
            var x = floor(Math.random() * this.gridX).toInt()
            var y = floor(Math.random() * this.gridY).toInt()
            var expansions = Constants.OBSTACLE_SIZE
            do {
                this.grid[x][y].createObstacle(this)
                this.adjPos[x][y].forEach { position ->
                    if (this.grid[position.x][position.y].type != Type.NEST)
                        this.grid[position.x][position.y] = Obstacle(
                            this,
                            position.x,
                            position.y,
                            size.x,
                            size.y
                        )
                }

                if (this.adjPos[x][y].isNotEmpty()) {
                    val nextPos = this.adjPos[x][y].random()
                    x = nextPos.x
                    y = nextPos.y
                }
                expansions--
            } while (expansions != 0)
        }
    }

    private fun getAdjPositions(): Array<Array<MutableList<Point>>> {
        val adjPos: Array<Array<MutableList<Point>>> = Array(gridX) { x ->
            Array(gridY) { y ->
                val temp = mutableListOf<Point>()
                val neighbours = this.getNeighbours(x, y)
                neighbours.forEach { position ->
                    try {
                        val cell = this.grid[position.x][position.y]
                        if (cell.type != Type.OBSTACLE) temp.add(Point(position))
                    } catch (error: ArrayIndexOutOfBoundsException) {
                    }
                }
                return@Array temp
            }
        }

        return adjPos
    }

    private fun getNeighbours(x: Int, y: Int): Array<Point> {
        return arrayOf(
            Point(x - 1, y - 1),
            Point(x, y - 1),
            Point(x + 1, y - 1),

            Point(x - 1, y),
            Point(x + 1, y),

            Point(x - 1, y + 1),
            Point(x, y + 1),
            Point(x + 1, y + 1),
        )
    }

    private fun initNest(nestX: Int, nestY: Int): Nest {
        val nest = Nest(this, nestX, nestY, this.size.x, this.size.y)
        this.grid[nestX][nestY] = nest
        return nest
    }

    private fun initAnts(ants: Int): MutableList<Ant> {
        val antsList = mutableListOf<Ant>()
        for (i in 0..ants)
            antsList.add(
                Ant(
                    this,
                    this.nest.position.x,
                    this.nest.position.y,
                    this.size.x,
                    this.size.y
                )
            )

        return antsList
    }

    private fun initFood(food: Int) {
        var food = food
        // Create food at random positions
        while (food != 0) {
            var x = floor(Math.random() * this.gridX).toInt()
            var y = floor(Math.random() * this.gridY).toInt()
            if (!(x == this.nest.position.x && y == this.nest.position.y)) {
                this.grid[x][y] = Food(this, x, y, this.size.x, this.size.y)
                food--
            }
        }
    }

    fun killRandomAnt() {
        ants.random().kill()
    }

    fun spawnAnt() {
        if (ants.size <= Constants.ANTS)
            ants.add(
                Ant(
                    this,
                    this.nest.position.x,
                    this.nest.position.y,
                    this.size.x,
                    this.size.y
                )
            )
    }

    fun update() {
        for (x in 0 until gridX)
            for (y in 0 until gridY)
                this.grid[x][y].update()

        this.ants.forEach { ant ->
            if (ant.state != State.DEAD_MODE)
                ant.update()
            else
                this.ants.remove(ant)
        }
    }

    private fun renderAllOnce(canvas: Canvas) {
        for (x in 0 until gridX)
            for (y in 0 until gridY)
                this.grid[x][y].render(canvas)
    }

    fun render(canvas: Canvas) {
        for (x in 0 until gridX)
            for (y in 0 until gridY)
                this.grid[x][y].render(canvas)

        this.ants.forEach { ant -> ant.render(canvas) }
        this.nest.render(canvas)
    }
}

object Constants {
    const val GRID_W: Int = 100
    const val GRID_H: Int = 100
    const val NEST_X: Int = 50
    const val NEST_Y: Int = 50
    const val ANTS: Int = 10
    const val FOOD: Int = 50
    const val FOOD_STOCK: Int = 10
    const val OBSTACLE_COUNT: Int = 10
    const val OBSTACLE_SIZE: Int = 5
}

enum class Type {
    NEST, FOOD, OBSTACLE, ANT, CELL
}