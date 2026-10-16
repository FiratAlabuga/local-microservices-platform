import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("org.springframework.boot") version "4.1.1" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
}

// ===== Merkezi sürüm tanımları =====
val javaVersion = 25
val springBootVersion = "4.1.1"
val flywayVersion = "11.9.1"
val testcontainersVersion = "1.21.1"
val logstashEncoderVersion = "8.1"

// Tüm subproject'lere uygulanacak ortak yapılandırma
subprojects {
    apply(plugin = "java")
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")

    group = "com.localplatform"
    version = "0.1.0-SNAPSHOT"

    configure<JavaPluginExtension> {
        toolchain {
            languageVersion = JavaLanguageVersion.of(javaVersion)
        }
    }

    repositories {
        mavenCentral()
    }

    dependencies {
        // Spring Boot starters — her servis kendi ihtiyacına göre ekler
        "annotationProcessor"("org.springframework.boot:spring-boot-configuration-processor")

        // Testler
        "testImplementation"("org.springframework.boot:spring-boot-starter-test")
    }

    // dependency-management BOM'larını import et
    the<io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension>().apply {
        imports {
            mavenBom("org.testcontainers:testcontainers-bom:$testcontainersVersion")
        }
        dependencies {
            dependency("net.logstash.logback:logstash-logback-encoder:$logstashEncoderVersion")
            dependency("org.flywaydb:flyway-core:$flywayVersion")
            dependency("org.flywaydb:flyway-database-postgresql:$flywayVersion")
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
        jvmArgs("--enable-preview")
    }

    tasks.withType<JavaCompile> {
        options.compilerArgs.add("--enable-preview")
        options.encoding = "UTF-8"
    }
}
