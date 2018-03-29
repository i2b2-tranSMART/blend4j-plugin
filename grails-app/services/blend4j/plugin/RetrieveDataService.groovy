package blend4j.plugin

import com.github.jmchilton.blend4j.galaxy.GalaxyInstanceFactory
import com.github.jmchilton.blend4j.galaxy.LibrariesClient
import com.github.jmchilton.blend4j.galaxy.beans.FileLibraryUpload
import com.github.jmchilton.blend4j.galaxy.beans.Library
import com.github.jmchilton.blend4j.galaxy.beans.LibraryFolder
import com.recomdata.transmart.domain.i2b2.AsyncJob
import com.sun.jersey.api.client.ClientResponse
import grails.transaction.Transactional
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.json.JSONObject
import org.springframework.beans.factory.annotation.Autowired
import org.transmart.plugin.shared.SecurityService
import org.transmart.plugin.shared.UtilService

@CompileStatic
@Slf4j('logger')
class RetrieveDataService {

	@Autowired private SecurityService securityService
	@Autowired private UtilService utilService

	@Transactional
	boolean saveStatusOfExport(String exportJobName, String libraryName) {
		try {
			StatusOfExport soe = new StatusOfExport(
					jobName: exportJobName,
					jobStatus: 'Started',
					lastExportName: libraryName,
					lastExportTime: new Date())
			soe.save()
			if (soe.hasErrors()) {
				logger.error 'StatusOfExport could not be saved: {}', utilService.errorStrings(soe)
				false
			}
			else {
				true
			}
		}
		catch (e) {
			logger.error 'StatusOfExport could not be saved', e
			false
		}
	}

	@Transactional
	boolean updateStatusOfExport(String exportJobName, String newState) {
		try {
			StatusOfExport newJob = getLatest(exportJobName)
			newJob.jobStatus = newState
			newJob.save()
			if (!newJob.hasErrors()) {
				return true
			}
			logger.error 'The export job for galaxy could not be updated: {}', utilService.errorStrings(newJob)
		}
		catch (e) {
			logger.error 'The export job for galaxy could not be updated', e
		}

		false
	}

	void uploadExportFolderToGalaxy(String galaxyUrl, String tempFolderDirectory,
	                                String exportJobName, String libraryName) {

		GalaxyUserDetails gud = GalaxyUserDetails.findWhere(username: securityService.currentUsername())

		LibrariesClient client = getLibrariesClient(galaxyUrl, gud.galaxyKey)
		Library persistedLibrary = client.createLibrary(new Library(libraryName + ' - ' + gud.mailAddress))
		LibraryFolder folder = new LibraryFolder(
				name: exportJobName,
				folderId: client.getRootFolder(persistedLibrary.id).id)

		LibraryFolder resultFolder = client.createFolder(persistedLibrary.id, folder)
		assert resultFolder.name == exportJobName
		assert resultFolder.id != null

		File[] files = new File(tempFolderDirectory, exportJobName).listFiles()
		createFoldersAndFiles(files, resultFolder, client, persistedLibrary.id)
	}

	protected LibrariesClient getLibrariesClient(String galaxyUrl, String galaxyKey) {
		GalaxyInstanceFactory.get(galaxyUrl, galaxyKey).librariesClient
	}

	private void createFoldersAndFiles(File[] files, LibraryFolder rootFolder,
	                                   LibrariesClient client, String persistedLibraryId) {
		for (File file in files) {
			if (file.isFile()) {
				try {
					FileLibraryUpload upload = new FileLibraryUpload(
							folderId: rootFolder.id,
							name: file.name,
							fileType: 'tabular',
							file: file)
					ClientResponse resultFile = client.uploadFile(persistedLibraryId, upload)
					assert resultFile.status == 200: resultFile.getEntity(String)
				}
				catch (IOException e) {
					throw new RuntimeException(e)
				}
			}
			else if (file.isDirectory()) {
				LibraryFolder folder = new LibraryFolder(name: file.name, folderId: rootFolder.id)
				LibraryFolder resultFolder = client.createFolder(persistedLibraryId, folder)
				assert resultFolder.name == file.name
				assert resultFolder.id != null

				createFoldersAndFiles(file.listFiles(), resultFolder, client, persistedLibraryId)
			}
		}
	}

	/**
	 * Get the jobs to show in the galaxy jobs tab
	 */
	Map getjobs() {
		List<AsyncJob> jobs = findJobs()
		List<Map> rows = []
		for (AsyncJob job in jobs) {
			Map m = [altViewerURL : job.altViewerURL,
			         jobInputsJson: new JSONObject(job.jobInputsJson ?: '{}'),
			         name         : job.jobName,
			         runTime      : job.jobStatusTime,
			         startDate    : job.lastRunOn,
			         status       : job.jobStatus,
			         viewerURL    : job.viewerURL]
			StatusOfExport d = getLatest(job.jobName)
			if (d) {
				m.lastExportName = d.lastExportName
				m.lastExportTime = d.lastExportTime.toString()
				m.exportStatus = d.jobStatus
			}
			else {
				m.lastExportName = 'Never Exported'
				m.lastExportTime = ' '
				m.exportStatus = ' '
			}
			rows << m
		}

		[success: true, totalCount: jobs.size(), jobs: rows]
	}

	@CompileDynamic
	private List<AsyncJob> findJobs() {
		AsyncJob.findAllByJobTypeAndLastRunOnGreaterThanAndJobNameLike'DataExport',
				new Date() - 7, securityService.currentUsername() + '%',
				[sort: 'lastRunOn', order: 'desc']
	}

	@CompileDynamic
	private StatusOfExport getLatest(String jobName) {
		StatusOfExport.findByJobName jobName, [sort: 'id', order: 'desc']
	}
}
