package emp.dsobjects

import groovy.sql.GroovyRowResult
import gtools.XCompileStatic

import java.io.ObjectOutputStream.DebugTraceInfoStack

import groovy.sql.GroovyResultSet
import zst.Helper
import zst.DataServiceObject
import zst.dsobjects.projects
import zst.dsobjects.kefags
import zst.dsobjects.sets
import zst.dsobjects.tasks
import zst.dsobjects.content

class algs extends DataServiceObject {

    algs(Helper h) {

        super(h)
    }

    //@XCompileStatic
    List<Object> get() {

        super.get()

        sql.eachRow("""
    select
        obj_id as "objectId",
        code as "code",
        name as "name"
    from employees_vw str
    where
        --Node filters
        (str.obj_id in (select column_value from table(z_split($filterObjects.algs))) or $filterObjects.algs is null and str.obj_id is not null)
        and str.prj_obj_id=nvl($filterObjects.projects,str.prj_obj_id)


        --Extra filters
AND (
        $filterObjects.milestones IS NULL and $filterObjects.tasks is NULL and $filterObjects.content IS NULL  OR exists(SELECT 1
                                                    FROM zst_content_vw con
                                                    WHERE con.mls_obj_id = nvl($filterObjects.milestones,con.mls_obj_id) AND

    
                    """) { GroovyResultSet dataRow ->

            attributes = new HashMap<String, Object>()
            attributes.put("objectId", dataRow.objectId)
            attributes.put("code", dataRow.code)
            attributes.put("name", dataRow.name)

            setExtraChildObjects(attributes,dataRow,this.class.simpleName)

            dsObjects.add(attributes)
        }

        dsObjects

    }


    Object post() {

        super.post()
    }

    Object put() {

        super.put()

        throw new Exception(errMsgNotImplemented);
    }


    def delete() {

        super.delete()
        false
    }
}