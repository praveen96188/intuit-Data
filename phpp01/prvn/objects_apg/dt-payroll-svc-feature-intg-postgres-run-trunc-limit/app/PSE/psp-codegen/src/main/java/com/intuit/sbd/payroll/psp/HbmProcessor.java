package com.intuit.sbd.payroll.psp;

import com.intuit.sbd.payroll.psp.bedl.BedlDataEntity;
import com.intuit.sbd.payroll.psp.bedl.BedlInternalNoteSpec;
import com.intuit.sbd.payroll.psp.bedl.BedlProcessor;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

/**
 * Created by IntelliJ IDEA.
 * User: rnorian
 * Date: Sep 18, 2008
 * Time: 1:26:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class HbmProcessor {

    static public void process(BedlProcessor bedlProcessor) {

        for (BedlDataEntity dataEntity : bedlProcessor.getDataEntities()) {
            if (dataEntity.hasInternalNoteSpec(BedlInternalNoteSpec.VERSIONING)) {

                String value = dataEntity.getInternalNoteSpec(BedlInternalNoteSpec.VERSIONING).getValue();
                if (value.equalsIgnoreCase("false") || value.equalsIgnoreCase("off")) {
                    removeOptimisticVersioning(bedlProcessor.getBedlFolderName(), dataEntity.getClassName());
                }
                if (value.startsWith("dirty")) {
                    setDirtyOptmisticVersioning(bedlProcessor, dataEntity.getClassName());
                }
            }
        }

        // Enable second-level caching for data objects
        for (BedlDataEntity dataObject : bedlProcessor.getDataObjects()) {
            addCacheUsageElement(bedlProcessor.getBedlFolderName(), dataObject.getClassName());
        }

    }

    private static void setDirtyOptmisticVersioning(BedlProcessor bedlProcessor, String className) {
        final String[] SEARCH_FOR = {"<version name=\"Version\" type=\"long\" column=\"VERSION\" unsaved-value=\"negative\" />",
                                     "<property name=\"ModifiedDate\" type=\"com.intuit.sbd.payroll.psp.hibernate.SpcfCalendarUserType\" column=\"MODIFIED_DATE\" not-null=\"true\"/>",
                                     "<property name=\"ModifierId\" type=\"string\" column=\"MODIFIER_ID\" not-null=\"false\" />",
                                     "<class name=\"com.intuit.sbd.payroll.psp.domain." + className + "\" entity-name=\"com.intuit.sbd.payroll.psp.domain." + className + "\" table=\"PSP_" +
                                             "" + bedlProcessor.getPspTableName(className) + "\">"};
        final String[] REPLACE_WITH = {"<property name=\"Version\" type=\"long\" column=\"VERSION\" update=\"false\"/>",
                                       "<property name=\"ModifiedDate\" type=\"com.intuit.sbd.payroll.psp.hibernate.SpcfCalendarUserType\" column=\"MODIFIED_DATE\" not-null=\"true\" optimistic-lock=\"false\"/>",
                                       "<property name=\"ModifierId\" type=\"string\" column=\"MODIFIER_ID\" not-null=\"false\" optimistic-lock=\"false\"/>",
                                       "<class name=\"com.intuit.sbd.payroll.psp.domain." + className + "\" entity-name=\"com.intuit.sbd.payroll.psp.domain." + className + "\" table=\"PSP_" + bedlProcessor.getPspTableName(className) + "\" optimistic-lock=\"dirty\" dynamic-update=\"true\">"};

        System.out.println("Setting Hibernate dirty concurrency versioning for type: " + className);
        replaceTexts(bedlProcessor.getBedlFolderName(), className, SEARCH_FOR, REPLACE_WITH);
    }

    private static void addCacheUsageElement(String bedlFolderName, String className) {
        final String VERSION_ELEMENT = "<id ";
        final String VERSION_REPLACEMENT = "<cache usage=\"read-write\" />\n    <id ";

        replaceText(bedlFolderName, className, VERSION_ELEMENT, VERSION_REPLACEMENT);
    }


    static public void removeOptimisticVersioning(String bedlFolderName, String className) {
        final String VERSION_ELEMENT = "<version name=\"Version\" type=\"long\" column=\"VERSION\" unsaved-value=\"negative\" />";
        final String VERSION_REPLACEMENT = "<property name=\"Version\" type=\"long\" column=\"VERSION\" not-null=\"true\" generated=\"insert\" insert=\"false\" update=\"false\"/>";

        System.out.println("Removing Hibernate automatic optimistic concurrency versioning tag from type: " + className);
        replaceText(bedlFolderName, className, VERSION_ELEMENT, VERSION_REPLACEMENT);
    }

    private static void replaceText(String bedlFolderName, String className, String searchFor, String replaceWith) {
        replaceTexts(bedlFolderName, className, new String[] {searchFor}, new String[] {replaceWith});
    }

    private static void replaceTexts(String bedlFolderName, String className, String[] searchForStrings, String[] replaceWithStrings) {
        // assumption: current working directory is the PSP branch root, i.e. c:\dev\psp\main
        String[] hbmResourcePath = new String[]{"target", "src", "java", "resources",
                "com", "intuit", "sbd", "payroll", "psp", "domain"};

        File hbmResourceDir = new File(bedlFolderName, buildPath(hbmResourcePath).getPath());
        if (!hbmResourceDir.exists()) {
            System.err.println(hbmResourceDir.getAbsolutePath() + " : could not locate hbm resource directory");
            System.exit(-1);
        }

        File hbmMappingFile = new File(hbmResourceDir, className + ".hbm.xml");
        String mappingContent = getFileContent(hbmMappingFile);
        for (int i = 0; i < searchForStrings.length; i++) {
            mappingContent = mappingContent.replaceFirst(searchForStrings[i], replaceWithStrings[i]);
        }
        writeFile(hbmMappingFile, mappingContent);
    }

    static private String getFileContent(File file) {
        String content = "";
        FileReader reader = null;
        try {
            reader = new FileReader(file);
            char[] buffer = new char[(int) file.length()];
            reader.read(buffer, 0, (int) file.length());
            content = new String(buffer);
            reader.close();
        }
        catch (Throwable t) {
            System.err.println(file.getAbsolutePath() + " : failed to read file contents\n" + t.getMessage());
            System.exit(-1);
        }

        return content;
    }

    static private void writeFile(File file, String content) {
        try {
            FileWriter writer = new FileWriter(file, false);
            writer.write(content);
            writer.close();
        }
        catch (Throwable t) {
            System.err.println(file.getAbsolutePath() + " : failed to write file contents\n" + t.getMessage());
            System.exit(-1);
        }
    }

    static private File buildPath(String[] pathElements) {
        File path = null;
        for (String pathElement : pathElements) {
            path = new File(path, pathElement);
        }
        return path;
    }
}
