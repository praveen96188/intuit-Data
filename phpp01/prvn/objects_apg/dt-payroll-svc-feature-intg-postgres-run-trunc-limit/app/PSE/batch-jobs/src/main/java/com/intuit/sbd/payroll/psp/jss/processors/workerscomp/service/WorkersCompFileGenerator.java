package com.intuit.sbd.payroll.psp.jss.processors.workerscomp.service;

import com.intuit.sbd.payroll.psp.common.pgp.PgpWriter;
import com.intuit.sbd.payroll.psp.common.pgp.impl.PGPWriterAESWithMultipleKeys;
import com.intuit.sbd.payroll.psp.jss.processors.workerscomp.model.PayrollDtoCompanyFileInfo;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.springframework.stereotype.Component;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

@Component
public abstract class WorkersCompFileGenerator<T> {
    private static final SpcfLogger logger = SpcfLogManager.getLogger(WorkersCompFileGenerator.class);
    private static ThreadLocal<Marshaller> mMarshaller = new ThreadLocal<Marshaller>();
    private static final boolean XML_XSD_VALIDATION= true;

    protected abstract String getXMLFileName(T payroll);
    private String expected_Dir = "/apps/batch/flux/lib/resources/";
    protected abstract List<String> getWCPgpKeys();
    protected abstract String getFileName();
    protected abstract String getFilePath();
    public String generateEncrptFilesFromPayrollObject(T payroll) throws Exception {
        logger.info("Creating XML files from payroll...");

        String fileName = getXMLFileName(payroll);

        convertObjectToXMLFileAndPGPEncrypt(payroll, fileName);
        logger.info("Created XML files from payroll");
        return fileName;
    }


    private void convertObjectToXMLFileAndPGPEncrypt(Object object, String fileNameWithoutExtension) throws Exception {
        PgpWriter fileWriter=null;
        try
        {
            logger.info("Action=convertObjectToXMLFile, status=start");

            String xmlFileName = getFilePath()+fileNameWithoutExtension.concat(".pgp");

            JAXBContext jaxbContext = JAXBContext.newInstance(object.getClass());
            mMarshaller.set(jaxbContext.createMarshaller());
            mMarshaller.get().setProperty("jaxb.formatted.output", true);

            //use PGPWriter to write it
            Writer writer = new StringWriter();
            mMarshaller.get().marshal(object, writer);
            if(XML_XSD_VALIDATION) {
                xmlSchemaValidator(new File(expected_Dir,getFileName()), writer);
            }
            fileWriter = new PGPWriterAESWithMultipleKeys(getWCPgpKeys());
            fileWriter.open(xmlFileName);
            fileWriter.write(writer.toString());

        }
        catch (JAXBException e)
        {
            logger.error("JAXB exception");
            throw e;
        }finally {
            if(fileWriter != null) {
                fileWriter.flush();
                fileWriter.close();
            }
        }
    }
    private void xmlSchemaValidator(File xsdFile, Writer writer)  {
        try {
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = schemaFactory.newSchema(new StreamSource(xsdFile));
            Validator validator = schema.newValidator();
            validator.validate(new StreamSource(writer.toString()));
        } catch (Exception e) {
            logger.info("xml schema validation failed filename=",e,xsdFile.getName());
        }
    }
}
