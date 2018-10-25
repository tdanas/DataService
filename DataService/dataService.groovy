package zst

import groovy.transform.BaseScript
import groovy.transform.Field
import gtools.XCompileStatic
import gtools.Json
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import gtools.clarity.MediaType

@BaseScript GroovletZst base

@Field def responseContent


@XCompileStatic
void prepareResponse() {

    try {
        helper.setRequestBodyAttributes(getRequestBody(request).toString())

        Object dsObject = helper.getDataServiceObject(helper.uriObjectType)

        assert dsObject instanceof DataServiceObject

        switch (request.method) {

            case "GET":
                if (params.responseType == 'pdf') {
                    responseContent = Json.objectString {
                        attr("$helper.uriObjectType", dsObject.get())
                    }
                } else {
                    responseContent = jsonObject {
                        attr("$helper.uriObjectType", dsObject.get())
                        responseStatus = statusSuccessRenderer
                    }
                }

                break;

            case "POST":

                responseContent = jsonObject {
                    attr("$helper.uriObjectType", dsObject.post())
                    responseStatus = statusSuccessRenderer
                }

                break;
            case "PUT":

                responseContent = jsonObject {
                    attr("$helper.uriObjectType", dsObject.put())
                    responseStatus = statusSuccessRenderer
                }

                break;
            case "DELETE":

                dsObject.delete()
                responseContent = getResponseStatusSuccess()

                break;
        }

    } catch (Exception e) {
        log.error "Error " + errorTicket, e
        responseContent = getResponseStatusFail(getErrMsgGeneral(errorTicket))
    }
}

//@XCompileStatic
//void renderResponse() {
//    // GH: catch Exception and render Error Response
//    try {
//        sendResponse STREAM_COMPRESSED + DEFERRED, (responseContent)
//    }
//    catch(Exception e){
//        String t = errorTicket
//        log.error "Error " + t, e
//        sendError DIRECTLY, getResponseStatusFail(getErrMsgGeneral(t))
//    }
//}

@XCompileStatic
void renderResponse() {

    onErrorSend jsonObject {
        log.error "Error " + errorTicket, error
        responseStatus = object {
            status = responseFail
            ticket=errorTicket
            errorMessage = helper.getDSException(error).toString()
        }
    }.andStatus(200)

    if (params.responseType == 'pdf') {

        JasperPrint print = helper.getJasperPrint(responseContent, helper.uriObjectType, params.template)

        JRPdfExporter exporter = new JRPdfExporter();
        exporter.setParameter(JRExporterParameter.JASPER_PRINT, print);

        sendResponse STREAM_COMPRESSED, streamContent({ OutputStream responseStream ->

            exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, responseStream);
            exporter.exportReport();
            responseStream.close();
        }).binary().with(MediaType.valueOf('application/pdf')).returnAsAttachment(params.reportFileName);

    } else {
        sendResponse STREAM_COMPRESSED + DEFERRED, (responseContent)
    }
}

prepareResponse()

renderResponse()
