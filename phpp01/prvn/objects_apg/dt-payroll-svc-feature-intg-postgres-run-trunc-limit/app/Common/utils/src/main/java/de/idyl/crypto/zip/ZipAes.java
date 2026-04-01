package de.idyl.crypto.zip;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileOutputStream;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.Collection;
import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: Jan 18, 2009
 * Time: 4:47:14 PM
 * To change this template use File | Settings | File Templates.
 */
public class ZipAes {
    /**
     * Compress inFile, writing to ZipOutputStream. Use if you want to control the compressed output stream.
     * @param pInFile The file you want to compress.
     * @param pIncludePathInfo Whether to include file path info in zip archive.
     * @param pZos The ZipOutputStream to write the compressed file.
     */
    public static void zip(File pInFile, boolean pIncludePathInfo, ZipOutputStream pZos) {
        try {
            FileInputStream fis = new FileInputStream(pInFile);

            try {
                try {
                    pZos.putNextEntry(new ZipEntry(pIncludePathInfo ? pInFile.getAbsolutePath() : pInFile.getName()));

                    byte[] buffer = new byte[1024];
                    int len;

                    while ((len = fis.read(buffer)) > 0) {
                        pZos.write(buffer, 0, len);
                    }
                } finally {
                    pZos.closeEntry();
                }
            } finally {
                fis.close();
            }
        } catch (IOException e) {
            throw new RuntimeException("Error compressing file to zip output stream. ", e);
        }
    }

    /**
     * Compress the files in the pInFiles list to the file pZipFileName.
     * @param pZipFileName The name of the resulting zip archive.
     * @param pIncludePathInfo Whether to include file path info in zip archive.
     * @param pInFiles The list of files to compress.
     */
    public static void zip(File pZipFileName, boolean pIncludePathInfo, Collection<String> pInFiles) {
        if (pInFiles.isEmpty()) {
            throw new RuntimeException("Input file list is empty (no files to compress).");
        }

        try {
            File compressedFile = File.createTempFile("psp", ".zip", pZipFileName.getAbsoluteFile().getParentFile());
            ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(compressedFile));
            boolean success = false;

            try {
                for (String file : pInFiles) {
                    zip(new File(file), pIncludePathInfo, zos);
                }

                success = true;
            } finally {
                zos.flush();
                zos.close();

                if (!success) {
                    compressedFile.deleteOnExit();
                } else {
                    if (pZipFileName.exists()) {
                        pZipFileName.delete();
                    }

                    compressedFile.renameTo(pZipFileName);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error compressing files. ", e);
        }
    }

    public static void zip(File pZipFileName, boolean pIncludePathInfo, String... pInFiles) {
        zip(pZipFileName, pIncludePathInfo, Arrays.asList(pInFiles));
    }

    /**
     * Encrypt the given unencrypted zip file. If pEncryptedZipFile is null, the resulting encrypted file will be
     * named the same as the input file (if the encryption is successful).
     * @param pUnencryptedZipFile The unencrypted zip file to encrypt.
     * @param pEncryptedZipFile The desired name of the encrypted file. If null, the encrypted file will be renamed
     * to pUnencryptedZipFile (if the encryption is successful).
     * @param pPassword The password of the encrypted file.
     */
    public static void encrypt(File pUnencryptedZipFile, File pEncryptedZipFile, String pPassword) {
        File encryptedFile;
        File outFileName;

        try {
            if (pEncryptedZipFile == null) {
                encryptedFile = File.createTempFile("psp", ".zip", pUnencryptedZipFile.getAbsoluteFile().getParentFile());
                outFileName = new File(encryptedFile.getParent(), pUnencryptedZipFile.getName());
            } else {
                encryptedFile = File.createTempFile("psp", ".zip", pEncryptedZipFile.getAbsoluteFile().getParentFile());
                outFileName = new File(encryptedFile.getParent(), pEncryptedZipFile.getName());
            }

            AesZipOutputStream aeszos = new AesZipOutputStream(encryptedFile);
            boolean success = false;

            try {
                ZipFile zipFile = new ZipFile(pUnencryptedZipFile);

                try {
                    aeszos.add(zipFile, pPassword);
                    success = true;
                } finally {
                    zipFile.close();
                }
            } finally {
                aeszos.finish();

                if (!success) {
                    encryptedFile.deleteOnExit();
                } else {
                    if (outFileName.exists()) {
                        outFileName.delete();
                    }

                    encryptedFile.renameTo(outFileName);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error encrypting file: " + pUnencryptedZipFile, e);
        }
    }

    /**
     * Convenience method to encrypt a zip file, naming the resulting encrypted file the same as the unencrypted file.
     * @param pUnencryptedZipFile The unencrypted zip file to encrypt.
     * @param pPassword The password of the encrypted file.
     */
    public static void encrypt(File pUnencryptedZipFile, String pPassword) {
        encrypt(pUnencryptedZipFile, null, pPassword);
    }

    /**
     * Convenience method to first zip a list of files and then encrypt the resulting zip archive.
     * This is the same as calling: zip(zipFile, fileList) and then encrypt(zipFile, password)
     * @param pZipFileName The name of the resulting encrypted zip archive.
     * @param pPassword The password for the encrypted file.
     * @param pIncludePathInfo Whether to include file path info in zip archive.
     * @param pInFiles The list of files to compress and encrypt.
     */
    public static void zipAndEncrypt(File pZipFileName,
                                     String pPassword,
                                     boolean pIncludePathInfo,
                                     Collection<String> pInFiles) {
        zip(pZipFileName, pIncludePathInfo, pInFiles);
        encrypt(pZipFileName, pPassword);
    }

    public static void zipAndEncrypt(File pZipFileName,
                                     String pPassword,
                                     boolean pIncludePathInfo,
                                     String... pInFiles) {
        zipAndEncrypt(pZipFileName, pPassword, pIncludePathInfo, Arrays.asList(pInFiles));
    }
}
