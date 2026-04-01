package com.intuit.sbd.payroll.psp.gateways.wc.util;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.common.utils.GUUID;
import com.intuit.sbd.payroll.psp.common.utils.offlineticket.ConfigType;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import org.springframework.util.CollectionUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.intuit.sbd.payroll.psp.common.utils.OfflineTicketHeader.getAuthNHeader;
import static com.intuit.sbd.payroll.psp.common.utils.OfflineTicketHeader.getHeader;

/**
 * Author: Sriram Nutakki
 * Date created: 11/19/12
 */
public class WCUtil {

     private static final String DATE_FORMAT_MM_DD_YYYY = "MM/dd/yyyy";
    private static SpcfLogger logger = Application.getLogger(WCUtil.class);

    public static String toMMDDYYYY(SpcfCalendar spcfCal) {
        String result = null;
        if (spcfCal != null) {
            GregorianCalendar c = new GregorianCalendar();
            c.clear();
            c.set(Calendar.YEAR, spcfCal.getYear());
            c.set(Calendar.MONTH, spcfCal.getMonth() - 1);
            c.set(Calendar.DAY_OF_MONTH, spcfCal.getDay());
            SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT_MM_DD_YYYY);
            result = formatter.format(c.getTime());
        }
        return result;
    }

    public static XMLGregorianCalendar createXMLGregorianCalendar(SpcfCalendar spcfCal) {
        XMLGregorianCalendar cal = null;
        if (spcfCal != null) {
            cal = createXMLGregorianCalendar(SpcfUtils.convertSpcfCalendarToDate(spcfCal));
        }
        return cal;
    }

    public static XMLGregorianCalendar createXMLGregorianCalendar(Date date) {
        XMLGregorianCalendar cal = null;
        if (date != null) {
            Calendar in = new GregorianCalendar();
            in.setTime(date);
            cal = createXMLGregorianCalendar(in.get(Calendar.YEAR), in.get(Calendar.MONTH), in.get(Calendar.DAY_OF_MONTH));
        }
        return cal;
    }

    public static XMLGregorianCalendar createXMLGregorianCalendar(int year, int month, int day) {
        try {
            GregorianCalendar c = new GregorianCalendar();
            c.set(Calendar.YEAR, year);
            c.set(Calendar.MONTH, month);
            c.set(Calendar.DAY_OF_MONTH, day);
            return DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static <E extends Object> List<List<E>> split(Collection<E> input, int size) {
        List<List<E>> master = new ArrayList<List<E>>();
        if (input != null && input.size() > 0) {
            List<E> col = new ArrayList<E>(input);
            boolean done = false;
            int startIndex = 0;
            int endIndex = col.size() > size ? size : col.size();
            while (!done) {
                master.add(col.subList(startIndex, endIndex));
                if (endIndex == col.size()) {
                    done = true;
                }
                else {
                    startIndex = endIndex;
                    endIndex = col.size() > (endIndex + size) ? (endIndex + size) : col.size();
                }
            }
        }
        return master;
    }

    public static <T> List<Set<T>> split(Map<T, Number> countByObj, int maxObjCountInBatch, int maxTotalCountInBatch) {
        List<Set<T>> batches = new ArrayList<Set<T>>();
        if (countByObj != null && countByObj.size() > 0) {
            Set<T> batch = new HashSet<T>();
            batches.add(batch);
            int batchCount = 0;
            for (T t : countByObj.keySet()) {
                int objCount = countByObj.get(t).intValue();
                if ((batchCount + objCount) <= maxTotalCountInBatch && batch.size() < maxObjCountInBatch) {
                    batch.add(t);
                    batchCount += objCount;
                }
                else {
                    if (batch.size() > 0) {
                        batch = new HashSet<T>();
                        batches.add(batch);
                    }
                    batch.add(t);
                    batchCount = objCount;
                }
            }
        }
        return batches;
    }

    public static String toXML(Object jaxbObject) {
        String out = null;
        try {
            StringWriter writer = new StringWriter();
            JAXBContext context = JAXBContext.newInstance(jaxbObject.getClass());
            context.createMarshaller().marshal(jaxbObject, writer);
            out = writer.toString();
        } catch (JAXBException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return out;
    }

    public static WebResource.Builder getWCResource(WorkersCompProperty.WorkersCompURLEnum workersCompURL,
                                                    String mediaType, String... paths) {
        Client client = Client.create();
        client.setFollowRedirects(true);
        String resourceUrl = workersCompURL.getAWSUrl();

        WebResource webResource = client.resource(resourceUrl);
        for (String path: paths)
            webResource=webResource.path(path);

        WebResource.Builder resourceBuilder = webResource.getRequestBuilder();

        Map<String, List<String>> offlineHeaders;
        if(FeatureFlags.get().booleanValue(FeatureFlags.Key.IS_IDENTITY2_ENABLED_FOR_WC_COMP,false)){
            offlineHeaders = getAuthNHeader(mediaType, GUUID.getUUID());
            logger.info("AuthN: Identity 2 Headers Built for WCComp");

        } else{
            offlineHeaders = getHeader(ConfigType.PSP, mediaType);
            logger.info("AuthN: Identity 1 Headers Built for WCComp");
        }
        for (String key : offlineHeaders.keySet()) {
            if (!CollectionUtils.isEmpty(offlineHeaders.get(key)))
                resourceBuilder = resourceBuilder.header(key, offlineHeaders.get(key).get(0));
        }
        return resourceBuilder;
    }
}
