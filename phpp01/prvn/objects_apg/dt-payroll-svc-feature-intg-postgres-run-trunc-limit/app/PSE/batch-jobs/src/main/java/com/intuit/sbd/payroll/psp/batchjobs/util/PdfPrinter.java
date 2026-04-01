package com.intuit.sbd.payroll.psp.batchjobs.util;

import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;
import com.sun.pdfview.PDFRenderer;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.AttributeSet;
import javax.print.attribute.HashAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.standard.PrinterName;
import javax.print.attribute.standard.Sides;
import java.awt.*;
import java.awt.print.Book;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class PdfPrinter {

    private String printJobName;
    private String printerName;

    public PdfPrinter(String printerName, String printJobName) {
        this.printerName = printerName;
        this.printJobName = printJobName;
    }

    public void printPdf(String pdfFileName, boolean isDuplex) {
        try {
            // Create a PDFFile from a File reference
            FileInputStream fis = new FileInputStream(pdfFileName);
            byte[] pdfContent = new byte[fis.available()];

            printPdf(pdfContent, isDuplex);
        }
        catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public void printPdf(byte[] pdfContent, boolean isDuplex) {
        try {
            PrinterJob pjob = createPrintJob(pdfContent, printJobName, isDuplex);
            PrintRequestAttributeSet printRequestAttributeSet = new HashPrintRequestAttributeSet();
            if(isDuplex) {
                printRequestAttributeSet.add(Sides.DUPLEX);
            }
            pjob.print(printRequestAttributeSet);
        }
        catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }


    /**
     * Initializes the job
     *
     * @param pdfContent
     * @param jobName
     * @throws IOException
     * @throws PrinterException
     */
    private PrinterJob createPrintJob(byte[] pdfContent, String jobName, boolean isDuplex) throws IOException, PrinterException {
        ByteBuffer bb = ByteBuffer.wrap(pdfContent);
        // Create PDF Print Page
        PDFFile pdfFile = new PDFFile(bb);
        PDFPrintPage pages = new PDFPrintPage(pdfFile);

        // Create Print Job
        PrinterJob printerJob = PrinterJob.getPrinterJob();
        printerJob.setPrintService(getPdfPrintService(isDuplex));

        PageFormat pf = PrinterJob.getPrinterJob().defaultPage();
        printerJob.setJobName(jobName);
        Book book = new Book();
        book.append(pages, pf, pdfFile.getNumPages());
        printerJob.setPageable(book);

        // to remove margins
        Paper paper = new Paper();
        paper.setImageableArea(0, 0, paper.getWidth(), paper.getHeight());
        pf.setPaper(paper);

        return printerJob;
    }

    private PrintService getPdfPrintService(boolean isDuplex) {
        AttributeSet printerAttributeSet = new HashAttributeSet();
        printerAttributeSet.add(new PrinterName(printerName, null));
        if(isDuplex) {
            printerAttributeSet.add(Sides.DUPLEX);
        }
        StringBuffer printersFound = new StringBuffer();
        PrintService[] services =  PrintServiceLookup.lookupPrintServices(null, printerAttributeSet);         //PrinterJob.lookupPrintServices(null, aset3);
        for (PrintService service : services) {
            printersFound.append(service.getName() + " ");
            if (service.getName().equals(printerName)) {
                return service;
            }
        }
        throw new RuntimeException("Could not find specified printer: " + printerName + " Printers foound: " + printersFound.toString());
    }


    /**
     * Class that actually converts the PDF file into Printable format
     */
    private class PDFPrintPage implements Printable {

        private PDFFile file;

        PDFPrintPage(PDFFile file) {
            this.file = file;
        }

        public int print(Graphics g, PageFormat format, int index) throws PrinterException {
            int pagenum = index + 1;
            if ((pagenum >= 1) && (pagenum <= file.getNumPages())) {
                Graphics2D g2 = (Graphics2D) g;
                PDFPage page = file.getPage(pagenum);

                // fit the PDFPage into the printing area
                Rectangle imageArea = new Rectangle((int) format.getImageableX(), (int) format.getImageableY(),
                        (int) format.getImageableWidth(), (int) format.getImageableHeight());
                g2.translate(0, 0);
                PDFRenderer pgs = new PDFRenderer(page, g2, imageArea, null, null);
                try {
                    page.waitForFinish();
                    pgs.run();
                } catch (InterruptedException ie) {
                    // nothing to do
                }
                return PAGE_EXISTS;
            } else {
                return NO_SUCH_PAGE;
            }
        }
    }
}

