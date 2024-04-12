package com.theendercore.data

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.raise.ensureNotNull
import com.theendercore.Database
import com.theendercore.log

sealed interface DomainError

data class GenericError(val msg: String = "", val trace: String = "") : DomainError
data class NoModFound(val msg: String) : DomainError
interface ModManger {
    fun getMod(id: String): Either<DomainError, Mod>
    fun getAllMods(): Either<DomainError, List<Mod>>
    fun deleteMod(mod: Mod): DomainError?
    fun addMod(mod: Mod): DomainError?
    fun updateMod(mod: Mod): DomainError?
    fun isModUpToDate(mod: Mod): Boolean


    fun getAllDependencies(): Either<DomainError, List<Dependency>>
    fun deleteDependency(dep: Dependency): DomainError?
    fun addDependency(dep: Dependency): DomainError?
    fun updateDependency(dep: Dependency): DomainError?
    fun isDependencyUpToDate(mod: Mod): Boolean

}

fun modManager(): ModManger {
    val driver = JdbcSqliteDriver("jdbc:sqlite:mods.db")
//    Database.Schema.create(driver)
    val database = Database(driver)
    val mods = database.modsQueries
    val dependencies = database.dependenciesQueries

    return object : ModManger {
        override fun getMod(id: String): Either<DomainError, Mod> = either {
            val mod = mods.getModById(id).executeAsOneOrNull()
            ensureNotNull(mod) { NoModFound("No mod found!") }
            Mod(mod)
        }

        override fun getAllMods(): Either<DomainError, List<Mod>> = either {
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
            val oldMod = mods.getModById(mod.projectId).executeAsOneOrNull() ?: return@catch false
            oldMod.latestVersion >= mod.latestVersion
        }.getOrElse { log.info(it.stackTraceToString()); false }

        override fun getAllDependencies(): Either<DomainError, List<Dependency>> = either {
            val modList = dependencies.selectAll().executeAsList()
            ensure(modList.isNotEmpty()) { GenericError("No Mods Found!") }
            modList.map { Dependency(it) }
        }

        override fun deleteDependency(dep: Dependency): DomainError? = Either
            .catch { dependencies.delete(dep.projectId) }
            .mapLeft { GenericError(it.message ?: "", it.stackTraceToString()) }
            .leftOrNull()


        override fun addDependency(dep: Dependency): DomainError? = Either
            .catch { dependencies.add(dep.toDependencies()) }
            .mapLeft { GenericError(it.message ?: "", it.stackTraceToString()) }
            .leftOrNull()


        override fun updateDependency(dep: Dependency): DomainError? = deleteDependency(dep) ?: addDependency(dep)


        override fun isDependencyUpToDate(mod: Mod): Boolean = Either.catch {
            val oldDep = dependencies.getDependencyById(mod.projectId).executeAsOneOrNull() ?: return@catch false
            oldDep.version == mod.latestVersion
        }.getOrElse { log.info(it.stackTraceToString()); false }
    }
}
