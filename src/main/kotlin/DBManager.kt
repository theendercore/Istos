package com.theendercore

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.theendercore.data.Mod

sealed interface DomainError

data class GenericError(val msg: String = "", val trace: String = "") : DomainError
interface ModManger {
    fun getMods(): Either<DomainError, List<Mod>>
    fun deleteMod(mod: Mod): DomainError?
    fun addMod(mod: Mod): DomainError?
    fun updateMod(mod: Mod): DomainError?
    fun isModUpToDate(mod: Mod): Boolean

}

fun database(): ModManger {
    val driver = JdbcSqliteDriver("jdbc:sqlite:mods.db")
    Database.Schema.create(driver)
    val database = Database(driver)
    val mods = database.modsQueries

    return object : ModManger {
        override fun getMods(): Either<DomainError, List<Mod>> = either {
            val modList = mods.selectAll().executeAsList()
            ensure(modList.isNotEmpty()) { GenericError("No Mods Found!") }
            modList.map { Mod(it) }
        }

        override fun deleteMod(mod: Mod): DomainError? = Either
            .catch { mods.delete(mod.projectId) }
            .mapLeft { GenericError(it.message ?: "", it.stackTraceToString()) }
            .leftOrNull()

        override fun addMod(mod: Mod): DomainError? = Either
            .catch { mods.add(mod.toMods()) }
            .mapLeft { GenericError(it.message ?: "", it.stackTraceToString()) }
            .leftOrNull()

        override fun updateMod(mod: Mod): DomainError? = deleteMod(mod) ?: addMod(mod)

        override fun isModUpToDate(mod: Mod): Boolean = Either.catch {
            mods.getModById(mod.projectId).executeAsOneOrNull() != null
        }.getOrElse { log.info(it.stackTraceToString()); false }
    }
}
