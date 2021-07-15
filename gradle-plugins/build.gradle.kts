plugins {
    kotlin("jvm")
    `kotlin-dsl`
    id("java-gradle-plugin")
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(kotlin("gradle-plugin"))
}

gradlePlugin {
    plugins {
        create("incompleteLibProducer") {
            id = "com.h0tk3y.incomplete.producer"
            implementationClass = "com.h0tk3y.incomplete.IncompleteLibProducerPlugin"
        }
        create("completingLibProducer") {
            id = "com.h0tk3y.incomplete.completing"
            implementationClass = "com.h0tk3y.incomplete.CompletingLibProducerPlugin"
        }
        create("incompleteLibsConsumer") {
            id = "com.h0tk3y.incomplete.consumer"
            implementationClass = "com.h0tk3y.incomplete.IncompleteLibConsumerPlugin"
        }
    }
}