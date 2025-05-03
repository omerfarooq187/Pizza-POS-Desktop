package data.repository

import data.model.Category
import database.CategoriesTable
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.neq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update


interface CategoryRepository {
    suspend fun getAllCategories(): List<Category>
    suspend fun createCategory(category: Category)
    suspend fun updateCategory(category: Category)
    suspend fun deleteCategory(categoryId: Int)
    suspend fun categoryExists(name: String, excludeId: Int? = null): Boolean
}


class CategoryRepositoryImpl: CategoryRepository {
    override suspend fun getAllCategories(): List<Category> = transaction {
        CategoriesTable.selectAll().map {
            Category(
                id = it[CategoriesTable.id],
                name = it[CategoriesTable.name]
            )
        }.toList()
    }

    override suspend fun createCategory(category: Category) {
        transaction {
            CategoriesTable.insert {
                it[name] = category.name
            }
        }
    }

    override suspend fun updateCategory(category: Category) {
        transaction {
            CategoriesTable.update({ CategoriesTable.id eq category.id}) {
                it[name] = category.name
            }
        }
    }

    override suspend fun deleteCategory(categoryId: Int) {
        transaction {
            CategoriesTable.deleteWhere {
                CategoriesTable.id eq categoryId
            }
        }
    }

    override suspend fun categoryExists(name: String, excludeId: Int?): Boolean = transaction {
        val query = CategoriesTable.name eq name
        val excludeQuery = excludeId?.let { CategoriesTable.id neq it }
        CategoriesTable.select {
            query and (excludeQuery?: Op.TRUE)
        }.count() > 0
    }

}