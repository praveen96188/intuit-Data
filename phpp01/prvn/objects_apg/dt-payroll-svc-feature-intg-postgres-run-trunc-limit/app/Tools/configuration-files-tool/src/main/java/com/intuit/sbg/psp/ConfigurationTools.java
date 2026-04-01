package com.intuit.sbg.psp;

import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import com.intuit.sbd.payroll.psp.Application;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import name.fraser.neil.plaintext.diff_match_patch;
/**
 * Created by ajhawar on 6/2/2016.
 */
public class ConfigurationTools {
    private static final SpcfLogger logger = Application.getLogger(ConfigurationTools.class);
    private static final String DIR_NAME_COMMAND = "-dir";
    private static final String FILE_NAME_COMMAND = "-file";
    private static final String PATTERN_NAME_COMMAND = "-pattern";
    private static final String INSERT_NAME_COMMAND = "-i";
    private static final String APPEND_NAME_COMMAND = "-a";
    private static final String REPLACE_NAME_COMMAND = "-r";
    private static final String DELETE_NAME_COMMAND = "-x";

    private static String mFileName = null;
    private static String mDirName = "../PSE/Configuration/env";
    private static String mPatternName = null;
    private static String mChangeType = null;

    private diff_match_patch dmp;
    private diff_match_patch.Operation DELETE = diff_match_patch.Operation.DELETE;
    private diff_match_patch.Operation EQUAL = diff_match_patch.Operation.EQUAL;
    private diff_match_patch.Operation INSERT = diff_match_patch.Operation.INSERT;


    public static void testDiffMain(String s,String t) {
        // Perform a trivial diff.
        diff_match_patch difference = new diff_match_patch();
        LinkedList<diff_match_patch.Diff> deltas = difference.diff_main(s, t);

        // Reconstruct texts from the deltas
        //  text1 = all deletion (-1) and equality (0).
        //  text2 = all insertion (1) and equality (0).
        String text1 = "";
        String text2 = "";
        for(diff_match_patch.Diff d: deltas)
        {
            if(d.operation== diff_match_patch.Operation.DELETE)
                text1 = d.text;
            else if(d.operation== diff_match_patch.Operation.INSERT)
                text2 = d.text;

        }
        text1 = text1.replace("\n", "").replace("\r", "");
        text2 = text2.replace("\n", "").replace("\r", "");
        if(!text1.isEmpty())
            logger.info("Deleted : " + text1);
        if (!text2.isEmpty())
            logger.info("Inserted : "+text2);
    }

    private static void parseArgs(String[] args) {
        // For new addition -a
        //For replacing the existing tags -r
        // Environment Options p: Production q: QA
        final String usage = "ConfigurationTool -dir=FullPathOfConfigDirectory -file=ChangeFile --i/-a/-r/-x";
        
        for (String arg : args) {
            String[] argParts = arg.split("=");
            if (argParts.length == 2) {
                if (argParts[0].equals(DIR_NAME_COMMAND)) {
                    mDirName = argParts[1];
                } else if (argParts[0].equals(FILE_NAME_COMMAND)) {
                    mFileName = argParts[1];
                } else if (argParts[0].equals(PATTERN_NAME_COMMAND)) {
                    mPatternName = argParts[1];
                }

            } else if (argParts.length == 1) {
                if (argParts[0].equals(INSERT_NAME_COMMAND) || argParts[0].equals(APPEND_NAME_COMMAND) || argParts[0].equals(REPLACE_NAME_COMMAND) || argParts[0].equals(DELETE_NAME_COMMAND)) {
                    mChangeType = argParts[0];

                }
            }

        }
        
    }

    public static ArrayList<String> FileWalk(String path) {
        File root = new File(path);
        String pattern = mPatternName;
        Pattern p = Pattern.compile(pattern);

        ArrayList<String> fileListPattern = new ArrayList<String>();
        if (root.isDirectory()) {
            File[] list = root.listFiles();
            if (list == null)
                return null;
            for (File f : list) {
                if (f.isFile()) {

                    Matcher m = p.matcher(f.getName());
                    if (m.matches()) {
                        fileListPattern.add(f.getAbsolutePath());
                    }
                }
            }
        }

        return fileListPattern;
    }

    static String readFile(String path, Charset encoding)
            throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }


    public static void InsertXmlNode(String configFile, String additionFile) throws IOException, DocumentException {
            /* Read the existing config file */
            File inputFile = new File(configFile);

            SAXReader reader = new SAXReader();
            Document document1 = reader.read(inputFile);
            Element rootElement1 = document1.getRootElement();
            String oldFile = readFile(configFile, StandardCharsets.UTF_8);
            /* Read additional root element added */
            File addFile = new File(additionFile);
            reader = new SAXReader();
            Document document2 = reader.read(addFile);
            Element rootElement2 = document2.getRootElement();
            rootElement1.add(rootElement2);
            writeToXmlFile(configFile, document1);
            String newFile = readFile(configFile, StandardCharsets.UTF_8);
            testDiffMain(oldFile,newFile);
    }


    public static void AppendXmlNode(String configFile, String additionFile) throws DocumentException, IOException {
        /* Read the existing config file */
            File inputFile = new File(configFile);
            SAXReader reader = new SAXReader();
            String oldFile = readFile(configFile, StandardCharsets.UTF_8);
            Document document1 = reader.read(inputFile);
        /* get changes to be done*/
            HashMap<String, String> changeHashMap = FileToHashMap(additionFile);

            Iterator it = changeHashMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                String valueString = pair.getKey().toString();
                String[] additionalNode = valueString.split("/");
                String additionNode = additionalNode[additionalNode.length - 1];
                String beforeAddingNode = valueString.substring(0, valueString.length() - additionNode.length() - 1);
                List<Node> rootNode = document1.selectNodes("/" + beforeAddingNode);
                Element element = (Element) rootNode.get(0);
                Element newElement = element.addElement(additionNode);
                newElement.setText(pair.getValue().toString());
                it.remove();
            }
            writeToXmlFile(configFile, document1);
            String newFile = readFile(configFile, StandardCharsets.UTF_8);
            testDiffMain(oldFile, newFile);
    }

    public static void EditFile(ArrayList<String> fileList) throws IOException, DocumentException {
        
        if (INSERT_NAME_COMMAND.equals(mChangeType)) {
            for (String file : fileList) {
                InsertXmlNode(file, mFileName);
            }
        } else if (APPEND_NAME_COMMAND.equals(mChangeType)) {
            for (String file : fileList) {
                AppendXmlNode(file, mFileName);
            }
        } else if (REPLACE_NAME_COMMAND.equals(mChangeType)) {
            for (String file : fileList) {
                ReplaceXmlNode(file, mFileName);
            }
        } else if (DELETE_NAME_COMMAND.equals(mChangeType)) {
            for (String file : fileList) {
                DeleteXmlNode(file, mFileName);
            }
        }
    }


    public static HashMap<String, String> FileToHashMap(String replacerFile) throws IOException {
        String delim = ":=";
        String filePath = replacerFile;
        HashMap<String, String> map = new HashMap<String, String>();
        String line;
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split(delim, 2);
            if (parts.length >= 2) {
                String key = parts[0];
                String value = parts[1];
                map.put(key, value);
            } else {
                System.out.println("ignoring line: " + line);
            }
        }
        reader.close();
        return map;
    }

    public static void DeleteXmlNode(String configFile, String deleteNodeFile) throws IOException, DocumentException {
            File inputFile = new File(configFile);
            SAXReader reader = new SAXReader();
            String oldFile = readFile(configFile, StandardCharsets.UTF_8);
            Document document1 = reader.read(inputFile);
            HashMap<String, String> changeHashMap = FileToHashMap(deleteNodeFile);
            Iterator it = changeHashMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                List<Node> rootNode = document1.selectNodes("/" + pair.getKey());
                List<Node> childNode = document1.selectNodes("/" + pair.getKey() + "/" + pair.getValue());
                Element rootElement = (Element) rootNode.get(0);
                Element childElement = (Element) childNode.get(0);
                rootElement.remove(childElement);
                it.remove();
            }

            // Pretty print the document to System.out
            writeToXmlFile(configFile, document1);
            String newFile = readFile(configFile, StandardCharsets.UTF_8);
            testDiffMain(oldFile, newFile);
    }

    public static void ReplaceXmlNode(String configFile, String replacerFile) throws IOException, DocumentException {
        /* Read the existing config file */
            File inputFile = new File(configFile);
            SAXReader reader = new SAXReader();
            String oldFile = readFile(configFile, StandardCharsets.UTF_8);
            Document document1 = reader.read(inputFile);

        /* get changes to be done*/
            HashMap<String, String> changeHashMap = FileToHashMap(replacerFile);

            Iterator it = changeHashMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                List<Node> rootNode = document1.selectNodes("/" + pair.getKey());
                Element element = (Element) rootNode.get(0);
                element.setText(pair.getValue().toString());
                it.remove();
            }

            writeToXmlFile(configFile, document1);
            String newFile = readFile(configFile, StandardCharsets.UTF_8);
            testDiffMain(oldFile, newFile);
    }

    public static void writeToXmlFile(String configFile, Document document) throws IOException {
        // Pretty print the document to System.out
        OutputFormat format = OutputFormat.createPrettyPrint();
        XMLWriter writer;
        PrintWriter outputFile = new PrintWriter(configFile);
        writer = new XMLWriter(outputFile, format);
        writer.write(document);
        outputFile.close();
        logger.info("Completed writing the changes in " + configFile);
    }

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        try {
            parseArgs(args);
            ArrayList<String> filelist = FileWalk(mDirName);
            logger.info("Beginning Config File Changes");
            EditFile(filelist);
            logger.info("Completed Config File Changes");
        } catch (DocumentException e) {
            logger.error(e.getMessage().split("Nested exception")[0]);
            System.exit(-1);
        } catch (IOException e) {
            if(e.getMessage().contains("(Access is denied)")) {
                logger.error("ERROR =  Access Denied, Please check out the files in Perforce.");
                System.exit(-1);
            }else {
                e.printStackTrace();
                System.exit(-1);
            }
        } catch (Exception e) {
            logger.error("ERROR: ");
            e.printStackTrace();
            System.exit(-1);
        }
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        logger.info("Time taken to successfully write the changes :" + totalTime);
    }

}
