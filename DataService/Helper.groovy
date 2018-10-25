/**
 * Created by danas on 16.12.27.
 */
package emp

import groovy.sql.GroovyRowResult
import groovy.sql.GroovyResultSet
import groovy.transform.CompileStatic
import gtools.Sql
import gtools.json.JsonEncoder

import java.lang.reflect.Constructor
import java.util.List

class Helper extends DataServiceHelper {

    private String workspaceId
    private HashSet<String> currUserRoles
    private HashSet<String> currUserAssignments
    private HashSet<String> currUserTasks

    Helper(Sql sql) {
        this.sql = sql
    }

    Helper(Sql sql, params) {
        this.sql = sql
        this.params = params
    }

    Helper(GroovletZst g) {

        super(g)

        setCurrUserRoles()
        setCurrUserAssignments()
    }


   
}
