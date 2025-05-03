package database

import org.jetbrains.exposed.sql.Table

object CategoriesTable : Table("categories") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 50).uniqueIndex()

    override val primaryKey = PrimaryKey(id)
}