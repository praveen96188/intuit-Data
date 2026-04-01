package com.intuit.sbd.payroll.psp.batchjobs.util;

import com.intuit.sbd.payroll.psp.domain.SystemParameter;
import com.intuit.spc.foundations.portabilitySpecific.util.SpcfDecimalImpl;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PRAcroForm;
import com.lowagie.text.pdf.PdfCopy;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.List;

public class CheckUtils
{
    
    static {
        FontFactory.registerDirectory(SystemParameter.findValue(SystemParameter.Code.CD_FONT_DIRECTORY));
        FontFactory.registerDirectories();
    }

	private static final String [] SPELL_NUMBER = {
		"zero",			// 0
		"one",			// 1
		"two",			// 2
		"three",		// 3
		"four",			// 4
		"five",			// 5
		"six",			// 6
		"seven",		// 7
		"eight",		// 8
		"nine",			// 9
		"ten",			// 10
		"eleven",		// 11
		"twelve",		// 12
		"thirteen",		// 13
		"fourteen",		// 14
		"fifteen",		// 15
		"sixteen",		// 16
		"seventeen",	// 17
		"eighteen",		// 18
		"nineteen",		// 19
		"twenty",		// 20
		"thirty",		// 21
		"forty",		// 22
		"fifty",		// 23
		"sixty",		// 24
		"seventy",		// 25
		"eighty",		// 26
		"ninety",		// 27
		"hundred",		// 28
		"thousand",		// 29
		"million",		// 30
		"billion",		// 31
		"trillion",		// 32
	};

	private static int HUNDREDS_INDEX   = 28;
	private static int THOUSANDS_INDEX  = 29;
	private static int MILLIONS_INDEX   = 30;
	private static int BILLIONS_INDEX   = 31;
	private static int TRILLIONS_INDEX  = 32;

	private static long THOUSAND  = 1000L;
	private static long MILLION   = THOUSAND * THOUSAND;
	private static long BILLION   = MILLION * THOUSAND;
	private static long TRILLION  = BILLION * THOUSAND;

	public static StringBuffer getWrittenAmount(BigDecimal amount)
	{
        SpcfMoney m = new SpcfMoney(new SpcfDecimalImpl(amount));

		StringBuffer sb_dollars = new StringBuffer();

		long dollars = m.getIntegerPart();

		if (dollars == 0) {
			sb_dollars.append("Zero");
		}
		else {
			long quotient  = 0;
			long remainder = 0;

			quotient  = (long) Math.floor(dollars / TRILLION);
			remainder = dollars % TRILLION;
			if (quotient > 0) {
				sb_dollars.append(getHundredsWrittenAmount(quotient));
				sb_dollars.append(" ");
				sb_dollars.append(SPELL_NUMBER[TRILLIONS_INDEX]);
			}

			quotient  = (long) Math.floor(remainder / BILLION);
			remainder = remainder % BILLION;
			if (quotient > 0) {
				if (sb_dollars.length() > 0) {
					sb_dollars.append(" ");
				}
				sb_dollars.append(getHundredsWrittenAmount(quotient));
				sb_dollars.append(" ");
				sb_dollars.append(SPELL_NUMBER[BILLIONS_INDEX]);
			}

			quotient  = (long) Math.floor(remainder / MILLION);
			remainder = remainder % MILLION;
			if (quotient > 0) {
				if (sb_dollars.length() > 0) {
					sb_dollars.append(" ");
				}
				sb_dollars.append(getHundredsWrittenAmount(quotient));
				sb_dollars.append(" ");
				sb_dollars.append(SPELL_NUMBER[MILLIONS_INDEX]);
			}

			quotient  = (long) Math.floor(remainder / THOUSAND);
			remainder = remainder % THOUSAND;
			if (quotient > 0) {
				if (sb_dollars.length() > 0) {
					sb_dollars.append(" ");
				}
				sb_dollars.append(getHundredsWrittenAmount(quotient));
				sb_dollars.append(" ");
				sb_dollars.append(SPELL_NUMBER[THOUSANDS_INDEX]);
			}
			if (remainder > 0) {
				if (sb_dollars.length() > 0) {
					sb_dollars.append(" ");
				}
				sb_dollars.append(getHundredsWrittenAmount(remainder));
			}
		}

		StringBuffer sb_cents = new StringBuffer();
		int cents = m.getFractionalPart();
		if (cents < 10) {
			sb_cents.append("0").append(cents);
		}
		else {
			sb_cents.append(cents);
		}
		sb_dollars.append(" and ").append(sb_cents.toString()).append("/100");

		char c = sb_dollars.charAt(0);
		sb_dollars.setCharAt(0, Character.toUpperCase(c));

		return sb_dollars;
	}

	private static String getHundredsWrittenAmount(long value)
	{
		StringBuffer sb = new StringBuffer();

		int quotient  = 0;
		int remainder = 0;

		quotient  = (int) Math.floor(value / 100);
		remainder = (int) (value % 100);

		if (quotient > 0) {
			sb.append(SPELL_NUMBER[quotient]).append(" ").append(SPELL_NUMBER[HUNDREDS_INDEX]);
		}

		if (remainder < 20) {
			if (remainder > 0) {
				if (sb.length() > 0) {
					sb.append(" ");
				}
				sb.append(SPELL_NUMBER[remainder]);
			}
		}
		else {
			if (sb.length() > 0) {
				sb.append(" ");
			}
			quotient  = (int) Math.floor(remainder / 10) - 2;		// It must be equal to or greater than 2.
			remainder = remainder % 10;

			sb.append(SPELL_NUMBER[quotient + 20]);
			if (remainder > 0) {
				sb.append("-").append(SPELL_NUMBER[remainder]);
			}
		}
		return sb.toString();
	}

    //combines all non-null PDFs
    public static byte[] combinePdfs(List<byte[]> pdfs) throws Exception {
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        Document document = null;
        PdfCopy writer = null;
        boolean firstFile = true;
        for (byte[] pdfBytes : pdfs) {
            if (pdfBytes != null && pdfBytes.length > 0) {
                PdfReader reader = new PdfReader(pdfBytes);
                reader.consolidateNamedDestinations();
                int numberOfPages = reader.getNumberOfPages();
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

        if(document != null) {
            document.close();
        }

        return byteArrayOutputStream.toByteArray();
    }

    public static int getBorder(int... pSides) {
        int total = 0;
        // or all of the sides together
        // ** Note: duplicate side error checking not implemented
        for (int side : pSides) {
            total += side;
        }
        return total;
    }

    public static Font getMICRFont() {
        Font micrFont;
        if (FontFactory.contains(PrintConstants.MICR)) {
            micrFont = FontFactory.getFont(PrintConstants.MICR, BaseFont.CP1252, BaseFont.EMBEDDED);
            micrFont.setSize(11.9f);
        } else if (FontFactory.contains(PrintConstants.IDAutomationMICR)) {
            micrFont = FontFactory.getFont(PrintConstants.IDAutomationMICR, BaseFont.CP1252, BaseFont.EMBEDDED);
            micrFont.setSize(11.9f);
        } else {
            throw new RuntimeException("MICR font was not found");
        }
        return micrFont;
    }
}
