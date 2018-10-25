package emp

import groovy.sql.GroovyResultSet
import groovy.sql.GroovyRowResult
import groovy.transform.CompileStatic
import gtools.Sql
import org.apache.log4j.Logger

@CompileStatic
class DataServiceObject implements DataServiceObjectI {

    final String errMsgNotImplemented = "Not implemented"
    final String errMsgOperationFailed = "Operation failed"
    final String errMsgInsufficientPrivileges = "Insufficient privileges"

    Helper helper
    Sql sql
    def currentUserId
    def currentUserLanguage
    Map<String, String> uriObjects
    Map<String, Object> filterObjects
    Map<String, String> uriParams
    LinkedHashMap<String, String> filterParams
    Map<String, Object> uriObjectHierarchy
    String uriObjectHierarchyCurrentPath
    def attr
    def log
    public List<Map> dsObjects = new ArrayList<Map>()
    public Map<String, Object> attributes
    public DataServiceObject dsObject
    GroovyRowResult uriObjectInfo

    DataServiceObject(Helper h) {
        helper = h
        sql = h.sql
        sql.cacheStatements = false;
        currentUserId = h.currentUserId;
        currentUserLanguage = h.currentUserLanguage;
        uriObjects = helper.uriObjects
        uriParams = helper.uriParams
        uriObjectHierarchy = helper.uriObjectHierarchy
        uriObjectHierarchyCurrentPath = h.uriObjectType
        log = h.groovlet.log
        attr = h.attr
        filterObjects = new HashMap<String, Object>(uriObjects)
        filterParams = new LinkedHashMap<String, String>(uriParams)
    }

    void setUriObjectInfo() {}

    Logger getLogger() {
        return Logger.getLogger(getClass());
    }

    Object get() {
        setUriObjectInfo()
    }

    Object get(def objectIds) {
        filterObjects[helper.uriObjectType] = objectIds
        get()
    }

    Object post() {
    }

    Object put() {
        setUriObjectInfo()
    }

    def delete() {
        setUriObjectInfo()
    }

    def setExtraChildObjects(HashMap<String, Object> attributes, GroovyResultSet dataRow, String parentDsObjectName) {

        // Add extra dynamic hierarchical attributes according to queryTree parameter
        for (String hierarchyMember : uriObjectHierarchy[uriObjectHierarchyCurrentPath]) {

            String dsObjectName = hierarchyMember.indexOf("^") != -1 ? hierarchyMember.substring(0, hierarchyMember.indexOf("^")) : hierarchyMember

            //Initiate new data service object
            dsObject = helper.getDataServiceObject(dsObjectName) as DataServiceObject
            dsObject.uriObjectHierarchyCurrentPath = uriObjectHierarchyCurrentPath + "/" + hierarchyMember

            //Override node specific filter parameters
            dsObject.filterObjects = new HashMap<String, Object>(filterObjects)
            dsObject.filterObjects[parentDsObjectName] = dataRow['objectId']

            dsObject.filterParams = new LinkedHashMap<String, String>(filterParams)
            dsObject.filterParams.putAll(helper.getParsedUriParams(hierarchyMember, "^"))

            attributes.put(dsObjectName, dsObject.get())
        }
    }

}
