package blend4j.plugin

import grails.transaction.Transactional
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.transmart.plugin.shared.UtilService

@Slf4j('logger')
@Transactional
class GalaxyUserDetailsService {

	@Autowired private UtilService utilService

	boolean saveNewGalaxyUser(String username, String galaxyKey, String mailAddress) {
		try {
			GalaxyUserDetails gud = new GalaxyUserDetails(
					galaxyKey: galaxyKey,
					mailAddress: mailAddress,
					username: username)
			gud.save()
			if (gud.hasErrors()) {
				logger.error 'The GalaxyUserDetails could not be saved: {}', utilService.errorStrings(gud)
				false
			}
			else {
				true
			}
		}
		catch (e) {
			logger.error 'The GalaxyUserDetails could not be saved', e
			false
		}
	}

	void deleteUser(String username) {
		GalaxyUserDetails.where { username == username }.deleteAll()
	}
}
