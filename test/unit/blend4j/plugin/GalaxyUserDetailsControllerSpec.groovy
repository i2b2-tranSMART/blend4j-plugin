package blend4j.plugin

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
@Mock(GalaxyUserDetails)
@TestFor(GalaxyUserDetailsController)
class GalaxyUserDetailsControllerSpec extends Specification {

	void 'test list'() {
		when:
		List<GalaxyUserDetails> list = [new GalaxyUserDetails()]
		boolean called = false
		GalaxyUserDetails.metaClass.static.list = { Map m ->
			called = true
			assert params.max == 999999
			list
		}
		Map model = controller.list()

		then:
		called
		model
		model.size() == 1
		list.is model.personList

		when:
		response.reset()
		called = false
		long max = 42
		params.max = max
		GalaxyUserDetails.metaClass.static.list = { Map m ->
			called = true
			assert params.max == max
			list
		}
		model = controller.list()

		then:
		called
		model
		model.size() == 1
		list.is model.personList
	}

	void 'test update'() {
		when:
		GalaxyUserDetails person = new GalaxyUserDetails()

		then:
		!person.galaxyKey
		!person.mailAddress
		!person.username

		when:
		String galaxyKey = '!galaxyKey!'
		String username = '!username!'
		params.galaxyKey = galaxyKey
		params.username = username

		controller.update person

		then:
		person.galaxyKey == galaxyKey
		person.username == username
		!person.mailAddress
		flash.message == 'Please enter an email'

		when:
		response.reset()
		flash.clear()
		String mailAddress = '!mailAddress!'
		params.mailAddress = mailAddress

		controller.update person

		then:
		person.galaxyKey == galaxyKey
		person.mailAddress == mailAddress
		person.username == username
		!flash
	}

	void 'test create'() {
		when:
		String galaxyKey = '!galaxyKey!'
		params.galaxyKey = galaxyKey
		Map model = controller.create()

		then:
		model
		model.size() == 1
		model.person.galaxyKey == galaxyKey
	}

	void 'test delete'() {
		when:
		String id = '!username!'
		params.id = id
		boolean called = false
		controller.galaxyUserDetailsService = new GalaxyUserDetailsService() {
			void deleteUser(String username) {
				assert username == id
				called = true
			}
		}

		controller.delete()

		then:
		called
	}

	void 'test save'() {
		when:
		String galaxyKey = '!galaxyKey!'
		String mailAddress = '!mailAddress!'
		params.galaxyKey = galaxyKey
		params.mailAddress = mailAddress

		controller.save()

		then:
		flash.size() == 1
		flash.message == 'Please enter a username'

		when:
		response.reset()
		params.clear()
		String username = '!username!'
		params.mailAddress = mailAddress
		params.username = username

		controller.save()

		then:
		flash.size() == 1
		flash.message == 'Please enter a Galaxy Key'

		when:
		response.reset()
		params.clear()
		params.galaxyKey = galaxyKey
		params.username = username

		controller.save()

		then:
		flash.size() == 1
		flash.message == 'Please enter an email'

		when:
		response.reset()
		params.mailAddress = mailAddress

		boolean called = false
		boolean ok = false
		controller.galaxyUserDetailsService = new GalaxyUserDetailsService() {
			boolean saveNewGalaxyUser(String username2, String galaxyKey2, String mailAddress2) {
				assert username == username2
				assert galaxyKey == galaxyKey2
				assert mailAddress == mailAddress2
				called = true
				ok
			}
		}

		controller.save()

		then:
		called
		flash.size() == 1
		flash.message == 'Cannot create user'

		when:
		response.reset()
		called = false
		ok = true

		controller.save()

		then:
		called
		flash.size() == 1
		flash.message == 'User Created'
	}
}
