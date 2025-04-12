plugins {
    application
}

dependencies {
    implementation(project(":common"))
    implementation("gg.scala.commons:store:3.4.5")
    implementation("gg.scala.commons:serializers:3.4.5")
    implementation("gg.scala.commons:core:3.4.5")
    implementation("gg.scala.commons:commons:3.4.5")
    implementation("com.google.code.gson:gson:2.9.1")
    implementation("commons-io:commons-io:2.11.0")
    implementation("org.yaml:snakeyaml:2.4")
    implementation(kotlin("stdlib"))
}

application {
    mainClass.set("gg.scala.cgs.server.StartCGS")
}