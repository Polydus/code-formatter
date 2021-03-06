import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	maven
	id("org.springframework.boot") version "2.4.4"
	id("io.spring.dependency-management") version "1.0.11.RELEASE"
	kotlin("jvm") version "1.4.32"
	kotlin("plugin.spring") version "1.4.32"
	kotlin("plugin.serialization") version "1.4.32"
}

group = "com.polydus"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11


repositories {
	mavenCentral()
}

dependencies {
	//implementation("org.springframework.boot:spring-boot-starter-jdbc")
	implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

	implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.1.0")
	//reactive
	implementation("io.reactivex.rxjava3:rxkotlin:3.0.1")

	//bootstrap
	implementation("org.webjars:bootstrap:5.0.0-beta2")
	implementation("org.webjars:jquery:3.6.0")

	//validation
	implementation("org.springframework.boot:spring-boot-starter-validation")

	// jackson
	implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.11.4")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "11"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
