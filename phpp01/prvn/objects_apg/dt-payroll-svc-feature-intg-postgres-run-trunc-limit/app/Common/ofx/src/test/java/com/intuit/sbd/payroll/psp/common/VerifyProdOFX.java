package com.intuit.sbd.payroll.psp.common;

import com.intuit.sbd.payroll.psp.util.OFXAPPVERObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by IntelliJ IDEA.
 * User: jchickanosky
 * Date: May 7, 2008
 * Time: 3:28:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class VerifyProdOFX {
    private String mInputDir;
    private String mOutputDir;
    private Map<String, Long> versionMap = new HashMap<String, Long>();
    private Map<String, Long> flavorMap = new HashMap<String, Long>();

    public static void main(String []args) {
        try {
            VerifyProdOFX verifyProdOFX = new VerifyProdOFX();
            verifyProdOFX.multithreadProcessing();
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public VerifyProdOFX() {
        mInputDir = "C:\\Documents and Settings\\znorcross\\Desktop\\Prod ofx\\Requests";
        mOutputDir = "C:\\Documents and Settings\\znorcross\\Desktop\\Prod ofx\\tested\\";
    }

    public void multithreadProcessing() {

        File[] files = new File(mInputDir).listFiles();

        ExecutorService threadPool = null;

        try {
            // Create threadPool with given parameters
            threadPool = new ThreadPoolExecutor(10, 20, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
            CompletionService<OFXAPPVERObject> completionService = new ExecutorCompletionService<OFXAPPVERObject>(threadPool);

            int numberOfFiles = 0;
            for (File file: files) {
                numberOfFiles++;
                final File finalFile = file;
                completionService.submit(new Callable<OFXAPPVERObject>() {
                    public OFXAPPVERObject call() {
                        return processFile(finalFile);
                    }
                });
            }
            System.out.println("Added " + numberOfFiles + " files.");

            // Get the results of each thread execution
            try {
                OFXAPPVERObject ofxappverObject;
                for (int t = 0; t < numberOfFiles; t++) {
                    Future<OFXAPPVERObject> f = completionService.take();
                    ofxappverObject = f.get();
                    if(ofxappverObject != null) {
                        Long versionCount = versionMap.get(ofxappverObject.getQBVersionStr());
                        if(versionCount == null) {
                            versionCount = (long) 0;
                        }
                        versionCount++;
                        versionMap.put(ofxappverObject.getQBVersionStr(), versionCount);
                        Long flavorCount = flavorMap.get(ofxappverObject.getFlavorId());
                        if(flavorCount == null) {
                            flavorCount = (long) 0;
                        }
                        flavorCount++;
                        flavorMap.put(ofxappverObject.getFlavorId(), flavorCount);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }

            System.out.println("Versions:");
            for (String version : versionMap.keySet()) {
                System.out.println(version + "," + versionMap.get(version));
            }

            System.out.println("Flavors:");
            for (String flavor : flavorMap.keySet()) {
                System.out.println(flavor + "," + flavorMap.get(flavor));
            }
        } finally {
            if (threadPool != null) {
                threadPool.shutdown(); // Disable new tasks from being submitted
                try {
                    int timeout = 0;
                    while (timeout < 300 && !threadPool.awaitTermination(60, TimeUnit.SECONDS)) {
                        timeout += 60;
                    }
                    if (timeout > 60) {
                        threadPool.shutdownNow();
                    }
                } catch (InterruptedException ie) {
                    // (Re-)Cancel if current thread also interrupted
                    threadPool.shutdownNow();
                    // Preserve interrupt status
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private long readFile(FileReader pFileReader, StringBuilder pStringBuilder) throws IOException {
        long lineCount = 0;
        BufferedReader input =  new BufferedReader(pFileReader);
        try {
            String line;
            while (( line = input.readLine()) != null){
                pStringBuilder.append(line);
                pStringBuilder.append(System.getProperty("line.separator"));
                lineCount++;
            }
        } finally {
            input.close();
        }

        return lineCount;
    }

    private OFXAPPVERObject processFile(File pFile) {
        try {
            StringBuilder ofxString = new StringBuilder();
            long lineCount = readFile(new FileReader(pFile), ofxString);
            Calendar beforeCal = Calendar.getInstance();
            com.intuit.sbd.payroll.psp.common.ofx.request.OFX ofxObj = OFXManager.ofxRequestToJava(ofxString.toString());
            //com.intuit.sbd.payroll.psp.common.ofx.response.OFX ofxObj = OFXManager.ofxResponseToJava(ofxString.toString());
            Calendar afterCal = Calendar.getInstance();
            double timeInSecs = (afterCal.getTimeInMillis()-beforeCal.getTimeInMillis())/1000.00;
            System.out.println(lineCount + "," + timeInSecs + "," + pFile.getName());
            pFile.renameTo(new File(mOutputDir + pFile.getName()));
            return new OFXAPPVERObject(ofxObj.getSIGNONMSGSRQV1().getSONRQ().getAPPVER());
            //return null;
        } catch (Exception e) {
            System.out.println("Error converting '"+pFile.getName()+"': "+e.toString());
        }

        return null;
    }
 }
