package pe.edu.upeu.pharmamobilee.di

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import pe.edu.upeu.pharmamobilee.data.repository.ProductoRepositorioEnMemoria
import pe.edu.upeu.pharmamobilee.domain.repository.ProductoRepository
import pe.edu.upeu.pharmamobilee.domain.usecase.RegistrarProductoUseCase
import pe.edu.upeu.pharmamobilee.presentation.producto.ProductoViewModel

val dataModule = module {
    single<ProductoRepository> {
        ProductoRepositorioEnMemoria()
    }
}

val domainModule = module {
    factory {
        RegistrarProductoUseCase(get())
    }
}

val presentationModule = module {
    viewModel {
        ProductoViewModel(
            registrarProductoUseCase = get(),
            productoRepository = get()
        )
    }
}

expect val platformModule: Module

fun initKoin(
    config: KoinApplication.() -> Unit = {}
) {
    startKoin {
        config()
        modules(
            dataModule,
            domainModule,
            presentationModule,
            platformModule
        )
    }
}
