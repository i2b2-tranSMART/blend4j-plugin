package blend4j.plugin

import grails.test.mixin.TestFor
import spock.lang.Specification

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
@TestFor(RetrieveDataController)
class RetrieveDataControllerSpec extends Specification {

	void 'test JobExportToGalaxy'() {
		when:
		String nameOfTheExportJob = '!ExportJob!'
		String nameOfTheLibrary = '!Library!'
		params.nameOfTheExportJob = nameOfTheExportJob
		params.nameOfTheLibrary = nameOfTheLibrary

		String galaxyUrl = controller.galaxyUrl = '!galaxyUrl!'
		String tempFolderDirectory = controller.tempFolderDirectory = '!tempFolderDirectory!'

		boolean ok = false
		boolean saveCalled = false
		boolean uploadCalled = false
		String state
		controller.retrieveDataService = new RetrieveDataService() {

			boolean saveStatusOfExport(String exportJobName, String libraryName) {
				assert exportJobName == nameOfTheExportJob
				assert libraryName == libraryName
				saveCalled = true
				ok
			}

			void uploadExportFolderToGalaxy(String galaxyUrl2, String tempFolderDirectory2, String exportJobName, String libraryName) {
				assert galaxyUrl2 == galaxyUrl
				assert tempFolderDirectory2 == tempFolderDirectory
				assert exportJobName == nameOfTheExportJob
				assert libraryName == libraryName
				uploadCalled = true
			}

			boolean updateStatusOfExport(String exportJobName, String newState) {
				assert exportJobName == nameOfTheExportJob
				state = newState
				true
			}
		}

		controller.JobExportToGalaxy()

		then:
		saveCalled
		!uploadCalled
		state == 'Error'
		response.text == '{"statusOk":false}'

		when:
		ok = true
		saveCalled = false

		response.reset()
		controller.JobExportToGalaxy()

		then:
		saveCalled
		uploadCalled
		state == 'Done'
		response.text == '{"statusOk":true}'
	}

	void 'test getjobs'() {
		when:
		Map map = [foo: 'bar']
		controller.retrieveDataService = new RetrieveDataService() {
			Map getjobs() {
				map
			}
		}

		controller.getjobs()

		then:
		response.text == '{"foo":"bar"}'
	}
}
