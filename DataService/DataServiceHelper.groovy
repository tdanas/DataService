/**
 * Created by danas on 16.12.27.
 */
package emp

import groovy.transform.CompileStatic
import gtools.Sql
import gtools.json.JsonEncoder
import org.codehaus.jackson.JsonNode
import org.codehaus.jackson.map.ObjectMapper
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JsonDataSource;
import zst.exceptions.DSException
import com.oprisk.clarity.Clarity;

class DataServiceHelper {

    Sql sql
    def params
    def currentUserId
    def currentUserLanguage
    def uriParentObjectType
    String uriObjectType
    def attr
    LinkedHashMap<String, String> uriParams
    GroovletZst groovlet;
    private Map<String, String> uriObjects = new HashMap<String, String>();
    Map<String, Object> uriObjectHierarchy = new HashMap<String, Object>()

    DataServiceHelper(GroovletZst g) {
        groovlet = g;
        sql = g.sql;
        params = g.params;
        currentUserId = g.currentUserId;
        currentUserLanguage = g.currentUserLanguage;
        setUriObjects()
        setUriParams(g.request.queryString,"&")
        setHierarchy()

    }


    def setHierarchy(JsonNode root = null, String currentPath = null) {

        ArrayList<String> hierarchyRow = new ArrayList<String>()

        if (uriParams.queryTree == null) {
            return
        }

        if (root == null) {
            ObjectMapper jsonMapper = new ObjectMapper();
            root = jsonMapper.readTree(uriParams.queryTree);
        }

        if (currentPath == null) {
            currentPath = uriObjectType
        }

        for (JsonNode node : root) {

            if (node.isObject()) {
                hierarchyRow.add(node.getFieldNames()[0])
                setHierarchy(node, currentPath + "/" + node.getFieldNames()[0])
            } else if (node.isArray()) {
                setHierarchy(node, currentPath)
            } else {
                hierarchyRow.add(node.getTextValue())
            }
        }

        if (hierarchyRow.size() > 0) {
            uriObjectHierarchy.put(currentPath, hierarchyRow)
        }

    }

    def setUriParams(String queryString, String delimiter){

        uriParams = new LinkedHashMap<String, String>();
        uriParams=getParsedUriParams(queryString, delimiter)
    }

    LinkedHashMap<String, String> getParsedUriParams(String queryString, String delimiter) {

        LinkedHashMap<String, String> uriParams = new LinkedHashMap<String, String>();

        if (queryString == null) {
            return uriParams
        }

        String uriParamsString = queryString.indexOf(delimiter) != -1 ? queryString.substring(queryString.indexOf(delimiter) + 1) : null


        if (uriParamsString != null) {

            uriParams = new LinkedHashMap<String, String>();
            final String[] pairs = uriParamsString.split(delimiter);
            for (String pair : pairs) {
                final int idx = pair.indexOf("=");
                final String key = idx > 0 ? URLDecoder.decode(pair.substring(0, idx), "UTF-8") : pair;
                if (!uriParams.containsKey(key)) {
                    uriParams.put(key, null);
                }
                final String value = idx > 0 && pair.length() > idx + 1 ? URLDecoder.decode(pair.substring(idx + 1), "UTF-8") : null;
                uriParams.put(key, value);
            }
        }
        uriParams

    }

    def Map<String, String> getUriObjects() {
        uriObjects
    }

    String getUri() {

        def queryString = groovlet.getRequest().getQueryString()
        if (queryString != null) {

            queryString.indexOf("&") != -1 ? queryString.substring(0, queryString.indexOf("&")) : queryString
        } else {
            null
        }
    }

    def setUriObjects() {

        String[] slParams = getUri() != null ? getUri().split("/") : null;

        if (slParams != null) {
            def slSize = slParams.size()
            for (int i = 0; i < slSize; i++) {
                uriObjects.put(slParams[i].toString(), slSize > i + 1 ? slParams[i + 1].toString() : null)
                uriParentObjectType = i > 1 ? slParams[i - 2].toString() : null
                uriObjectType = slParams[i].toString()
                i++
            }
        }
    }


//    Object getDataServiceObject(def className, def path =null) {
//
//        GroovyClassLoader loader = new GroovyClassLoader(getClass().getClassLoader())
//        return loader.loadClass("zst.dsobjects." + className).newInstance(this);
//    }

    Object getDataServiceObject(def className) {

        GroovyClassLoader loader = new GroovyClassLoader(getClass().getClassLoader())
        return loader.loadClass("zst.dsobjects." + className).newInstance(this);
    }

// Helper method to ensure that in a type-safe context the usage of the closure is known by compiler or IDE
    @CompileStatic
    static <T> Closure<T> useAsJsonObjectRenderer(@DelegatesTo(JsonEncoder.ObjectWriter) Closure<T> closure) {
        return closure
    }
// Helper method to ensure that in a type-safe context the usage of the closure is known by compiler or IDE
    @CompileStatic
    static <T> Closure<T> useAsJsonArrayRenderer(@DelegatesTo(JsonEncoder.ArrayWriter) Closure<T> closure) {
        return closure
    }

    @CompileStatic
    def setRequestBodyAttributes(def body) throws UnsupportedEncodingException {

        switch (groovlet.request.method) {
            case "POST": attr = params
                break
            case "PUT":

                final Map<String, String> query_pairs = new LinkedHashMap<String, String>();
                final String[] pairs = body.toString().split("&");
                for (String pair : pairs) {
                    final int idx = pair.indexOf("=");
                    final String key = idx > 0 ? URLDecoder.decode(pair.substring(0, idx), "UTF-8") : pair;
                    if (!query_pairs.containsKey(key)) {
                        query_pairs.put(key, null);
                    }
                    final String value = idx > 0 && pair.length() > idx + 1 ? URLDecoder.decode(pair.substring(idx + 1), "UTF-8") : null;
                    query_pairs.put(key, value);
                }

                attr = query_pairs
                break
        }
    }

    @CompileStatic
    public Throwable getDSException(Throwable e) {

        if (e instanceof DSException) {
            return e
        } else if (e.getCause() != null) {
            return getDSException(e.getCause())
        }
        return e
    }

    static JasperPrint getJasperPrint(def dataJson, def dataExpression, def reportFormFile) {

        JsonDataSource jds = new JsonDataSource(new ByteArrayInputStream(dataJson.getBytes()), dataExpression);

        File reportFile = Clarity.findFile(reportFormFile);
        JasperReport rep = JasperCompileManager.compileReport(reportFile.getAbsolutePath());

        HashMap params = new HashMap();
        params.put("Path", reportFile.getParentFile().getAbsolutePath() + "/");

        return JasperFillManager.fillReport(rep, params, jds);

    }

}
