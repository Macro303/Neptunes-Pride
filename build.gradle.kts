import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
	id("application")
	kotlin("jvm") version "1.4.32"
	id("com.github.ben-manes.versions") version "0.39.0"
	id("com.github.johnrengelman.shadow") version "6.1.0"
}

repositories {
	mavenCentral()
	mavenLocal()
	jcenter()
}

dependencies {
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation(group = "org.yaml", name="snakeyaml", version="1.28")
	implementation(group = "com.konghq", name="unirest-java", version="3.11.11")
	runtimeOnly(group = "org.xerial", name="sqlite-jdbc", version="3.34.0")

	//Ktor
	val ktorVersion = "1.5.2"
	implementation(group = "io.ktor", name="ktor-server-netty", version=ktorVersion)
	implementation(group = "io.ktor", name="ktor-gson", version=ktorVersion)

	//Exposed
	val exposedVersion = "0.29.1"
	implementation(group = "org.jetbrains.exposed", name = "exposed-core", version = exposedVersion)
	implementation(group = "org.jetbrains.exposed", name = "exposed-dao", version = exposedVersion)
	implementation(group = "org.jetbrains.exposed", name = "exposed-jdbc", version = exposedVersion)
	implementation(group = "org.jetbrains.exposed", name = "exposed-java-time", version = exposedVersion)

	//Log4j
	val logVersion = "2.14.1"
	implementation(group = "org.apache.logging.log4j", name = "log4j-api", version = logVersion)
	runtimeOnly(group = "org.apache.logging.log4j", name = "log4j-slf4j-impl", version = logVersion)
}

application {
	mainClass.set("macro.dashboard.Server")
	mainClassName = mainClass.get()
	applicationName = "Neptunes-Dashboard"
}

tasks.jar {
	manifest {
		attributes(
			"Main-Class" to application.mainClass,
			"Multi-Release" to true
		)
	}
}

tasks.compileKotlin {
	sourceCompatibility = JavaVersion.VERSION_1_8.toString()
	targetCompatibility = JavaVersion.VERSION_1_8.toString()

	kotlinOptions.jvmTarget = JavaVersion.VERSION_1_8.toString()
	kotlinOptions.apiVersion = "1.4"
	kotlinOptions.languageVersion = "1.4"
}

tasks {
	named<ShadowJar>("shadowJar") {
		archiveBaseName.set("Neptunes-Dashboard")
	}
}

fun isNonStable(version: String): Boolean {
	val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.toUpperCase().contains(it) }
	val regex = "^[0-9,.v-]+(-r)?$".toRegex()
	val isStable = stableKeyword || regex.matches(version)
	return isStable.not()
}

tasks.named("dependencyUpdates", DependencyUpdatesTask::class.java).configure {
	rejectVersionIf {
		isNonStable(candidate.version)
	}
	rejectVersionIf {
		isNonStable(candidate.version) && !isNonStable(currentVersion)
	}
	resolutionStrategy {
		componentSelection {
			all {
				if (isNonStable(candidate.version) && !isNonStable(currentVersion)) {
					reject("Release candidate")
				}
			}
		}
	}
}