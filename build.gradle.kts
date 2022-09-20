import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "2.5.5"
	id("io.spring.dependency-management") version "1.0.11.RELEASE"
	kotlin("jvm") version "1.5.31"
	kotlin("plugin.spring") version "1.5.31"
}

group = "dev.przbetkier"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-neo4j:2.7.0")
	implementation("org.springframework.boot:spring-boot-starter-web:2.7.0")
	implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server:2.7.0")

	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("io.github.microutils:kotlin-logging:1.8.3")


	implementation("com.google.firebase:firebase-admin:8.2.0")
	implementation("io.minio:minio:8.4.3") {
		exclude(group = "com.squareup.okhttp3", module = "okhttp")
	}
	implementation("com.squareup.okhttp3:okhttp:4.9.3")

	testImplementation("org.amshove.kluent:kluent:1.68")
	testImplementation("org.springframework.boot:spring-boot-starter-test:2.5.5")
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
