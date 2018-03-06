package blend4j.plugin

class GalaxyUserDetails implements Serializable {
	private static final long serialVersionUID = 1

	String galaxyKey
	String mailAddress
	String username

	static mapping = {
		table 'GALAXY.USERS_DETAILS_FOR_EXPORT_GAL'
		id generator: 'increment', params: [sequence: 'GALAXY.HIBERNATE_ID']
		version false
	}
}
