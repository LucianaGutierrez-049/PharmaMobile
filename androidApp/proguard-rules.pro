# Reglas R8 de la versión de entrega (sesión 14).
# Se mantienen solo las clases que se resuelven en tiempo de ejecución.

# Trazas legibles en Crashlytics.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# kotlinx.serialization: anotaciones, firmas genéricas y serializadores generados
# de los DTO de transporte y de la Sesion guardada cifrada.
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
-keep,includedescriptorclasses class pe.edu.upeu.pharmamobilee.**$$serializer { *; }
-keepclassmembers class pe.edu.upeu.pharmamobilee.** {
    *** Companion;
}
-keepclasseswithmembers class pe.edu.upeu.pharmamobilee.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Ktor y OkHttp referencian clases opcionales de otras plataformas.
-dontwarn org.slf4j.**
-dontwarn java.lang.management.**
-dontwarn io.ktor.util.debug.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
