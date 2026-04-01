package com.intuit.sbd.payroll.psp.gateways.echosign;

import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.domain.SystemParameter;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import echosign.*;
import org.apache.commons.lang.WordUtils;

import javax.xml.ws.BindingProvider;
import java.io.IOException;
import java.net.*;
import java.util.*;

/**
 * User: dweinberg
 * Date: Sep 23, 2010
 * Time: 1:03:56 PM
 */
public class EchoSignGateway {

    private static String host;

    private static EchoSignDocumentService9PortType cachedService;
    protected static EchoSignDocumentService9PortType getService() {
        if (cachedService == null) {

            String proxyAddress = ConfigurationManager.getSettingValue
                    (ConfigurationModule.TaxCreditsAdapter, "proxyAddress");
            String proxyPort = ConfigurationManager.getSettingValue
                    (ConfigurationModule.TaxCreditsAdapter, "proxyPort");

            if (proxyAddress == null || proxyPort == null || proxyAddress.length() == 0 || proxyPort.length() == 0) {
                //don't use proxy
            } else {
                ProxySelector.setDefault(new MyProxySelector(ProxySelector.getDefault(), proxyAddress, proxyPort));
            }

            String endpoint = SystemParameter.findStringValue(SystemParameter.Code.TAX_CREDITS_ECHOSIGN_ENDPOINT, null);
            
            EchoSignDocumentService9 service = new EchoSignDocumentService9();
            cachedService =  service.getEchoSignDocumentService9HttpPort();

            if (endpoint != null) { //if null, just use one in wsdl
                ((BindingProvider)cachedService).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpoint);
            }

            try {
                URI endpointURI = new URI((((String) ((BindingProvider)cachedService).getRequestContext().get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY))));
                host = endpointURI.getHost();
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }


        }
        return cachedService;
    }

    public static class MyProxySelector extends ProxySelector {
        private ProxySelector defaultSelector = null;
        private String proxyAddress;
        private String proxyPort;

        public MyProxySelector(ProxySelector defaultSelector, String proxyAddress, String proxyPort) {
            this.defaultSelector = defaultSelector;
            this.proxyAddress = proxyAddress;
            this.proxyPort = proxyPort;
        }

        public java.util.List<Proxy> select(URI uri) {
            if (uri == null) {
                throw new IllegalArgumentException("URI can't be null.");
            }
            String protocol = uri.getScheme();
            if (("http".equalsIgnoreCase(protocol) || "https".equalsIgnoreCase(protocol)) && uri.getHost().equals(host)) {
                ArrayList<Proxy> l = new ArrayList<Proxy>();
                l.add(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyAddress, Integer.parseInt(proxyPort))));
                return l;
            } else {
                return defaultSelector.select(uri);
            }
        }

        @Override
        public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
            throw new RuntimeException("Failed connecting: " + uri.toString() + ":" + sa.toString(), ioe);
        }
    }

     public static String sendPacketForSigning(byte[] packet, String employerEmail, String employeeEmail, String companyName, String employeeName, String password, Date hireDate, int deadlineDays) {
        DocumentCreationInfo documentCreationInfo = new DocumentCreationInfo();

        ArrayOfString tos = new ArrayOfString();
        tos.getString().add(employerEmail);
        tos.getString().add(employeeEmail);
        documentCreationInfo.setTos(tos);

        documentCreationInfo.setName(String.format("Tax Credits Application: %s/%s", companyName, employeeName));

        FileInfo fileInfo = new FileInfo();
        fileInfo.setFileName(String.format("TaxCreditsApplication%s-%s.pdf", nameToFileName(companyName), nameToFileName(employeeName)));
        fileInfo.setFile(packet);
        ArrayOfFileInfo fileInfos = new ArrayOfFileInfo();
        fileInfos.getFileInfo().add(fileInfo);
        documentCreationInfo.setFileInfos(fileInfos);

        documentCreationInfo.setSignatureType(SignatureType.ESIGN);
        documentCreationInfo.setSignatureFlow(SignatureFlow.SENDER_SIGNATURE_NOT_REQUIRED);

        SecurityOptions securityOptions = new SecurityOptions();
        securityOptions.setPassword(password);
        securityOptions.setProtectSignature(true);
        securityOptions.setProtectOpen(true);
        documentCreationInfo.setSecurityOptions(securityOptions);

        SpcfCalendar hireCal = SpcfCalendar.createInstance(hireDate.getTime());
        hireCal.addDays(deadlineDays);
        long daysToSign = (hireCal.getTimeInMilliseconds() - PSPDate.getPSPTime().getTimeInMilliseconds()) / (24 * 60 * 60 * 1000);

        documentCreationInfo.setDaysUntilSigningDeadline((int)daysToSign);

        ArrayOfDocumentKey docKeys = getService().sendDocument(getApiKey(), null, documentCreationInfo);
        return docKeys.getDocumentKey().get(0).getDocumentKey();

    }

    private static String nameToFileName(String name) {
        return WordUtils.capitalizeFully(name).replaceAll("\\W", "");
    }

    public static Map<String, DocumentSigners> getDocumentUpdates(List<String> docIds) {
        Map<String, DocumentSigners> documentSigners = new HashMap<String, DocumentSigners>();
        for (String docId : docIds) {
            DocumentInfo documentInfo = getService().getDocumentInfo(getApiKey(), docId);

            DocumentSigners ds = new DocumentSigners();
            ds.setDocId(docId);

            List<String> signersRemaining = new ArrayList<String>();
            for (NextParticipantInfo nextParticipantInfo : documentInfo.getNextParticipantInfos().getNextParticipantInfo()) {
                signersRemaining.add(nextParticipantInfo.getEmail());
            }
            ds.setSignersRemaining(signersRemaining);

            documentSigners.put(docId, ds);
        }
        return documentSigners;
    }

    public static byte[] getLatestSignedDocument(String docId) {
        return getService().getLatestDocument(getApiKey(), docId);
    }

    private static String getApiKey() {
        return ConfigurationManager.getSettingValue
                (ConfigurationModule.TaxCreditsAdapter, "echoSignApiKey");
    }

}
