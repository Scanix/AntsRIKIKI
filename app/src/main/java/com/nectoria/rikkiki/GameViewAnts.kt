package com.nectoria.rikkiki

import android.content.Context
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView

import android.graphics.Canvas


class GameViewAnts(context: Context, attributes: AttributeSet) : SurfaceView(context, attributes),
    SurfaceHolder.Callback {
    private val thread: GameThreadAnts
    private lateinit var world: World
    private var lastIsFaceDetected = false
    private var delaySpawnKill = 0


    init {
        holder.addCallback(this)
        thread = GameThreadAnts(holder, this)
    }

    override fun surfaceCreated(p0: SurfaceHolder) {
        val rowWidth = this.width / 100
        val rowHeight = this.height / 100
        world = createWorld(rowWidth, rowHeight)

        thread.setRunning(true)
        thread.start()
    }

    override fun surfaceDestroyed(p0: SurfaceHolder) {
        var retry = true
        while (retry) {
            try {
                thread.setRunning(false)
                thread.join()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            retry = false
        }
    }


    override fun surfaceChanged(p0: SurfaceHolder, p1: Int, p2: Int, p3: Int) {

    }

    /**
     * Function to update the positions of player and game objects
     */
    fun update() {
        if (lastIsFaceDetected != MainActivity.isFaceDetected) {
            delaySpawnKill = 0
        } else {
            delaySpawnKill++

            if (delaySpawnKill > (10 * (this.world.ants.size + 1)) && MainActivity.isFaceDetected) {
                spawnAnt()
                delaySpawnKill = 0
            }

            if (delaySpawnKill > 25 && !MainActivity.isFaceDetected) {
                killRandomAnt()
                delaySpawnKill = 0
            }
        }

        lastIsFaceDetected = MainActivity.isFaceDetected
        world.update()
    }

    fun killRandomAnt() {
        world.killRandomAnt()
    }

    fun spawnAnt() {
        world.spawnAnt()
    }

    /**
     * Everything that has to be drawn on Canvas
     */
    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        /*val paint = Paint()
        paint.color = Color.GREEN
        canvas.drawRect(0F, 0F, this.width.toFloat(), this.height.toFloat(), paint)*/
        world.render(canvas)
    }

    private fun createWorld(rowWidth: Int, rowHeight: Int): World {
        var newWorld: World
        do {
            newWorld = World(rowWidth, rowHeight)
        } while (newWorld.adjPos[newWorld.nest.position.x][newWorld.nest.position.y].isEmpty())

        return newWorld
    }

}