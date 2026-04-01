package com.intuit.sbd.payroll.psp.batchjobs.printedchecks;

import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.printedchecks.dtos.CheckDTO;
import com.intuit.sbd.payroll.psp.batchjobs.printedchecks.dtos.LineItemDTO;
import com.intuit.sbd.payroll.psp.batchjobs.printedchecks.dtos.PayeeDTO;
import com.intuit.sbd.payroll.psp.batchjobs.printedchecks.dtos.PayerDTO;
import com.intuit.sbd.payroll.psp.batchjobs.util.CheckUtils;
import com.intuit.sbd.payroll.psp.batchjobs.util.PrintConstants;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Jun 24, 2011
 * Time: 3:00:33 PM
 */
public class CheckTemplate {
    private static final SpcfLogger logger = PayrollServices.getLogger(CheckTemplate.class);

    private static final float[] STUB_WIDTHS = new float[]{1.5f, 1.05f, 3.2f, 0.5f, 1.25f, 1.55f};
    private static final int LEFT_ALIGN_PADDING = 0;
    private static final int RIGHT_ALIGN_PADDING = 0;

    private DecimalFormat mDecimalFormatter = new DecimalFormat(PrintConstants.MONEY_FORMAT);
    private DecimalFormat mZeroDecimalFormatter = new DecimalFormat(PrintConstants.ZERO_MONEY_FORMAT);

    // date formatter is not thread safe so each check creates its own
    private DateFormat mDateFormatter = new SimpleDateFormat("MM/dd/yyyy");

    private Document mDocument;
    private Font mMICRFont;

    public CheckTemplate(Document pDocument, Font pMICRFont) {
        mDocument = pDocument;
        mMICRFont = pMICRFont;
    }

    public byte[] generateManualCheck(CheckDTO pCheckDTO) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            PdfWriter.getInstance(mDocument, outputStream);
            mDocument.open();

            mDocument.add(createStubTable(pCheckDTO));
            mDocument.add(createCheckTable(pCheckDTO));

            mDocument.close();
        } catch (DocumentException e) {
            logger.error("Error in generateManualCheck", e);
        } catch (IOException ie) {
            logger.error("Error in generateManualCheck", ie);
        }

        return outputStream.toByteArray();
    }

    private PdfPTable createCheckTable(CheckDTO pCheckDTO) throws DocumentException, IOException {
        PdfPTable checkTable = new PdfPTable(1);
        checkTable.setWidthPercentage(100);

        // this has table has one fixed height cell that contains the check information
        // the cell is fixed height so that the MICR line does not move up or down if the information in the
        // check changes
        PdfPCell checkCell = new PdfPCell();
        checkCell.setPaddingTop(63);
        checkCell.setPaddingLeft(LEFT_ALIGN_PADDING);
        checkCell.setPaddingRight(RIGHT_ALIGN_PADDING);
        checkCell.setBorder(PdfPCell.NO_BORDER);
        checkCell.setFixedHeight(195);

        PdfPTable paycheckTable = new PdfPTable(1);
        paycheckTable.setWidthPercentage(100);

        PdfPCell topCell = new PdfPCell(createCheckTopTable(pCheckDTO));
        topCell.setBorder(PdfPCell.NO_BORDER);
        topCell.setFixedHeight(55);
        topCell.setPadding(0);
        paycheckTable.addCell(topCell);

        addTableToTable(paycheckTable, createTaxIdCheckDateAndAmountTable(pCheckDTO));

        PdfPCell netPayInWords = new PdfPCell(createAmountInWordsParagraph(pCheckDTO));
        netPayInWords.setBorder(PdfPCell.NO_BORDER);
        netPayInWords.setPadding(0);
        netPayInWords.setPaddingTop(5);
        netPayInWords.setPaddingBottom(5);
        netPayInWords.setNoWrap(true);
        paycheckTable.addCell(netPayInWords);

        PdfPCell payToOrderBankLogoAndSignature = new PdfPCell(createPayToTheOrderOfBankLogoAndSignatureTable(pCheckDTO));
        payToOrderBankLogoAndSignature.setVerticalAlignment(PdfPCell.ALIGN_TOP);
        payToOrderBankLogoAndSignature.setBorder(PdfPCell.NO_BORDER);
        payToOrderBankLogoAndSignature.setPadding(0);
        payToOrderBankLogoAndSignature.setFixedHeight(80);
        paycheckTable.addCell(payToOrderBankLogoAndSignature);

        checkCell.addElement(paycheckTable);

        checkTable.addCell(checkCell);

        PdfPCell micrCell = new PdfPCell(createMICRTable(pCheckDTO));
        micrCell.setBorder(PdfPCell.NO_BORDER);
        micrCell.setPadding(0);
        checkTable.addCell(micrCell);

        return checkTable;
    }

    private PdfPTable createCheckTopTable(CheckDTO pCheckDTO) throws DocumentException, IOException {
        PdfPTable table;
        if(pCheckDTO.getPayerDTO().getLogo() != null) {
            table = new PdfPTable(new float[]{1.5f, 3f, 2f});
        } else {
            table = new PdfPTable(new float[]{8f, 2f});
        }
        table.setWidthPercentage(100);

        PayerDTO payerDTO = pCheckDTO.getPayerDTO();

        if(pCheckDTO.getPayerDTO().getLogo() != null) {
            com.lowagie.text.Image logo = com.lowagie.text.Image.getInstance(pCheckDTO.getPayerDTO().getLogo());
            logo.scalePercent(25);

            PdfPCell logoCell = new PdfPCell(logo);
            logoCell.setPadding(0);
            logoCell.setPaddingRight(5);
            logoCell.setVerticalAlignment(PdfPCell.ALIGN_TOP);
            logoCell.setBorder(PdfPCell.NO_BORDER);
            table.addCell(logoCell);
        }
        
        PdfPTable payerAddressTable = new PdfPTable(1);
        create_0padding_noBorder_cell(payerAddressTable, payerDTO.getNameLine1(), PrintConstants.FONT_COURIER_BOLD_14PT);
        create_0padding_noBorder_cell(payerAddressTable, payerDTO.getNameLine2(), PrintConstants.FONT_COURIER_BOLD_10PT);
        create_0padding_noBorder_cell(payerAddressTable, payerDTO.getAddressLine1(), PrintConstants.FONT_COURIER_BOLD_10PT);
        create_0padding_noBorder_cell(payerAddressTable, payerDTO.getAddressLine2(), PrintConstants.FONT_COURIER_BOLD_10PT);
        create_0padding_noBorder_cell(payerAddressTable,
                                      formatCityStateZip(payerDTO.getCity(), payerDTO.getState(), payerDTO.getZip()),
                                      PrintConstants.FONT_COURIER_BOLD_10PT);
        addTableToTable(table, payerAddressTable);

        PdfPTable checkNumberTable = new PdfPTable(1);
        PdfPCell checkNumberCell = create_0padding_noBorder_cell(pCheckDTO.getCheckNumber(), PrintConstants.FONT_COURIER_BOLD_12PT);
        checkNumberCell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
        checkNumberCell.setVerticalAlignment(PdfPCell.ALIGN_TOP);
        checkNumberTable.addCell(checkNumberCell);
        addTableToTable(table, checkNumberTable);

        return table;
    }

    private Paragraph createAmountInWordsParagraph(CheckDTO pCheckDTO) throws DocumentException {
        String netPayInWords = customFormatAmountInWords(pCheckDTO.getCheckAmount());
        return new Paragraph("PAY " + netPayInWords, PrintConstants.FONT_COURIER_BOLD_12PT);
    }

    private PdfPTable createTaxIdCheckDateAndAmountTable(CheckDTO pCheckDTO) throws DocumentException, IOException {
        PdfPTable table = new PdfPTable(new float[]{1f, 4.2f, 2.8f, 2f, 2.6f});

        // padding cell left side
        PdfPCell cell = new PdfPCell(new Paragraph(" ", PrintConstants.FONT_COURIER_BOLD_10PT));
        cell.setPadding(0);
        cell.setNoWrap(true);
        cell.setBorder(PdfPCell.NO_BORDER);
        table.addCell(cell);

        // tax id table
        PdfPTable taxIdTable = new PdfPTable(1);
        PdfPCell taxIdHeader = new PdfPCell(new Paragraph("TAX ID", PrintConstants.FONT_COURIER_BOLD_8PT));
        taxIdHeader.setPadding(1);
        taxIdHeader.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        taxIdHeader.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
        taxIdHeader.setBorder(PdfPCell.BOX);
        taxIdTable.addCell(taxIdHeader);

        String taxId = "";
        if (!pCheckDTO.isSuperCheck()) {
            taxId = pCheckDTO.getTaxId();
            if(taxId.length() == 0) {
                taxId = "FEIN: " + pCheckDTO.getFEIN() + " " + pCheckDTO.getCompanyLegalName();
            }
            taxId = truncateString(taxId, 29);
        }


        PdfPCell taxIdCell = new PdfPCell(new Paragraph(taxId, PrintConstants.FONT_COURIER_BOLD_10PT));
        taxIdCell.setPaddingLeft(2);
        taxIdCell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        taxIdCell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
        taxIdCell.setBorder(CheckUtils.getBorder(PdfPCell.LEFT, PdfPCell.BOTTOM, PdfPCell.RIGHT));
        taxIdCell.setNoWrap(true);
        taxIdTable.addCell(taxIdCell);

        addTableToTable(table, taxIdTable);

        // padding cell middle
        cell = new PdfPCell(new Paragraph(" ", PrintConstants.FONT_COURIER_BOLD_10PT));
        cell.setPadding(0);
        cell.setNoWrap(true);
        cell.setBorder(PdfPCell.NO_BORDER);
        table.addCell(cell);

        // check date table
        PdfPTable checkDateTable = new PdfPTable(1);
        PdfPCell checkDateHeader = new PdfPCell(new Paragraph("CHECK DATE", PrintConstants.FONT_COURIER_BOLD_8PT));
        checkDateHeader.setPadding(1);
        checkDateHeader.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        checkDateHeader.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
        checkDateHeader.setBorder(PdfPCell.BOX);
        checkDateTable.addCell(checkDateHeader);

        PdfPCell checkDateCell = new PdfPCell(new Paragraph(mDateFormatter.format(pCheckDTO.getCheckDate()), PrintConstants.FONT_COURIER_BOLD_10PT));
        checkDateCell.setPadding(0);
        checkDateCell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        checkDateCell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
        checkDateCell.setBorder(CheckUtils.getBorder(PdfPCell.LEFT, PdfPCell.BOTTOM, PdfPCell.RIGHT));
        checkDateTable.addCell(checkDateCell);

        addTableToTable(table, checkDateTable);

        // amount table
        PdfPTable amountTable = new PdfPTable(1);
        PdfPCell amountHeader = new PdfPCell(new Paragraph("AMOUNT", PrintConstants.FONT_COURIER_BOLD_8PT));
        amountHeader.setPadding(1);
        amountHeader.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        amountHeader.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
        amountHeader.setBorder(PdfPCell.BOX);
        amountTable.addCell(amountHeader);

        PdfPCell amountCell = new PdfPCell(new Paragraph(customFormatPadding(pCheckDTO.getCheckAmount()), PrintConstants.FONT_COURIER_BOLD_10PT));
        amountCell.setPadding(0);
        amountCell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        amountCell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
        amountCell.setBorder(CheckUtils.getBorder(PdfPCell.LEFT, PdfPCell.BOTTOM, PdfPCell.RIGHT));
        amountTable.addCell(amountCell);

        addTableToTable(table, amountTable);

        return table;
    }

    private PdfPTable createPayToTheOrderOfBankLogoAndSignatureTable(CheckDTO pCheckDTO) throws DocumentException, IOException {
        PdfPTable table = new PdfPTable(2);
        table.setTotalWidth(new float[]{5f, 3f});

        // pay to the order of and payee address info
        PdfPTable payeeAddressOnCheck = new PdfPTable(2);
        payeeAddressOnCheck.setTotalWidth(new float[]{0.75f, 5f});

        PdfPTable payToTheOrderOf = new PdfPTable(1);
        PdfPCell toTheCell = new PdfPCell(new Paragraph("To The", PrintConstants.FONT_COURIER_BOLD_6PT));
        toTheCell.setPadding(0);
        toTheCell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
        toTheCell.setPaddingTop(7);
        toTheCell.setPaddingRight(15);
        toTheCell.setBorder(PdfPCell.NO_BORDER);
        payToTheOrderOf.addCell(toTheCell);

        PdfPCell orderOfCell = new PdfPCell(new Paragraph("Order Of", PrintConstants.FONT_COURIER_BOLD_6PT));
        orderOfCell.setPadding(0);
        toTheCell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
        orderOfCell.setPaddingTop(4);
        orderOfCell.setBorder(PdfPCell.NO_BORDER);
        payToTheOrderOf.addCell(orderOfCell);
        addTableToTable(payeeAddressOnCheck, payToTheOrderOf);

        PdfPCell cell = new PdfPCell(createPayeeAddressOnCheckTable(pCheckDTO));
        cell.setBorder(PdfPCell.NO_BORDER);
        cell.setPadding(0);
        cell.setFixedHeight(53);
        payeeAddressOnCheck.addCell(cell);

        if(pCheckDTO.getPayerDTO().getBankLogo() != null) {
            PdfPCell blankCell = new PdfPCell(new Paragraph(" ", PrintConstants.FONT_COURIER_BOLD_10PT));
            blankCell.setBorder(PdfPCell.NO_BORDER);
            payeeAddressOnCheck.addCell(blankCell);

            com.lowagie.text.Image bankLogo = com.lowagie.text.Image.getInstance(pCheckDTO.getPayerDTO().getBankLogo());
            bankLogo.scalePercent(12);

            PdfPCell bankLogoCell = new PdfPCell(bankLogo);
            bankLogoCell.setPadding(0);
            bankLogoCell.setPaddingTop(5);
            bankLogoCell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
            bankLogoCell.setBorder(PdfPCell.NO_BORDER);
            payeeAddressOnCheck.addCell(bankLogoCell);
        }
        addTableToTable(table, payeeAddressOnCheck);

        // signature
        PdfPTable signatureTable = new PdfPTable(1);

        com.lowagie.text.Image signature = com.lowagie.text.Image.getInstance(pCheckDTO.getPayerDTO().getSignature());
        signature.scalePercent(15);        

        PdfPCell signatureCell = new PdfPCell(signature);
        signatureCell.setPadding(0);
        signatureCell.setPaddingBottom(1);
        signatureCell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        signatureCell.setBorder(PdfPCell.NO_BORDER);
        signatureTable.addCell(signatureCell);

        PdfPCell authLineCell = new PdfPCell(new Paragraph("AUTHORIZED SIGNATURE", PrintConstants.FONT_HELVETICA_NORMAL_7PT));
        authLineCell.setPadding(0);
        authLineCell.setPaddingTop(0);
        authLineCell.setBorder(PdfPCell.TOP);
        authLineCell.setBorderWidthTop(1f);
        authLineCell.setBorderColorTop(Color.BLACK);
        authLineCell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        signatureTable.addCell(authLineCell);

        addTableToTable(table, signatureTable);

        table.setWidthPercentage(100);

        return table;
    }

    private PdfPTable createPayeeAddressOnCheckTable(CheckDTO pCheckDTO) {
        PayeeDTO payeeDTO = pCheckDTO.getPayeeDTO();

        PdfPTable payeeAddress = new PdfPTable(1);

        create_0padding_noBorder_cell(payeeAddress, truncateString(payeeDTO.getNameLine1(), PrintConstants.MAX_PAYEE_LENGTH), PrintConstants.FONT_COURIER_BOLD_10PT);
        create_0padding_noBorder_cell(payeeAddress, truncateString(payeeDTO.getNameLine2(), PrintConstants.MAX_PAYEE_LENGTH), PrintConstants.FONT_COURIER_BOLD_10PT);
        create_0padding_noBorder_cell(payeeAddress, truncateString(payeeDTO.getAddressLine1(), PrintConstants.MAX_PAYEE_LENGTH), PrintConstants.FONT_COURIER_BOLD_10PT);
        create_0padding_noBorder_cell(payeeAddress, truncateString(payeeDTO.getAddressLine2(), PrintConstants.MAX_PAYEE_LENGTH), PrintConstants.FONT_COURIER_BOLD_10PT);
        create_0padding_noBorder_cell(payeeAddress,
                                      formatCityStateZip(truncateString(payeeDTO.getCity(), PrintConstants.MAX_PAYEE_LENGTH - 15), payeeDTO.getState(), payeeDTO.getZip()),
                                      PrintConstants.FONT_COURIER_BOLD_10PT);
        return payeeAddress;
    }

    private PdfPTable createMICRTable(CheckDTO pCheckDTO) throws DocumentException {
        PdfPTable micrTable = new PdfPTable(3);
        micrTable.setTotalWidth(new float[]{2.2f, 2.700f, 5.590f});

        PdfPCell checkNumber = new PdfPCell(new Paragraph("C" + pCheckDTO.getCheckNumber() + "C", mMICRFont));
        checkNumber.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
        checkNumber.setPaddingRight(3);
        checkNumber.setBorder(PdfPCell.NO_BORDER);
        checkNumber.setVerticalAlignment(PdfPCell.ALIGN_CENTER);
        micrTable.addCell(checkNumber);

        PdfPCell routingNumber = new PdfPCell(new Paragraph("A" + pCheckDTO.getRoutingNumber() + "A", mMICRFont));
        routingNumber.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        routingNumber.setPaddingLeft(4);
        routingNumber.setBorder(PdfPCell.NO_BORDER);
        routingNumber.setVerticalAlignment(PdfPCell.ALIGN_CENTER);
        micrTable.addCell(routingNumber);

        PdfPCell accountNumber = new PdfPCell(new Paragraph(pCheckDTO.getBankAccountNumber() + "C", mMICRFont));
        accountNumber.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        accountNumber.setPaddingLeft(3);
        accountNumber.setBorder(PdfPCell.NO_BORDER);
        accountNumber.setVerticalAlignment(PdfPCell.ALIGN_CENTER);
        micrTable.addCell(accountNumber);

        PdfPTable paddingTable = new PdfPTable(1);
        PdfPCell paddingCell = new PdfPCell(micrTable);
        paddingCell.setPadding(0);
        paddingCell.setPaddingLeft(69);
        paddingCell.setBorder(PdfPCell.NO_BORDER);
        paddingTable.addCell(paddingCell);
        paddingTable.setWidthPercentage(100);

        return paddingTable;
    }


    private PdfPTable createStubTable(CheckDTO pCheckDTO) throws DocumentException {
        PdfPTable payStubTable = new PdfPTable(1);        
        payStubTable.setWidthPercentage(100);

        addTableToTable(payStubTable, createStubHeader(pCheckDTO));

        PdfPCell grayHeaderCell = new PdfPCell();
        grayHeaderCell.setBackgroundColor(Color.LIGHT_GRAY);
        grayHeaderCell.setPadding(0);
        grayHeaderCell.setFixedHeight(5);
        grayHeaderCell.setBorder(CheckUtils.getBorder(PdfPCell.LEFT, PdfPCell.BOTTOM, PdfPCell.RIGHT));
        payStubTable.addCell(grayHeaderCell);

        PdfPCell stubInfoCell = new PdfPCell(createStubInfoTable(pCheckDTO));
        stubInfoCell.setBorder(PdfPCell.NO_BORDER);
        stubInfoCell.setPadding(0);
        stubInfoCell.setFixedHeight(475);
        payStubTable.addCell(stubInfoCell);

        addTableToTable(payStubTable, createCheckTotalTable(pCheckDTO));

        return payStubTable;
    }

    private PdfPTable createStubHeader(CheckDTO pCheckDTO) {
        PdfPTable headerTable = new PdfPTable(3);

        PdfPTable printDateTable = new PdfPTable(1);
        PdfPCell printDateHeader = new PdfPCell(new Paragraph("PRINT DATE", PrintConstants.FONT_COURIER_BOLD_10PT));
        printDateHeader.setPadding(2);
        printDateHeader.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        printDateHeader.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
        printDateHeader.setBorder(PdfPCell.BOX);
        printDateTable.addCell(printDateHeader);

        PdfPCell printDateCell = new PdfPCell(new Paragraph(mDateFormatter.format(SpcfUtils.convertSpcfCalendarToDate(PSPDate.getPSPTime())), PrintConstants.FONT_COURIER_BOLD_10PT));
        printDateCell.setPadding(2);
        printDateCell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        printDateCell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
        printDateCell.setBorder(CheckUtils.getBorder(PdfPCell.LEFT, PdfPCell.BOTTOM, PdfPCell.RIGHT));
        printDateTable.addCell(printDateCell);

        addTableToTable(headerTable, printDateTable);

        PdfPTable checkDateTable = new PdfPTable(1);
        PdfPCell checkDateHeader = new PdfPCell(new Paragraph("CHECK DATE", PrintConstants.FONT_COURIER_BOLD_10PT));
        checkDateHeader.setPadding(2);
        checkDateHeader.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        checkDateHeader.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
        checkDateHeader.setBorder(CheckUtils.getBorder(PdfPCell.BOTTOM, PdfPCell.RIGHT, PdfPCell.TOP));
        checkDateTable.addCell(checkDateHeader);

        PdfPCell checkDateCell = new PdfPCell(new Paragraph(mDateFormatter.format(pCheckDTO.getCheckDate()), PrintConstants.FONT_COURIER_BOLD_10PT));
        checkDateCell.setPadding(2);
        checkDateCell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        checkDateCell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
        checkDateCell.setBorder(CheckUtils.getBorder(PdfPCell.BOTTOM, PdfPCell.RIGHT));
        checkDateTable.addCell(checkDateCell);

        addTableToTable(headerTable, checkDateTable);

        PdfPTable checkNumberTable = new PdfPTable(1);
        PdfPCell checkNumberHeader = new PdfPCell(new Paragraph("CHECK NUMBER", PrintConstants.FONT_COURIER_BOLD_10PT));
        checkNumberHeader.setPadding(2);
        checkNumberHeader.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        checkNumberHeader.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
        checkNumberHeader.setBorder(CheckUtils.getBorder(PdfPCell.BOTTOM, PdfPCell.RIGHT, PdfPCell.TOP));
        checkNumberTable.addCell(checkNumberHeader);

        PdfPCell checkNumberCell = new PdfPCell(new Paragraph(pCheckDTO.getCheckNumber(), PrintConstants.FONT_COURIER_BOLD_10PT));
        checkNumberCell.setPadding(2);
        checkNumberCell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        checkNumberCell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
        checkNumberCell.setBorder(CheckUtils.getBorder(PdfPCell.BOTTOM, PdfPCell.RIGHT));
        checkNumberTable.addCell(checkNumberCell);

        addTableToTable(headerTable, checkNumberTable);

        return headerTable;
    }

    private PdfPTable createStubInfoTable(CheckDTO pCheckDTO) {
        PdfPTable infoTable = new PdfPTable(STUB_WIDTHS);

        PdfPCell taxIdHeader = new PdfPCell(new Paragraph("TAX ID", PrintConstants.FONT_COURIER_BOLD_10PT));
        taxIdHeader.setPadding(2);
        taxIdHeader.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        taxIdHeader.setVerticalAlignment(PdfPCell.ALIGN_TOP);
        taxIdHeader.setBorder(CheckUtils.getBorder(PdfPCell.LEFT, PdfPCell.BOTTOM, PdfPCell.RIGHT));
        infoTable.addCell(taxIdHeader);

        PdfPCell companyHeader = new PdfPCell(new Paragraph("COMPANY", PrintConstants.FONT_COURIER_BOLD_10PT));
        companyHeader.setPadding(2);
        companyHeader.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        companyHeader.setVerticalAlignment(PdfPCell.ALIGN_TOP);
        companyHeader.setBorder(CheckUtils.getBorder(PdfPCell.BOTTOM, PdfPCell.RIGHT));
        infoTable.addCell(companyHeader);

        PdfPCell companyNameHeader = new PdfPCell(new Paragraph("COMPANY NAME", PrintConstants.FONT_COURIER_BOLD_10PT));
        companyNameHeader.setPadding(2);
        companyNameHeader.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        companyNameHeader.setVerticalAlignment(PdfPCell.ALIGN_TOP);
        companyNameHeader.setBorder(CheckUtils.getBorder(PdfPCell.BOTTOM, PdfPCell.RIGHT));
        infoTable.addCell(companyNameHeader);

        PdfPCell quarterHeader = new PdfPCell(new Paragraph("Q/Y", PrintConstants.FONT_COURIER_BOLD_10PT));
        quarterHeader.setPadding(2);
        quarterHeader.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        quarterHeader.setVerticalAlignment(PdfPCell.ALIGN_TOP);
        quarterHeader.setBorder(CheckUtils.getBorder(PdfPCell.BOTTOM, PdfPCell.RIGHT));
        infoTable.addCell(quarterHeader);

        PdfPCell typeHeader = new PdfPCell(new Paragraph("TYPE", PrintConstants.FONT_COURIER_BOLD_10PT));
        typeHeader.setPadding(2);
        typeHeader.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        typeHeader.setVerticalAlignment(PdfPCell.ALIGN_TOP);
        typeHeader.setBorder(CheckUtils.getBorder(PdfPCell.BOTTOM, PdfPCell.RIGHT));
        infoTable.addCell(typeHeader);

        PdfPCell amountHeader = new PdfPCell(new Paragraph("AMOUNT", PrintConstants.FONT_COURIER_BOLD_10PT));
        amountHeader.setPadding(2);
        amountHeader.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
        amountHeader.setBorder(CheckUtils.getBorder(PdfPCell.BOTTOM, PdfPCell.RIGHT));
        infoTable.addCell(amountHeader);

        for (LineItemDTO lineItem : pCheckDTO.getLineItems()) {
            PdfPCell taxIdCell = new PdfPCell(new Paragraph(truncateString(pCheckDTO.getTaxId(), 14), PrintConstants.FONT_COURIER_NORMAL_10PT));
            taxIdCell.setPaddingTop(2);
            taxIdCell.setPaddingLeft(2);
            taxIdCell.setNoWrap(true);
            taxIdCell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
            taxIdCell.setBorder(CheckUtils.getBorder(PdfPCell.LEFT, PdfPCell.RIGHT));
            infoTable.addCell(taxIdCell);

            PdfPCell companyIdCell = new PdfPCell(new Paragraph(truncateString(pCheckDTO.getSourceCompanyNumber(), 9), PrintConstants.FONT_COURIER_NORMAL_10PT));
            companyIdCell.setPaddingTop(2);
            companyIdCell.setPaddingLeft(2);
            companyIdCell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
            companyIdCell.setBorder(PdfPCell.RIGHT);
            companyIdCell.setNoWrap(true);
            infoTable.addCell(companyIdCell);

            PdfPCell companyNameCell = new PdfPCell(new Paragraph(truncateString(pCheckDTO.getCompanyLegalName(), 30), PrintConstants.FONT_COURIER_NORMAL_10PT));
            companyNameCell.setPaddingTop(2);
            companyNameCell.setPaddingLeft(2);
            companyNameCell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
            companyNameCell.setBorder(PdfPCell.RIGHT);
            companyNameCell.setNoWrap(true);
            infoTable.addCell(companyNameCell);

            PdfPCell quarterCell = new PdfPCell(new Paragraph(lineItem.getLiabilityQuarter() + "/" + ("" + lineItem.getLiabilityYear()).substring(2), PrintConstants.FONT_COURIER_NORMAL_10PT));
            quarterCell.setPaddingTop(2);
            quarterCell.setPaddingLeft(2);
            quarterCell.setPaddingRight(2);
            quarterCell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
            quarterCell.setBorder(PdfPCell.RIGHT);
            quarterCell.setNoWrap(true);
            infoTable.addCell(quarterCell);

            PdfPCell typeCell = new PdfPCell(new Paragraph(truncateString(lineItem.getType(), 12), PrintConstants.FONT_COURIER_NORMAL_10PT));
            typeCell.setPaddingTop(2);
            typeCell.setPaddingLeft(2);
            typeCell.setPaddingRight(2);
            typeCell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
            typeCell.setBorder(PdfPCell.RIGHT);
            typeCell.setNoWrap(true);
            infoTable.addCell(typeCell);

            PdfPCell amountCell = new PdfPCell(new Paragraph(mDecimalFormatter.format(lineItem.getAmount()), PrintConstants.FONT_COURIER_NORMAL_10PT));
            amountCell.setPaddingTop(2);
            amountCell.setPaddingLeft(2);
            amountCell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
            amountCell.setBorder(PdfPCell.RIGHT);
            amountCell.setNoWrap(true);
            infoTable.addCell(amountCell);
        }

        return infoTable;
    }

    private PdfPTable createCheckTotalTable(CheckDTO pCheckDTO) {
        PdfPTable checkTotalTable = new PdfPTable(STUB_WIDTHS);

        PdfPCell blankCell = new PdfPCell(new Paragraph(" ", PrintConstants.FONT_COURIER_BOLD_10PT));
        blankCell.setPadding(2);
        blankCell.setBorder(CheckUtils.getBorder(PdfPCell.LEFT, PdfPCell.BOTTOM, PdfPCell.TOP));
        checkTotalTable.addCell(blankCell);

        blankCell = new PdfPCell(new Paragraph(" ", PrintConstants.FONT_COURIER_BOLD_10PT));
        blankCell.setPadding(2);
        blankCell.setBorder(CheckUtils.getBorder(PdfPCell.BOTTOM, PdfPCell.TOP));
        checkTotalTable.addCell(blankCell);

        blankCell = new PdfPCell(new Paragraph(" ", PrintConstants.FONT_COURIER_BOLD_10PT));
        blankCell.setPadding(2);
        blankCell.setBorder(CheckUtils.getBorder(PdfPCell.BOTTOM, PdfPCell.TOP));
        checkTotalTable.addCell(blankCell);

        blankCell = new PdfPCell(new Paragraph(" ", PrintConstants.FONT_COURIER_BOLD_10PT));
        blankCell.setPadding(2);
        blankCell.setBorder(CheckUtils.getBorder(PdfPCell.BOTTOM, PdfPCell.TOP));
        checkTotalTable.addCell(blankCell);

        PdfPCell totalLabelCell = new PdfPCell(new Paragraph("CHECK TOTAL", PrintConstants.FONT_COURIER_BOLD_10PT));
        totalLabelCell.setPadding(2);
        totalLabelCell.setBorder(CheckUtils.getBorder(PdfPCell.RIGHT, PdfPCell.BOTTOM, PdfPCell.TOP));
        totalLabelCell.setNoWrap(true);
        totalLabelCell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
        totalLabelCell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
        checkTotalTable.addCell(totalLabelCell);

        PdfPCell totalAmountCell = new PdfPCell(new Paragraph(mDecimalFormatter.format(pCheckDTO.getCheckAmount()), PrintConstants.FONT_COURIER_BOLD_10PT));
        totalAmountCell.setPadding(2);
        totalAmountCell.setBorder(CheckUtils.getBorder(PdfPCell.RIGHT, PdfPCell.BOTTOM, PdfPCell.TOP));
        totalAmountCell.setNoWrap(true);
        totalAmountCell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
        totalAmountCell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
        checkTotalTable.addCell(totalAmountCell);

        return checkTotalTable;
    }

    private void create_0padding_noBorder_cell(PdfPTable pPdfPTable, String pText, Font pFont) {
        PdfPCell cell = create_0padding_noBorder_cell(pText, pFont);
        if(cell != null) {
            pPdfPTable.addCell(cell);
        }
    }

    private PdfPCell create_0padding_noBorder_cell(String pText, Font pFont) {
        if(isStringEmpty(pText)) {
            return null;
        }

        PdfPCell cell = new PdfPCell(new Paragraph(pText, pFont));
        cell.setPadding(0);
        cell.setNoWrap(true);
        cell.setBorder(PdfPCell.NO_BORDER);
        return cell;
    }

    // ----- util ------

    private void addTableToTable(PdfPTable pTable, PdfPTable pTableToAdd) {
        PdfPCell cell = new PdfPCell(pTableToAdd);
        cell.setBorder(PdfPCell.NO_BORDER);
        cell.setPadding(0);
        pTable.addCell(cell);
    }

    private String customFormatPadding(BigDecimal value) {
        String output =  mZeroDecimalFormatter.format(value);
        int totalLength = 17;
        String dollar = "$";

        int netPayLength = output.length();
        for (int i = 0; i < (totalLength - netPayLength - 1); i++) {

            output = "*" + output;
        }
        output = dollar + output;
        return output;
    }

    private String customFormatAmountInWords(BigDecimal value) {
        String output = "*";

        int totalLength = 75;
        String netPayInWords = CheckUtils.getWrittenAmount(value).toString();

        int netPayWordsLength = netPayInWords.length();
        if(netPayWordsLength > totalLength - 4) {
            netPayInWords = mDecimalFormatter.format(value);
            netPayWordsLength = netPayInWords.length();
        }
        for (int i = 0; i < (totalLength - netPayWordsLength - 4); i++) {
            output = output + "*";
        }

        output = output + netPayInWords;

        return output;
    }

    private String formatCityStateZip(String pCity, String pState, String pZip) {
        return pCity + ", " + pState + " " + pZip;
    }

    private String truncateString(String initalString, int maxCharacters) {
        if(initalString != null && initalString.length() > maxCharacters) {
            return initalString.substring(0, maxCharacters);
        }
        return initalString;
    }

    private boolean isStringEmpty(String string) {
        return !(string != null && string.trim().length() > 0);
    }
}
