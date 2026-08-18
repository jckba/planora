import java.io.File

plugins {
    java
    application

    alias(libs.plugins.spring.boot)
    alias(libs.plugins.jooq)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(
            libs.versions.java.get().toInt()
        )
    }
}

val envProperties = File(rootProject.projectDir, "infra/docker/.env")
    .readLines()
    .filter { it.isNotBlank() && !it.trimStart().startsWith("#") }
    .map { it.split("=", limit = 2) }
    .filter { it.size == 2 }
    .associate { it[0].trim() to it[1].trim() }

fun env(name: String): String =
    System.getenv(name)
        ?: envProperties[name]
        ?: error("Environment variable $name is not set")

jooq {
    configuration {
        jdbc {
            driver = "org.postgresql.Driver"
            url = "jdbc:postgresql://${env("POSTGRES_HOST")}:${env("POSTGRES_PORT")}/${env("POSTGRES_DB")}"
            user = env("POSTGRES_USER")
            password = env("POSTGRES_PASSWORD")
        }
        generator {
            database {
                name = "org.jooq.meta.postgres.PostgresDatabase"
                inputSchema = "planora"

                includes = ".*"
                excludes = "flyway_schema_history"

                isTableValuedFunctions = false

                forcedTypes {
                    forcedType {
                        name = "VARCHAR"
                        includeTypes = "(?i:.*citext.*)"
                    }
                }
            }
            target {
                packageName = "com.planora.persistence.jooq"
                directory = layout.buildDirectory
                    .dir("generated-src/jooq")
                    .get()
                    .asFile
                    .absolutePath
            }
            generate {
                records = true
                daos = false
                pojos = false
            }

        }
    }

}

sourceSets {
    main {
        java.srcDir(layout.buildDirectory.dir("generated-src/jooq"))
    }
}

val mockitoAgent = configurations.create("mockitoAgent")

dependencies {
    implementation(project(":database"))

    compileOnly(libs.lombok)
    implementation(platform(libs.spring.bom))

    implementation(libs.spring.jdbc)
    implementation(libs.spring.web)
    implementation(libs.spring.validation)
    implementation(libs.spring.flyway)

    implementation(libs.spring.jooq)
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    jooqCodegen(libs.postgresql)

    runtimeOnly(libs.flyway.postgresql)
    runtimeOnly(libs.postgresql)

    annotationProcessor(libs.lombok)

    testCompileOnly(libs.lombok)
    testAnnotationProcessor(libs.lombok)
    testImplementation(platform(libs.spring.bom))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")

    mockitoAgent("org.mockito:mockito-core:5.21.0") {
        isTransitive = false
    }

}

tasks.named<org.springframework.boot.gradle.tasks.run.BootRun>("bootRun") {
    environment("POSTGRES_HOST", env("POSTGRES_HOST"))
    environment("POSTGRES_PORT", env("POSTGRES_PORT"))
    environment("POSTGRES_DB", env("POSTGRES_DB"))
    environment("POSTGRES_USER", env("POSTGRES_USER"))
    environment("POSTGRES_PASSWORD", env("POSTGRES_PASSWORD"))
}


tasks.compileJava {
    dependsOn(tasks.jooqCodegen)
}


tasks.test {
    environment("POSTGRES_HOST", env("POSTGRES_HOST"))
    environment("POSTGRES_PORT", env("POSTGRES_PORT"))
    environment("POSTGRES_DB", env("POSTGRES_DB"))
    environment("POSTGRES_USER", env("POSTGRES_USER"))
    environment("POSTGRES_PASSWORD", env("POSTGRES_PASSWORD"))

    useJUnitPlatform()
    jvmArgs("-javaagent:${mockitoAgent.asPath}")
}
