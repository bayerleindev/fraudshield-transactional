plugins {
	java
	id("org.springframework.boot") version "3.3.5"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.fraudshield"
version = "0.0.1-SNAPSHOT"
description = "Transactional anti-fraud decisioning backend"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

val testcontainersVersion = "2.0.5"

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.flywaydb:flyway-core")
	implementation("org.flywaydb:flyway-database-postgresql")
	runtimeOnly("org.postgresql:postgresql")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation(platform("org.testcontainers:testcontainers-bom:$testcontainersVersion"))
	testImplementation("org.testcontainers:testcontainers-junit-jupiter")
	testImplementation("org.testcontainers:testcontainers-postgresql")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

configurations.matching { it.name.startsWith("test") }.configureEach {
	resolutionStrategy.eachDependency {
		if (requested.group == "org.testcontainers") {
			useVersion(testcontainersVersion)
			because("Keep Testcontainers modules aligned for Docker Desktop 29 API compatibility in integration tests.")
		}
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
	environment("DOCKER_API_VERSION", "1.41")
}

tasks.test {
	exclude(
		"**/*IT.class",
		"**/PersistenceRepositoryTest.class",
		"**/TransactionEvaluationControllerTest.class"
	)

	useJUnitPlatform {
		excludeTags("integration")
	}
}

tasks.register<Test>("integrationTest") {
	description = "Runs integration tests that require Docker/Testcontainers."
	group = "verification"

	testClassesDirs = sourceSets.test.get().output.classesDirs
	classpath = sourceSets.test.get().runtimeClasspath
	shouldRunAfter(tasks.test)
	outputs.upToDateWhen { false }

	useJUnitPlatform {
		includeTags("integration")
	}
}
