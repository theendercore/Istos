package com.theendercore.data

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import masecla.modrinth4j.endpoints.SearchEndpoint
import masecla.modrinth4j.model.project.ProjectType
import masecla.modrinth4j.model.project.SupportStatus
import java.time.Instant

@Serializable
data class SearchResultWrapper(
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
    @Contextual val dateCreated: Instant,
    @Contextual val dateModified: Instant,
    val latestVersion: String,
    val license: String,
    val gallery: List<String>
){
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
        searchResult.dateCreated,
        searchResult.dateModified,
        searchResult.latestVersion,
        searchResult.license,
        searchResult.gallery
    )
}
