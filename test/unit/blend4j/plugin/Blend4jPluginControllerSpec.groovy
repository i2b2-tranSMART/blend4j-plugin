package blend4j.plugin

import grails.converters.JSON
import grails.test.mixin.TestFor
import org.codehaus.groovy.grails.web.json.JSONElement
import org.codehaus.groovy.grails.web.json.JSONObject
import spock.lang.Specification

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
@TestFor(Blend4jPluginController)
class Blend4jPluginControllerSpec extends Specification {

	void 'test loadScripts'() {
		when:
		servletContext.contextPath = '!contextPath!'
		controller.loadScripts()
		String jsonResponse = response.contentAsString
		JSONElement je = JSON.parse(jsonResponse)

		then:
		je instanceof JSONObject

		when:
		JSONObject json = je

		then:
		true.is json.success
		1 == json.totalCount
		json.files instanceof List

		when:
		List files = json.files

		then:
		files.size() == 1
		files[0] instanceof Map

		when:
		Map map = files[0]

		then:
		map.size() == 2
		map.path == '!contextPath!/js/galaxyExport.js'
		map.type == 'script'
	}
}
