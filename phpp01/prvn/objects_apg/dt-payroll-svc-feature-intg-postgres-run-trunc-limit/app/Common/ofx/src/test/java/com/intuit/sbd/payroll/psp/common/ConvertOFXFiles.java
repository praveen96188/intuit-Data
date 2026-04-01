package com.intuit.sbd.payroll.psp.common;

/**
 * Created by IntelliJ IDEA.
 * User: jchickanosky
 * Date: May 12, 2008
 * Time: 10:17:55 AM
 * To change this template use File | Settings | File Templates.
 */
import java.io.*;

/**
 * Created by IntelliJ IDEA.
 * User: jchickanosky
 * Date: May 7, 2008
 * Time: 2:00:14 PM
 * To change this template use File | Settings | File Templates.
 */
public class ConvertOFXFiles {
    public static void main(String []args) {
        try {
            File inputDir = new File("C:\\tempdir\\prodofx\\data\\input");
            File outputDir = new File("C:\\tempdir\\prodofx\\data\\output");
            File[] files = inputDir.listFiles();
            for (File f: files) {
                BufferedReader infileBuff =
                    new BufferedReader(new FileReader(f));
                String line = null;
                do {
                    line = infileBuff.readLine();
                } while (line.compareTo("<OFX>") != 0);
                File outfile = new File(outputDir,f.getName());
                BufferedWriter outfileBuff =
                        new BufferedWriter(new FileWriter(outfile));
                outfileBuff.write("<OFX>\n");
                do {
                    line = infileBuff.readLine();
                    outfileBuff.write(line + "\n");
                } while (line.compareTo("</OFX>") != 0);
                outfileBuff.close();
                infileBuff.close();
                f.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }
}
