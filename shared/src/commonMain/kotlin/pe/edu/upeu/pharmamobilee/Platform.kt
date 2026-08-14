package pe.edu.upeu.pharmamobilee

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform