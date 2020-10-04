import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

version = "1.0-SNAPSHOT"

plugins {
    idea
    application
    kotlin("jvm") version "1.4.10"
    id("org.jlleitschuh.gradle.ktlint") version "9.4.0"
    id("org.openjfx.javafxplugin") version "0.0.9"
}

javafx {
    version = "15"
    modules("javafx.controls", "javafx.fxml")
}

application {
    mainClassName = "com.xhstormr.app.MainKt"
}

repositories {
    maven("https://mirrors.huaweicloud.com/repository/maven")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-javafx:+")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:+")

    implementation("org.springframework:spring-webflux:+")
    implementation("io.projectreactor.netty:reactor-netty:+")
}

tasks {
    val exe by creating(JavaExec::class) {
        dependsOn("jar")
        buildDir.resolve("bin").mkdirs()
        val launch4jJar = "${ext["launch4j_home"]}/launch4j.jar"
        val launch4jCfg = "$rootDir/assets/config.xml"
        classpath = files(launch4jJar)
        args = listOf(launch4jCfg)
    }

    withType<Jar> {
        manifest.attributes["Main-Class"] = application.mainClassName
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        from(configurations.runtimeClasspath.get().map { zipTree(it) })
        exclude("**/*.kotlin_module")
        exclude("**/*.kotlin_metadata")
        exclude("**/*.kotlin_builtins")
    }

    withType<Wrapper> {
        gradleVersion = "6.6"
        distributionType = Wrapper.DistributionType.ALL
    }

    withType<Test> {
        useJUnitPlatform()
    }

    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "14"
            freeCompilerArgs = listOf("-Xjsr305=strict", "-Xjvm-default=enable")
        }
    }

    withType<JavaCompile> {
        options.encoding = Charsets.UTF_8.name()
        options.isFork = true
        options.isIncremental = true
        sourceCompatibility = JavaVersion.VERSION_14.toString()
        targetCompatibility = JavaVersion.VERSION_14.toString()
    }
}
