grails.project.work.dir = 'target'

grails.project.dependency.resolver = 'maven'
grails.project.dependency.resolution = {

	inherits 'global'
	log 'warn'

	repositories {
		mavenLocal() // Note: use 'grails maven-install' to install required plugins locally
		grailsCentral()
		mavenCentral()
		mavenRepo 'https://repo.transmartfoundation.org/content/repositories/public/'
	}

	dependencies {
		compile 'com.github.jmchilton.blend4j:blend4j:0.1.2'
		compile 'org.json:json:20090211'
	}

	plugins {
		compile ':transmart-legacy-db:18.1-SNAPSHOT'

		build ':release:3.1.2', ':rest-client-builder:2.1.1', {
			export = false
		}
	}
}
