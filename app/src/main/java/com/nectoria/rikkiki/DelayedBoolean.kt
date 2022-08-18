package com.nectoria.rikkiki

class DelayedBoolean(value: Boolean, delay: Int) {
    private var value: Boolean = value
    private var array: MutableList<Boolean> = mutableListOf()
    private var delay: Int = delay

    fun setValue(value: Boolean) {
        this.array.add(value)
        if (this.array.size == this.delay) {
            var trueValue = 0
            var falseValue = 0
            this.array.forEach { b ->
                if (b) trueValue++ else falseValue++
            }

            this.value = trueValue > falseValue
            this.array = mutableListOf()
        }
    }

    fun getValue(): Boolean {
        return this.value
    }
}