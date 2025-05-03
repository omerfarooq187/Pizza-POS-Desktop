package di

import data.repository.CategoryRepository
import data.repository.CategoryRepositoryImpl
import database.PosDatabase
import org.koin.dsl.module
import presentation.viewmodel.CategoryViewModel

val appModule = module {
    single { PosDatabase.init() }
    single<CategoryRepository> { CategoryRepositoryImpl() }
    single { CategoryViewModel() }
}
