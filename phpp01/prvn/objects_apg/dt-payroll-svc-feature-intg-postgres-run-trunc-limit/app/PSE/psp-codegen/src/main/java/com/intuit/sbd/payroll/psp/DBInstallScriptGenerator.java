package com.intuit.sbd.payroll.psp;

import com.intuit.sbd.payroll.psp.bedl.BedlDataEntity;
import com.intuit.sbd.payroll.psp.bedl.BedlProcessor;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
 * @author Allen Chaves
 */
public class DBInstallScriptGenerator {

    static String NEW_LINE = System.getProperty("line.separator");
    private static final String TOAD_COMPARISON_RESULT_FILENAME = "SyncScript.sql";

    public void generateCode(BedlProcessor pBedlProcessor) throws IOException {

        // The generated files
        String generatedPspOracleCreateTables = pBedlProcessor.getBedlFolderName() + "CreateTables.sql";
        String savedGeneratedPspOracleCreateTables = pBedlProcessor.getBedlFolderName() + "SavedCreateTables.sql";
        String generatedPspOracleCreateData = pBedlProcessor.getBedlFolderName() + "target/src/resources/oracle.createData.sql";

        // The files this process will generate
        String createDBFullFileName = pBedlProcessor.getBedlFolderName() + "DBCreate.sql";
        String upgradeDBFullFileName = pBedlProcessor.getBedlFolderName() + "DBUpgrade.sql";
        String populateDBFullFileName = pBedlProcessor.getBedlFolderName() + "DBPopulate.sql";
        String dbPatchUpdateFileData = pBedlProcessor.getBedlFolderName() + "DBPatchUpdate.sql";
        String previousBedlFileName = pBedlProcessor.getBedlFolderName() + "PreviousPSP.bedl.xml";

        // The version in the model
        String newModelVersionNumber = pBedlProcessor.getSchemaVersion();
        System.out.println("Model Version: " + newModelVersionNumber);
        String oldModelVersionNumber = newModelVersionNumber;

        // If PreviousPSP.bedl.xml exists
        File previousBedlFile = new File(previousBedlFileName);
        if (!previousBedlFile.exists()) {
            File currentCreateDBFile = new File(createDBFullFileName);
            Boolean isReadOnly = !currentCreateDBFile.canWrite();

            // we just need to generate new DBCreate.sql and DBPopulate.sql in case new database objects are added
            // through scripts
            generateDBCreateSqlScript(generatedPspOracleCreateTables, createDBFullFileName, newModelVersionNumber, newModelVersionNumber, dbPatchUpdateFileData);
            generateDBPopulateSqlScript(generatedPspOracleCreateData, populateDBFullFileName, newModelVersionNumber, newModelVersionNumber);

            if (isReadOnly) {
                // Restore read only attribute
                File currentDBPopulateFile = new File(populateDBFullFileName);

                currentCreateDBFile.setReadOnly();
                currentDBPopulateFile.setReadOnly();
            }
            return;
        }

        // If developer set the upgrade file read only flag, don't regenerated it (and the others)
        // This is for account for the fact that sometimes we need to do manual adjustments to the
        // toad generated file but we still ideally run through all the steps of upgrading a database
        // automatically
        File diffScriptFile = new File(pBedlProcessor.getBedlFolderName(), "DBUpgrade_" + getPaddedSchemaVersion(newModelVersionNumber) + ".sql");
        Boolean generateNewUpgradeFiles = !diffScriptFile.exists() || diffScriptFile.canWrite();

        // Bedl processor for previous bedl
        BedlProcessor previousBedlProcessor = new BedlProcessor(previousBedlFileName);

        oldModelVersionNumber = previousBedlProcessor.getSchemaVersion();
        System.out.println("Old Model Version: " + oldModelVersionNumber);

        System.out.println("");
        System.out.println("==========================================================================================");
        System.out.println("I - Create PSPAPP and PREVIOUSPSPAPP users");
        System.out.println("==========================================================================================");
        executeSqlPlus("PSP_LOCAL", "PSP_LOCAL", pBedlProcessor.getBedlFolderName() + "CreateUser.sql", "PSPAPP", "NONDBA");
        executeSqlPlus("PSP_LOCAL", "PSP_LOCAL", pBedlProcessor.getBedlFolderName() + "CreateUser.sql", "PREVIOUSPSPAPP", "NONDBA");

        System.out.println("");
        System.out.println("==========================================================================================");
        System.out.println("II - Generate database scripts from previous bedl file (version " + previousBedlProcessor.getSchemaVersion() + ")");
        System.out.println("==========================================================================================");
        renameFile(generatedPspOracleCreateTables, savedGeneratedPspOracleCreateTables);
        new CreateTableGenerator().generateCode(previousBedlProcessor);
        generateDBCreateSqlScript(generatedPspOracleCreateTables, createDBFullFileName, newModelVersionNumber, newModelVersionNumber, dbPatchUpdateFileData);
        appendToFile(createDBFullFileName, "exit 0\n", "/\n");

        //Generate scripts to add constraints.
        new ColumnConstraintGenerator().generateCode(previousBedlProcessor);

        System.out.println("");
        System.out.println("==========================================================================================");
        System.out.println("III - Clean PREVIOUS_PSP_LOCAL schema and recreate with generated previous scripts");
        System.out.println("==========================================================================================");
        executeSqlPlus("PSP_LOCAL", "PSP_LOCAL", pBedlProcessor.getBedlFolderName() + "CreateUser.sql", "PREVIOUS_PSP_LOCAL", "DBA");
        executeSqlPlus("PREVIOUS_PSP_LOCAL", "PREVIOUS_PSP_LOCAL", pBedlProcessor.getBedlFolderName() + "DropDBObjects.sql");

        // Add tables
        executeSqlPlus("PREVIOUS_PSP_LOCAL", "PREVIOUS_PSP_LOCAL", createDBFullFileName, "PREVIOUSPSPAPP", "PSPAPP_ROLE", "CRUD");

        // Add indexes
        copyFile(pBedlProcessor.getBedlFolderName() + "../sql/Index/create_indexes.sql", pBedlProcessor.getBedlFolderName() + "temp_create_indexes.sql");
        appendToFile(pBedlProcessor.getBedlFolderName() + "temp_create_indexes.sql",
                "/\n",
                "delete from psp_applied_database_patch where database_patch_version = '" + getPaddedSchemaVersion(newModelVersionNumber) + "'\n",
                "/\n",
                "exit 0\n",
                "/\n");
        executeSqlPlus("PREVIOUS_PSP_LOCAL", "PREVIOUS_PSP_LOCAL", pBedlProcessor.getBedlFolderName() + "temp_create_indexes.sql", "PREVIOUSPSPAPP", "PSPAPP_ROLE", "CRUD");

        // Add sequences
        File sequenceFolder = new File(pBedlProcessor.getBedlFolderName() + "../sql/Sequence");
        for (File sequenceScript : sequenceFolder.listFiles()) {
            if (sequenceScript.getName().endsWith(".sql")) {
                copyFile(sequenceScript.getAbsolutePath(), pBedlProcessor.getBedlFolderName() + "temp_sequence.sql");
                appendToFile(pBedlProcessor.getBedlFolderName() + "temp_sequence.sql",
                        "\n",
                        "exit 0\n",
                        "/\n");
                executeSqlPlus("PREVIOUS_PSP_LOCAL", "PREVIOUS_PSP_LOCAL", pBedlProcessor.getBedlFolderName() + "temp_sequence.sql", "PREVIOUSPSPAPP", "PSPAPP_ROLE", "CRUD");
            }
        }

        //Add constraints
        copyFile(pBedlProcessor.getBedlFolderName() + "DB_Generated_Constraints.sql", pBedlProcessor.getBedlFolderName() + "temp_DB_Generated_Constraints.sql");
        // Add exit statements at the end
        appendToFile(pBedlProcessor.getBedlFolderName() + "temp_DB_Generated_Constraints.sql", "\n " +
                "exit 0\n" +
                "/\n");

        executeSqlPlus("PREVIOUS_PSP_LOCAL", "PREVIOUS_PSP_LOCAL", pBedlProcessor.getBedlFolderName() + "temp_DB_Generated_Constraints.sql", "PREVIOUSPSPAPP", "PSPAPP_ROLE", "CRUD");

        renameFile(savedGeneratedPspOracleCreateTables, generatedPspOracleCreateTables);

        System.out.println("");
        System.out.println("==========================================================================================");
        System.out.println("IV - Generate new DBCreate.sql and DBPopulate.sql");
        System.out.println("==========================================================================================");
        updateDBPatchUpdateScript(dbPatchUpdateFileData, newModelVersionNumber);
        generateDBCreateSqlScript(generatedPspOracleCreateTables, createDBFullFileName, oldModelVersionNumber, newModelVersionNumber, dbPatchUpdateFileData);
        generateDBPopulateSqlScript(generatedPspOracleCreateData, populateDBFullFileName, oldModelVersionNumber, newModelVersionNumber);
        //Generate scripts to add constraints.
        new ColumnConstraintGenerator().generateCode(pBedlProcessor);

        System.out.println("");
        System.out.println("==========================================================================================");
        System.out.println("V - Clean PSP_LOCAL schema (running DropDBObjects.sql) and recreate with InstallDB.sql.");
        System.out.println("==========================================================================================");
        executeSqlPlus("PSP_LOCAL", "PSP_LOCAL", pBedlProcessor.getBedlFolderName() + "DropDBObjects.sql");
        executeSqlPlus("PSP_LOCAL", "PSP_LOCAL", pBedlProcessor.getBedlFolderName() + "InstallDB.sql", "PSPAPP", "PSPAPP_ROLE", "CRUD", "NONE", "NONE");

        if (generateNewUpgradeFiles) {
            System.out.println("");
            System.out.println("==========================================================================================");
            System.out.println("VI - Run toad to compare PREVIOUS_PSP_LOCAL and PSP-LOCAL schemas and generate diff script");
            System.out.println("     This will generate the DBUpgrade_XXX.XXX.XXX.XXX.sql");
            System.out.println("==========================================================================================");
            executeToadMacro(pBedlProcessor.getBedlFolderName(), "ToadDiff.ini");
            renameFile(pBedlProcessor.getBedlFolderName() + TOAD_COMPARISON_RESULT_FILENAME, diffScriptFile.getAbsolutePath());
            appendToFile(diffScriptFile.getAbsolutePath(), NEW_LINE + "PROMPT finished " + diffScriptFile.getName());

            // Generate DBUpgradeFrom____To_____ Before and After files if they do not exist
            // These are the files that developers can hand code for data migration purposes
            generateDataMigrationFiles(pBedlProcessor.getBedlFolderName(), newModelVersionNumber, newModelVersionNumber);

            // Update DBUpgrade.sql with call to diff script
            updateDBUpgradeScript(pBedlProcessor.getBedlFolderName(), diffScriptFile.getName(), newModelVersionNumber, newModelVersionNumber);
        }
        else {
            System.out.println("");
            System.out.println("==========================================================================================");
            System.out.println("VI - We are not generating the upgrade files because " + diffScriptFile.getAbsolutePath() + " is read only");
            System.out.println("==========================================================================================");
        }


        System.out.println("");
        System.out.println("==========================================================================================");
        System.out.println("VII - At this point, PREVIOUS_PSP_LOCAL has the schema before the changes and PSP_LOCAL has the new schema");
        System.out.println("    We now update PREVIOUS_PSP_LOCAL by running InstallDB.sql");
        System.out.println("==========================================================================================");
        executeSqlPlus("PREVIOUS_PSP_LOCAL", "PREVIOUS_PSP_LOCAL", pBedlProcessor.getBedlFolderName() + "InstallDB.sql", "PREVIOUSPSPAPP", "PSPAPP_ROLE", "CRUD", "NONE", "NONE");

        System.out.println("");
        System.out.println("==========================================================================================");
        System.out.println("VIII - At this point, PREVIOUS_PSP_LOCAL and PSP_LOCAL should be identical");
        System.out.println("       To make sure, run toad again to compare the schemas");
        System.out.println("==========================================================================================");
        executeToadMacro(pBedlProcessor.getBedlFolderName(), "ToadDiff.ini");
        Collection<String> outputFileContents = readAllFile(pBedlProcessor.getBedlFolderName() + TOAD_COMPARISON_RESULT_FILENAME);
        for (String outputLine : outputFileContents) {
            if (!outputLine.startsWith("--") &&
                    !outputLine.startsWith("Prompt") &
                            outputLine.trim().length() != 0) {
                throw new RuntimeException("PREVIOUS_PSP_LOCAL <> PSP_LOCAL: Upgrade script did not upgrade correctly: " + outputLine);
            }
        }
    }

    private void generateDBPopulateSqlScript(String generatedPspOracleCreateData, String populateDBFullFileName, String modelVersionNumber, String createDBCurrentVersion) {
        // Generate new DBPopulateObjects.sql
        deleteFile(populateDBFullFileName);
        concatenateFiles(populateDBFullFileName, createDBCurrentVersion, modelVersionNumber,
                generatedPspOracleCreateData);
    }

    private void generateDBCreateSqlScript(String generatedPspOracleCreateTables, String createDBFullFileName, String modelVersionNumber, String createDBCurrentVersion, String dbPatchUpdateFileData) {
        deleteFile(createDBFullFileName);
        
        // Generate new DBCreateObjects.sql by concatenating all generated DDL files
        concatenateFiles(createDBFullFileName, createDBCurrentVersion, modelVersionNumber,
                generatedPspOracleCreateTables, dbPatchUpdateFileData);
    }

    private void updateDBUpgradeScript(String pBedlFolderName, String pDiffScriptFileName, String previousVersion, String newVersion) {
        String dbUpgradeFileName = pBedlFolderName + "DBUpgrade.sql";
        String diffScriptBareFileName = pDiffScriptFileName.substring(0, pDiffScriptFileName.lastIndexOf("."));

        Collection<String> upgradeFileContents = readAllFile(dbUpgradeFileName);
        for (String line : upgradeFileContents) {
            if (line.indexOf(diffScriptBareFileName) > 0) {
                // Nothing to do
                return;
            }
        }

        String uid1 = UUID.randomUUID().toString();
        String uid2 = UUID.randomUUID().toString();

        // Append to file
        appendToFile(dbUpgradeFileName,
                "--------------------------------------------------------------------------------------" + NEW_LINE,
                "-- Upgrading from " + previousVersion + " to " + newVersion + NEW_LINE,
                "---------------------------------------------------------------------------------------" + NEW_LINE,
                "spool off" + NEW_LINE,
                "set termout off" + NEW_LINE,
                "set heading off" + NEW_LINE,
                "" + NEW_LINE,
                "spool Upgrade.sql" + NEW_LINE,
                "" + NEW_LINE,
                "declare" + NEW_LINE,
                " rec_count number;" + NEW_LINE,
                " upgraderec_count number;" + NEW_LINE,
                " sql_str   varchar2(100);" + NEW_LINE,
                "begin" + NEW_LINE,
                "select count(*) into rec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '" + getPaddedSchemaVersion(newVersion) + "' and DATABASE_PATCH_TYPE_CD='DataMigration'; " + NEW_LINE,
                "select count(*) into upgraderec_count from PSP_APPLIED_DATABASE_PATCH where DATABASE_PATCH_VERSION = '" + getPaddedSchemaVersion(newVersion) + "' and DATABASE_PATCH_TYPE_CD='SchemaUpgrade'; " + NEW_LINE,
                "select " + NEW_LINE,
                "decode(rec_count,0,'@" + diffScriptBareFileName + "_Before.sql',' ') " + NEW_LINE,
                " into sql_str from dual;" + NEW_LINE,
                "dbms_output.put_line(sql_str);" + NEW_LINE,
                "" + NEW_LINE,
                "select" + NEW_LINE,
                " decode(upgraderec_count,0,'@" + diffScriptBareFileName + ".sql',' ') " + NEW_LINE,
                " into sql_str from dual;" + NEW_LINE,
                "dbms_output.put_line(sql_str);" + NEW_LINE,
                "" + NEW_LINE,
                "select" + NEW_LINE,
                " decode(rec_count,0,'@" + diffScriptBareFileName + "_After.sql',' ') " + NEW_LINE,
                " into sql_str from dual;" + NEW_LINE,
                "dbms_output.put_line(sql_str);" + NEW_LINE,
                "" + NEW_LINE,
                "if(rec_count = 0) then" + NEW_LINE,
                "insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)" + NEW_LINE,
                "values ('" + uid1 + "',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '" + getPaddedSchemaVersion(newVersion) + "','DataMigration') ;" + NEW_LINE,
                "" + NEW_LINE,
                "COMMIT;" + NEW_LINE,
                "end if;" + NEW_LINE,
                "if(upgraderec_count = 0) then" + NEW_LINE,
                "insert into PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)" + NEW_LINE,
                "values ('" + uid2 + "',0,'System',SYSTIMESTAMP, SYSTIMESTAMP, '" + getPaddedSchemaVersion(newVersion) + "','SchemaUpgrade') ;" + NEW_LINE,
                "" + NEW_LINE,
                "COMMIT;" + NEW_LINE,
                "end if;" + NEW_LINE,
                "end;" + NEW_LINE,
                "/" + NEW_LINE,
                "" + NEW_LINE,
                "spool off" + NEW_LINE,
                "set termout on" + NEW_LINE,
                "set heading on" + NEW_LINE,
                "" + NEW_LINE,
                "spool InstallDB.log append" + NEW_LINE,
                "" + NEW_LINE,
                "@Upgrade.sql",
                NEW_LINE);
    }

    /**
     * Padds with zeros each part of the schema version, so it can be compared using character ordering in SQL.
     * It only works up to numbers < 1000.
     *
     * @param pSchemaVersion
     * @return
     */
    private static String getPaddedSchemaVersion(String pSchemaVersion) {
        String[] ss = pSchemaVersion.split("\\.");
        String paddedSchemaVersion = "";
        String padding = "000";
        for (String s : ss) {
            if (paddedSchemaVersion.length() > 0) {
                paddedSchemaVersion += ".";
            }
            paddedSchemaVersion += padding.substring(0, padding.length() - s.length()) + s;
        }
        return paddedSchemaVersion;
    }

    private void appendFiles(String toFile, String fromFile) {
        BufferedWriter bw = null;
        BufferedReader br = null;

        try {
            br = new BufferedReader(new FileReader(fromFile));
            bw = new BufferedWriter(new FileWriter(toFile, true));

            String line = br.readLine();
            while (line != null) {
                bw.write(line);
                bw.newLine();

                line = br.readLine();
            }

            bw.flush();
        }
        catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
        finally {                       // always close the file
            if (bw != null) try {
                bw.close();
            }
            catch (IOException ioe2) {
                // just ignore it
            }
            if (br != null) try {
                br.close();
            }
            catch (IOException ioe3) {

            }

        }

    }

    private void appendToFile(String pFullFileName, String... textLines) {
        BufferedWriter bw = null;

        try {
            bw = new BufferedWriter(new FileWriter(pFullFileName, true));

            for (String textLine : textLines) {
                bw.write(textLine);
            }

            bw.flush();
        }
        catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
        finally {                       // always close the file
            if (bw != null) try {
                bw.close();
            }
            catch (IOException ioe2) {
                // just ignore it
            }
        }
    }

    private void executeToadMacro(String pBedlFolderName, String pToadMacroFileName) {
        String toadMacroFileName = pBedlFolderName + pToadMacroFileName;

        System.out.println("starting executeToadMacro with macro " + toadMacroFileName);
        File toadExecutable = null;
        String toadHome = null;

        Process pr;
        BufferedReader input;
        try {
            pr = Runtime.getRuntime().exec("cmd.exe /C echo %TOAD_HOME%");
            input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
            toadHome = input.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Try to find where TOAD is installed
        System.out.println("TOAD_HOME = " + toadHome);
        if(toadHome!= null && toadHome.trim().length() > 0) {
          toadExecutable = new File(toadHome.trim() + File.separator +  "TOAD.exe");
        }

        if (toadExecutable == null || !toadExecutable.exists()) {
            toadExecutable = new File("C:/Program Files (x86)/Quest Software/Toad for Oracle 10.6/TOAD.exe");
        }
        if (!toadExecutable.exists()) {                 
            toadExecutable = new File("C:/Program Files (x86)/Quest Software/Toad for Oracle 10.6/TOAD.exe");
        }
        if (!toadExecutable.exists()) {
            toadExecutable = new File("C:/Program Files (x86)/Quest Software/Toad for Oracle 10.6/TOAD.exe");
        }
        if (!toadExecutable.exists()) {
            throw new RuntimeException("ERROR: Could not find TOAD.exe");
        }

        try {
            String[] args = {toadExecutable.getAbsolutePath(), "-a", "\"SchemaCompare->CompareSchemas | " + toadMacroFileName + "\""};

            System.out.println(String.format("Running Schema Compare: %s %s %s", args[0], args[1], args[2]));

            runCommand(args);

            System.out.println("finished executeToadMacro with macro " + toadMacroFileName);
        } catch (Exception e) {
            System.out.println(e.toString());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private void runCommand(String[] pArgs) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(pArgs);
            processBuilder.redirectErrorStream(true);

            Process process =  processBuilder.start();

            BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = null;
            while ((line = input.readLine()) != null) {
                System.out.println(line);
            }

            process.waitFor();
        } catch (Exception e) {
            System.out.println(e.toString());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private void generateDataMigrationFiles(String baseFolder, String createDBCurrentVersion, String modelVersionNumber) {
        String dbUpgradeBaseName = baseFolder + "DBUpgrade_" + getPaddedSchemaVersion(modelVersionNumber);
        String dbUpgradeAfterFile = dbUpgradeBaseName + "_After.sql";
        String dbUpgradeBeforeFile = dbUpgradeBaseName + "_Before.sql";

        File afterFile = new File(dbUpgradeAfterFile);
        if (!afterFile.exists()) {
            try {
                FileWriter writer = new FileWriter(dbUpgradeAfterFile);
                writer.write("--" + NEW_LINE);
                writer.write("-- This script will be executed AFTER the automatically generated" + NEW_LINE);
                writer.write("-- " + dbUpgradeBaseName + ".sql" + NEW_LINE);
                writer.write("--" + NEW_LINE);
                writer.write("-- Developers can hand code logic here for data migration purposes" + NEW_LINE);
                writer.write("--" + NEW_LINE);

                writer.close();
            }
            catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }

        File beforeFile = new File(dbUpgradeBeforeFile);
        if (!beforeFile.exists()) {
            try {
                FileWriter writer = new FileWriter(dbUpgradeBeforeFile);
                writer.write("--" + NEW_LINE);
                writer.write("-- This script will be executed BEFORE the automatically generated" + NEW_LINE);
                writer.write("-- " + dbUpgradeBaseName + ".sql" + NEW_LINE);
                writer.write("--" + NEW_LINE);
                writer.write("-- Developers can hand code logic here for data migration purposes" + NEW_LINE);
                writer.write("--" + NEW_LINE);

                writer.close();
            }
            catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }

    }

    private void concatenateFiles(String createDBFullFileName, String previousDBVersion, String currentDBVersion, String... filesToConcatenate) {
        // Write to output file by "fixing" the files generated by SPC-F code gen
        ReadState initialState = ReadState.NotInteresting;
        try {
            FileWriter writer = new FileWriter(createDBFullFileName);
            writer.write("--CurrentSchemaVersion=" + currentDBVersion + NEW_LINE);
            writer.write("--PreviousSchemaVersion=" + previousDBVersion + NEW_LINE);

            for (String fileToConcatenate : filesToConcatenate) {
                FileReader fr = new FileReader(fileToConcatenate);
                BufferedReader br = new BufferedReader(fr);
                String line = br.readLine();
                while (line != null) {
                    line += NEW_LINE;

                    // We need to delete blank lines after PRIMARY KEY(

                    // We need to delete repeated "/" lines

                    /* We need to delete all lines between
                     DECLARE
                         VPD_IS_ENABLED INTEGER;

                     and

                     DECLARE
                         index_exists PLS_INTEGER;

                       We need to delete all lines between

                       -- Apply VPD triggers & policies to Dynamic Entity Tables
                       and
                       CREATE OR REPLACE PROCEDURE update_freestanding_column
                     */
                    ReadState currentState = getLineType(line);

                    switch (initialState) {
                        case PrimaryKey:
                            if (currentState != ReadState.BlankLine) {
                                writer.write(line);
                            }
                            initialState = ReadState.NotInteresting;
                            break;
                        case DeclareVpdIsEnabled:
                            if (currentState == ReadState.IndexExists) {
                                writer.write("DECLARE" + NEW_LINE);
                                writer.write(line);

                                initialState = ReadState.NotInteresting;
                            }
                            break;
                        case BeginSpcfVpd:
                            if (currentState == ReadState.EndSpcfVpd) {
                                writer.write(line);

                                initialState = ReadState.NotInteresting;
                            }
                            break;
                        case SqlPlusExecute:
                            if (currentState != ReadState.BlankLine &&
                                    currentState != ReadState.SqlPlusExecute) {

                                if (currentState != ReadState.DeclareVpdIsEnabled) {
                                    writer.write(line);
                                }
                                initialState = currentState;
                            }
                            break;
                        default:
                            if (currentState != ReadState.DeclareVpdIsEnabled) {
                                writer.write(line);
                            }

                            initialState = currentState;
                            break;
                    }
                    line = br.readLine();

                    if ("COMMIT/".equals(line)) line = "COMMIT" + NEW_LINE + "/";
                    if ("//".equals(line)) line = "/";
                }
                br.close();
            }

            //writer.write("exit");
            //writer.write("select 'finished " + createDBFullFileName.substring(createDBFullFileName.lastIndexOf(File.separator) + 1) + " ' || to_char(sysdate, 'MM/DD/YYYY HH24:MI:SS') from dual");
            writer.close();
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private ReadState getLineType(String line) {
        if (line.trim().startsWith("PRIMARY KEY")) {
            return ReadState.PrimaryKey;
        }
        if (line.trim().length() == 0) {
            return ReadState.BlankLine;
        }
        if (line.trim().startsWith("DECLARE VPD_IS_ENABLED INTEGER;")) {
            return ReadState.DeclareVpdIsEnabled;
        }
        if (line.trim().startsWith("index_exists PLS_INTEGER;")) {
            return ReadState.IndexExists;
        }
        if (line.trim().startsWith("-- Apply VPD triggers & policies to Dynamic Entity Tables")) {
            return ReadState.BeginSpcfVpd;
        }
        if (line.trim().startsWith("-- Apply VPD triggers & policies to Dynamic Entity Tables")) {
            return ReadState.BeginSpcfVpd;
        }
        if (line.trim().startsWith("CREATE OR REPLACE PROCEDURE update_freestanding_column")) {
            return ReadState.EndSpcfVpd;
        }
        if (line.trim().startsWith("/")) {
            return ReadState.SqlPlusExecute;
        }
        return ReadState.NotInteresting;
    }

    private enum ReadState {
        PrimaryKey,
        DeclareVpdIsEnabled,
        BlankLine,
        IndexExists,
        SqlPlusExecute,
        BeginSpcfVpd,
        EndSpcfVpd,
        NotInteresting
    }

    private Collection<String> readAllFile(String fullFileName) {
        try {
            ArrayList<String> lines = new ArrayList<String>();
            FileReader fr = new FileReader(fullFileName);
            BufferedReader br = new BufferedReader(fr);
            String line = br.readLine();
            while (line != null) {
                lines.add(line);

                line = br.readLine();
            }
            br.close();
            return lines;
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private void executeSqlPlus(String userName, String userPassword, String scriptFileName, String... paramValues) {
        try {
            String scriptPathName = scriptFileName.substring(0, scriptFileName.lastIndexOf(File.separator) + 1);
            String paramString = ParamValuesToParamString(paramValues);
            System.out.println("starting " + scriptFileName + " for schema " + userName + " with parameters " + paramString);

            deleteFile(scriptPathName + "InstallDB.log");

            // Run SqlPlus and wait for it to finish
            Process pr = Runtime.getRuntime().exec("sqlplus " + userName + "/" + userPassword + "@XE @" + scriptFileName + " " + paramString, null, new File(scriptPathName));

            BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
            String line = null;
            while ((line = input.readLine()) != null) {
                //System.out.println(line);
            }
            pr.waitFor();

            // Process errors (if any)
            BufferedReader errorOutput = new BufferedReader(new FileReader(scriptPathName + "InstallDB.log"));
            ArrayList<String> errorList = getSqlPlusErrors(errorOutput);

            //System.out.println("Exited with error code " + exitVal);
            if (errorList.size() > 0) {
                System.out.println("Errors found: ");
                for (String errorMessage : errorList) {
                    System.out.println(errorMessage);
                }
                throw new RuntimeException(scriptFileName + " for schema " + userName + " with parameters " + paramString + " run with errors");

            } else {
                System.out.println("No errors found");
            }

            System.out.println("finished " + scriptFileName + " for schema " + userName + " with parameters " + paramString);

        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private ArrayList<String> getSqlPlusErrors(BufferedReader input) throws IOException {
        //
        // Code below assumes a few facts about executeSqlPlus
        // Assumption: executeSqlPlus terminates succesfull commands with a "."
        // Assumption: executeSqlPlus only terminates succesfull commands with a "."
        //
        String line = null;
        ArrayList<String> linesBetweenTwoCommands = new ArrayList<String>();
        ArrayList<String> errorList = new ArrayList<String>();
        Boolean hasError = false;
        while ((line = input.readLine()) != null) {
            if (line.endsWith(".")) {
                if (hasError) {
                    errorList.addAll(linesBetweenTwoCommands);
                }
                linesBetweenTwoCommands.clear();
                hasError = false;
            } else {
                linesBetweenTwoCommands.add(line);
            }

            if (line.startsWith("ERROR") ||
                    line.startsWith("PLS-") ||
                    line.startsWith("ORA-")) {
                hasError = true;
            }
            //System.out.println(line);
        }

        return errorList;
    }

    private String ParamValuesToParamString(String[] paramValues) {
        StringBuilder sb = new StringBuilder();
        for (String paramValue : paramValues) {
            sb.append(paramValue);
            sb.append(" ");
        }
        return sb.toString();
    }

    private void renameFile(String oldName, String newName) {
        deleteFile(newName);

        File newFile = new File(newName);
        File oldFile = new File(oldName);
        oldFile.renameTo(newFile);
    }

    private void deleteFile(String fileName) {
        File file = new File(fileName);
        file.delete();
    }

    private void copyFile(String from, String to) {
        try {
            FileChannel inChannel = new FileInputStream(from).getChannel();
            FileChannel outChannel = new FileOutputStream(to).getChannel();
            try {
                inChannel.transferTo(0, inChannel.size(), outChannel);
            }
            catch (IOException e) {
                throw e;
            }
            finally {
                if (inChannel != null) inChannel.close();
                if (outChannel != null) outChannel.close();
            }
        }
        catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }


    private String getCurrentSchemaVersion(String fileName) {
        BufferedReader inputFile = null;
        try {
            inputFile = new BufferedReader(new FileReader(fileName));

            String firstLine = inputFile.readLine();

            return firstLine.substring(firstLine.indexOf('=') + 1);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        finally {
            if (inputFile != null) {
                try {
                    inputFile.close();
                }
                catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }

    private String getPreviousSchemaVersion(String fileName) {
        BufferedReader inputFile = null;
        try {
            inputFile = new BufferedReader(new FileReader(fileName));

            inputFile.readLine();
            String secondLine = inputFile.readLine();

            return secondLine.substring(secondLine.indexOf('=') + 1);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        finally {
            if (inputFile != null) {
                try {
                    inputFile.close();
                }
                catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }

    private void updateDBPatchUpdateScript(String pPatchUpdateScriptFileName, String newVersion) {
        String uid1 = UUID.randomUUID().toString();
        String uid2 = UUID.randomUUID().toString();
        // Append to file
        appendToFile(pPatchUpdateScriptFileName,
                NEW_LINE,
                "INSERT INTO PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)" + NEW_LINE,
                "VALUES('" + uid1 + "',0,'System',SYS_EXTRACT_UTC(SYSTIMESTAMP),SYS_EXTRACT_UTC(SYSTIMESTAMP),'" + getPaddedSchemaVersion(newVersion) + "','SchemaUpgrade')" + NEW_LINE,
                "/" + NEW_LINE,
                "INSERT INTO PSP_APPLIED_DATABASE_PATCH (APPLIED_DATABASE_PATCH_SEQ,VERSION,CREATOR_ID,CREATED_DATE,MODIFIED_DATE,DATABASE_PATCH_VERSION,DATABASE_PATCH_TYPE_CD)" + NEW_LINE,
                "VALUES('" + uid2 + "',0,'System',SYS_EXTRACT_UTC(SYSTIMESTAMP),SYS_EXTRACT_UTC(SYSTIMESTAMP),'" + getPaddedSchemaVersion(newVersion) + "','DataMigration')" + NEW_LINE,
                "/" + NEW_LINE,
                "COMMIT" + NEW_LINE,
                "/" + NEW_LINE);
    }

    public void generateCode2
            (BedlProcessor
                    pBedlProcessor) throws IOException {
        String generatedDataEntityFolder = pBedlProcessor.getBedlFolderName() + "target/src/java/com/intuit/sbd/payroll/psp/domain/";
        String domainEntityFolder = pBedlProcessor.getBedlFolderName() + "../java/com/intuit/sbd/payroll/psp/domain/";

        for (BedlDataEntity dataEntity : pBedlProcessor.getDataEntities()) {
            String className = dataEntity.getClassName();

            String dataEntityFullFileName = generatedDataEntityFolder + className + ".java";
            String baseDataEntityFullFileName = generatedDataEntityFolder + "Base" + className + ".java";

            // Change name of class to have "Base" appended and change super class name
            BufferedReader inputFile = new BufferedReader(new FileReader(dataEntityFullFileName));
            BufferedWriter outputFile = new BufferedWriter(new FileWriter(baseDataEntityFullFileName));

            String line = null;
            String[] search = {
                    "public class " + className + " extends SpcfEntity",
                    "public class " + className + " extends",
                    "public " + className + "()",
                    "(this)",
                    "public static final class " + className + "PropertyId",
                    className + "PropertyId.",
                    "private " + className + "PropertyId(String name)"
            };
            String[] replace = {
                    "public class Base" + className + " extends com.intuit.sbd.payroll.psp.DomainEntity",
                    "public class Base" + className + " extends",
                    "public Base" + className + "()",
                    "((" + className + ")this)",
                    "public static class Base" + className + "PropertyId",
                    className + "." + className + "PropertyId.",
                    "protected Base" + className + "PropertyId(String name)"
            };

            while ((line = inputFile.readLine()) != null) {
                for (int j = 0; j < search.length; j++) {
                    if (line.indexOf(search[j]) != -1) {
                        line = line.substring(0, line.indexOf(search[j])) + replace[j] + line.substring((line.indexOf(search[j]) + search[j].length()), line.length());
                    }
                }


                outputFile.write(line);
                outputFile.newLine();

                if (line.equals("import com.intuit.spc.foundations.subsystem.dataAccess.entity.SpcfPropertyId;")) {
                    outputFile.write("import com.intuit.sbd.payroll.psp.domain." + className + "." + className + "PropertyId;");
                    outputFile.newLine();
                }

            }

            inputFile.close();
            outputFile.close();

            File fileToDelete = new File(dataEntityFullFileName);
            fileToDelete.delete();

            // Create new data entity class (if it doesn't exist)
            // These are the ones developers will write business logic in
            StringTemplateGroup templates = new StringTemplateGroup("DomainEntityGroup", Generator.getTemplateLocation() + "domainEntity");
            StringTemplate t = templates.getInstanceOf("DomainEntity");
            t.setAttribute("className", className);

            File dirName = new File(domainEntityFolder);
            dirName.mkdirs();

            outputFile = new BufferedWriter(new FileWriter(dirName + "/" + className + ".java"));
            outputFile.write(t.toString());
            outputFile.close();
        }
    }

    private void extractFromJar(String jarLocation, String inputFileName, String outputFileName) {
        try {
            JarFile jar = new JarFile(jarLocation);
            ZipEntry entry = jar.getEntry(inputFileName);
            File efile = new File(outputFileName);

            InputStream in =
                    new BufferedInputStream(jar.getInputStream(entry));
            OutputStream out =
                    new BufferedOutputStream(new FileOutputStream(efile));
            byte[] buffer = new byte[2048];
            for (; ;) {
                int nBytes = in.read(buffer);
                if (nBytes <= 0) break;
                out.write(buffer, 0, nBytes);
            }
            out.flush();
            out.close();
            in.close();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

}
