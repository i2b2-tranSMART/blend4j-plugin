package blend4j.plugin

class GalaxyUserDetailsController {

	GalaxyUserDetailsService galaxyUserDetailsService

	def list() {
		if (!params.max) {
			params.max = 999999
		}
		[personList: GalaxyUserDetails.list(params)]
	}

	def update(GalaxyUserDetails person) {
		person.properties = params

		if (!params.mailAddress) {
			flash.message = 'Please enter an email'
			render view: 'edit', model: [person: person]
			return
		}
	}

	def create() {
		[person: new GalaxyUserDetails(params)]
	}

	def delete() {
		flash.message = 'User deleted'
		galaxyUserDetailsService.deleteUser(params.id)
		render view: 'list'
	}

	def save(String username, String galaxyKey, String mailAddress) {

		String message
		if (!username) {
			message = 'Please enter a username'
		}
		else if (!galaxyKey) {
			message = 'Please enter a Galaxy Key'
		}
		else if (!mailAddress) {
			message = 'Please enter an email'
		}

		if (message) {
			flash.message = message
			render view: 'create', model: [person: new GalaxyUserDetails(params)]
			return
		}

		if (galaxyUserDetailsService.saveNewGalaxyUser(username, galaxyKey, mailAddress)) {
			flash.message = 'User Created'
		}
		else {
			flash.message = 'Cannot create user'
		}

		render view: 'create', model: [person: new GalaxyUserDetails()]
	}
}
