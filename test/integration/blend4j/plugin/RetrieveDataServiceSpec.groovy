package blend4j.plugin

import com.github.jmchilton.blend4j.galaxy.beans.FileLibraryUpload
import com.recomdata.transmart.domain.i2b2.AsyncJob

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
class RetrieveDataServiceSpec extends AbstractIntegrationSpec {

	RetrieveDataService retrieveDataService

	void 'test saveStatusOfExport'() {
		expect:
		!StatusOfExport.count()

		when:
		String exportJobName = '!exportJobName!'
		String libraryName = '!libraryName!'

		then:
		!retrieveDataService.saveStatusOfExport(null, libraryName)
		!StatusOfExport.count()

		!retrieveDataService.saveStatusOfExport(exportJobName, null)
		!StatusOfExport.count()

		retrieveDataService.saveStatusOfExport exportJobName, libraryName

		when:
		flush()

		then:
		StatusOfExport.count() == 1
	}

	void 'test updateStatusOfExport'() {
		when:
		String exportJobName = '!exportJobName!'
		String newState = '!state!'

		StatusOfExport statusOfExport1 = new StatusOfExport(
				jobName: exportJobName, jobStatus: 's1',
				lastExportName: 'e1', lastExportTime: new Date()).save()

		StatusOfExport statusOfExport2 = new StatusOfExport(
				jobName: exportJobName, jobStatus: 's2',
				lastExportName: 'e2', lastExportTime: new Date()).save()

		StatusOfExport statusOfExport3 = new StatusOfExport(
				jobName: exportJobName + '_not_', jobStatus: 's3',
				lastExportName: 'e3', lastExportTime: new Date()).save()

		then:
		statusOfExport1
		statusOfExport2
		statusOfExport3
		!retrieveDataService.updateStatusOfExport(exportJobName, null)
		retrieveDataService.updateStatusOfExport exportJobName, newState
		statusOfExport2.jobStatus == newState
	}

	void 'test uploadExportFolderToGalaxy'() {
		when:
		String galaxyKey = '!galaxyKey!'
		String mailAddress = '!mailAddress!'
		String username = '!username!'
		new GalaxyUserDetails(galaxyKey: galaxyKey, mailAddress: mailAddress, username: username).save()
		flush()
		authenticateAs username

		String galaxyUrl = '!galaxyUrl!'
		File tempDir = File.createTempDir('uploadExportFolderToGalaxy', 'tmp')
		String tempFolderDirectory = tempDir.path

		String exportJobName = '_exportJobName_'
		String libraryName = '!libraryName!'

		File exportFiles = new File(tempFolderDirectory, exportJobName)
		assert exportFiles.mkdirs()
		new File(exportFiles, 'foo.txt').text = 'foo'
		new File(exportFiles, 'bar.txt').text = 'bar'

		retrieveDataService.uploadExportFolderToGalaxy galaxyUrl, tempFolderDirectory, exportJobName, libraryName
		List<FileLibraryUpload> uploaded = retrieveDataService.uploaded

		then:
		uploaded.size() == 2
		uploaded*.name.sort() == ['bar.txt', 'foo.txt']

		cleanup:
		tempDir.deleteDir()
	}

	void 'test getjobs'() {
		when:
		new AsyncJob(altViewerURL: 'altViewerURL0', jobName: 'j0', jobStatus: 's0').save()
		new AsyncJob(altViewerURL: 'altViewerURL1', jobName: 'null1', jobStatus: 's1',
				jobType: 'DataExport', lastRunOn: new Date() - 1).save()
		new AsyncJob(altViewerURL: 'altViewerURL2', jobName: 'null2', jobStatus: 's2',
				jobType: 'DataExport', lastRunOn: new Date() - 2).save()
		new StatusOfExport(jobName: 'null1', jobStatus: 's1', lastExportName: 'e1', lastExportTime: new Date()).save()
		flush()

		then:
		AsyncJob.count() == 3
		StatusOfExport.count() == 1

		when:
		Map map = retrieveDataService.getjobs()

		then:
		map.success
		map.totalCount == 2
		map.jobs instanceof List

		when:
		List jobs = map.jobs

		then:
		jobs.size() == 2
		jobs[0] instanceof Map
		jobs[1] instanceof Map

		when:
		Map jobData = jobs[0]

		then:
		jobData.altViewerURL == 'altViewerURL1'
		jobData.name == 'null1'
		jobData.lastExportName == 'e1'
		jobData.lastExportTime
		jobData.exportStatus == 's1'

		when:
		jobData = jobs[1]

		then:
		jobData.altViewerURL == 'altViewerURL2'
		jobData.name == 'null2'
		jobData.lastExportName == 'Never Exported'
		!jobData.lastExportTime?.trim()
		!jobData.exportStatus?.trim()
	}
}
