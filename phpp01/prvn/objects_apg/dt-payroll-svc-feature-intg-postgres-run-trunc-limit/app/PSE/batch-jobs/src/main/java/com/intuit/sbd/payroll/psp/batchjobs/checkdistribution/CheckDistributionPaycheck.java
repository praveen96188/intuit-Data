package com.intuit.sbd.payroll.psp.batchjobs.checkdistribution;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.CheckPrintDTO;
import com.intuit.sbd.payroll.psp.api.dtos.CheckPrintPaycheckDDINfo;
import com.intuit.sbd.payroll.psp.api.dtos.CheckPrintPaycheckDTO;
import com.intuit.sbd.payroll.psp.api.dtos.CheckPrintPaycheckEarningLineDTO;
import com.intuit.sbd.payroll.psp.api.dtos.CheckPrintPaycheckLineDTO;
import com.intuit.sbd.payroll.psp.batchjobs.util.PrintConstants;
import com.intuit.sbd.payroll.psp.batchjobs.util.CheckUtils;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Feb 27, 2010
 * Time: 4:39:48 PM
 */
public class CheckDistributionPaycheck {
    private static final SpcfLogger logger = PayrollServices.getLogger(CheckDistributionPaycheck.class);

    private static final BigDecimal ZERO = new BigDecimal(0.00);

    private static final int NUMBER_PADDING_RIGHT = 1;
    private static final int TEXT_PADDING_LEFT = 2;
    private static final int LEFT_ALIGN_PADDING = 13;
    private static final int RIGHT_ALIGN_PADDING = 15;
    private static final float MAX_AMOUNT_AND_DESCRIPTION_TABLE_HEIGHT = 508f;
    private static final int RATE_HOURS_MAX_CHARACTERS = 8;
    private static final int CURRENT_YTD_MAX_CHARATERS = 12;

    private static final float[] AMOUNT_AND_DESCRIPTION_WIDTHS = new float[]{1.45f, 0.75f, 0.75f, 1.1f, 1.1f};
    private static final float AMOUNT_AND_DESCRIPTION_WIDTH = 286.3f;

    // formatters
    private DecimalFormat mDecimalFormatter = new DecimalFormat(PrintConstants.MONEY_FORMAT);
    private DecimalFormat mZeroDecimalFormatter = new DecimalFormat(PrintConstants.ZERO_MONEY_FORMAT);
    private DecimalFormat mRateDecimalFormatter = new DecimalFormat(PrintConstants.RATE_FORMAT);

    private Document mDocument;
    private Font mMICRFont;

    public CheckDistributionPaycheck(Document pDocument, Font pMICRFont) {
        mDocument = pDocument;
        mMICRFont = pMICRFont;
    }

    public byte[] generatePaycheck(CheckPrintDTO pCheckPrintDTO, CheckPrintPaycheckDTO pCheckPrintPaycheckDTO) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            PdfWriter.getInstance(mDocument, outputStream);
            mDocument.open();

            mDocument.add(createPayStubTable(pCheckPrintDTO, pCheckPrintPaycheckDTO));
            mDocument.add(createPaycheckTable(pCheckPrintDTO, pCheckPrintPaycheckDTO));
            if (pCheckPrintPaycheckDTO.getCheckType().equals(CheckPrintPaycheckDTO.PaycheckType.ManualCheck)) {
                mDocument.add(createMICRTable(pCheckPrintDTO, pCheckPrintPaycheckDTO));
            }

            // print addresses on back of page
            mDocument.newPage();
            mDocument.add(createCompanyBackSideAddress(pCheckPrintDTO, pCheckPrintPaycheckDTO));
            mDocument.add(createEmployeeBackSideAddress(pCheckPrintPaycheckDTO));

            mDocument.close();
        } catch (DocumentException e) {
            logger.error("Error in generatePaychecks", e);
        } catch (IOException ie) {
            logger.error("Error in generatePaychecks", ie);            
        }

        return outputStream.toByteArray();
    }

    private PdfPTable createPayStubTable(CheckPrintDTO pCheckPrintDTO, CheckPrintPaycheckDTO pCheckPrintPaycheckDTO) throws DocumentException {
        PdfPTable payStubTable = new PdfPTable(2);
        payStubTable.setTotalWidth(new float[]{5.8f, 6.5f});
        payStubTable.setWidthPercentage(100);

        PdfPCell leftSide = new PdfPCell(createPayStubLeftTable(pCheckPrintDTO, pCheckPrintPaycheckDTO));
        leftSide.setPadding(0);
        payStubTable.addCell(leftSide);

        PdfPCell righttSide = new PdfPCell(createPayStubRightTable(pCheckPrintPaycheckDTO));
        righttSide.setPadding(0);
        payStubTable.addCell(righttSide);

        PdfPCell copyrightCell = new PdfPCell(createCopyrightTable());
        copyrightCell.setPadding(0);
        copyrightCell.setBorder(PdfPCell.NO_BORDER);
        copyrightCell.setColspan(2);
        payStubTable.addCell(copyrightCell);

        return payStubTable;
    }

    private PdfPTable createPayStubLeftTable(CheckPrintDTO pCheckPrintDTO, CheckPrintPaycheckDTO pCheckPrintPaycheckDTO) throws DocumentException {
        PdfPTable leftTable = new PdfPTable(1);
        leftTable.setWidthPercentage(100);

        addTableToTable(leftTable, createLeftHeaderTable(pCheckPrintDTO, pCheckPrintPaycheckDTO));

        PdfPCell companyPayStubAddress = new PdfPCell(createCompanyPayStubAddressTable(pCheckPrintDTO, pCheckPrintPaycheckDTO));
        companyPayStubAddress.setBorder(PdfPCell.BOTTOM);
        companyPayStubAddress.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
        companyPayStubAddress.setFixedHeight(60);
        companyPayStubAddress.setPaddingLeft(LEFT_ALIGN_PADDING);
        leftTable.addCell(companyPayStubAddress);

        PdfPCell employeePayStubAddress = new PdfPCell(createEmployeePayStubAddressTable(pCheckPrintPaycheckDTO));
        employeePayStubAddress.setBorder(PdfPCell.BOTTOM);
        employeePayStubAddress.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
        employeePayStubAddress.setFixedHeight(40);
        employeePayStubAddress.setPaddingLeft(LEFT_ALIGN_PADDING);
        leftTable.addCell(employeePayStubAddress);

        leftTable.addCell(createLeftSideGrayHeaderCell("Withholding Tax Information"));
        PdfPCell withholdingInformation = new PdfPCell(createWithholdingTable(pCheckPrintPaycheckDTO));
        withholdingInformation.setBorder(PdfPCell.BOTTOM);
        withholdingInformation.setFixedHeight(40);
        withholdingInformation.setPaddingLeft(LEFT_ALIGN_PADDING);
        leftTable.addCell(withholdingInformation);

        if (pCheckPrintPaycheckDTO.getCheckType().equals(CheckPrintPaycheckDTO.PaycheckType.DirectDeposit)) {
            leftTable.addCell(createLeftSideGrayHeaderCell("Direct Deposit"));
            PdfPCell direcDeposit = new PdfPCell(createDirectDepositTable(pCheckPrintPaycheckDTO));
            direcDeposit.setBorder(PdfPCell.BOTTOM);
            direcDeposit.setFixedHeight(40);
            direcDeposit.setPaddingLeft(LEFT_ALIGN_PADDING);
            leftTable.addCell(direcDeposit);
        }

        PdfPCell emptyCell = new PdfPCell(new Paragraph(" "));                        
        leftTable.addCell(emptyCell);

        return leftTable;
    }

    private PdfPTable createLeftHeaderTable(CheckPrintDTO pCheckPrintDTO, CheckPrintPaycheckDTO pCheckPrintPaycheckDTO) throws DocumentException {
        PdfPTable headerTable = new PdfPTable(5);
        float[] widths = {1.3f, 1f, 1.5f, 1f, 1f};
        headerTable.setTotalWidth(widths);
        headerTable.setWidthPercentage(100);

        PdfPCell grayHeaderCell = createGrayHeaderCell();
        grayHeaderCell.setColspan(3);
        grayHeaderCell.setBorder(PdfPCell.BOTTOM);

        PdfPTable companyIdTable = new PdfPTable(1);
        PdfPCell cell = new PdfPCell(new Paragraph("Company", PrintConstants.FONT_HELVETICA_NORMAL_7PT));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(0);
        cell.setBorder(PdfPCell.NO_BORDER);
        companyIdTable.addCell(cell);

        cell = new PdfPCell(new Paragraph(pCheckPrintDTO.getCompanyId(), PrintConstants.FONT_HELVETICA_NORMAL_7PT));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(0);
        cell.setBorder(PdfPCell.NO_BORDER);
        companyIdTable.addCell(cell);

        grayHeaderCell.addElement(companyIdTable);
        headerTable.addCell(grayHeaderCell);

        grayHeaderCell = createGrayHeaderCell();
        grayHeaderCell.setColspan(2);        
        grayHeaderCell.setBorder(CheckUtils.getBorder(PdfPCell.LEFT, PdfPCell.BOTTOM));

        PdfPTable checkNumberTable = new PdfPTable(1);
        cell = new PdfPCell(new Paragraph("Check  #", PrintConstants.FONT_HELVETICA_NORMAL_7PT));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(0);
        cell.setBorder(PdfPCell.NO_BORDER);
        checkNumberTable.addCell(cell);

        cell = new PdfPCell(new Paragraph(pCheckPrintPaycheckDTO.getCheckNumber(), PrintConstants.FONT_HELVETICA_NORMAL_7PT));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(0);
        cell.setBorder(PdfPCell.NO_BORDER);
        checkNumberTable.addCell(cell);

        grayHeaderCell.addElement(checkNumberTable);
        headerTable.addCell(grayHeaderCell);

        int fixedHeight = 10;
        PdfPCell checkDateCell = new PdfPCell(new Paragraph("Check Date", PrintConstants.FONT_HELVETICA_NORMAL_7PT));
        checkDateCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        checkDateCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        checkDateCell.setBorder(PdfPCell.BOTTOM);
        checkDateCell.setFixedHeight(fixedHeight);
        checkDateCell.setColspan(2);
        headerTable.addCell(checkDateCell);

        PdfPCell scheduleCell = new PdfPCell(new Paragraph("Schedule", PrintConstants.FONT_HELVETICA_NORMAL_7PT));
        scheduleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        scheduleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        scheduleCell.setBorder(CheckUtils.getBorder(PdfPCell.LEFT, PdfPCell.BOTTOM));
        scheduleCell.setFixedHeight(fixedHeight);
        headerTable.addCell(scheduleCell);

        PdfPCell beginDateCell = new PdfPCell(new Paragraph("Begining", PrintConstants.FONT_HELVETICA_NORMAL_7PT));
        beginDateCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        beginDateCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        beginDateCell.setBorder(CheckUtils.getBorder(PdfPCell.LEFT, PdfPCell.BOTTOM));
        beginDateCell.setFixedHeight(fixedHeight);
        headerTable.addCell(beginDateCell);

        PdfPCell endDateCell = new PdfPCell(new Paragraph("Ending", PrintConstants.FONT_HELVETICA_NORMAL_7PT));
        endDateCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        endDateCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        endDateCell.setBorder(CheckUtils.getBorder(PdfPCell.LEFT, PdfPCell.BOTTOM));
        endDateCell.setFixedHeight(fixedHeight);
        headerTable.addCell(endDateCell);

        checkDateCell = new PdfPCell(
                new Paragraph(pCheckPrintPaycheckDTO.getCheckDate().getMMDDYY(), PrintConstants.FONT_HELVETICA_NORMAL_7PT));
        checkDateCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        checkDateCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        checkDateCell.setBorder(PdfPCell.BOTTOM);
        checkDateCell.setFixedHeight(fixedHeight);
        checkDateCell.setColspan(2);
        headerTable.addCell(checkDateCell);

        scheduleCell = new PdfPCell(new Paragraph(pCheckPrintPaycheckDTO.getEmployeePaySchedule(), PrintConstants.FONT_HELVETICA_NORMAL_7PT));
        scheduleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        scheduleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        scheduleCell.setBorder(CheckUtils.getBorder(PdfPCell.LEFT, PdfPCell.BOTTOM));
        scheduleCell.setFixedHeight(fixedHeight);
        headerTable.addCell(scheduleCell);

        beginDateCell = new PdfPCell(
                new Paragraph(pCheckPrintPaycheckDTO.getPeriodStartDate().getMMDDYY(), PrintConstants.FONT_HELVETICA_NORMAL_7PT));
        beginDateCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        beginDateCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        beginDateCell.setBorder(CheckUtils.getBorder(PdfPCell.LEFT, PdfPCell.BOTTOM));
        beginDateCell.setFixedHeight(fixedHeight);
        headerTable.addCell(beginDateCell);

        endDateCell = new PdfPCell(
                new Paragraph(pCheckPrintPaycheckDTO.getPeriodEndDate().getMMDDYY(), PrintConstants.FONT_HELVETICA_NORMAL_7PT));
        endDateCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        endDateCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        endDateCell.setBorder(CheckUtils.getBorder(PdfPCell.LEFT, PdfPCell.BOTTOM));
        endDateCell.setFixedHeight(fixedHeight);
        headerTable.addCell(endDateCell);

        return headerTable;
    }

    private PdfPTable createCompanyPayStubAddressTable(CheckPrintDTO pCheckPrintDTO, CheckPrintPaycheckDTO pCheckPrintPaycheckDTO) {
        PdfPTable table = new PdfPTable(1);

        table.addCell(createCompanyPayStubAddressCell(pCheckPrintDTO.getCompanyLegalName()));
        table.addCell(createCompanyPayStubAddressCell(pCheckPrintDTO.getCompanyLegalAddress().getAddressLine1()));
        if(!isStringEmpty(pCheckPrintDTO.getCompanyLegalAddress().getAddressLine2())) {
            table.addCell(createCompanyPayStubAddressCell(pCheckPrintDTO.getCompanyLegalAddress().getAddressLine2()));
        }
        table.addCell(createCompanyPayStubAddressCell(getCityStateZip(pCheckPrintDTO)));
        table.addCell(createCompanyIdEmployeeIdPaycheckIdCell(pCheckPrintDTO, pCheckPrintPaycheckDTO));

        return table;
    }

    private PdfPCell createCompanyPayStubAddressCell(String pText) {
        PdfPCell cell = new PdfPCell(new Paragraph(pText, PrintConstants.FONT_COURIER_NORMAL_10PT));
        cell.setPadding(0);
        cell.setBorder(PdfPCell.NO_BORDER);
        return cell;
    }

    private PdfPTable createEmployeePayStubAddressTable(CheckPrintPaycheckDTO pCheckPrintPaycheckDTO) {
        PdfPTable table = new PdfPTable(1);

        table.addCell(createEmployeePayStubAddressCell(pCheckPrintPaycheckDTO.getEmployeePrintName()));
        table.addCell(createEmployeePayStubAddressCell(pCheckPrintPaycheckDTO.getEmployeeAddress().getAddressLine1()));
        if(!isStringEmpty(pCheckPrintPaycheckDTO.getEmployeeAddress().getAddressLine2())) {
            table.addCell(createEmployeePayStubAddressCell(pCheckPrintPaycheckDTO.getEmployeeAddress().getAddressLine2()));
        }
        table.addCell(createEmployeePayStubAddressCell(getEmployeeCityStateZip(pCheckPrintPaycheckDTO)));

        return table;
    }

    private PdfPCell createEmployeePayStubAddressCell(String pText) {
        PdfPCell cell = new PdfPCell(new Paragraph(pText, PrintConstants.FONT_HELVETICA_NORMAL_7PT));
        cell.setPadding(0);
        cell.setBorder(PdfPCell.NO_BORDER);
        return cell;
    }

    private PdfPTable createWithholdingTable(CheckPrintPaycheckDTO pCheckPrintPaycheckDTO) {
        PdfPTable table = new PdfPTable(1);

        PdfPCell federalTaxWithholding = new PdfPCell(new Paragraph("Federal: " + pCheckPrintPaycheckDTO.getEmployeeFedFilingStatus() +
                " - " + pCheckPrintPaycheckDTO.getEmployeeFedAllowances(), PrintConstants.FONT_HELVETICA_NORMAL_7PT));
        federalTaxWithholding.setHorizontalAlignment(Element.ALIGN_LEFT);
        federalTaxWithholding.setBorder(PdfPCell.NO_BORDER);
        federalTaxWithholding.setPaddingBottom(3);
        table.addCell(federalTaxWithholding);

        /*PdfPCell stateTaxWithholding = new PdfPCell(new Paragraph(pCheckPrintPaycheckDTO.getEmployeeWorkState() +
                ": " + pCheckPrintPaycheckDTO.getEmployeeStateFilingStatus() +
                " - " + pCheckPrintPaycheckDTO.getEmployeeStateAllowances(), PrintConstants.FONT_HELVETICA_NORMAL_7PT));
        stateTaxWithholding.setHorizontalAlignment(Element.ALIGN_LEFT);
        stateTaxWithholding.setBorder(PdfPCell.NO_BORDER);
        table.addCell(stateTaxWithholding);*/

        return table;
    }

    private PdfPTable createDirectDepositTable(CheckPrintPaycheckDTO pCheckPrintPaycheckDTO) throws DocumentException {
        PdfPTable directDepositTable = new PdfPTable(3);
        directDepositTable.setWidths(new float[]{1f,1.5f, 1f});
        int paddingBottom = 2;
        for (CheckPrintPaycheckDDINfo checkPrintPaycheckDDINfo : pCheckPrintPaycheckDTO.getDirectDeposits()) {
            PdfPCell accountType = create_0padding_noBorder_cell(checkPrintPaycheckDDINfo.getAccountType() + ":", PrintConstants.FONT_HELVETICA_NORMAL_7PT);
            accountType.setPaddingBottom(paddingBottom);
            directDepositTable.addCell(accountType);

            PdfPCell accountNumber = create_0padding_noBorder_cell(obscureBankAccountNumber(checkPrintPaycheckDDINfo.getAccountId()), PrintConstants.FONT_HELVETICA_NORMAL_7PT);
            accountNumber.setHorizontalAlignment(Element.ALIGN_RIGHT);
            accountNumber.setPaddingBottom(paddingBottom);
            directDepositTable.addCell(accountNumber);

            PdfPCell amount = create_0padding_noBorder_cell("$" + customFormat(checkPrintPaycheckDDINfo.getDDAmount(), true), PrintConstants.FONT_HELVETICA_NORMAL_7PT);
            amount.setHorizontalAlignment(Element.ALIGN_RIGHT);            
            amount.setPaddingBottom(paddingBottom);
            amount.setPaddingRight(20);
            directDepositTable.addCell(amount);
        }

        return directDepositTable;
    }

    private PdfPCell createLeftSideGrayHeaderCell(String pText) {
        PdfPCell cell = new PdfPCell(new Paragraph(pText, PrintConstants.FONT_HELVETICA_NORMAL_7PT));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_BOTTOM);
        cell.setBackgroundColor(Color.LIGHT_GRAY);
        cell.setBorder(PdfPCell.NO_BORDER);
        cell.setPadding(0);
        cell.setPaddingTop(7);
        return cell;
    }

    private PdfPTable createPayStubRightTable(CheckPrintPaycheckDTO pCheckPrintPaycheckDTO) throws DocumentException {
        PdfPTable table = new PdfPTable(1);

        PdfPCell grayHeader = createGrayHeaderCell();
        grayHeader.addElement(new Paragraph(pCheckPrintPaycheckDTO.getEmployeePrintName(), PrintConstants.FONT_COURIER_NORMAL_8PT));
        grayHeader.setHorizontalAlignment(Element.ALIGN_LEFT);
        grayHeader.setPaddingLeft(10);
        grayHeader.setPaddingBottom(5);
        table.addCell(grayHeader);

        PdfPCell payItems = new PdfPCell(createPayItemsTable(pCheckPrintPaycheckDTO));
        payItems.setFixedHeight(MAX_AMOUNT_AND_DESCRIPTION_TABLE_HEIGHT);
        table.addCell(payItems);

        return table;
    }

    private PdfPCell createGrayHeaderCell() {
        PdfPCell grayHeaderCell = new PdfPCell();
        grayHeaderCell.setBackgroundColor(Color.LIGHT_GRAY);
        grayHeaderCell.setPadding(0);
        grayHeaderCell.setFixedHeight(20);
        grayHeaderCell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        grayHeaderCell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
        return grayHeaderCell;
    }

    private PdfPTable createMICRTable(CheckPrintDTO pCheckPrintDTO, CheckPrintPaycheckDTO pCheckPrintPaycheckDTO) throws DocumentException {
        PdfPTable micrTable = new PdfPTable(3);
        micrTable.setTotalWidth(new float[]{2.2f, 2.700f, 5.590f});

        PdfPCell checknumber = new PdfPCell(new Paragraph("C" + pCheckPrintPaycheckDTO.getCheckNumber() + "C", mMICRFont));
        checknumber.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
        checknumber.setPaddingRight(3);
        checknumber.setBorder(PdfPCell.NO_BORDER);
        checknumber.setVerticalAlignment(PdfPCell.ALIGN_CENTER);
        micrTable.addCell(checknumber);

        PdfPCell routingNumber = new PdfPCell(new Paragraph("A" + pCheckPrintDTO.getCompanyBankRoutingNumber() + "A", mMICRFont));
        routingNumber.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        routingNumber.setPaddingLeft(4);
        routingNumber.setBorder(PdfPCell.NO_BORDER);
        routingNumber.setVerticalAlignment(PdfPCell.ALIGN_CENTER);
        micrTable.addCell(routingNumber);

        PdfPCell accountNumber = new PdfPCell(new Paragraph(pCheckPrintDTO.getCompanyBankAccountNumber() + "C", mMICRFont));
        accountNumber.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        accountNumber.setPaddingLeft(1);
        accountNumber.setBorder(PdfPCell.NO_BORDER);
        accountNumber.setVerticalAlignment(PdfPCell.ALIGN_CENTER);
        micrTable.addCell(accountNumber);

        PdfPTable paddingTable = new PdfPTable(1);
        PdfPCell paddingCell = new PdfPCell(micrTable);
        paddingCell.setPadding(0);
        paddingCell.setPaddingRight(5);
        paddingCell.setBorder(PdfPCell.NO_BORDER);
        paddingTable.addCell(paddingCell);
        paddingTable.setWidthPercentage(80);

        return paddingTable;
    }

    private PdfPTable createPaycheckTable(CheckPrintDTO pCheckPrintDTO, CheckPrintPaycheckDTO pCheckPrintPaycheckDTO) throws DocumentException, IOException {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);

        // this has table has one fixed height cell that contains the paycheck information
        // the cell is fixed height so that the MICR line does not move up or down if the information in the
        // paycheck changes
        PdfPCell paycheck = new PdfPCell();
        paycheck.setPaddingTop(39);
        paycheck.setPaddingLeft(LEFT_ALIGN_PADDING);
        paycheck.setPaddingRight(RIGHT_ALIGN_PADDING);
        paycheck.setBorder(PdfPCell.NO_BORDER);
        paycheck.setFixedHeight(235);
        boolean isPaycheck = pCheckPrintPaycheckDTO.getCheckType() != CheckPrintPaycheckDTO.PaycheckType.DirectDeposit;
        PdfPTable paycheckTable = new PdfPTable(1);
        paycheckTable.setWidthPercentage(100);

        PdfPCell topCell = new PdfPCell(createTopPaycheckTable(pCheckPrintDTO, pCheckPrintPaycheckDTO, isPaycheck));
        topCell.setBorder(PdfPCell.NO_BORDER);
        topCell.setFixedHeight(80);
        topCell.setPadding(0);
        paycheckTable.addCell(topCell);

        PdfPCell netPayInWords = new PdfPCell(createAmountInWordsParagraph(pCheckPrintPaycheckDTO, isPaycheck));
        netPayInWords.setBorder(PdfPCell.NO_BORDER);        
        netPayInWords.setPadding(0);
        netPayInWords.setNoWrap(true);
        paycheckTable.addCell(netPayInWords);

        PdfPCell payToOrderAndSignature = new PdfPCell(createPayToTheOrderOfAndSignatureTable(pCheckPrintDTO, pCheckPrintPaycheckDTO, isPaycheck));
        payToOrderAndSignature.setVerticalAlignment(PdfPCell.ALIGN_BOTTOM);
        payToOrderAndSignature.setBorder(PdfPCell.NO_BORDER);
        payToOrderAndSignature.setPadding(0);
        payToOrderAndSignature.setFixedHeight(75);
        paycheckTable.addCell(payToOrderAndSignature);

        paycheck.addElement(paycheckTable);

        table.addCell(paycheck);

        return table;
    }

    private PdfPTable createTopPaycheckTable(CheckPrintDTO pCheckPrintDTO, CheckPrintPaycheckDTO pCheckPrintPaycheckDTO, boolean isPaycheck) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);

        addTableToTable(table, createEmployerAddressOnCheck(pCheckPrintDTO, pCheckPrintPaycheckDTO));

        addTableToTable(table, createCheckDateNumberNetpayTable(pCheckPrintDTO, pCheckPrintPaycheckDTO, isPaycheck));
        return table;
    }

    private PdfPTable createEmployerAddressOnCheck(CheckPrintDTO pCheckPrintDTO, CheckPrintPaycheckDTO pCheckPrintPaycheckDTO) {
        PdfPTable table = new PdfPTable(1);

        table.addCell(create_0padding_noBorder_cell(pCheckPrintDTO.getCompanyLegalName(), PrintConstants.FONT_COURIER_BOLD_14PT));
        table.addCell(create_0padding_noBorder_cell(pCheckPrintDTO.getCompanyLegalAddress().getAddressLine1(), PrintConstants.FONT_COURIER_BOLD_10PT));
        if(!isStringEmpty(pCheckPrintDTO.getCompanyLegalAddress().getAddressLine2())) {
            table.addCell(create_0padding_noBorder_cell(pCheckPrintDTO.getCompanyLegalAddress().getAddressLine2(), PrintConstants.FONT_COURIER_BOLD_10PT));
        }
        table.addCell(create_0padding_noBorder_cell(getCityStateZip(pCheckPrintDTO), PrintConstants.FONT_COURIER_BOLD_10PT));
        table.addCell(createCompanyIdEmployeeIdPaycheckIdCell(pCheckPrintDTO, pCheckPrintPaycheckDTO));
        return table;       
    }

    private PdfPTable createCheckDateNumberNetpayTable(CheckPrintDTO pCheckPrintDTO, CheckPrintPaycheckDTO pCheckPrintPaycheckDTO, boolean isPaycheck) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidths(new float[]{3.9f, 2.1f});

        String bankName = " ";
        String bankCityStateZip = " ";
        String checkDate = pCheckPrintPaycheckDTO.getCheckDate().getMMDDYYYY();
        String netPay = "**************";
        if(isPaycheck) {
            bankName = pCheckPrintDTO.getCompanyBankName();
            if(!pCheckPrintPaycheckDTO.getIsTestCheck()) {
                netPay = customFormatPadding(pCheckPrintPaycheckDTO.getCheckNetPay());
            }
        }

        table.addCell(create_0padding_noBorder_cell(" ", PrintConstants.FONT_COURIER_BOLD_12PT));

        PdfPCell checkNumberCell = create_0padding_noBorder_cell(pCheckPrintPaycheckDTO.getCheckNumber(), PrintConstants.FONT_COURIER_BOLD_12PT);
        checkNumberCell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
        table.addCell(checkNumberCell);

        PdfPCell emplyorBank = create_0padding_noBorder_cell(bankName, PrintConstants.FONT_COURIER_NORMAL_8PT);
        emplyorBank.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        table.addCell(emplyorBank);
        table.addCell(create_0padding_noBorder_cell(" ", PrintConstants.FONT_COURIER_NORMAL_8PT));

        PdfPCell employerCityStateZip = create_0padding_noBorder_cell(bankCityStateZip, PrintConstants.FONT_COURIER_NORMAL_8PT);
        employerCityStateZip.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        employerCityStateZip.setPaddingBottom(5);
        table.addCell(employerCityStateZip);
        table.addCell(create_0padding_noBorder_cell(" ", PrintConstants.FONT_COURIER_NORMAL_8PT));

        PdfPCell checkDateCell = create_0padding_noBorder_cell(checkDate, PrintConstants.FONT_COURIER_NORMAL_10PT);
        checkDateCell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
        checkDateCell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        table.addCell(checkDateCell);

        PdfPCell netPayCell = new PdfPCell(new Paragraph(netPay, PrintConstants.FONT_COURIER_NORMAL_10PT));
        netPayCell.setPadding(0);
        netPayCell.setFixedHeight(20);
        netPayCell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
        netPayCell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        netPayCell.setNoWrap(true);
        table.addCell(netPayCell);

        // empty cells that will take up the remainder of the table
        table.addCell(create_0padding_noBorder_cell(" ", PrintConstants.FONT_COURIER_NORMAL_10PT));
        table.addCell(create_0padding_noBorder_cell(" ", PrintConstants.FONT_COURIER_NORMAL_10PT));

        return table;
    }

    private Paragraph createAmountInWordsParagraph(CheckPrintPaycheckDTO pCheckPrintPaycheckDTO, boolean isPaycheck) throws DocumentException {
        if(isPaycheck) {
            String netPayInWords;
            if(pCheckPrintPaycheckDTO.getIsTestCheck()) {
                netPayInWords = " ***TEST ONLY THIS IS NOT A CHECK***";
            }
            else {
                netPayInWords = customFormatAmountInWords(pCheckPrintPaycheckDTO.getCheckNetPay());
            }
            return new Paragraph("PAY" + netPayInWords, PrintConstants.FONT_COURIER_NORMAL_12PT);
        }
        else {
            return new Paragraph(
                    "*******************ADVICE OF DEPOSIT - NON-NEGOTIABLE*****************",
                    PrintConstants.FONT_COURIER_NORMAL_12PT);
        }
    }

    private PdfPTable createPayToTheOrderOfAndSignatureTable(CheckPrintDTO pCheckPrintDTO, CheckPrintPaycheckDTO pCheckPrintPaycheckDTO, boolean isPaycheck) throws DocumentException, IOException {
        PdfPTable table = new PdfPTable(2);
        table.setTotalWidth(new float[]{5f, 3f});

        PdfPTable employeeAddressOnCheck;
        if(isPaycheck) {
            employeeAddressOnCheck = new PdfPTable(2);
            employeeAddressOnCheck.setTotalWidth(new float[]{0.75f, 5f});
        }
        else {
            employeeAddressOnCheck = new PdfPTable(1);
        }

        if(isPaycheck) {
            PdfPTable payToTheOrderOf = new PdfPTable(1);

            PdfPCell toTheCell = new PdfPCell(new Paragraph("To The", PrintConstants.FONT_COURIER_NORMAL_6PT));
            toTheCell.setPadding(0);
            toTheCell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
            toTheCell.setPaddingTop(7);
            toTheCell.setPaddingRight(10);
            toTheCell.setBorder(PdfPCell.NO_BORDER);
            payToTheOrderOf.addCell(toTheCell);

            PdfPCell orderOfCell = new PdfPCell(new Paragraph("Order Of", PrintConstants.FONT_COURIER_NORMAL_6PT));
            orderOfCell.setPadding(0);
            toTheCell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
            orderOfCell.setPaddingTop(4);
            orderOfCell.setBorder(PdfPCell.NO_BORDER);
            payToTheOrderOf.addCell(orderOfCell);

            addTableToTable(employeeAddressOnCheck, payToTheOrderOf);
        }
        addTableToTable(employeeAddressOnCheck, createEmployeeAddressOnCheckTable(pCheckPrintPaycheckDTO));
        addTableToTable(table, employeeAddressOnCheck);

        if(isPaycheck){
            PdfPTable signatureTable = new PdfPTable(1);

            Image image = Image.getInstance(pCheckPrintDTO.getCheckSignature());
            image.scalePercent(15);

            PdfPCell signature = new PdfPCell(image);
            signature.setPadding(0);
            signature.setPaddingBottom(1);
            signature.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
            signature.setBorder(PdfPCell.NO_BORDER);
            signatureTable.addCell(signature);

            PdfPCell authLineCell = new PdfPCell(new Paragraph("AUTHORIZED SIGNATURE", PrintConstants.FONT_HELVETICA_NORMAL_7PT));
            authLineCell.setPadding(0);
            authLineCell.setPaddingTop(0);
            authLineCell.setBorder(PdfPCell.NO_BORDER);
            authLineCell.setBorderWidthTop(1f);
            authLineCell.setBorderColorTop(Color.BLACK);
            authLineCell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
            signatureTable.addCell(authLineCell);

            addTableToTable(table, signatureTable);
        }
        else {
            PdfPTable nonNegotiableTable = new PdfPTable(1);
            PdfPCell nonNegotiableCell = new PdfPCell(new Paragraph("*NON-NEGOTIABLE*", PrintConstants.FONT_COURIER_BOLD_14PT));
            nonNegotiableCell.setBorder(PdfPCell.NO_BORDER);
            nonNegotiableCell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
            nonNegotiableCell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
            nonNegotiableCell.setPadding(0);
            nonNegotiableTable.addCell(nonNegotiableCell);
            addTableToTable(table, nonNegotiableTable);
        }

        table.setWidthPercentage(100);

        return table;
    }

    private PdfPTable createEmployeeAddressOnCheckTable(CheckPrintPaycheckDTO pCheckPrintPaycheckDTO) {
        PdfPTable employeeAddress = new PdfPTable(1);
        int maxCharacters = 36;
        employeeAddress.addCell(create_0padding_noBorder_cell(truncateString(pCheckPrintPaycheckDTO.getEmployeePrintName(), maxCharacters), PrintConstants.FONT_COURIER_NORMAL_12PT));
        employeeAddress.addCell(create_0padding_noBorder_cell(truncateString(pCheckPrintPaycheckDTO.getEmployeeAddress().getAddressLine1(), maxCharacters), PrintConstants.FONT_COURIER_NORMAL_12PT));
        if(!isStringEmpty(pCheckPrintPaycheckDTO.getEmployeeAddress().getAddressLine2())) {
            employeeAddress.addCell(create_0padding_noBorder_cell(truncateString(pCheckPrintPaycheckDTO.getEmployeeAddress().getAddressLine2(), maxCharacters), PrintConstants.FONT_COURIER_NORMAL_12PT));
        }
        employeeAddress.addCell(create_0padding_noBorder_cell(truncateString(getEmployeeCityStateZip(pCheckPrintPaycheckDTO), maxCharacters), PrintConstants.FONT_COURIER_NORMAL_12PT ));
        return employeeAddress;
    }

    private PdfPTable createCopyrightTable() throws DocumentException {
        PdfPTable tableCopy = new PdfPTable(2);
        float[] copyWidths = {1f, 2f};
        tableCopy.setTotalWidth(copyWidths);

        PdfPCell cellPayroll = new PdfPCell(new Paragraph("Payrolls by RENO PROCESSING CENTER", PrintConstants.FONT_HELVETICA_NORMAL_7PT));
        cellPayroll.setVerticalAlignment(PdfPCell.ALIGN_TOP);
        cellPayroll.setPaddingTop(0);
        cellPayroll.setBorder(PdfPCell.NO_BORDER);
        tableCopy.addCell(cellPayroll);

        PdfPCell cellCopyright = new PdfPCell(new Paragraph("Copyright 2010 Intuit, Inc.", PrintConstants.FONT_HELVETICA_NORMAL_7PT));
        cellCopyright.setVerticalAlignment(PdfPCell.ALIGN_TOP);
        cellCopyright.setPaddingLeft(22);
        cellCopyright.setPaddingTop(0);
        cellCopyright.setBorder(PdfPCell.NO_BORDER);
        tableCopy.addCell(cellCopyright);

        tableCopy.setWidthPercentage(100);
        return tableCopy;
    }

    private PdfPTable createPayItemsTable(CheckPrintPaycheckDTO pCheckPrintPaycheckDTO) throws DocumentException {
        PdfPTable table = new PdfPTable(1);

        float heightLeft = MAX_AMOUNT_AND_DESCRIPTION_TABLE_HEIGHT;
        if(pCheckPrintPaycheckDTO.getEarnings().size() > 0) {
            PdfPTable earningsTable = createEarningsTable(pCheckPrintPaycheckDTO);
            while (earningsTable.calculateHeights(false) > heightLeft) {
                earningsTable.deleteLastRow();
            }
            heightLeft -= earningsTable.calculateHeights(false);
            addTableToTable(table, earningsTable);
        }

        if(pCheckPrintPaycheckDTO.getPreTaxDeductions().size() > 0 && heightLeft >= 10) {
            PdfPTable preTaxTable = createPretaxDeductionsTable(pCheckPrintPaycheckDTO);
            while (preTaxTable.calculateHeights(false) > heightLeft) {
                preTaxTable.deleteLastRow();
            }
            heightLeft -= preTaxTable.calculateHeights(false);
            addTableToTable(table, preTaxTable);
        }

        if(pCheckPrintPaycheckDTO.getTaxes().size() > 0 && heightLeft >= 10) {
            PdfPTable taxesTable = createTaxesDeductionsTable(pCheckPrintPaycheckDTO);
            while (taxesTable.calculateHeights(false) > heightLeft) {
                taxesTable.deleteLastRow();
            }
            heightLeft -= taxesTable.calculateHeights(false);
            addTableToTable(table, taxesTable);
        }

        if(pCheckPrintPaycheckDTO.getDeductions().size() > 0 && heightLeft >= 10) {
            PdfPTable deductionsTable = createDeductionsTable(pCheckPrintPaycheckDTO);
            while (deductionsTable.calculateHeights(false) > heightLeft) {
                deductionsTable.deleteLastRow();
            }
            heightLeft -= deductionsTable.calculateHeights(false);
            addTableToTable(table, deductionsTable);
        }

        if(heightLeft >= 10) {
            addTableToTable(table, createNetPayTable(pCheckPrintPaycheckDTO));
        }

        if(pCheckPrintPaycheckDTO.getCompanyTaxableContributions().size() > 0 && heightLeft >= 10) {
            PdfPTable taxableAdditions = createTaxableCompanyContributionsTable(pCheckPrintPaycheckDTO);
            while (taxableAdditions.calculateHeights(false) > heightLeft) {
                taxableAdditions.deleteLastRow();
            }
            heightLeft -= taxableAdditions.calculateHeights(false);
            addTableToTable(table, taxableAdditions);
        }

        if(pCheckPrintPaycheckDTO.getCompanyContributions().size() > 0 && heightLeft >= 10) {
            PdfPTable nonTaxableAdditions = createNonTaxableCompanyContributionsTable(pCheckPrintPaycheckDTO);
            while (nonTaxableAdditions.calculateHeights(false) > heightLeft) {
                nonTaxableAdditions.deleteLastRow();
            }
            heightLeft -= nonTaxableAdditions.calculateHeights(false);
            addTableToTable(table, nonTaxableAdditions);
        }

        // if there is space left add spacer cells to take up the remaining space
        if(pCheckPrintPaycheckDTO.getCompanyContributions().size() > 0 && heightLeft >= 10) {
            PdfPTable spacerTable = new PdfPTable(5);
            spacerTable.setWidths(AMOUNT_AND_DESCRIPTION_WIDTHS);
            spacerTable.setTotalWidth(AMOUNT_AND_DESCRIPTION_WIDTH);

            PdfPCell emptyCell = new PdfPCell();
            emptyCell.setBorder(PdfPCell.NO_BORDER);
            emptyCell.setColspan(3);
            spacerTable.addCell(emptyCell);

            emptyCell = new PdfPCell();
            emptyCell.setBorder(PdfPCell.LEFT);

            spacerTable.addCell(emptyCell);
            spacerTable.addCell(emptyCell);
            
            addTableToTable(table, spacerTable);
        }

        return table;
    }

    // left out Type, OT, and X out don't know what they stand for
    private PdfPTable createEarningsTable(CheckPrintPaycheckDTO pCheckPrintPaycheckDTO) throws DocumentException {
        Set<CheckPrintPaycheckEarningLineDTO> checkPrintPaycheckEarningLineDTOs = pCheckPrintPaycheckDTO.getEarnings();
        PdfPTable earningsTable = new PdfPTable(5);
        earningsTable.setWidths(AMOUNT_AND_DESCRIPTION_WIDTHS);
        earningsTable.setTotalWidth(AMOUNT_AND_DESCRIPTION_WIDTH);

        int headerPaddingTop = 2;
        int headerPaddingBottom = 2;

        PdfPCell descriptionHeader = new PdfPCell(new Paragraph("Description", PrintConstants.FONT_HELVETICA_NORMAL_7PT));
        descriptionHeader.setNoWrap(true);
        descriptionHeader.setPaddingBottom(headerPaddingBottom);
        descriptionHeader.setPaddingTop(headerPaddingTop);
        descriptionHeader.setPaddingLeft(TEXT_PADDING_LEFT);
        descriptionHeader.setBorder(PdfPCell.BOTTOM);
        earningsTable.addCell(descriptionHeader);

        PdfPCell hoursHeader = new PdfPCell(new Paragraph("Hours", PrintConstants.FONT_HELVETICA_NORMAL_7PT));
        hoursHeader.setNoWrap(true);
        hoursHeader.setPaddingBottom(headerPaddingBottom);
        hoursHeader.setPaddingTop(headerPaddingTop);
        hoursHeader.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
        hoursHeader.setPaddingLeft(TEXT_PADDING_LEFT);
        hoursHeader.setBorder(PdfPCell.BOTTOM);
        earningsTable.addCell(hoursHeader);

        PdfPCell rateHeader = new PdfPCell(new Paragraph("Rate", PrintConstants.FONT_HELVETICA_NORMAL_7PT));
        rateHeader.setNoWrap(true);
        rateHeader.setPaddingBottom(headerPaddingBottom);
        rateHeader.setPaddingTop(headerPaddingTop);
        rateHeader.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
        rateHeader.setPaddingLeft(TEXT_PADDING_LEFT);
        rateHeader.setBorder(PdfPCell.BOTTOM);
        earningsTable.addCell(rateHeader);

        PdfPCell currentCell = new PdfPCell(new Paragraph("Current", PrintConstants.FONT_HELVETICA_NORMAL_7PT));
        currentCell.setNoWrap(true);
        currentCell.setPaddingBottom(headerPaddingBottom);
        currentCell.setPaddingTop(headerPaddingTop);
        currentCell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        currentCell.setBorder(CheckUtils.getBorder(PdfPCell.LEFT, PdfPCell.BOTTOM));
        earningsTable.addCell(currentCell);

        PdfPCell ytdCell = new PdfPCell(new Paragraph("Year  to  Date", PrintConstants.FONT_HELVETICA_NORMAL_7PT));
        ytdCell.setNoWrap(true);
        ytdCell.setPaddingBottom(headerPaddingBottom);
        ytdCell.setPaddingTop(headerPaddingTop);
        ytdCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        ytdCell.setBorder(CheckUtils.getBorder(PdfPCell.LEFT, PdfPCell.BOTTOM));
        earningsTable.addCell(ytdCell);

        addGrayHeaderCells(earningsTable, "Earnings and Hours");

        int i = 0;
        for (CheckPrintPaycheckEarningLineDTO checkPrintPaycheckEarningLineDTO : checkPrintPaycheckEarningLineDTOs) {
            if(checkPrintPaycheckEarningLineDTO.getYtdAmount().compareTo(ZERO) != 0 && checkPrintPaycheckEarningLineDTO.getPaylineAmount().compareTo(ZERO) != 0) {
                int paddingTop = 0;
                int paddingBottom = 2;
                if (i == 0) {
                    paddingTop = 2;
                }

            String description = checkPrintPaycheckEarningLineDTO.getPaylineDescription();
            String hours = maxCharacterReplacement(customFormat(checkPrintPaycheckEarningLineDTO.getHours(), false), RATE_HOURS_MAX_CHARACTERS);
            String rate;
            if(!checkPrintPaycheckEarningLineDTO.getRateType().equalsIgnoreCase("R")) {
                rate = customFormat(checkPrintPaycheckEarningLineDTO.getRate(), false);
            }
            else {
                rate = customRateFormat(checkPrintPaycheckEarningLineDTO.getRate());
            }
            rate = maxCharacterReplacement(rate, RATE_HOURS_MAX_CHARACTERS);
            if(hours.length() > 1 && description.length() > 20) {
                description = description.substring(0, 20);
            }
            else if(rate.length() > 6 && description.length() > 30) {
                description = description.substring(0, 30);
            }
            PdfPCell descriptionCell = new PdfPCell(new Paragraph(description, PrintConstants.FONT_HELVETICA_NORMAL_7PT));
            descriptionCell.setNoWrap(true);
            descriptionCell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
            descriptionCell.setVerticalAlignment(PdfPCell.ALIGN_BOTTOM);
            descriptionCell.setPaddingTop(paddingTop);
            descriptionCell.setPaddingBottom(paddingBottom);
            descriptionCell.setPaddingLeft(TEXT_PADDING_LEFT);
            descriptionCell.setBorder(PdfPCell.NO_BORDER);
            earningsTable.addCell(descriptionCell);

            PdfPCell hoursCell = new PdfPCell(new Paragraph(hours, PrintConstants.FONT_COURIER_NORMAL_8PT));
            hoursCell.setNoWrap(true);
            hoursCell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
            hoursCell.setPaddingTop(paddingTop);
            hoursCell.setPaddingBottom(paddingBottom);
            hoursCell.setPaddingLeft(TEXT_PADDING_LEFT);
            hoursCell.setBorder(PdfPCell.NO_BORDER);
            earningsTable.addCell(hoursCell);

            PdfPCell rateCell = new PdfPCell(new Paragraph(rate, PrintConstants.FONT_COURIER_NORMAL_8PT));
            rateCell.setNoWrap(true);
            rateCell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
            rateCell.setPaddingTop(paddingTop);
            rateCell.setPaddingBottom(paddingBottom);
            rateCell.setPaddingLeft(TEXT_PADDING_LEFT);
            rateCell.setBorder(PdfPCell.NO_BORDER);
            earningsTable.addCell(rateCell);

            PdfPCell currentAmount = new PdfPCell(new Paragraph(maxCharacterReplacement(customFormat(checkPrintPaycheckEarningLineDTO.getPaylineAmount(), checkPrintPaycheckEarningLineDTO.isInCurrentCheck()), CURRENT_YTD_MAX_CHARATERS), PrintConstants.FONT_COURIER_NORMAL_8PT));
            currentAmount.setNoWrap(true);
            currentAmount.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
            currentAmount.setPaddingTop(paddingTop);
            currentAmount.setPaddingBottom(paddingBottom);
            currentAmount.setPaddingRight(NUMBER_PADDING_RIGHT);
            currentAmount.setBorder(PdfPCell.LEFT);
            earningsTable.addCell(currentAmount);

            PdfPCell ytdAmount = new PdfPCell(new Paragraph(maxCharacterReplacement(customFormat(checkPrintPaycheckEarningLineDTO.getYtdAmount(), (checkPrintPaycheckEarningLineDTO.isInCurrentCheck() && (ZERO.compareTo(checkPrintPaycheckEarningLineDTO.getPaylineAmount()) == 0))), CURRENT_YTD_MAX_CHARATERS), PrintConstants.FONT_COURIER_NORMAL_8PT));
            ytdAmount.setNoWrap(true);
            ytdAmount.setHorizontalAlignment(Element.ALIGN_RIGHT);
            ytdAmount.setPaddingTop(paddingTop);
            ytdAmount.setPaddingBottom(paddingBottom);
            ytdAmount.setPaddingRight(NUMBER_PADDING_RIGHT);
            ytdAmount.setBorder(PdfPCell.LEFT);
            earningsTable.addCell(ytdAmount);
            i++;
        }
        }

        addTotalCells(earningsTable,
                pCheckPrintPaycheckDTO.getEarningsCheckTotal(),
                pCheckPrintPaycheckDTO.getEarningsYtdTotal(),
                "TOTAL EARNINGS", true);

        return earningsTable;
    }

    private PdfPTable createPretaxDeductionsTable(CheckPrintPaycheckDTO pCheckPrintPaycheckDTO) throws DocumentException {
        Set<CheckPrintPaycheckLineDTO> checkPrintPaycheckLineDTOs = pCheckPrintPaycheckDTO.getPreTaxDeductions();
        PdfPTable pretaxDeductionsTable = new PdfPTable(5);
        pretaxDeductionsTable.setWidths(AMOUNT_AND_DESCRIPTION_WIDTHS);
        pretaxDeductionsTable.setTotalWidth(AMOUNT_AND_DESCRIPTION_WIDTH);

        addGrayHeaderCells(pretaxDeductionsTable, "Deductions From Gross");

        addPaylineItems(checkPrintPaycheckLineDTOs, pretaxDeductionsTable);

        addTotalCells(pretaxDeductionsTable,
                pCheckPrintPaycheckDTO.getPreTaxDeductionsCheckTotal(),
                pCheckPrintPaycheckDTO.getPreTaxDeductionsYtdTotal(),
                "TOTAL DEDUCTIONS FROM GROSS", false);

        addTotalCells(pretaxDeductionsTable,
                pCheckPrintPaycheckDTO.getAdjustedEarningsCheckTotal(),
                pCheckPrintPaycheckDTO.getAdjustedEarningsYtdTotal(),
                "TOTAL ADJUSTED EARNINGS", true);

        return pretaxDeductionsTable;
    }

    private PdfPTable createTaxesDeductionsTable(CheckPrintPaycheckDTO pCheckPrintPaycheckDTO) throws DocumentException {
        Set<CheckPrintPaycheckLineDTO> checkPrintPaycheckLineDTOs = pCheckPrintPaycheckDTO.getTaxes();
        PdfPTable taxDeductionsTable = new PdfPTable(5);
        taxDeductionsTable.setWidths(AMOUNT_AND_DESCRIPTION_WIDTHS);
        taxDeductionsTable.setTotalWidth(AMOUNT_AND_DESCRIPTION_WIDTH);

        addGrayHeaderCells(taxDeductionsTable, "Taxes");

        addPaylineItems(checkPrintPaycheckLineDTOs, taxDeductionsTable);

        addTotalCells(taxDeductionsTable,
                pCheckPrintPaycheckDTO.getTaxesCheckTotal(),
                pCheckPrintPaycheckDTO.getTaxesYtdTotal(),
                "TOTAL TAXES", true);

        return taxDeductionsTable;
    }

    private PdfPTable createDeductionsTable(CheckPrintPaycheckDTO pCheckPrintPaycheckDTO) throws DocumentException {
        Set<CheckPrintPaycheckLineDTO> checkPrintPaycheckLineDTOs = pCheckPrintPaycheckDTO.getDeductions();
        PdfPTable deductionsTable = new PdfPTable(5);
        deductionsTable.setWidths(AMOUNT_AND_DESCRIPTION_WIDTHS);
        deductionsTable.setTotalWidth(AMOUNT_AND_DESCRIPTION_WIDTH);

        addGrayHeaderCells(deductionsTable, "Adjustments to Net Pay");

        addPaylineItems(checkPrintPaycheckLineDTOs, deductionsTable);

        addTotalCells(deductionsTable,
                pCheckPrintPaycheckDTO.getDeductionsCheckTotal(),
                pCheckPrintPaycheckDTO.getDeductionsYtdTotal(),
                "TOTAL ADJUSTMENTS TO NET PAY", true);

        return deductionsTable;
    }

    private PdfPTable createTaxableCompanyContributionsTable(CheckPrintPaycheckDTO pCheckPrintPaycheckDTO) throws DocumentException {
        Set<CheckPrintPaycheckLineDTO> checkPrintPaycheckLineDTOs = pCheckPrintPaycheckDTO.getCompanyTaxableContributions();
        PdfPTable deductionsTable = new PdfPTable(5);
        deductionsTable.setWidths(AMOUNT_AND_DESCRIPTION_WIDTHS);
        deductionsTable.setTotalWidth(AMOUNT_AND_DESCRIPTION_WIDTH);

        addGrayHeaderCells(deductionsTable, "Taxable Company Items");

        addPaylineItems(checkPrintPaycheckLineDTOs, deductionsTable);

        return deductionsTable;
    }

    private PdfPTable createNonTaxableCompanyContributionsTable(CheckPrintPaycheckDTO pCheckPrintPaycheckDTO) throws DocumentException {
        Set<CheckPrintPaycheckLineDTO> checkPrintPaycheckLineDTOs = pCheckPrintPaycheckDTO.getCompanyContributions();
        PdfPTable deductionsTable = new PdfPTable(5);
        deductionsTable.setWidths(AMOUNT_AND_DESCRIPTION_WIDTHS);
        deductionsTable.setTotalWidth(AMOUNT_AND_DESCRIPTION_WIDTH);

        addGrayHeaderCells(deductionsTable, "Non-taxable Company Items");

        addPaylineItems(checkPrintPaycheckLineDTOs, deductionsTable);

        return deductionsTable;
    }

    private PdfPTable createNetPayTable(CheckPrintPaycheckDTO pCheckPrintPaycheckDTO) throws DocumentException {
        PdfPTable netPayTable = new PdfPTable(5);
        netPayTable.setWidths(AMOUNT_AND_DESCRIPTION_WIDTHS);
        netPayTable.setTotalWidth(AMOUNT_AND_DESCRIPTION_WIDTH);

        PdfPCell headerCell = new PdfPCell(new Paragraph("Net Pay", PrintConstants.FONT_HELVETICA_NORMAL_7PT));
        headerCell.setNoWrap(true);
        headerCell.setPaddingBottom(4);
        headerCell.setPaddingTop(4);
        headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        headerCell.setVerticalAlignment(Element.ALIGN_TOP);
        headerCell.setBackgroundColor(Color.LIGHT_GRAY);
        headerCell.setBorder(PdfPCell.NO_BORDER);
        headerCell.setColspan(3);
        netPayTable.addCell(headerCell);

        PdfPCell totalCurrentAmount = new PdfPCell(new Paragraph(mZeroDecimalFormatter.format(pCheckPrintPaycheckDTO.getCheckNetPay()), PrintConstants.FONT_COURIER_NORMAL_8PT));
        totalCurrentAmount.setNoWrap(true);
        totalCurrentAmount.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
        totalCurrentAmount.setPaddingRight(NUMBER_PADDING_RIGHT);
        totalCurrentAmount.setBorder(CheckUtils.getBorder(PdfPCell.LEFT, PdfPCell.TOP));
        netPayTable.addCell(totalCurrentAmount);

        PdfPCell totalYtdAmount = new PdfPCell(new Paragraph(mZeroDecimalFormatter.format(pCheckPrintPaycheckDTO.getYtdNetPay()), PrintConstants.FONT_COURIER_NORMAL_8PT));
        totalYtdAmount.setNoWrap(true);
        totalYtdAmount.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
        totalYtdAmount.setPaddingRight(NUMBER_PADDING_RIGHT);
        totalYtdAmount.setBorder(CheckUtils.getBorder(PdfPCell.LEFT, PdfPCell.TOP));
        netPayTable.addCell(totalYtdAmount);

        PdfPCell cell1 = new PdfPCell();
        cell1.setBorder(PdfPCell.NO_BORDER);
        cell1.setColspan(3);
        netPayTable.addCell(cell1);

        PdfPCell cell2 = new PdfPCell();
        cell2.setBorder(PdfPCell.LEFT);
        netPayTable.addCell(cell2);

        PdfPCell cell3 = new PdfPCell();
        cell3.setBorder(PdfPCell.LEFT);
        netPayTable.addCell(cell3);

        return netPayTable;
    }

    private void addGrayHeaderCells(PdfPTable pTable, String pHeaderText) {
        PdfPCell headerCell = new PdfPCell(new Paragraph(pHeaderText, PrintConstants.FONT_HELVETICA_NORMAL_7PT));
        headerCell.setNoWrap(true);
        headerCell.setPaddingBottom(0);
        headerCell.setPaddingTop(0);
        headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        headerCell.setVerticalAlignment(Element.ALIGN_TOP);
        headerCell.setBackgroundColor(Color.LIGHT_GRAY);
        headerCell.setBorder(PdfPCell.NO_BORDER);
        headerCell.setColspan(3);
        pTable.addCell(headerCell);

        PdfPCell emptyCell = new PdfPCell();
        emptyCell.setBorder(PdfPCell.LEFT);

        pTable.addCell(emptyCell);
        pTable.addCell(emptyCell);
    }

    private void addPaylineItems(Set<CheckPrintPaycheckLineDTO> pCheckPrintPaycheckLineDTOs, PdfPTable pTable) {
        int i = 0;
        for (CheckPrintPaycheckLineDTO checkPrintPaycheckLineDTO : pCheckPrintPaycheckLineDTOs) {
            int paddingTop = 0;
            int paddingBottom = 2;
            if (i == 0) {
                paddingTop = 2;
            }

            PdfPCell description = new PdfPCell(new Paragraph(checkPrintPaycheckLineDTO.getPaylineDescription(), PrintConstants.FONT_HELVETICA_NORMAL_7PT));
            description.setNoWrap(true);
            description.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
            description.setVerticalAlignment(PdfPCell.ALIGN_BOTTOM);
            description.setPaddingTop(paddingTop);
            description.setPaddingBottom(paddingBottom);
            description.setPaddingLeft(TEXT_PADDING_LEFT);
            description.setBorder(PdfPCell.NO_BORDER);
            description.setColspan(3);
            pTable.addCell(description);

            PdfPCell currentAmount = new PdfPCell(new Paragraph(maxCharacterReplacement(customFormat(checkPrintPaycheckLineDTO.getPaylineAmount(), checkPrintPaycheckLineDTO.isInCurrentCheck()), CURRENT_YTD_MAX_CHARATERS), PrintConstants.FONT_COURIER_NORMAL_8PT));
            currentAmount.setNoWrap(true);
            currentAmount.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
            currentAmount.setPaddingTop(paddingTop);
            currentAmount.setPaddingBottom(paddingBottom);
            currentAmount.setPaddingRight(NUMBER_PADDING_RIGHT);
            currentAmount.setBorder(PdfPCell.LEFT);
            pTable.addCell(currentAmount);

            PdfPCell ytdAmount = new PdfPCell(new Paragraph(maxCharacterReplacement(customFormat(checkPrintPaycheckLineDTO.getYtdAmount(), (checkPrintPaycheckLineDTO.isInCurrentCheck() && (ZERO.compareTo(checkPrintPaycheckLineDTO.getPaylineAmount()) == 0))), CURRENT_YTD_MAX_CHARATERS), PrintConstants.FONT_COURIER_NORMAL_8PT));
            ytdAmount.setNoWrap(true);
            ytdAmount.setHorizontalAlignment(Element.ALIGN_RIGHT);
            ytdAmount.setPaddingTop(paddingTop);
            ytdAmount.setPaddingBottom(paddingBottom);
            ytdAmount.setPaddingRight(NUMBER_PADDING_RIGHT);
            ytdAmount.setBorder(PdfPCell.LEFT);
            pTable.addCell(ytdAmount);
            i++;
        }
    }

    private void addTotalCells(PdfPTable pTable, BigDecimal pTotal, BigDecimal pYTD, String pTitle, boolean isLastRow) {
        PdfPCell totalTitle = new PdfPCell(new Paragraph(pTitle, PrintConstants.FONT_HELVETICA_NORMAL_7PT));
        totalTitle.setNoWrap(true);
        totalTitle.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
        totalTitle.setColspan(3);
        totalTitle.setBorder(PdfPCell.NO_BORDER);
        pTable.addCell(totalTitle);

        PdfPCell totalCurrentAmount = new PdfPCell(new Paragraph(maxCharacterReplacement(mZeroDecimalFormatter.format(pTotal), CURRENT_YTD_MAX_CHARATERS), PrintConstants.FONT_COURIER_NORMAL_8PT));
        totalCurrentAmount.setNoWrap(true);
        totalCurrentAmount.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
        totalCurrentAmount.setPaddingRight(NUMBER_PADDING_RIGHT);
        totalCurrentAmount.setBorder(CheckUtils.getBorder(PdfPCell.LEFT, PdfPCell.TOP));
        if(!isLastRow) {
            totalCurrentAmount.setPaddingBottom(4);
        }
        pTable.addCell(totalCurrentAmount);

        PdfPCell totalYtdAmount = new PdfPCell(new Paragraph(maxCharacterReplacement(mZeroDecimalFormatter.format(pYTD), CURRENT_YTD_MAX_CHARATERS), PrintConstants.FONT_COURIER_NORMAL_8PT));
        totalYtdAmount.setNoWrap(true);
        totalYtdAmount.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
        totalYtdAmount.setPaddingRight(NUMBER_PADDING_RIGHT);
        totalYtdAmount.setBorder(CheckUtils.getBorder(PdfPCell.LEFT, PdfPCell.TOP));
        if(!isLastRow) {
            totalYtdAmount.setPaddingBottom(4);
        }
        pTable.addCell(totalYtdAmount);
    }

    private PdfPTable createCompanyBackSideAddress(CheckPrintDTO pCheckPrintDTO, CheckPrintPaycheckDTO pCheckPrintPaycheckDTO) throws DocumentException {
        PdfPTable addressTable = new PdfPTable(1);

        int truncate = 40;
        addressTable.addCell(create_0padding_noBorder_cell(truncateString(pCheckPrintDTO.getCompanyLegalName(), truncate), PrintConstants.FONT_COURIER_NORMAL_10PT));
        addressTable.addCell(create_0padding_noBorder_cell(truncateString(pCheckPrintDTO.getCompanyLegalAddress().getAddressLine1(), truncate), PrintConstants.FONT_COURIER_NORMAL_10PT));
        if(!isStringEmpty(pCheckPrintDTO.getCompanyLegalAddress().getAddressLine2())) {
            addressTable.addCell(create_0padding_noBorder_cell(truncateString(pCheckPrintDTO.getCompanyLegalAddress().getAddressLine2(), truncate), PrintConstants.FONT_COURIER_NORMAL_10PT));
        }
        addressTable.addCell(create_0padding_noBorder_cell(truncateString(getCityStateZip(pCheckPrintDTO), truncate), PrintConstants.FONT_COURIER_NORMAL_10PT));
        addressTable.addCell(createCompanyIdEmployeeIdPaycheckIdCell(pCheckPrintDTO, pCheckPrintPaycheckDTO));

        PdfPTable paddingTable = new PdfPTable(1);
        PdfPCell paddingCell = new PdfPCell(addressTable);
        paddingCell.setBorder(PdfPCell.NO_BORDER);
        paddingCell.setPaddingLeft(20);
        paddingCell.setPaddingTop(275);
        paddingCell.setFixedHeight(140);
        paddingTable.addCell(paddingCell);
        paddingTable.setWidthPercentage(100);

        return paddingTable;
    }

    private PdfPTable createEmployeeBackSideAddress(CheckPrintPaycheckDTO pCheckPrintPaycheckDTO) throws DocumentException {
        PdfPTable addressTable = new PdfPTable(1);

        int truncate = 40;
        addressTable.addCell(create_0padding_noBorder_cell(truncateString(pCheckPrintPaycheckDTO.getEmployeePrintName(), truncate), PrintConstants.FONT_COURIER_NORMAL_10PT));
        addressTable.addCell(create_0padding_noBorder_cell(truncateString(pCheckPrintPaycheckDTO.getEmployeeAddress().getAddressLine1(), truncate), PrintConstants.FONT_COURIER_NORMAL_10PT));
        if(!isStringEmpty(pCheckPrintPaycheckDTO.getEmployeeAddress().getAddressLine2())) {
            addressTable.addCell(create_0padding_noBorder_cell(truncateString(pCheckPrintPaycheckDTO.getEmployeeAddress().getAddressLine2(), truncate), PrintConstants.FONT_COURIER_NORMAL_10PT));
        }
        addressTable.addCell(create_0padding_noBorder_cell(truncateString(getEmployeeCityStateZip(pCheckPrintPaycheckDTO), truncate), PrintConstants.FONT_COURIER_NORMAL_10PT));

        PdfPTable paddingTable = new PdfPTable(1);

        PdfPCell paddingCell = new PdfPCell(addressTable);
        paddingCell.setBorder(PdfPCell.NO_BORDER);
        paddingCell.setPaddingLeft(160);
        paddingTable.addCell(paddingCell);
        paddingTable.setWidthPercentage(100);
        return paddingTable;
    }

    private PdfPCell createCompanyIdEmployeeIdPaycheckIdCell(CheckPrintDTO pCheckPrintDTO, CheckPrintPaycheckDTO pCheckPrintPaycheckDTO) {
        PdfPCell cell = new PdfPCell(new Paragraph(companyIdEmployeeIdPaycheckId(pCheckPrintDTO, pCheckPrintPaycheckDTO), PrintConstants.FONT_COURIER_NORMAL_4PT));
        cell.setPadding(0);
        cell.setBorder(PdfPCell.NO_BORDER);
        return cell;
    }

    private PdfPCell create_0padding_noBorder_cell(String pText, Font pFont) {
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

    private String customFormat(BigDecimal value, boolean isInCurrentCheck) {
        if(isInCurrentCheck) {
            return mZeroDecimalFormatter.format(value);
        }
        return (ZERO.compareTo(value) == 0) ? " " : mDecimalFormatter.format(value);
    }

    private String customRateFormat(BigDecimal rate) {
        if(ZERO.compareTo(rate) == 0) {
            return " ";
        }
        rate = rate.multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP);
        String rateStr = mRateDecimalFormatter.format(rate);
        if(rateStr.endsWith(".")) {
            rateStr = rateStr.substring(0, rateStr.length()-1);
        }
        return rateStr + "%";
    }

    private String customFormatPadding(BigDecimal value) {
        String output =  mZeroDecimalFormatter.format(value);
        int totalLength = 14;
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

        int totalLength = 70;
        String netPayInWords = CheckUtils.getWrittenAmount(value).toString();

        int netPayWordsLength = netPayInWords.length();
        if(netPayWordsLength > 66) {
            netPayInWords = mDecimalFormatter.format(value);
            netPayWordsLength = netPayInWords.length();
        }
        for (int i = 0; i < (totalLength - netPayWordsLength - 4); i++) {

            output = output + "*";
        }

        output = output + netPayInWords;

        return output;
    }

    private String getCityStateZip(CheckPrintDTO pCheckPrintDTO) {
        String city = pCheckPrintDTO.getCompanyLegalAddress().getCity();
        String state = pCheckPrintDTO.getCompanyLegalAddress().getState();
        String zip = pCheckPrintDTO.getCompanyLegalAddress().getZipCode();
        return city + ", " + state + " " + zip;
    }

    private String getEmployeeCityStateZip(CheckPrintPaycheckDTO pCheckPrintPaycheckDTO) {
        String empCity = pCheckPrintPaycheckDTO.getEmployeeAddress().getCity();
        String empState = pCheckPrintPaycheckDTO.getEmployeeAddress().getState();
        String empZip = pCheckPrintPaycheckDTO.getEmployeeAddress().getZipCode();
        return empCity + ", " + empState + " " + empZip;
    }

    private String companyIdEmployeeIdPaycheckId(CheckPrintDTO pCheckPrintDTO, CheckPrintPaycheckDTO pCheckPrintPaycheckDTO) {
        String companyId = pCheckPrintDTO.getCompanyId();
        String employeeId = pCheckPrintPaycheckDTO.getEmployeeId();
        String paycheckId = Long.toString(pCheckPrintPaycheckDTO.getPaycheckId());
        return "C:" + companyId + " E:" + employeeId + " P:" + paycheckId;
    }

    private String obscureBankAccountNumber(String bankAccountNumber) {
        int accountNumberLength = bankAccountNumber.length();
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < accountNumberLength - 4; i++) {
            stringBuilder.append("*");
        }
        stringBuilder.append(bankAccountNumber.substring(accountNumberLength-4, accountNumberLength));

        return stringBuilder.toString();
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

    private String maxCharacterReplacement(String string, int maxCharcters) {
        if(string != null && string.length() > maxCharcters) {
            StringBuilder stringBuilder = new StringBuilder();
            for(int i = 0; i<maxCharcters; i++) {
                stringBuilder.append("*");
            }
            return stringBuilder.toString();
        }

        return string;
    }    
}
