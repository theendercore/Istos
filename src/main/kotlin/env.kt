package com.theendercore

import arrow.core.Either
import arrow.core.getOrElse
import com.theendercore.data.ModManger
import com.theendercore.data.RunData
import com.theendercore.data.modManager
import kotlinx.serialization.decodeFromString
import masecla.modrinth4j.client.agent.UserAgent
import masecla.modrinth4j.main.ModrinthAPI


fun dependencies(): Dependencies {
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
        modManager()
    )
}

data class Dependencies(val api: ModrinthAPI, val runData: RunData?, val modManager: ModManger)
