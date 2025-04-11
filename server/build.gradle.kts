plugins {
    application
}

dependencies {
    implementation(project(":common"))
    implementation(kotlin("stdlib"))
}

application {
    mainClass.set("gg.scala.cgs.server.StartCGS")
}