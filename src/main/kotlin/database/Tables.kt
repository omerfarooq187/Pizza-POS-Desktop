package database

import data.model.DiscountType
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Table



object Categories : Table("categories") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 50).uniqueIndex()

    override val primaryKey = PrimaryKey(id)
}

object MenuItems: IntIdTable("menu_items") {
    val categoryId = integer("category_id").references(Categories.id)
    val name = varchar("name", 100)
    val description = text("description").nullable()
    val isActive = bool("is_active").default(true)
    val discountType = enumerationByName("discount_type", 20, DiscountType::class).nullable()
    val discountValue = double("discount_value").nullable()
}

object ItemVariants: IntIdTable("item_variants") {
    val itemId = integer("item_id").references(MenuItems.id)
    val size = varchar("size", 20)
    val price = double("price")
    val memberPrice = double("member_price").nullable()
}