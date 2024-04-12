package com.theendercore

import com.theendercore.data.Dependency
import com.theendercore.data.Mod
import com.theendercore.data.NoModFound
import com.theendercore.data.RunData
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.encodeToString
import masecla.modrinth4j.endpoints.SearchEndpoint
import masecla.modrinth4j.model.project.ProjectType
import masecla.modrinth4j.model.search.Facet
import masecla.modrinth4j.model.search.FacetCollection
import net.peanuuutz.tomlkt.Toml
import net.peanuuutz.tomlkt.TomlIndentation
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import kotlin.time.Duration.Companion.hours


val filter = listOf("fabric", "forge", "neoforge", "quilt")
val query: SearchEndpoint.SearchRequest = SearchEndpoint.SearchRequest.builder()
    .facets(
        FacetCollection.builder()
            .facets(
                listOf(
                    listOf(Facet.projectType(ProjectType.MOD)),
                    listOf(
                        Facet.version("1.20"),
                        Facet.version("1.20.1"),
                        Facet.version("1.20.2"),
                        Facet.version("1.20.3"),
                        Facet.version("1.20.4"),
                    )
                )
            )
            .build()
    )
    .limit(60)
    .build()

val toml = Toml {
    ignoreUnknownKeys = true
    indentation = TomlIndentation.Space4
}
val log: Logger = LoggerFactory.getLogger("Istos")
val RunDataFile = File("run-data.toml")

fun main() {
    val (api, runData, modManager) = dependencies()

    if (runData == null || have6HoursPassed(runData.lastRan)) {
        log.info("Making file!")
        RunDataFile.writeText(toml.encodeToString(RunData.new()))

        val results = api.search(query).get()
        log.info("[${results.limit}, ${results.offset}, ${results.totalHits}]")
        var count = 0
        results.hits.map { Mod(it) }.forEach {
            if (!modManager.isModUpToDate(it)) {
                log.info("[${it.title}] not up to date or undocumented! Updating...")
                modManager.addMod(it)?.let { err -> log.error(err.toString()); return@forEach }
                count++
            }
        }

        if (count > 0) log.info("{} mods updated", count)
    } else log.info("Not Time to update mods")


    val mods = modManager.getAllMods().getOrNull()
    if (!mods.isNullOrEmpty()) {
        var count = 0
        mods.forEach {
            if (!modManager.isDependencyUpToDate(it)) {
                log.info("[${it.title}] doesnt have the latest dependencies!")
                val dep = api.projects().getProjectDependencies(it.slug).get()
                modManager.addDependency(Dependency(it.projectId, it.latestVersion, dep.projects.map { p -> p.id }))
                count++
            }
        }
        if (count > 0) log.info("{} dependencies updated", count)
        else log.info("No Dependencies updated!")
    } else log.warn("No mods!")


    val deps = modManager.getAllDependencies().getOrNull()
    if (!deps.isNullOrEmpty()) {
        val modList = mutableListOf<String>()
        deps.forEach {
            val x = modManager.getMod(it.projectId).leftOrNull() ?: return@forEach
            if (x is NoModFound) modList.add(it.projectId)
        }
        if (modList.isEmpty()) println("EMPT")
        api.projects().get(modList).get().map { Mod(it) }.forEach {
            log.info("Added mod from dep [{}]", it)
            modManager.addMod(it)
        }
    } else println("!!")

}

fun have6HoursPassed(lastRan: Instant): Boolean {
    return lastRan <= Clock.System.now().minus(6.hours)
}
