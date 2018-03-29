package blend4j.plugin
/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
class GalaxyUserDetailsServiceSpec extends AbstractIntegrationSpec {

	GalaxyUserDetailsService galaxyUserDetailsService

	void 'test saveNewGalaxyUser'() {
		expect:
		!GalaxyUserDetails.count()

		when:
		String galaxyKey = '!galaxyKey!'
		String mailAddress = '!mailAddress!'
		String username = '!username!'

		then:
		!galaxyUserDetailsService.saveNewGalaxyUser(null, galaxyKey, mailAddress)
		!GalaxyUserDetails.count()

		!galaxyUserDetailsService.saveNewGalaxyUser(username, null, mailAddress)
		!GalaxyUserDetails.count()

		!galaxyUserDetailsService.saveNewGalaxyUser(username, galaxyKey, null)
		!GalaxyUserDetails.count()

		galaxyUserDetailsService.saveNewGalaxyUser username, galaxyKey, mailAddress

		when:
		flush()

		then:
		GalaxyUserDetails.count() == 1
	}

	void 'test deleteUser'() {
		expect:
		!GalaxyUserDetails.count()

		when:
		String username = '!username!'
		new GalaxyUserDetails(galaxyKey: 'x', mailAddress: 'x', username: username).save()

		then:
		GalaxyUserDetails.count() == 1

		when:
		galaxyUserDetailsService.deleteUser username

		then:
		!GalaxyUserDetails.count()
	}
}
