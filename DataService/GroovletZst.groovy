package emp

import groovy.sql.GroovyResultSet
import groovy.sql.GroovyRowResult
import gtools.XCompileStatic
import groovy.transform.Field

//@CompileStatic
abstract class GroovletEmp extends EMP.Groovlet {

	final responseStatusAttr = "responseStatus"
	final errorMessageAttr = "errorMessage"
	final updatedDateAttr = "updatedDate"

	final responseSuccess = "SUCCESS"
	final responseFail = "FAIL"

	@Lazy
	public Helper helper = new Helper(this);

	String getErrMsgGeneral(String ticket) {
		return (currentUserLanguage == 'de' ? 'Es ist ein Fehler aufgetreten' : 'An unexpected error has occurred') + ', Ticket = ' + ticket
	}

	String getErrMsgInsufficientPrivileges() {
		return (currentUserLanguage == 'de' ? 'TODO:translate DE:Insufficient privileges' : 'Insufficient privileges')
	}

	def getRequestBody(def request) {
		
				Scanner s = new Scanner(request.getInputStream(), "UTF-8").useDelimiter("\\A");
				return s.hasNext() ? s.next() : "";
			}
	

	

	@XCompileStatic
	Closure getStatusSuccessRenderer() {
		return Helper.useAsJsonObjectRenderer({
			status = responseSuccess
			updatedDate = new java.util.Date()
		})
	}


	def getResponseStatusSuccess() {
		jsonObject { responseStatus = object statusSuccessRenderer }
	}


	def getResponseStatusFail(errMsg) {
		jsonObject {
			responseStatus = object {
				status = responseFail
				errorMessage = errMsg
			}
		}
	}

}
