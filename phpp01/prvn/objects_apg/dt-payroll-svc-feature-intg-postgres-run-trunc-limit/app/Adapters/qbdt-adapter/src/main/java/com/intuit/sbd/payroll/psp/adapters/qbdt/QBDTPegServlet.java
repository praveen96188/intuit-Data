package com.intuit.sbd.payroll.psp.adapters.qbdt;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.adapters.qbdt.socket.ISocketManager;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.common.OFXManager;
import com.intuit.sbd.payroll.psp.common.OfxConversionOptions;
import com.intuit.sbd.payroll.psp.common.ofx.response.OFX;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.context.PSPRequestContextManagerHelper;
import com.intuit.sbd.payroll.psp.domain.SystemParameter;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.zip.Inflater;

/**
 * @version 1.0
 * @created 27-Jan-2008 10:27:27
 */

public class QBDTPegServlet extends HttpServlet {
    public static final String QB_COMPRESS_CONFIG_STR = "QBDataCompressionEnabled";
    public static final String BLAST_COMPRESSION = "1";
    public static final String ZLIB_COMPRESSION = "2";
    private ISocketManager mISocketManager = null;

    private SpcfLogger logger = PayrollServices.getLogger(this.getClass());

    /**
     * QBDTPegServlet
     */
    public QBDTPegServlet() {

    }

    // this method should only be used for testing
    public QBDTPegServlet(ISocketManager pISocketManager) {
        mISocketManager = pISocketManager;
    }

    /**
     * @param pRequest  - Http request
     * @param pResponse - Http Response
     */
    protected void doGet(HttpServletRequest pRequest, HttpServletResponse pResponse) {
        processesRequest(pRequest, pResponse);
    }

    /**
     * @param pRequest  - Http request
     * @param pResponse - Http Response
     */
    protected void doPost(HttpServletRequest pRequest, HttpServletResponse pResponse) {
        processesRequest(pRequest, pResponse);
    }

    /**
     * @param pRequest  - Http request
     * @param pResponse - Http Response
     */
    protected void processesRequest(HttpServletRequest pRequest, HttpServletResponse pResponse) {
        boolean enableCompression = Boolean.parseBoolean(ConfigurationManager.getSettingValue
                (ConfigurationModule.QBDTAdapter, QB_COMPRESS_CONFIG_STR));

        // This needs to be executed for each Thread JBoss creates.
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.QBDTAdapter));

        QBDTProcessResult<String> ofxFromHTTPStreamPR;
        try {

            if (pRequest == null) {
                throw new Exception("HttpServletRequest was null.");
            }

            String compressionStr = pRequest.getHeader("intuit-peg-compression");
            ofxFromHTTPStreamPR = convertRequestStreamToString(compressionStr, pRequest.getInputStream());
            // Get client's IP address
            String clientIP = getClientIP(pRequest.getHeader("X-Forwarded-For"));
            String requestStr = ofxFromHTTPStreamPR.getResult();

            //logger.info("Original OFX request:\r\n"+requestStr);

            QBDTProcessResult<String> psidResult = QBDTRequestProcessor.retrieveCompanyPSIDFromRequestString(requestStr);
            String companyPSID = null;
            if (psidResult.getResult() != null) {
                companyPSID = psidResult.getResult();
                try {
                    PayrollServices.beginUnitOfWork();
                    String parameterCode = "UNCOMPRESSED_OFX_" + companyPSID;
                    SystemParameter systemParameter = SystemParameter.findSystemParameter(parameterCode);
                    if (systemParameter != null) {
                        enableCompression = false;
                    }
                } catch (Throwable t) {
                    logger.error("unable to read UNCOMPRESSED_OFX system record for PSID = " + companyPSID, t);
                } finally {
                    PayrollServices.rollbackUnitOfWork();
                }
            }

            if (!ofxFromHTTPStreamPR.isSuccess()) {
                ErrorMessage errMsg = ofxFromHTTPStreamPR.getMessage();
                logger.warn(errMsg.getTransmissionErrorDescription());
                OFX responseOFX = ProcessingErrorHandler.handleSignOnError(errMsg);
                String responseStr = OFXManager.javaToOFX(responseOFX, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
                populateHTTPResponse(pResponse, responseStr, enableCompression);
                return;
            } else if (companyPSID == null) {
                logger.warn("The following request was received without a PSID: " + requestStr);
                com.intuit.sbd.payroll.psp.common.ofx.response.OFX rtnOFX = ProcessingErrorHandler.handleSignOnError(psidResult.getMessage());
                String responseStr = OFXManager.javaResponseToOFX(rtnOFX, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
                populateHTTPResponse(pResponse, responseStr, enableCompression);
                return;
            }

            if (requestStr != null && requestStr.lastIndexOf("</OFX>") == -1) {
                recordPSIDForUncompressedTraffic(companyPSID);
                enableCompression = false;
                String responseStr =
                        OFXManager.javaResponseToOFX(
                                ProcessingErrorHandler.handleOfxParsingError(
                                        ErrorMessages.BadOFXError("Missing ending OFX tag."), requestStr, null), OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
                populateHTTPResponse(pResponse, responseStr, enableCompression);
                return;
            }

            QBDTRequestProcessor qbdtRequestProcessor;
            if (mISocketManager == null) {
                qbdtRequestProcessor = new QBDTRequestProcessor();
            } else {
                qbdtRequestProcessor = new QBDTRequestProcessor(mISocketManager);
            }

            String responseStr = qbdtRequestProcessor.processRequest(requestStr, companyPSID, clientIP);
            populateHTTPResponse(pResponse, responseStr, enableCompression);

        } catch (Throwable t) {
            try {
                String responseStr;
                if (t instanceof CompressionException) {
                    responseStr = ProcessingErrorHandler.getUnsupportedCompressionProcessingErrorString();
                } else {
                    logger.warn(t.getMessage(), t);
                    responseStr = ProcessingErrorHandler.getUnrecoverableProcessingErrorString();
                }
                populateHTTPResponse(pResponse, responseStr, enableCompression);
            } catch (Throwable t1) {
                logger.error(t1.getMessage(), t1);
            }
        } finally {
            PSPRequestContextManagerHelper.getPSPRequestContextManager().clearRequestContext();
            logger.info("Event=ClearRequestContext Type=OFX Status=Completed");
        }
    }

    /**
     * Populate the HTTP resposne object passed in.
     *
     * @param pResponse   - Http Response
     * @param responseStr - Response OFX String
     * @throws IOException - Thrown if there is a problem writing to Http response.
     */
    private void populateHTTPResponse(HttpServletResponse pResponse, String responseStr, boolean enableCompression) throws IOException {
        pResponse.setContentType("application/x-ofx");
        if (enableCompression) {
            pResponse.setHeader("Intuit-PEG-Compression", "1");
        }
        PrintWriter printWriter = pResponse.getWriter();
        printWriter.write(responseStr);
    }

    /**
     * Convert the input stream into a String.
     *
     * @param compressed
     * @param inputStream
     * @return
     * @throws IOException
     */
    QBDTProcessResult<String> convertRequestStreamToString(String compressed, InputStream inputStream) throws IOException, CompressionException {
        QBDTProcessResult<String> processResult = new QBDTProcessResult<String>();

        String rtnStr = "";
        if (compressed != null && compressed.length() > 0) {

            byte[] compressedData = readCompressedDataFromStream(inputStream);
            try {
                if (BLAST_COMPRESSION.equals(compressed)) {
                    throw new CompressionException("Unsupported compression - Blast");
                } else if (ZLIB_COMPRESSION.equals(compressed)) {
                    rtnStr = zlibDecompression(compressedData);
                }
            } catch (Throwable t) {
                if (t instanceof CompressionException) {
                    throw (CompressionException) t;
                } else {
                    logger.error("failed to uncompresss stream   type: " + compressed + "   message: " + t.getMessage());
                    processResult.setMessage(ErrorMessages.ErrorUncompressingHTTPRequest());
                    return processResult;
                }
            }
            compressedData = null;
        } else {

            StringBuilder request = new StringBuilder();
            CharBuffer charBuffer = CharBuffer.allocate(4096);

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            while (bufferedReader.read(charBuffer) != -1) {
                charBuffer.flip();
                request.append(charBuffer.toString());
                charBuffer.clear();
            }

            rtnStr = request.toString();
        }

        if (rtnStr != null) {
            rtnStr = rtnStr.trim();
        }

        processResult.setResult(rtnStr);
        return processResult;
    }

    private static String getClientIP(String pXForwardHeader) {
        String clientIP = pXForwardHeader;
        if (clientIP != null && clientIP.length() > 20) {
            clientIP = clientIP.substring(0, 20);
        }
        if (clientIP != null && clientIP.contains(",")) {
            clientIP = clientIP.substring(0, clientIP.indexOf(","));
        }
        return clientIP;
    }

    private static String zlibDecompression(byte[] pCompressedData) throws Exception {
        Inflater inflater = new Inflater();
        inflater.setInput(pCompressedData);

        ByteArrayOutputStream bos = new ByteArrayOutputStream(pCompressedData.length);

        byte[] buf = new byte[1024];
        while (!inflater.finished()) {
            int count = inflater.inflate(buf);
            bos.write(buf, 0, count);
        }
        bos.close();

        return bos.toString().trim();
    }

    /**
     * Read Pkware DCL compressed data stream from the input stream.
     *
     * @throws IOException
     */
    private static byte[] readCompressedDataFromStream(InputStream inputStream) throws IOException {
        DataInputStream dataStream = new DataInputStream(new BufferedInputStream(inputStream));

        // 250 4K blocks is approximately 1 MB
        ByteBuilder bufferedBlocks = new ByteBuilder(250, 4096);
        try {
            while (true) {
                bufferedBlocks.append(dataStream.readByte());
            }
        } catch (EOFException eof) {
        }

        byte[] compressedData = bufferedBlocks.toArray();
        return compressedData;
    }

    /**
     * Store a record in the SystemParameter table that marks the PSID associated
     * with this request as a company that requires uncompressed traffic.
     * <p/>
     * This is to work around the issue where the JNIBlast wrapper code cannot correctly
     * decompress and return valid OFX.  This flag instructs PSP to direct the QBDT client to send
     * OFX uncompressed in future transmissions.
     */
    private void recordPSIDForUncompressedTraffic(final String companyPSID) {
        logger.info("marking PSID: " + companyPSID + " for uncompressed traffic due to detection of malformed OFX.");
        String parameterCode = "UNCOMPRESSED_OFX_" + companyPSID;
        try {
            PayrollServices.beginUnitOfWork();
            SystemParameter systemParameter = SystemParameter.findSystemParameter(parameterCode);
            if (systemParameter == null) {
                SystemParameter newEntry = new SystemParameter();
                newEntry.setSystemParameterCd(parameterCode);
                newEntry.setSystemParameterValue("true");
                Application.save(newEntry);
            } else {
                logger.error("malformedOFX for PSID " + companyPSID + " that is using uncompressed stream");
            }
            PayrollServices.commitUnitOfWork();
        } catch (Throwable thrown) {
            logger.error("unable to create an UNCOMPRESSED_OFX_" + companyPSID + " record", thrown);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    public static void main(String[] args) throws Exception {

        try {
            long start = 0;
            long stop = 0;
            String decompresssed = "";
            byte[] compressedData;

            if (ZLIB_COMPRESSION.equals(args[0])) {
                FileInputStream fis = new FileInputStream("C:\\dev\\PSP\\rel-1.3\\Adapters\\QBDT\\test\\resources\\zlib_compression.dat");
                start = System.currentTimeMillis();
                compressedData = readCompressedDataFromStream(fis);
                decompresssed = zlibDecompression(compressedData);
                stop = System.currentTimeMillis();
            }

            compressedData = null;
            System.out.println("decompresssed = " + decompresssed.length());
            System.out.println("duration = " + (stop - start));
            System.out.println(decompresssed);
        } catch (Throwable t) {
            System.err.println("ERROR: " + t.getMessage());
            t.printStackTrace(System.err);
        }

    }
}

/**
 * Helper class for 'semi' efficiently aggregate blocks of bytes
 * read and converting them to a single byte array.
 */
class ByteBuilder {
    private int initialBlockCapacity;
    private int blockSize;

    private byte[] currentBuffer;
    private int bytesWritten;

    private ArrayList<byte[]> bufferedBlocks;


    ByteBuilder(int initialBlockCapacity, int blockSize) {
        this.initialBlockCapacity = initialBlockCapacity;
        this.blockSize = blockSize;

        bufferedBlocks = new ArrayList<byte[]>(initialBlockCapacity);
        resetCurrentBuffer();
    }

    public ByteBuilder append(byte b) {
        if (bytesWritten == currentBuffer.length) {
            bufferedBlocks.add(currentBuffer);
            resetCurrentBuffer();
        }
        currentBuffer[bytesWritten] = b;
        bytesWritten++;
        return this;
    }

    private void resetCurrentBuffer() {
        currentBuffer = new byte[blockSize];
        bytesWritten = 0;
    }

    public int size() {
        int size = 0;
        for (byte[] bufferedBlock : bufferedBlocks) {
            size += bufferedBlock.length;
        }

        if (currentBuffer != null)
            size += currentBuffer.length;

        return size;
    }

    public byte[] toArray() {
        return toArray(true);
    }

    public byte[] toArray(boolean clearBuffer) {
        byte[] byteArray = new byte[size()];

        int bytesCopied = 0;
        for (int i = 0; i < bufferedBlocks.size(); i++) {
            byte[] block = bufferedBlocks.get(i);
            System.arraycopy(block, 0, byteArray, bytesCopied, block.length);
            bytesCopied += block.length;
            // make the data block available for garbage collection
            if (clearBuffer)
                bufferedBlocks.set(i, null);
        }

        System.arraycopy(currentBuffer, 0, byteArray, bytesCopied, this.bytesWritten);

        if (clearBuffer) {
            bufferedBlocks.clear();
            bufferedBlocks = new ArrayList<byte[]>(initialBlockCapacity);
            resetCurrentBuffer();
        }

        return byteArray;
    }
}
