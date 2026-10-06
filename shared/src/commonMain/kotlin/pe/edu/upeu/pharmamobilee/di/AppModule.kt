package pe.edu.upeu.pharmamobilee.di

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import pe.edu.upeu.pharmamobilee.data.remote.ProductoApi
import pe.edu.upeu.pharmamobilee.data.remote.crearHttpClient
import pe.edu.upeu.pharmamobilee.data.repository.ProductoRepositorioEnMemoria
import pe.edu.upeu.pharmamobilee.data.repository.ProductoRepositoryImpl
import pe.edu.upeu.pharmamobilee.domain.repository.ProductoRepository
import pe.edu.upeu.pharmamobilee.domain.usecase.ActualizarProductoUseCase
import pe.edu.upeu.pharmamobilee.domain.usecase.EliminarProductoUseCase
import pe.edu.upeu.pharmamobilee.domain.usecase.ListarProductosUseCase
import pe.edu.upeu.pharmamobilee.domain.usecase.RegistrarProductoUseCase
import pe.edu.upeu.pharmamobilee.presentation.producto.ProductoViewModel

val dataModule = module {
    single { crearHttpClient(get(), get(named("urlBase"))) }
    single { ProductoApi(get()) }
    single { ProductoRepositorioEnMemoria() }
    single<ProductoRepository> {
        ProductoRepositoryImpl(
            api = get(),
            categoriaPorDefecto = 1L
        )
    }
}

val domainModule = module {
    factory {
        RegistrarProductoUseCase(get())
    }
    factory {
        ActualizarProductoUseCase(get())
    }
    factory {
        ListarProductosUseCase(get())
    }
    factory {
        EliminarProductoUseCase(get())
    }
}

val presentationModule = module {
    viewModel {
        ProductoViewModel(
            listarProductosUseCase = get(),
            registrarProductoUseCase = get(),
            actualizarProductoUseCase = get(),
            eliminarProductoUseCase = get(),
            compartidor = get()
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
