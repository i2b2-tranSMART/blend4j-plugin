class Blend4jPluginGrailsPlugin {
	def version = '18.1-SNAPSHOT'
	def grailsVersion = '2.3 > *'
	def title = 'Blend4j Plugin'
	def author = 'Transmart Foundation'
	def authorEmail = 'admin@transmartproject.org'
	def description = 'TODO'
	def documentation = 'TODO'
	def license = 'GPL3'
	def organization = [name: 'TODO', url: 'TODO']
	def developers = [
		[name: 'Axel Oehmichen', email: 'ao1011@imperial.ac.uk'],
		[name: 'Burt Beckwith', email: 'burt_beckwith@hms.harvard.edu']
	]
	def issueManagement = [system: 'TODO', url: 'TODO']
	def scm = [url: 'https://github.com/transmart/transmart-extensions']

	def doWithApplicationContext = { ctx ->
		boolean galaxyEnabled = application.config.com.galaxy.blend4j.galaxyEnabled
		if (galaxyEnabled && ctx.containsBean('transmartExtensionsRegistry')) {
            ctx.transmartExtensionsRegistry.registerAnalysisTabExtension(
            	'blend4j-plugin',
            	'/Blend4jPlugin/loadScripts',
            	'addGalaxyPanel')
		}
	}
}
