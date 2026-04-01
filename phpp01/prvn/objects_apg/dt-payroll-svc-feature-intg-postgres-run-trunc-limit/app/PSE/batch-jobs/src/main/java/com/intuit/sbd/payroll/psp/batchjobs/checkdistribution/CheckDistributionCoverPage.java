package com.intuit.sbd.payroll.psp.batchjobs.checkdistribution;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.CheckPrintDTO;
import com.intuit.sbd.payroll.psp.batchjobs.util.PrintConstants;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.Barcode128;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Feb 27, 2010
 * Time: 12:22:41 PM
 */
public class CheckDistributionCoverPage {
    private static final SpcfLogger logger = PayrollServices.getLogger(CheckDistributionCoverPage.class);    

    private Document mDocument;

    public CheckDistributionCoverPage(Document pDocument) {
        mDocument = pDocument;
    }

    public byte[] generateCoverPage(CheckPrintDTO checkPrintDTO, Date currentDate, boolean isTest) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            SimpleDateFormat formatterBarcode = new SimpleDateFormat("yyyyMMddHHmmss");
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd HHmmss");
            String currentDateString = formatter.format(currentDate);

            PdfWriter writer = PdfWriter.getInstance(mDocument, outputStream);
            mDocument.open();

            mDocument.add(createSenderAddressTable(checkPrintDTO, isTest));
            if(!isTest) {
                mDocument.add(createBarcodeTable(checkPrintDTO, formatterBarcode.format(currentDate), writer));
            }
            mDocument.add(createShipperInformationTable(checkPrintDTO, currentDateString));
            mDocument.add(createConfidentialTable(isTest));
            mDocument.add(createPayrollAdminTable(checkPrintDTO));

            mDocument.close();
        } catch (DocumentException e) {
            logger.error("Error in generateCoverPage", e);            
        }

        return outputStream.toByteArray();
    }

    private PdfPTable createSenderAddressTable(CheckPrintDTO checkPrintDTO, boolean isTest) throws DocumentException {
        PdfPTable table = new PdfPTable(3);
        table.setTotalWidth(new float[]{1.5f, 6f, 1.5f});

        PdfPCell from = create_0padding_noBorder_cell("FROM: ", PrintConstants.FONT_HELVETICA_BOLD_16PT);
        from.setPaddingRight(5);
        from.setVerticalAlignment(PdfPCell.ALIGN_TOP);
        table.addCell(from);
        PdfPCell sender = create_0padding_noBorder_cell(checkPrintDTO.getSenderName(), PrintConstants.FONT_HELVETICA_NORMAL_16PT);
        sender.setVerticalAlignment(PdfPCell.ALIGN_TOP);
        table.addCell(sender);
        if(!isTest) {
            table.addCell(create_0padding_noBorder_cell(Integer.toString(checkPrintDTO.getPaychecks().size()), PrintConstants.FONT_HELVETICA_NORMAL_12PT));
        }
        else {
            table.addCell(create_0padding_noBorder_cell("Test", PrintConstants.FONT_HELVETICA_NORMAL_12PT));
        }

        table.addCell(create_0padding_noBorder_cell(" ", PrintConstants.FONT_HELVETICA_NORMAL_16PT));
        table.addCell(create_0padding_noBorder_cell(checkPrintDTO.getSenderAddress().getAddressLine1(), PrintConstants.FONT_HELVETICA_NORMAL_16PT));
        table.addCell(create_0padding_noBorder_cell(" ", PrintConstants.FONT_HELVETICA_NORMAL_16PT));

        if(!isStringEmpty(checkPrintDTO.getSenderAddress().getAddressLine2())) {
            table.addCell(create_0padding_noBorder_cell(" ", PrintConstants.FONT_HELVETICA_NORMAL_16PT));
            table.addCell(create_0padding_noBorder_cell(checkPrintDTO.getSenderAddress().getAddressLine2(), PrintConstants.FONT_HELVETICA_NORMAL_16PT));
            table.addCell(create_0padding_noBorder_cell(" ", PrintConstants.FONT_HELVETICA_NORMAL_16PT));
        }

        String city = checkPrintDTO.getSenderAddress().getCity();
        String state = checkPrintDTO.getSenderAddress().getState();
        String zip = checkPrintDTO.getSenderAddress().getZipCode();
        String cityStZip = city + "," + " " + state + " " + zip;
        table.addCell(create_0padding_noBorder_cell(" ", PrintConstants.FONT_HELVETICA_NORMAL_16PT));
        table.addCell(create_0padding_noBorder_cell(cityStZip, PrintConstants.FONT_HELVETICA_NORMAL_16PT));
        table.addCell(create_0padding_noBorder_cell(" ", PrintConstants.FONT_HELVETICA_NORMAL_16PT));

        table.setWidthPercentage(70);
        table.setSpacingAfter(30);
        return table;
    }

    private PdfPTable createBarcodeTable(CheckPrintDTO checkPrintDTO, String pCurrentDate, PdfWriter pWriter) throws DocumentException {
        PdfPTable tableBarcode = new PdfPTable(1);
        PdfContentByte cb = pWriter.getDirectContent();

        Barcode128 barCode128 = new Barcode128();
        barCode128.setCode("PS" + checkPrintDTO.getCompanyId() + pCurrentDate);
        barCode128.setAltText(" ");
        barCode128.setBarHeight(12);
        Image barcodeImage = barCode128.createImageWithBarcode(cb, null, null);

        PdfPCell cellBarcode = new PdfPCell(barcodeImage);
        cellBarcode.setPadding(0);
        cellBarcode.setBorder(PdfPCell.NO_BORDER);
        cellBarcode.setVerticalAlignment(PdfPCell.ALIGN_BOTTOM);
        tableBarcode.addCell(cellBarcode);

        tableBarcode.setWidthPercentage(70);

        return tableBarcode;
    }

    private PdfPTable createShipperInformationTable(CheckPrintDTO checkPrintDTO, String pCurrentDate) throws DocumentException {
        PdfPTable upsTable = new PdfPTable(2);

        PdfPCell cellUpsNextDay = create_0padding_noBorder_cell("UPS NEXT DAY", PrintConstants.FONT_HELVETICA_NORMAL_8PT);
        cellUpsNextDay.setPaddingTop(4);
        upsTable.addCell(cellUpsNextDay);

        PdfPCell cellcompIdDateTime = create_0padding_noBorder_cell("|" + checkPrintDTO.getCompanyId() +
                "|" + pCurrentDate, PrintConstants.FONT_HELVETICA_NORMAL_12PT);
        upsTable.addCell(cellcompIdDateTime);
        upsTable.setSpacingAfter(5);        
        upsTable.setWidthPercentage(70);

        return upsTable;
    }

    private PdfPTable createConfidentialTable(boolean isTest) throws DocumentException {
        PdfPTable confTable = new PdfPTable(1);
        PdfPTable confTableMain = new PdfPTable(1);

        PdfPCell confCell;
        if(!isTest) {
            confCell = create_0padding_noBorder_cell("CONFIDENTIAL", PrintConstants.FONT_HELVETICA_NORMAL_38PT);
        }
        else {
            confCell = create_0padding_noBorder_cell("Test Print", PrintConstants.FONT_HELVETICA_NORMAL_38PT);
        }
        confCell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        confCell.setPaddingBottom(2);
        confTable.addCell(confCell);

        PdfPCell authCell = create_0padding_noBorder_cell("To be opened only by Authorized Personnel", PrintConstants.FONT_HELVETICA_NORMAL_18PT);
        authCell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        authCell.setPaddingBottom(5);
        confTable.addCell(authCell);

        confTable.setWidthPercentage(100);

        PdfPCell confCellMain = new PdfPCell(confTable);
        confCellMain.setBackgroundColor(Color.LIGHT_GRAY);
        confCellMain.setBorder(PdfPCell.NO_BORDER);
        confTableMain.addCell(confCellMain);

        confTableMain.setSpacingAfter(30);
        confTableMain.setWidthPercentage(100);

        return confTableMain;
    }

    private PdfPTable createPayrollAdminTable(CheckPrintDTO checkPrintDTO) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setTotalWidth(new float[]{1.2f, 6.8f});

        PdfPCell fromCell = create_0padding_noBorder_cell("ATTN: ", PrintConstants.FONT_HELVETICA_BOLD_16PT);
        fromCell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
        fromCell.setVerticalAlignment(PdfPCell.ALIGN_TOP);
        fromCell.setPaddingRight(5);
        table.addCell(fromCell);

        PdfPCell payrollAdmin = create_0padding_noBorder_cell(checkPrintDTO.getPayrollAdminName(), PrintConstants.FONT_HELVETICA_NORMAL_16PT);
        payrollAdmin.setVerticalAlignment(PdfPCell.ALIGN_TOP);
        table.addCell(payrollAdmin);

        table.addCell(create_0padding_noBorder_cell(" ", PrintConstants.FONT_HELVETICA_NORMAL_16PT));
        table.addCell(create_0padding_noBorder_cell(checkPrintDTO.getCompanyLegalAddress().getAddressLine1(), PrintConstants.FONT_HELVETICA_NORMAL_16PT));

        if(!isStringEmpty(checkPrintDTO.getCompanyLegalAddress().getAddressLine2())) {
            table.addCell(create_0padding_noBorder_cell(" ", PrintConstants.FONT_HELVETICA_NORMAL_16PT));
            table.addCell(create_0padding_noBorder_cell(checkPrintDTO.getCompanyLegalAddress().getAddressLine2(), PrintConstants.FONT_HELVETICA_NORMAL_16PT));
        }

        String cityAdmin = checkPrintDTO.getCompanyLegalAddress().getCity();
        String stateAdmin = checkPrintDTO.getCompanyLegalAddress().getState();
        String zipAdmin = checkPrintDTO.getCompanyLegalAddress().getZipCode();
        String cityStZipAdmin = cityAdmin + "," + " " + stateAdmin + " " + zipAdmin;
        table.addCell(create_0padding_noBorder_cell(" ", PrintConstants.FONT_HELVETICA_NORMAL_16PT));
        table.addCell(create_0padding_noBorder_cell(cityStZipAdmin, PrintConstants.FONT_HELVETICA_NORMAL_16PT));

        table.setWidthPercentage(70);
        table.setSpacingBefore(42);

        return table;
    }

    private PdfPCell create_0padding_noBorder_cell(String pText, Font pFont) {
        PdfPCell cell = new PdfPCell(new Paragraph(pText, pFont));
        cell.setPadding(0);
        cell.setBorder(PdfPCell.NO_BORDER);
        return cell;
    }

    private boolean isStringEmpty(String string) {
        return !(string != null && string.trim().length() > 0);
    }
}
