package di

import org.koin.dsl.module

fun appModule() = module {
    // appModule: refers to Shared module dependencies
    single { Sample(12) }
}

data class Sample(
    val id: Int = 1
)