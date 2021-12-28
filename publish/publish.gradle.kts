plugins {
    java
}

group = "com.api.publish"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    annotationProcessor(Libraries.lombok)

    implementation(Libraries.guava)
    implementation(Libraries.gson)
    compileOnly(Libraries.lombok)
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

tasks {
    register<JavaExec>("Publish.main()") {
        group = "run"

        classpath = project.sourceSets.main.get().runtimeClasspath
        main = "Publish"
        args = listOf("../api/build/libs/bluelite-api.jar")
    }
}