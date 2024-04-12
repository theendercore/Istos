package com.theendercore.data

import com.theendercore.database.Mods
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import masecla.modrinth4j.endpoints.SearchEndpoint
import masecla.modrinth4j.model.project.Project
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

    constructor(project: Project) : this(
        project.slug,
        project.title,
        project.description,
        project.categories,
        project.clientSide,
        project.serverSide,
        project.projectType,
        project.downloads,
        project.iconUrl,
        project.id,
        project.team,
        project.additionalCategories,
        project.versions,
        project.followers,
        Instant.fromEpochMilliseconds(project.published.toEpochMilli()),
        Instant.fromEpochMilliseconds(project.updated.toEpochMilli()),
        project.versions.first(),
        project.license.name,
        project.gallery.map { it.url }
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

    fun toMods() = Mods(
        this.slug,
        this.title,
        this.description,
        this.categories.joinToString(","),
        this.clientSide.toString(),
        this.serverSide.toString(),
        this.projectType.toString(),
        this.downloads.toLong(),
        this.iconUrl,
        this.projectId,
        this.author,
        this.displayCategories.joinToString(","),
        this.versions.joinToString(","),
        this.follows.toLong(),
        this.dateCreated.toString(),
        this.dateModified.toString(),
        this.latestVersion,
        this.license,
        this.gallery.joinToString(",")
    )

}
