package com.theendercore

import arrow.core.Either
import arrow.core.getOrElse
import com.theendercore.DBManager.isUpToDate
import com.theendercore.data.Mod
import com.theendercore.data.RunData
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.decodeFromString
import masecla.modrinth4j.client.agent.UserAgent
import masecla.modrinth4j.endpoints.SearchEndpoint
import masecla.modrinth4j.main.ModrinthAPI
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
    .limit(1)
    .build()

val toml = Toml {
    ignoreUnknownKeys = true
    indentation = TomlIndentation.Space4
}
val log: Logger = LoggerFactory.getLogger("Istos")
val RunDataFile = File("run-data.toml")

fun main() {
    val (api, runData) = dependencies()

    if (runData == null || have6HoursPassed(runData.lastRan)) {
        log.info("Making file!")
//        RunDataFile.writeText(toml.encodeToString(RunData.new()))

        val results = api.search(query).get()
        log.info("[${results.limit}, ${results.offset}, ${results.totalHits}]")
        var count = 0
        results.hits.map { Mod(it) }.forEach {
            if (!it.isUpToDate()) {
                log.info("${it.title} not up to date or registered Updating!")
                DBManager.updateMod(it)
                count++
            }
        }

        if (count > 0) log.info("{} mods updated", count)
    }

    /*

        val a = results.hits.map { SearchResultWrapper(it) }.map {
            listOf(
                it.title,
                it.author,
                it.projectType,
                it.categories.filter { i -> filter.contains(i) }.joinToString(", "),
                it.description.replace("\n", "  "),
            ).joinToString(" | ")

        }
        a.forEach(::log.info)

     */
}

fun have6HoursPassed(lastRan: Instant): Boolean {
    return lastRan <= Clock.System.now().minus(6.hours)
}

fun dependencies(): Dependencies {

//database.modsQeurries

    val userAgent = UserAgent.builder()
        .authorUsername("TheEnderCore")
        .projectName("Istos")
        .projectVersion("0.1.0")
        .contact("theendercore@gmail.com")
        .build()
    log.info(userAgent.toString())
    val runData = Either.catch {
        toml.decodeFromString<RunData>(RunDataFile.readText())
    }.getOrElse {
        log.warn("$it\n ${it.stackTrace}")
        return@getOrElse null
    }

    return Dependencies(
        ModrinthAPI.rateLimited(userAgent, ""),
        runData,
        database()
    )
}

data class Dependencies(val api: ModrinthAPI, val runData: RunData?, val database: ModManger)
