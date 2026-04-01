package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.domain.CheckPrintSignature;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;

import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.io.*;
import java.util.zip.CRC32;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Mar 3, 2010
 * Time: 7:20:14 AM
 */
public class AddCheckPrintSignatureCore extends Process implements IProcess {
    private SourceSystemCode mSourceSystem;
    private String mSourceCompanyId;
    private byte[] mSignature;
    private Company mCompany;

    public AddCheckPrintSignatureCore(SourceSystemCode pSourceSystemCd, String pCompanyId, byte[] pSignature) {
        mSourceSystem = pSourceSystemCd;
        mSourceCompanyId = pCompanyId;
        mSignature = pSignature;
    }

    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        validationResult.merge(com.intuit.sbd.payroll.psp.api.managers.util.Validator.validCompanyParameters(mSourceSystem, mSourceCompanyId));
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        if (mSignature == null) {
            validationResult.getMessages().InvalidArgument(EntityName.CheckPrintSignature, "Check Print Signature", "Check Print Signature");
            return validationResult;
        }

        //Check if company exists
        mCompany = Company.findCompany(mSourceCompanyId, mSourceSystem);
        if (mCompany == null) {
            validationResult.getMessages().CompanyDoesNotExist(EntityName.Company, mSourceCompanyId,
                    mSourceSystem.toString(), mSourceCompanyId);
            return validationResult;
        }

        // validate the signature is a png, 1050 X 285, and 1200 dpi
        DataInputStream dataIn = new DataInputStream(new ByteArrayInputStream(mSignature));
        try {
            // file type
            if(!PNGDecoder.isPNGSignature(dataIn)) {
                validationResult.getMessages().InvalidCheckPrintImageType(EntityName.CheckPrintSignature, mSourceCompanyId);
                return validationResult;
            }

            PNGData chunks = PNGDecoder.readChunks(dataIn);
            // size
            if(chunks.getWidth() != 1050 || chunks.getHeight() != 285) {
                validationResult.getMessages().IncorrectCheckPrintImageSize(EntityName.CheckPrintSignature, mSourceCompanyId,
                    chunks.getWidth(), chunks.getHeight());
                return validationResult;
            }

            // resolution
            if(chunks.getPPI() < 1000) {
                validationResult.getMessages().IncorrectCheckPrintImageResolution(EntityName.CheckPrintSignature, mSourceCompanyId, chunks.getPPI());
                return validationResult;
            }
        } catch (IOException e) {
            validationResult.getMessages().ReadCheckPrintImageError(EntityName.CheckPrintSignature, mSourceCompanyId, e);
            return validationResult;
        }

        return validationResult;
    }

    public ProcessResult<CheckPrintSignature> process() {
        ProcessResult<CheckPrintSignature> processResult = new ProcessResult<CheckPrintSignature>();

        CheckPrintSignature checkPrintSignature = CheckPrintSignature.findCheckPrintSignature(mCompany);
        if(checkPrintSignature == null) {
            checkPrintSignature = new CheckPrintSignature();
            checkPrintSignature.setCompany(mCompany);
        }
        checkPrintSignature = Application.save(checkPrintSignature);
        checkPrintSignature.setSignatureAsImage(mSignature);
        processResult.setResult(checkPrintSignature);
        processResult.setSuccess(true);

        return processResult;
    }
}

class PNGDecoder {

        protected static boolean isPNGSignature(DataInputStream in) throws IOException {
            long signature = in.readLong();
            return signature == 0x89504e470d0a1a0aL;
        }

        protected static PNGData readChunks(DataInputStream in) throws IOException {
            PNGData chunks = new PNGData();

            boolean trucking = true;
            while (trucking) {
                try {
                    // Read the length.
                    int length = in.readInt();
                    if (length < 0)
                        throw new IOException("Sorry, that file is too long.");
                    // Read the type.
                    byte[] typeBytes = new byte[4];
                    in.readFully(typeBytes);
                    // Read the data.
                    byte[] data = new byte[length];
                    in.readFully(data);
                    // Read the CRC.
                    long crc = in.readInt() & 0x00000000ffffffffL; // Make it
                    // unsigned.
                    if (!verifyCRC(typeBytes, data, crc)) {
                        throw new IOException("The file appears to be corrupted.");
                    }

                    PNGChunk chunk = new PNGChunk(typeBytes, data);
                    chunks.add(chunk);
                } catch (EOFException eofe) {
                    trucking = false;
                }
            }
            return chunks;
        }

        protected static boolean verifyCRC(byte[] typeBytes, byte[] data, long crc) {
            CRC32 crc32 = new CRC32();
            crc32.update(typeBytes);
            crc32.update(data);
            long calculated = crc32.getValue();
            return (calculated == crc);
        }
    }

    class PNGData {
        private static final double METER_INCH_CONVERSION = 0.0254;
        private int mNumberOfChunks;

        private PNGChunk[] mChunks;

        public PNGData() {
            mNumberOfChunks = 0;
            mChunks = new PNGChunk[10];
        }

        public void add(PNGChunk chunk) {
            mChunks[mNumberOfChunks++] = chunk;
            if (mNumberOfChunks >= mChunks.length) {
                PNGChunk[] largerArray = new PNGChunk[mChunks.length + 10];
                System.arraycopy(mChunks, 0, largerArray, 0, mChunks.length);
                mChunks = largerArray;
            }
        }

        public long getWidth() {
            return getChunk("IHDR").getUnsignedInt(0);
        }

        public long getHeight() {
            return getChunk("IHDR").getUnsignedInt(4);
        }

        public short getBitsPerPixel() {
            return getChunk("IHDR").getUnsignedByte(8);
        }

        public long getPPI() {
            // returns ppi for x-axis, should be the same as y-axis
            // meters
            if(getPPIMeasure() == 1) {
                return (long)(getPPIX() * METER_INCH_CONVERSION);
            }
            // unknown
            else {
                return getPPIX();
            }
        }

        public long getPPIX() {
            return getChunk("pHYs").getUnsignedInt(0);
        }

        public long getPPIY() {
            return getChunk("pHYs").getUnsignedInt(4);
        }

        public long getPPIMeasure() {
            return getChunk("pHYs").getUnsignedByte(8);
        }

        public short getColorType() {
            return getChunk("IHDR").getUnsignedByte(9);
        }

        public short getCompression() {
            return getChunk("IHDR").getUnsignedByte(10);
        }

        public short getFilter() {
            return getChunk("IHDR").getUnsignedByte(11);
        }

        public short getInterlace() {
            return getChunk("IHDR").getUnsignedByte(12);
        }

        public ColorModel getColorModel() {
            short colorType = getColorType();
            int bitsPerPixel = getBitsPerPixel();

            if (colorType == 3) {
                byte[] paletteData = getChunk("PLTE").getData();
                int paletteLength = paletteData.length / 3;
                return new IndexColorModel(bitsPerPixel, paletteLength,
                        paletteData, 0, false);
            }
            System.out.println("Unsupported color type: " + colorType);
            return null;
        }

        public PNGChunk getChunk(String type) {
            for (int i = 0; i < mNumberOfChunks; i++)
                if (mChunks[i].getTypeString().equals(type))
                    return mChunks[i];
            return null;
        }
    }

    class PNGChunk {
        private byte[] mType;

        private byte[] mData;

        public PNGChunk(byte[] type, byte[] data) {
            mType = type;
            mData = data;
        }

        public String getTypeString() {
            try {
                return new String(mType, "UTF8");
            } catch (UnsupportedEncodingException uee) {
                return "";
            }
        }

        public byte[] getData() {
            return mData;
        }

        public long getUnsignedInt(int offset) {
            long value = 0;
            for (int i = 0; i < 4; i++)
                value += (mData[offset + i] & 0xff) << ((3 - i) * 8);
            return value;
        }

        public short getUnsignedByte(int offset) {
            return (short) (mData[offset] & 0x00ff);
        }
    }
