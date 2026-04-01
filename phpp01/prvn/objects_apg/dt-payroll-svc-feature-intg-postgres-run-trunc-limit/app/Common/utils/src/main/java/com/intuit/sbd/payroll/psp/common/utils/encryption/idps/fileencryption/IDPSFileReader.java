package com.intuit.sbd.payroll.psp.common.utils.encryption.idps.fileencryption;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import com.intuit.idps.domain.item.Key;
import com.intuit.idps.service.IdpsException;
import java.io.*;

/**
 * @author snasim
 * File Readerwrapper for IDPS
 */
public class IDPSFileReader extends InputStreamReader {

    public IDPSFileReader(String fileName, Key key) throws FileNotFoundException,IOException, IdpsException  {
        super(new IDPSFileInputStream(fileName,key));
    }

    public IDPSFileReader( File file, Key key) throws FileNotFoundException,IOException, IdpsException  {
        super(new IDPSFileInputStream( file,key));
    }

    public IDPSFileReader(FileDescriptor fd, Key key)throws IOException, IdpsException {
        super(new IDPSFileInputStream(fd,key));
    }
    public IDPSFileReader( File file, Key key, String charsetNam) throws FileNotFoundException,IOException, IdpsException  {
        super(new IDPSFileInputStream( file,key),charsetNam);
    }

}
