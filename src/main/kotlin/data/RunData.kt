package com.theendercore.data

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class RunData(val lastRan: Instant) {
    companion object {
        fun new() = RunData(Clock.System.now())
    }
}
