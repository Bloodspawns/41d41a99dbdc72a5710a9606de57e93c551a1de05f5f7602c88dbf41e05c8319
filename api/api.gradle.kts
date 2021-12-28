plugins {
    java
}

group = "org.api.api"
version = "1.0"

//repositories {
//    ivy {
//        url = uri("https://github.com/")
//        patternLayout {
//            artifact("/[organisation]/c0603cb96187d5c295173c5c90d3b389671964dab55056f913c3d86c3333300b/releases/download/[revision]/[module].[ext]")
//        }
//        metadataSources { artifact() }
//    }
//    mavenCentral()
//}

dependencies {
    annotationProcessor(Libraries.lombok)

    compileOnly(Libraries.javax)
    compileOnly(Libraries.lombok)
    compileOnly(Libraries.orangeExtensions)

//    compileOnly("Bloodspawns:api-injected:1.0")

    implementation(Libraries.microsoft_alm_common)
    implementation(Libraries.microsoft_alm_storage)
    implementation(Libraries.jcodec_javase)
    implementation(Libraries.jcodec)
    implementation(Libraries.runelite_client)
    implementation(Libraries.logback)
    implementation(Libraries.gson)
    implementation(Libraries.guava)
    implementation(Libraries.guice)
    implementation(Libraries.h2)
    implementation(Libraries.rxrelay)
    implementation(Libraries.okhttp3)
    implementation(Libraries.rxjava)
    implementation(Libraries.jna)
    implementation(Libraries.jnaPlatform)
    implementation(Libraries.discord)
    implementation(Libraries.substance)
    implementation(Libraries.jopt)
    implementation(Libraries.apacheCommonsText)
    implementation(Libraries.plexus)
    implementation(Libraries.annotations)
    implementation(Libraries.sentry)
    implementation(Libraries.slf4jApi)
}

fun isBluePatch(name : String): Boolean {
    return Regex("blue-patch.jar").matches(name)
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_11
}

tasks.jar {
    archiveFileName.set("bluelite-api.jar")
}