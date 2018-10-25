/**
 * Created by danas on 17.1.12.
 */
package emp
interface DataServiceObjectI {
    void setUriObjectInfo()
    Object get()
    Object get(def objectIds)
    Object post()
    def put()
    def delete()
}
