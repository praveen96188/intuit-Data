package com.intuit.sbd.payroll.psp;

import com.intuit.sbd.payroll.psp.bedl.BedlDataEntity;
import com.intuit.sbd.payroll.psp.bedl.BedlEnumeration;
import com.intuit.sbd.payroll.psp.bedl.BedlProcessor;
import com.intuit.sbd.payroll.psp.bedl.BedlProperty;
import org.w3c.dom.Element;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: achaves
 * Date: Dec 28, 2007
 * Time: 8:19:24 PM
 * To change this template use File | Settings | File Templates.
 */
public class ColumnConstraintGenerator {
    private BedlProcessor bedlProcessor;
    HashMap<String, Integer> lastConstraintNumber = new HashMap<String, Integer>();
    static String NEW_LINE = System.getProperty("line.separator");

    public void generateCode(BedlProcessor pBedlProcessor) throws IOException {
        bedlProcessor = pBedlProcessor;

        // Out file
        BufferedWriter outputFile1 = new BufferedWriter(new FileWriter(pBedlProcessor.getBedlFolderName() + "DB_Generated_Constraints.sql"));
        BufferedWriter outputFile2 = new BufferedWriter(new FileWriter(pBedlProcessor.getBedlFolderName() + "DB_Update_Constraints.sql"));

        /**
         <Enumeration Name="TXPType">
         <Fields>
         <Field Name="FormatNumeric" />
         <Field Name="FormatAlphanumeric" />
         </Fields>
         </Enumeration>
         /**
         *
         <Property Name="NewStatus" HandcodedAccessorType="None">
         <Type Type="BankAccountStatus" />
         </Property>

         Generate alter column with constraint for enumerations
         */
        for (int i = 0; i < pBedlProcessor.getProperties().getLength(); i++) {
            Element propertyElement = (Element) pBedlProcessor.getProperties().item(i);
            String propertyName = propertyElement.getAttribute("Name");

            Element classElement = (Element) ((Element) propertyElement.getParentNode()).getParentNode();
            String className = classElement.getAttribute("Name");
            String classNameInUpperCase = className.toUpperCase();

            BedlDataEntity dataEntity = bedlProcessor.findDataEntityOrDataObjectOrDataType(className);
            BedlProperty property = dataEntity.findProperty(propertyName);

            BedlEnumeration enumeration = bedlProcessor.findEnumeration(property.getPropertyType());
            if (enumeration != null) {
                String columnName = property.getPspColumnNames().get(0);

                String tableName = NameOverrides.getOverride(classNameInUpperCase);
                if (tableName != null) {
                    tableName = "PSP_" + tableName;
                } else {
                    tableName = "PSP_" + classNameInUpperCase;
                }

                String constraintName = "C_" + tableName.toUpperCase();
                if (constraintName.length() > 27) {
                    constraintName = constraintName.substring(0, 26);
                }
                constraintName += Integer.toString(getConstraintNumber(constraintName));

                outputFile1.write("ALTER TABLE ");
                outputFile1.write(tableName);
                outputFile1.write(" ADD CONSTRAINT ");
                outputFile1.write(constraintName);
                outputFile1.write(" CHECK(");
                outputFile1.write(columnName);
                outputFile1.write(" IN(");

                StringBuffer enumStr = new StringBuffer();
                enumStr.append(columnName);
                enumStr.append(" IN(");
                
                int estimatedLineLength = 80;
                int estimatedLineLength1 = 80;
                Boolean first = true;
                for (String enumerationValue : enumeration.getEnumerationValues()) {
                    if (!first) {
                        outputFile1.write(", ");
                        enumStr.append(", ");
                    }
                    outputFile1.write("'" + enumerationValue + "'");
                    enumStr.append("''").append(enumerationValue).append("''");                    

                    estimatedLineLength += (enumerationValue.length() + 3);
                    estimatedLineLength1 += (enumerationValue.length() + 5);
                    if (estimatedLineLength > 2200) {
                        outputFile1.newLine();
                        estimatedLineLength = 0;
                    }

                    if (estimatedLineLength1 > 2200) {
                        enumStr.append(NEW_LINE);
                        estimatedLineLength1 = 0;
                    }

                    first = false;
                }
                outputFile1.write("))");
                outputFile1.newLine();
                enumStr.append(")");

                outputFile1.write("/");
                outputFile1.newLine();

                //Script for updated constraints.
                outputFile2.write("DECLARE " + NEW_LINE);
                outputFile2.write("v_text             varchar2(8000); " + NEW_LINE);
                outputFile2.write("v_column_name      varchar2(250); " + NEW_LINE);
                outputFile2.write("in_text            varchar2(8000); " + NEW_LINE);
                outputFile2.write("query1             varchar2(8000); " + NEW_LINE);
                outputFile2.write("query2             varchar2(8000); " + NEW_LINE);

                outputFile2.write("BEGIN " + NEW_LINE);
                outputFile2.write("  in_text := '" + enumStr.toString() + "';" + NEW_LINE);
                outputFile2.write(NEW_LINE);
                outputFile2.write("    FOR rec in ( SELECT CONSTRAINT_NAME,SEARCH_CONDITION FROM USER_CONSTRAINTS ");
                outputFile2.write(" WHERE CONSTRAINT_TYPE='C' AND CONSTRAINT_NAME LIKE 'C_%'");
                outputFile2.write(" AND TABLE_NAME = '");
                outputFile2.write(tableName);
                outputFile2.write("')" + NEW_LINE);

                outputFile2.write("    LOOP " + NEW_LINE);

                outputFile2.write("      v_text := replace(replace(rec.search_condition, chr(13),''), chr(10), '');" + NEW_LINE);
                outputFile2.write("      in_text := replace(replace(in_text, chr(13),''), chr(10), '');" + NEW_LINE);
                outputFile2.write("      v_column_name := substr(rec.search_condition,1, instr(rec.search_condition,' ')-1);" + NEW_LINE);
                outputFile2.write("      IF((v_column_name = '" + columnName + "') AND (v_text != in_text)) THEN " + NEW_LINE);
                outputFile2.write("        BEGIN " + NEW_LINE);
                outputFile2.write("          query1 := 'ALTER TABLE " + tableName + " DROP CONSTRAINT '||rec.CONSTRAINT_NAME;" + NEW_LINE);
                outputFile2.write("          query2 := 'ALTER TABLE " + tableName + " ADD CONSTRAINT '||rec.CONSTRAINT_NAME||' CHECK('||in_text||') NOValidate';" + NEW_LINE);
                outputFile2.write(NEW_LINE);
                                            outputFile2.write("dbms_output.put_line('Query1 -->'||query1);"+ NEW_LINE);
                outputFile2.write("          EXECUTE IMMEDIATE query1;" + NEW_LINE);
                outputFile2.write("          EXECUTE IMMEDIATE query2;" + NEW_LINE);
                outputFile2.write(NEW_LINE);
                outputFile2.write("        EXCEPTION " + NEW_LINE);
                outputFile2.write("          WHEN OTHERS THEN RAISE; " + NEW_LINE);
                outputFile2.write("        END; " + NEW_LINE);
                outputFile2.write("      END IF; " + NEW_LINE);
                outputFile2.write("    END LOOP; " + NEW_LINE);
                outputFile2.write("END; " + NEW_LINE);
                outputFile2.write("/");
                outputFile2.newLine();
            }

        }//end of for loop with i var

        // modify table definitions to include default value for those entities that have
        // the default hibernate & SPC-F versioning policy turned off 
        for (BedlDataEntity dataEntity : pBedlProcessor.getDataEntities()) {
            if (!dataEntity.supportsVersioning()) {
                // ALTER TABLE <table-name> MODIFY VERSION DEFAULT -1;
                outputFile1.write("ALTER TABLE ");
                outputFile1.write("PSP_" + dataEntity.getPspTableName());
                outputFile1.write(" MODIFY VERSION DEFAULT 0");
                outputFile1.newLine();
                outputFile1.write("/");
                outputFile1.newLine();
            }
        }

        outputFile1.close();
        outputFile2.close();
    }

    private int getConstraintNumber(String pConstraintName) {
        Integer lastNumber = lastConstraintNumber.get(pConstraintName);
        if (lastNumber == null) {
            lastNumber = 0;
        } else {
            lastNumber++;
        }

        lastConstraintNumber.put(pConstraintName, lastNumber);
        return lastNumber;

    }

}
