package com.theendercore

import com.theendercore.data.SearchResultWrapper
import masecla.modrinth4j.client.agent.UserAgent
import masecla.modrinth4j.endpoints.SearchEndpoint
import masecla.modrinth4j.main.ModrinthAPI
import masecla.modrinth4j.model.project.ProjectType
import masecla.modrinth4j.model.search.Facet
import masecla.modrinth4j.model.search.FacetCollection

fun main() {
    println("Hello World!")
    val userAgent = UserAgent.builder()
        .authorUsername("TheEnderCore")
        .projectName("Istos")
        .projectVersion("0.1.0")
        .contact("theendercore@gmail.com")
        .build()
    println(userAgent.toString())

    val filter = listOf("fabric", "forge", "neoforge", "quilt")


    val api = ModrinthAPI.rateLimited(userAgent, "")

    val query = SearchEndpoint.SearchRequest.builder()
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
        .limit(10)
        .build()
    val results = api.search(query).get()
    println("[${results.limit}, ${results.offset}, ${results.totalHits}]")

    val a = results.hits.map { SearchResultWrapper(it) }.map {
        listOf(
            it.title,
            it.author,
            it.projectType,
            it.categories.filter { i -> filter.contains(i) }.joinToString(", "),
            it.description.replace("\n", "  "),
        ).joinToString(" | ")

    }
    a.forEach(::println)
}
