package blend4j.plugin

import grails.transaction.Transactional

@Transactional
class GalaxyUserDetailsService {

	boolean saveNewGalaxyUser(String username, String galaxyKey, String mailAddress) {
		try {
			new GalaxyUserDetails(username: username, galaxyKey: galaxyKey, mailAddress: mailAddress).save()
			true
		}
		catch (e) {
			log.error("The export job for galaxy couldn't be saved")
			false
		}
	}

	void deleteUser(String username) {
		GalaxyUserDetails.findByUsername(username).delete()
	}
}
