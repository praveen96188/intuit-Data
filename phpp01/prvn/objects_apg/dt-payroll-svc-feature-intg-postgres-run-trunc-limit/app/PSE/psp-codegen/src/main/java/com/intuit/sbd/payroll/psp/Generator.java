/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intuit.sbd.payroll.psp;

import java.io.File;
import com.intuit.sbd.payroll.psp.configuration.DatabaseType;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author suganyas315
 */
public class Generator {
    
     /** 
     * Template files are available under src\main\resources package in psp-codegen module     
     * @return 
     */
    public static final String getTemplateLocation(){
        String currentWorkingDirectory = new File(".").getAbsolutePath();
        String templateLocation = currentWorkingDirectory + "/../../../../psp-codegen/src/main/resources/templates/";
        return templateLocation;
    }
    
    /**
     * Each one of the generator class will generate the java classes corresponding to domainEntity, enumerations, expression, etc.,.
     * All the classes generated should be under the same package
     * @return 
     */
    public static String getGeneratedDataEntityFolderLocation(){
        String currentWorkingDirectory = new File(".").getAbsolutePath();
        return currentWorkingDirectory + "/target/src/java/com/intuit/sbd/payroll/psp/";
    }
    
    public static String getGeneratedResourceFolderLocation(){
        String currentWorkingDirectory = new File(".").getAbsolutePath();
        return currentWorkingDirectory + "/target/src/java/resources/com/intuit/sbd/payroll/psp/domain/";
    }
    
    public static String getGeneratedDomainFolderLocation(){
        String currentWorkingDirectory = new File(".").getAbsolutePath();
        return currentWorkingDirectory + "/target/src/java/com/intuit/sbd/payroll/psp/domain/";
    }
    public static String getDatabaseSpecificLocation(String path, DatabaseType databaseType){
        String databasePath=null;

        switch (databaseType){
            case POSTGRES: databasePath = "postgres";
                break;
            case ORACLE:databasePath = "";
                break;
            default: throw new RuntimeException(String.format("%s database not supported!!", databaseType.toString()));
        }

        if(StringUtils.isAllBlank(databasePath))
            return path;

        databasePath = path.endsWith(File.separator) ? databasePath.concat(File.separator) : File.separator.concat(databasePath);

        return path.concat(databasePath);
    }
}
