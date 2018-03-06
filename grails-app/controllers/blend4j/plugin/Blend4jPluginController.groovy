package blend4j.plugin

import grails.converters.JSON

class Blend4jPluginController {

	/**
	 * The path to javascript resources such that the plugin can be loaded in the datasetExplorer
	 */
	def loadScripts() {
		List rows = [[path: servletContext.contextPath + pluginContextPath + '/js/galaxyExport.js', type: 'script']]
		render([success: true, totalCount: 1, files: rows] as JSON)
	}
}
