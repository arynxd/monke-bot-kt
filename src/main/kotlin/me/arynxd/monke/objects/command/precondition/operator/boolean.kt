package me.arynxd.monke.objects.command.precondition.operator

import me.arynxd.monke.objects.command.CommandEvent
import me.arynxd.monke.objects.command.precondition.Precondition

fun Precondition.not(): Precondition {
    return object: Precondition {
        override fun pass(event: CommandEvent): Boolean {
            return !this@not.pass(event)
        }

        override fun onFail(event: CommandEvent) {
            this@not.onFail(event)
        }

        override fun onSuccess(event: CommandEvent) {
            this@not.onSuccess(event)
        }
    }
}

fun Precondition.or(other: Precondition): Precondition {
    return object: Precondition {
        private val fails = mutableListOf<Precondition>()
        private val passes = mutableListOf<Precondition>()

        override fun pass(event: CommandEvent): Boolean {
            var result = true

            if (other.pass(event)) {
                result = false
                fails.add(other)
            }
            else {
                passes.add(other)
            }

            if (this@or.pass(event)) {
                result = false
                fails.add(this@or)
            }
            else {
                passes.add(this@or)
            }

            return result
        }

        override fun onFail(event: CommandEvent) {
            fails.forEach { it.onFail(event) }
        }

        override fun onSuccess(event: CommandEvent) {
            passes.forEach { it.onSuccess(event) }
        }
    }
}