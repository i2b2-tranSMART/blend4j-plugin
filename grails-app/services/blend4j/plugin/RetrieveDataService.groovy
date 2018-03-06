package blend4j.plugin

import com.github.jmchilton.blend4j.galaxy.GalaxyInstance
import com.github.jmchilton.blend4j.galaxy.GalaxyInstanceFactory
import com.github.jmchilton.blend4j.galaxy.LibrariesClient
import com.github.jmchilton.blend4j.galaxy.beans.FileLibraryUpload
import com.github.jmchilton.blend4j.galaxy.beans.Library
import com.github.jmchilton.blend4j.galaxy.beans.LibraryContent
import com.github.jmchilton.blend4j.galaxy.beans.LibraryFolder
import com.recomdata.transmart.domain.i2b2.AsyncJob
import com.sun.jersey.api.client.ClientResponse
import grails.transaction.Transactional
import org.json.JSONObject

class RetrieveDataService {

	@Transactional
	boolean saveStatusOfExport(String exportJobName, String lbraryName) {
		try {
			new StatusOfExport(
					jobName: exportJobName,
					jobStatus: 'Started',
					lastExportName: lbraryName,
					lastExportTime: new Date()).save()
			true
		}
		catch (e) {
			log.error "The export job for galaxy couldn't be saved"
			false
		}
	}

	@Transactional
	boolean updateStatusOfExport(String exportJobName, String newState) {
		try {
			StatusOfExport newJob = getLatest(StatusOfExport.findAllByJobName(exportJobName))
			newJob.jobStatus = newState
			newJob.save()
			true
		}
		catch (e) {
			log.error("The export job for galaxy couldn't be updated")
			false
		}
	}

	void uploadExportFolderToGalaxy(String galaxyUrl, String tempFolderDirectory, String userId,
	                                String exportJobName, String libraryName) {

		GalaxyUserDetails gud = GalaxyUserDetails.findByUsername(userId)

		LibrariesClient client = GalaxyInstanceFactory.get(galaxyUrl, gud.galaxyKey).librariesClient
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

	void createFoldersAndFiles(File[] files, LibraryFolder rootFolder, LibrariesClient client, String persistedLibraryId) {
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
	Map getjobs(String userName, String jobType = null) {
		List<Map> rows = []

		List<AsyncJob> jobs = AsyncJob.createCriteria() {
			like('jobName', userName + '%')
			if (jobType) {
				eq('jobType', jobType)
			}
			else {
				or {
					ne('jobType', 'DataExport')
					isNull('jobType')
				}
			}
			ge('lastRunOn', new Date() - 7)
			order('lastRunOn', 'desc')
		}

		for (AsyncJob job in jobs) {
			Map m = [altViewerURL: job.altViewerURL,
			         jobInputsJson: new JSONObject(job.jobInputsJson ?: '{}'),
			         name: job.jobName,
			         runTime: job.jobStatusTime,
			         startDate: job.lastRunOn,
			         status: job.jobStatus,
			         viewerURL: job.viewerURL]
			StatusOfExport d = getLatest(StatusOfExport.findAllByJobName(job.jobName))
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

	// TODO don't pull everything from the database and sort client-side to get the most recent
	private StatusOfExport getLatest(List<StatusOfExport> exports) {

		switch (exports.size()) {
			case 0:
				log.error("An error has occured while exporting to galaxy. The job name doesn't exist in the database")
				return null
			case 1:
				return exports[0]
			default:
				def latest = exports[0]
				for (i in 1..exports.size() - 1) {
					if (exports[i].id > latest.id) {
						latest = exports[i]
					}
				}
				return latest
		}
	}
}
