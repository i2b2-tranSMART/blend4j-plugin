package blend4j.plugin

class StatusOfExport {

	String jobName
	String jobStatus
	String lastExportName
	Date lastExportTime

	static mapping = {
		table 'GALAXY.STATUS_OF_EXPORT_JOB'
		id generator: 'increment', params: [sequence: 'GALAXY.STATUS_OF_EXPORT_JOB_SEQ']
		version false

		jobName column: 'JOB_NAME_ID'
	}
}
