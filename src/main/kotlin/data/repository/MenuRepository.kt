package data.repository

import data.model.ItemVariant
import data.model.MenuItem
import database.ItemVariants
import database.MenuItems
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

interface MenuRepository {
    suspend fun getItemsByCategory(categoryId: Int): List<MenuItem>
    suspend fun createItem(item: MenuItem)
    suspend fun updateItem(item: MenuItem)
    suspend fun deleteItem(itemId: Int)
    suspend fun toggleItemActive(itemId: Int)
    suspend fun itemExists(name: String, categoryId: Int): Boolean
}

class MenuRepositoryImpl: MenuRepository {
    override suspend fun getItemsByCategory(categoryId: Int): List<MenuItem> {
        return transaction {
            MenuItems
                .select { MenuItems.categoryId eq categoryId }
                .map { menuItemRow->
                    val variants = ItemVariants
                        .select { ItemVariants.itemId eq  menuItemRow[MenuItems.id].value}
                        .map { variantRow->
                            ItemVariant(
                                size = variantRow[ItemVariants.size],
                                price = variantRow[ItemVariants.price],
                                memberPrice = variantRow[ItemVariants.memberPrice]
                            )
                        }

                    MenuItem(
                        id = menuItemRow[MenuItems.id].value,
                        categoryId = menuItemRow[MenuItems.categoryId],
                        name = menuItemRow[MenuItems.name],
                        description = menuItemRow[MenuItems.description],
                        variants = variants,
                        isActive = menuItemRow[MenuItems.isActive]
                    )
                }
        }
    }

    override suspend fun createItem(item: MenuItem) {
        transaction {
            val itemId = MenuItems.insert {
                it[categoryId] = item.categoryId
                it[name] = item.name
                it[description] = item.description
                it[isActive] = item.isActive
                it[discountType] = item.discountType
                it[discountValue] = item.discountValue
            } get MenuItems.id

            item.variants.forEach { variant ->
                ItemVariants.insert {
                    it[ItemVariants.itemId] = itemId.value
                    it[size] = variant.size
                    it[price] = variant.price
                    it[memberPrice] = variant.memberPrice
                }
            }
        }
    }

    override suspend fun updateItem(item: MenuItem) {
        transaction {
            // Update the main MenuItem
            MenuItems.update({ MenuItems.id eq item.id }) {
                it[categoryId] = item.categoryId
                it[name] = item.name
                it[description] = item.description
                it[isActive] = item.isActive
                it[discountType] = item.discountType
                it[discountValue] = item.discountValue
            }

            // Delete old variants
            ItemVariants.deleteWhere { ItemVariants.itemId eq item.id }

            // Insert new variants
            item.variants.forEach { variant ->
                ItemVariants.insert {
                    it[itemId] = item.id
                    it[size] = variant.size
                    it[price] = variant.price
                    it[memberPrice] = variant.memberPrice
                }
            }
        }
    }

    override suspend fun deleteItem(itemId: Int) {
        transaction {
            // First delete item variants
            ItemVariants.deleteWhere { ItemVariants.itemId eq itemId }

            // Then delete the menu item
            MenuItems.deleteWhere { MenuItems.id eq itemId }
        }
    }



    override suspend fun toggleItemActive(itemId: Int) {
        transaction {
            val currentStatus = MenuItems
                .select { MenuItems.id eq itemId }
                .singleOrNull()?.get(MenuItems.isActive) ?: return@transaction

            MenuItems.update({ MenuItems.id eq itemId }) {
                it[isActive] = !currentStatus
            }
        }
    }


    override suspend fun itemExists(name: String, categoryId: Int): Boolean {
        return transaction {
            MenuItems.select {
                (MenuItems.name eq name) and
                        (MenuItems.categoryId eq categoryId)
            }.count() > 0
        }
    }

}