plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization") version "1.9.0"
    id("com.vanniktech.maven.publish")
}

val baseArtifactId: String by project
val artifactId: String = "$baseArtifactId-solana"

kotlin {
    jvm {
        jvmToolchain(11)
        withJava()
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
        macosX64(),
        macosArm64()
    ).forEach {
        it.binaries.framework {
            baseName = artifactId
        }
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0-RC")
                implementation(project(mapOf("path" to ":core")))
                implementation("io.github.funkatronics:multimult:0.2.0")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
//                implementation("io.github.funkatronics:kborsh:0.1.0")
            }
        }
    }
}

mavenPublishing {
    coordinates(group as String, artifactId, version as String)
}
