package com.theendercore.data

import com.theendercore.database.Mods
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import masecla.modrinth4j.endpoints.SearchEndpoint
import masecla.modrinth4j.model.project.ProjectType
import masecla.modrinth4j.model.project.SupportStatus

//import java.time.Instant

@Serializable
data class Mod(
    val slug: String,
    val title: String,
    val description: String,
    val categories: List<String>,
    val clientSide: SupportStatus,
    val serverSide: SupportStatus,
    val projectType: ProjectType,
    val downloads: Int,
    val iconUrl: String,
    val projectId: String,
    val author: String,
    val displayCategories: List<String>,
    val versions: List<String>,
    val follows: Int,
    val dateCreated: Instant,
    val dateModified: Instant,
    val latestVersion: String,
    val license: String,
    val gallery: List<String>
) {
    constructor(searchResult: SearchEndpoint.SearchResult) : this(
        searchResult.slug,
        searchResult.title,
        searchResult.description,
        searchResult.categories,
        searchResult.clientSide,
        searchResult.serverSide,
        searchResult.projectType,
        searchResult.downloads,
        searchResult.iconUrl,
        searchResult.projectId,
        searchResult.author,
        searchResult.displayCategories,
        searchResult.versions,
        searchResult.follows,
        Instant.fromEpochMilliseconds(searchResult.dateCreated.toEpochMilli()),
        Instant.fromEpochMilliseconds(searchResult.dateModified.toEpochMilli()),
        searchResult.latestVersion,
        searchResult.license,
        searchResult.gallery
    )

    constructor(simple: Mods) : this(
        simple.slug,
        simple.title,
        simple.description,
        simple.categories.split(",").map { it.trim() },
        SupportStatus.valueOf(simple.clientSide),
        SupportStatus.valueOf(simple.serverSide),
        ProjectType.valueOf(simple.projectType),
        simple.downloads.toInt(),
        simple.iconUrl,
        simple.projectId,
        simple.author,
        simple.displayCategories.split(",").map { it.trim() },
        simple.versions.split(",").map { it.trim() },
        simple.follows.toInt(),
        Instant.parse(simple.dateCreated),
        Instant.parse(simple.dateModified),
        simple.latestVersion,
        simple.license,
        simple.gallery.split(",").map { it.trim() }
    )
}

fun Mods(original: Mod) = Mods(
    original.slug,
    original.title,
    original.description,
    original.categories.joinToString(","),
    original.clientSide.toString(),
    original.serverSide.toString(),
    original.projectType.toString(),
    original.downloads.toLong(),
    original.iconUrl,
    original.projectId,
    original.author,
    original.displayCategories.joinToString(","),
    original.versions.joinToString(","),
    original.follows.toLong(),
    original.dateCreated.toString(),
    original.dateModified.toString(),
    original.latestVersion,
    original.license,
    original.gallery.joinToString(",")
)