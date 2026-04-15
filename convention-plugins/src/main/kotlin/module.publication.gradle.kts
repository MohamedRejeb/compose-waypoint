plugins {
    id("com.vanniktech.maven.publish")
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()

    coordinates(group.toString(), project.name, version.toString())

    pom {
        name.set("Waypoint")
        description.set("Compose Multiplatform product tour / showcase library")
        url.set("https://github.com/MohamedRejeb/Waypoint")

        licenses {
            license {
                name.set("Apache-2.0")
                url.set("https://opensource.org/licenses/Apache-2.0")
            }
        }

        developers {
            developer {
                id.set("MohamedRejeb")
                name.set("Mohamed Ben Rejeb")
                email.set("mohamedrejeb445@gmail.com")
            }
        }

        issueManagement {
            system.set("Github")
            url.set("https://github.com/MohamedRejeb/Waypoint/issues")
        }

        scm {
            connection.set("https://github.com/MohamedRejeb/Waypoint.git")
            url.set("https://github.com/MohamedRejeb/Waypoint")
        }
    }
}
