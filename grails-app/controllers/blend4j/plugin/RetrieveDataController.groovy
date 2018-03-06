package blend4j.plugin

import grails.converters.JSON
import org.springframework.beans.factory.annotation.Value

class RetrieveDataController {

	RetrieveDataService retrieveDataService
	def springSecurityService

	@Value('${com.galaxy.blend4j.galaxyURL:}')
	private String galaxyUrl

	@Value('${com.recomdata.plugins.tempFolderDirectory:}')
	private String tempFolderDirectory

	def JobExportToGalaxy(String nameOfTheExportJob, String nameOfTheLibrary) {
		boolean ok = retrieveDataService.saveStatusOfExport(nameOfTheExportJob, nameOfTheLibrary)
		if (ok) {
			retrieveDataService.uploadExportFolderToGalaxy(galaxyUrl, tempFolderDirectory,
					springSecurityService.principal.username, nameOfTheExportJob, nameOfTheLibrary)
			retrieveDataService.updateStatusOfExport(nameOfTheExportJob, 'Done')
		}
		else {
			retrieveDataService.updateStatusOfExport(nameOfTheExportJob, 'Error')
		}

		render([statusOk: ok] as JSON)
	}

	/**
	 * Get the jobs to show in the galaxy jobs tab
	 */
	def getjobs() {
		Map jobs = retrieveDataService.getjobs(springSecurityService.principal.username, 'DataExport')
		render(jobs as JSON)
	}
}
