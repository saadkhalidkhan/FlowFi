plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.google.devtools.ksp)
    alias(libs.plugins.dokka)
    alias(libs.plugins.maven.publish)
}

android {
    namespace = "com.example.flowfi.core"
    compileSdk = 36

    defaultConfig {
        minSdk = 26
        consumerProguardFiles("consumer-rules.pro")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    api(libs.androidx.room.runtime)
    api(libs.androidx.room.ktx)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.core)
    ksp(libs.androidx.room.compiler)
}

mavenPublishing {
    coordinates(
        groupId = findProperty("GROUP") as String? ?: "io.github.saadkhalidkhan",
        artifactId = findProperty("POM_ARTIFACT_ID") as String? ?: "flowfi-core",
        version = findProperty("VERSION_NAME") as String? ?: "1.0.0"
    )
    pom {
        name.set("FlowFi Core")
        description.set("Room-backed data layer for FlowFi personal finance tracking.")
        inceptionYear.set("2026")
        url.set("https://github.com/saadkhalidkhan/FlowFi")
        licenses {
            license {
                name.set("MIT License")
                url.set("https://opensource.org/licenses/MIT")
            }
        }
        developers {
            developer {
                id.set("saadkhalidkhan")
                name.set("Saad Khalid Khan")
                url.set("https://github.com/saadkhalidkhan")
            }
        }
        scm {
            connection.set("scm:git:git://github.com/saadkhalidkhan/FlowFi.git")
            developerConnection.set("scm:git:ssh://github.com/saadkhalidkhan/FlowFi.git")
            url.set("https://github.com/saadkhalidkhan/FlowFi")
        }
    }
    publishToMavenCentral()
    signAllPublications()
}
