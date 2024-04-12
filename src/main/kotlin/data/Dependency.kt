package com.theendercore.data

import com.theendercore.database.Dependencies

data class Dependency(
    val projectId: String,
    val version: String,
    val items: List<String>,
) {
    constructor(dep: Dependencies) : this(dep.projectId, dep.version, dep.items.split(","))

    fun toDependencies() =
        Dependencies(
            this.projectId,
            this.version,
            this.items.joinToString(",")
        )
}
