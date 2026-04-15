import com.vanniktech.maven.publish.SonatypeHost

plugins {
    id("com.vanniktech.maven.publish")
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()

    coordinates(project.group.toString(), project.name, project.version.toString())

    pom {
        name.set("Waypoint")
        description.set("Compose Multiplatform product tour / showcase library")
        inceptionYear.set("2025")
        url.set("https://github.com/MohamedRejeb/Waypoint")

        licenses {
            license {
                name.set("Apache-2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("repo")
            }
        }

        developers {
            developer {
                id.set("MohamedRejeb")
                name.set("Mohamed Ben Rejeb")
                url.set("https://github.com/MohamedRejeb")
            }
        }

        scm {
            url.set("https://github.com/MohamedRejeb/Waypoint")
            connection.set("scm:git:git://github.com/MohamedRejeb/Waypoint.git")
            developerConnection.set("scm:git:ssh://git@github.com/MohamedRejeb/Waypoint.git")
        }

        issueManagement {
            system.set("GitHub Issues")
            url.set("https://github.com/MohamedRejeb/Waypoint/issues")
        }
    }
}
