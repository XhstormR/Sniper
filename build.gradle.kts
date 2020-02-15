import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

version = "1.0-SNAPSHOT"

plugins {
    idea
    application
    kotlin("jvm") version "1.3.61"
    id("org.jlleitschuh.gradle.ktlint") version "9.1.1"
    id("org.openjfx.javafxplugin") version "0.0.8"
}

javafx {
    version = "13.0.2"
    modules("javafx.controls", "javafx.fxml")
}

application {
    mainClassName = "com.xhstormr.app.MainKt"
}

repositories {
    maven("https://maven.aliyun.com/repository/jcenter")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-javafx:1.3.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.3.3")

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
        from(configurations.runtimeClasspath.get().map { zipTree(it) })
    }

    withType<Wrapper> {
        gradleVersion = "6.1.1"
        distributionType = Wrapper.DistributionType.ALL
    }

    withType<Test> {
        useJUnitPlatform()
    }

    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "12"
            freeCompilerArgs = listOf("-Xjsr305=strict", "-Xjvm-default=enable")
        }
    }

    withType<JavaCompile> {
        options.encoding = Charsets.UTF_8.name()
        options.isFork = true
        options.isIncremental = true
        sourceCompatibility = JavaVersion.VERSION_12.toString()
        targetCompatibility = JavaVersion.VERSION_12.toString()
    }
}
