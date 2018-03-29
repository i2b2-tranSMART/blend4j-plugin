package blend4j.plugin

import grails.test.mixin.integration.Integration
import groovy.transform.CompileStatic
import org.hibernate.SessionFactory
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User
import spock.lang.Specification

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
@CompileStatic
@Integration
abstract class AbstractIntegrationSpec extends Specification {

	SessionFactory sessionFactory

	void cleanup() {
		SecurityContextHolder.context.authentication = null
	}

	void flush() {
		sessionFactory.currentSession.flush()
	}

	void authenticateAs(String username) {
		SecurityContextHolder.context.authentication = new UsernamePasswordAuthenticationToken(
				new User(username, username, []), username)
	}
}
