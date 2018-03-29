import blend4j.plugin.RetrieveDataService
import com.github.jmchilton.blend4j.galaxy.LibrariesClient
import com.github.jmchilton.blend4j.galaxy.beans.FileLibraryUpload
import com.github.jmchilton.blend4j.galaxy.beans.Library
import com.github.jmchilton.blend4j.galaxy.beans.LibraryContent
import com.github.jmchilton.blend4j.galaxy.beans.LibraryFolder
import com.sun.jersey.api.client.ClientResponse
import grails.util.Environment

beans = {
	if (Environment.current == Environment.TEST) {
		retrieveDataService(TestRetrieveDataService)
	}
}

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
class TestRetrieveDataService extends RetrieveDataService {

	List<FileLibraryUpload> uploaded = []

	protected LibrariesClient getLibrariesClient(String galaxyUrl, String galaxyKey) {
		[createLibrary: { Library library ->
			library.id = library.name
			library
		 },
		 getRootFolder: { String id ->
			new LibraryContent(id: id)
		 },
		 createFolder: { String id, LibraryFolder folder ->
			folder.id = id
			folder
		 },
		 uploadFile: { String libraryId, FileLibraryUpload upload ->
			uploaded << upload
			new ClientResponse(200, null, null, null)
		 }] as LibrariesClient
	}
}
