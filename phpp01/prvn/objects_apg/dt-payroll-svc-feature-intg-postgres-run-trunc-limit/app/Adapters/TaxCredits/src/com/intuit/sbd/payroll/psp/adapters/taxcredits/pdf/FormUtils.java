package com.intuit.sbd.payroll.psp.adapters.taxcredits.pdf;

import com.intuit.sbd.payroll.psp.Application;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Jan 14, 2010
 * Time: 5:23:18 PM
 */
public class FormUtils {

    //combines all non-null PDFs
    public static byte[] combinePdfs(List<byte[]> pdfs) throws Exception {
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        int pageOffset = 0;
        ArrayList<HashMap> master = new ArrayList<HashMap>();
        Document document = null;
        PdfCopy writer = null;
        boolean firstFile = true;
        for (byte[] pdfBytes : pdfs) {
            if (pdfBytes != null && pdfBytes.length > 0) {
                PdfReader reader = new PdfReader(pdfBytes);
                reader.consolidateNamedDestinations();
                int numberOfPages = reader.getNumberOfPages();
                List<HashMap> bookmarks = SimpleBookmark.getBookmark(reader);
                if (bookmarks != null) {
                    if (pageOffset != 0)
                        SimpleBookmark.shiftPageNumbers
                                (bookmarks, pageOffset, null);
                    master.addAll(bookmarks);
                }
                pageOffset += numberOfPages;
                if (firstFile) {
                    firstFile = false;
                    document = new Document(reader.getPageSizeWithRotation(1));
                    writer = new PdfCopy(document, byteArrayOutputStream);
                    document.open();
                }
                PdfImportedPage page;
                for (int i = 0; i < numberOfPages; ) {
                    ++i;
                    page = writer.getImportedPage(reader, i);
                    writer.addPage(page);
                }
                PRAcroForm form = reader.getAcroForm();
                if (form != null) {
                    writer.copyAcroForm(reader);
                }
            }
        }

        if (!master.isEmpty() && writer != null) {
            writer.setOutlines(master);
        }
        if(document != null) {
            document.close();
        }

        return byteArrayOutputStream.toByteArray();
    }
    
    // util
    public static byte[] getBytesFromFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);

        // Get the size of the file
        long length = file.length();

        // You cannot create an array using a long type.
        // It needs to be an int type.
        // Before converting to an int type, check
        // to ensure that file is not larger than Integer.MAX_VALUE.
        if (length > Integer.MAX_VALUE) {
            // File is too large
        }

        // Create the byte array to hold the data
        byte[] bytes = new byte[(int)length];

        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
                && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
            offset += numRead;
        }

        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file "+file.getName());
        }

        // Close the input stream and return bytes
        is.close();
        return bytes;
    }

    public static byte[] flatten(byte[] pdf) throws Exception {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PdfReader reader = new PdfReader(pdf);
        PdfStamper stamper = new PdfStamper(reader, byteArrayOutputStream);
        stamper.setFormFlattening(true);
        stamper.close();        
        return byteArrayOutputStream.toByteArray();
    }
}
