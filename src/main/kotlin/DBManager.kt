package com.theendercore

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.theendercore.data.Mod
import com.theendercore.data.SimpleMod
import io.github.z4kn4fein.semver.toVersion
import java.sql.DriverManager
import java.sql.SQLException
import java.sql.Statement

object DBManager {

    private const val MODS_TABLE = "mods"
    fun init() {
        connect {
            val sql = """
            create table if not exists $MODS_TABLE (
                slug text,
                title text,
                description text,
                categories text,
                clientSide text,
                serverSide text,
                projectType text,
                downloads integer,
                iconUrl text,
                projectId text,
                author text,
                displayCategories text,
                versions text,
                follows integer,
                dateCreated text,
                dateModified text,
                latestVersion text,
                license text,
                gallery text
            );
        """
            println(sql)
            it.execute(sql)
        }
    }

    fun Mod.isUpToDate(): Boolean {
        return connect("testing dates?") {
            val sql = "select latestVersion from $MODS_TABLE where projectId=${this.projectId};"
            println(sql)
            val query = it.executeQuery(sql)
            return@connect if (query.next()) {
                val dbVersion = query.getString("latestVersion") ?: return@connect null
                return@connect dbVersion.toVersion() <= this.latestVersion.toVersion()
            } else null
        } ?: false
    }

    fun updateMod(mod: Mod) {
        log.debug("update")
        deleteMod(mod)
        addMod(mod)
    }

    fun addMod(mod: Mod) {
        val simpleMod = SimpleMod(mod)
        log.info("adding")
        connect("Add") {
            val sql =
                """
                insert into $MODS_TABLE(slug, title, description, categories, clientSide, serverSide, projectType, downloads, iconUrl, projectId, author, displayCategories, versions, follows, dateCreated, dateModified, latestVersion, license, gallery)
                values(
               "${simpleMod.slug}",
                "${simpleMod.title}",
                "${simpleMod.description}",
                "${simpleMod.categories}",
                "${simpleMod.clientSide}",
                "${simpleMod.serverSide}",
                "${simpleMod.projectType}",
                ${simpleMod.downloads},
                "${simpleMod.iconUrl}",
                "${simpleMod.projectId}",
                "${simpleMod.author}",
                "${simpleMod.displayCategories}",
                "${simpleMod.versions}",
                ${simpleMod.follows},
                "${simpleMod.dateCreated}",
                "${simpleMod.dateModified}",
                "${simpleMod.latestVersion}",
                "${simpleMod.license}",
                "${simpleMod.gallery}"
                );
            """.trimIndent()
            println(sql)
            it.execute(sql)
        }
    }

    fun deleteMod(mod: Mod) {
        log.info("deleting")
        connect("Delete") {
            val sql = "delete from $MODS_TABLE where projectId=${mod.projectId};"
            println(sql)
            it.execute(sql)
        }
    }


    private fun <C> connect(name: String = "", callback: (statement: Statement) -> C): C? {
        var x: C? = null
        Either.catch {
            Class.forName("org.sqlite.JDBC")
            DriverManager.getConnection("jdbc:sqlite:./mods.db").use { con ->
                con.createStatement().use {
                    x = callback(it)
                }
            }
        }.getOrElse {
            if (name.isNotBlank()) log.warn("Running $name")
            when (it) {
                is ClassNotFoundException -> log.error("Could not load Driver! {}", it.message)
                is SQLException -> log.error("SQL Error {}", it.message)
                else -> log.error(it.toString())
            }
            it.printStackTrace()
        }
        return x
    }
}

sealed interface DomainError

data class GenericError(val msg: String, val trace: String = "") : DomainError
interface ModManger {
    fun getMods(): Either<DomainError, List<Mod>>

}

fun database(): ModManger {
    val database = Database(JdbcSqliteDriver("jdbc:sqlite:test.db"))
    val mods = database.modsQueries
//    database.Mods


    return object : ModManger {
        override fun getMods(): Either<DomainError, List<Mod>> = either {
            val modList = mods.selectAll().executeAsList()
            ensure(modList.isNotEmpty()) { GenericError("No Mods Found!") }
            modList.map { Mod(it) }
        }
    }
}
