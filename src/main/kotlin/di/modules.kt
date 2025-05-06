package di

import data.repository.CategoryRepository
import data.repository.CategoryRepositoryImpl
import data.repository.MenuRepository
import data.repository.MenuRepositoryImpl
import database.PosDatabase
import org.koin.dsl.module
import presentation.viewmodel.CategoryViewModel
import presentation.viewmodel.MenuItemViewModel

val appModule = module {
    single(createdAtStart = true) {
        PosDatabase.init()
    }
    single<CategoryRepository> { CategoryRepositoryImpl() }
    single { CategoryViewModel() }
    single<MenuRepository> { MenuRepositoryImpl() }
    single { MenuItemViewModel() }
}
