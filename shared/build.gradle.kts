import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.vanniktech.publish)
    id("signing")
}

kotlin {
    jvm()

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "KMPNetworkMonitor"
            isStatic = true
        }
    }

    androidLibrary {
        namespace = "io.github.froyder.networkmonitor"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

val localProps = gradleLocalProperties(rootDir, providers)

group = "io.github.froyder"
version = "1.0.1"

signing {
    val signingKeyId = localProps.getProperty("signing.keyId") ?: ""
    val signingPassword = localProps.getProperty("signing.password") ?: ""
    val signingSecretKeyFile = localProps.getProperty("signing.secretKeyFile") ?: ""

    if (signingKeyId.isNotEmpty() && signingSecretKeyFile.isNotEmpty()) {
        signing {
            useInMemoryPgpKeys(
                signingKeyId,
                file(signingSecretKeyFile).readText(),
                signingPassword
            )
        }
    }
}

mavenPublishing {
    publishToMavenCentral(com.vanniktech.maven.publish.SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()

    coordinates(
        groupId = "io.github.froyder",
        artifactId = "kmp-network-monitor",
        version = "1.0.1"
    )

    pom {
        name = "KMP Network Monitor"
        description = "A Kotlin Multiplatform library for observing network connectivity on Android and iOS"
        url = "https://github.com/Froyder/kmp-network-monitor"

        licenses {
            license {
                name = "Apache License 2.0"
                url = "https://www.apache.org/licenses/LICENSE-2.0"
            }
        }

        developers {
            developer {
                id = "froyder"
                name = "Ilia Khomutskikh"
                email = "homutskih@gmail.com"
            }
        }

        scm {
            url = "https://github.com/Froyder/kmp-network-monitor"
            connection = "scm:git:git://github.com/Froyder/kmp-network-monitor.git"
            developerConnection = "scm:git:ssh://git@github.com/Froyder/kmp-network-monitor.git"
        }
    }
}