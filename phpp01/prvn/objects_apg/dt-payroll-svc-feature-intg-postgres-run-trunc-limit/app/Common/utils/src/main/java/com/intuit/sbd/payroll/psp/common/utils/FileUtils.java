package com.intuit.sbd.payroll.psp.common.utils;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by IntelliJ IDEA.
 * User: dhaddan
 * Date: Apr 21, 2011
 * Time: 3:10:44 PM
 * To change this template use File | Settings | File Templates.
 */
public class FileUtils {
    //took this from //common in P4
    public static final int DEFAULTBUFFERSIZE = 1024;
    private static SpcfLogger mLogger = Application.getLogger(FileUtils.class);

    public static final boolean copyInputStream(InputStream in, OutputStream out) throws Throwable {
            byte[] buffer = new byte[DEFAULTBUFFERSIZE];
            int len;

            while ((len = in.read(buffer)) >= 0) {
                out.write(buffer, 0, len);
            }

            in.close();
            out.close();


        return true;
    }

    public static void zip(File file, String zipFileName) throws IOException {
        FileOutputStream fos = null;
        ZipOutputStream zos = null;
        FileInputStream fis = null;
        try {
            fos = new FileOutputStream(zipFileName);
            zos = new ZipOutputStream(fos);
            ZipEntry ze = new ZipEntry(file.getName());
            zos.putNextEntry(ze);
            fis = new FileInputStream(file);
            IOUtils.copy(fis,zos);
        }finally {
            if(zos!=null){
                zos.closeEntry();
                zos.close();
            }
            if(fis != null)
                fis.close();
            if(fos != null)
                fos.close();
        }
    }

    public static final String gUnZip(String gzipFileName) throws Throwable {
        FileOutputStream out = null;
        GZIPInputStream in = null;
        String outFile = null;

        in = new GZIPInputStream(new FileInputStream(gzipFileName));
        outFile = gzipFileName.substring(0, gzipFileName.lastIndexOf('.', gzipFileName.length()));
        out = new FileOutputStream(outFile);



        return (copyInputStream(in, out) ? outFile : null);
    }

    public static List<File> getFilesInDirectory(String directoryName, String regex){
        List<File> returnList = new ArrayList<File>();
        File directory = new File(directoryName);
         class RegexFilenameFilter implements FilenameFilter{
             String regex;
            RegexFilenameFilter(String regex){
                this.regex = regex;
            }
             public boolean accept(File dir, String name){
                 return name.matches(regex);
             }
        }
        File[] matchingFiles = directory.listFiles(new RegexFilenameFilter(regex));
        for(File file:matchingFiles){
            returnList.add(file);
        }
        return returnList;
    }

    public static List<File> getFilesInDirectory(String directoryName, String prefix, String suffix){
        StringBuilder builder = new StringBuilder();
        if(StringUtils.isNotEmpty(prefix)){
            builder.append("^\\Q"+prefix+"\\E");
        }
        builder.append(".*");
        if(StringUtils.isNotEmpty(suffix)){
            builder.append("\\Q"+suffix+"\\E$");
        }
        return FileUtils.getFilesInDirectory(directoryName, builder.toString());
    }

    public static List<String> getAbsoluteFilenamesFromFiles(List<File> fileList){
        List<String> returnList = new ArrayList<String>();
        for(File file: fileList){
            returnList.add(file.getName());
        }
        return returnList;
    }

    public static boolean deleteFile(String pFileName) {
        File file = new File(pFileName);
        return !file.exists() || file.delete();
    }

    public static void moveFileTo(File pFile, String pDestFolder) {
        try {
            pFile.renameTo(new File(pDestFolder + "/" + pFile.getName()));
        } catch (Exception e) {
            throw new RuntimeException("Error to move file " + pFile.getName() + " to " + pDestFolder);
        }
    }

    public static void moveFilesTo(List<File> pFiles, String pDestFolder){
        if (pFiles != null) {
            for (File aFile : pFiles) {
                moveFileTo(aFile, pDestFolder);
            }
        }
    }

}
