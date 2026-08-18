plugins {
    `java-library`
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(
            libs.versions.java.get().toInt()
        )
    }
}
