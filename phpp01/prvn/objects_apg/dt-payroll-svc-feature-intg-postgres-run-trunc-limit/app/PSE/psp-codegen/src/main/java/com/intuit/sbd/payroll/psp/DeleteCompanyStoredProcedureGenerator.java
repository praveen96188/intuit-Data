package com.intuit.sbd.payroll.psp;

import com.intuit.sbd.payroll.psp.bedl.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DeleteCompanyStoredProcedureGenerator {
    public class RelationPath {
        public RelationPath(RelationPath parentPath, BedlProperty childProperty) {
            if (parentPath != null) {
                for (BedlProperty property : parentPath.properties) {
                    properties.add(property);
                }
            }

            properties.add(childProperty);
        }

        public List<BedlProperty> properties = new ArrayList<BedlProperty>();

        @Override
        public String toString() {
            StringBuffer sb = new StringBuffer();

            for (BedlProperty property : properties) {
                if (sb.length() > 0) {
                    sb.append(" -> ");
                }
                sb.append(property.getPropertyType());
            }

            return "Company -> " + sb.toString();
        }

        public String generateDeleteStmt(BedlProcessor pBedlProcessor) {
            StringBuffer sb = new StringBuffer();

            BedlProperty property = properties.get(properties.size() - 1);
            BedlDataEntity entity = pBedlProcessor.findDataEntityOrDataObjectOrDataType(property.getPropertyType());

            // Determine tableName and whereColumnName
            String tableName = "";
            String whereColumnName = "";

            if (property instanceof BedlClassReferenceProperty) {
                tableName = "PSP_" + entity.getPspTableName();
                whereColumnName = ((BedlClassReferenceProperty) property).getInverseColumnName();
            } else {
                BedlCollectionProperty collectionProperty = ((BedlCollectionProperty) property);
                if (collectionProperty.getIsManyToMany()) {
                    tableName = collectionProperty.getJoinTableName();
                    if (collectionProperty.getIsSource()) {
                        whereColumnName = collectionProperty.getInverseJoinColumnName();
                    } else {
                        whereColumnName = collectionProperty.getJoinColumnName();
                    }
                } else {
                    tableName = "PSP_" + entity.getPspTableName();
                    whereColumnName = property.getFirstPspColumnName();
                }
            }

            // Generate delete statements for derived classes
            for (BedlDataEntity derivedClass : entity.getDerivedClasses()) {
                sb.append("  DELETE FROM PSP_" + derivedClass.getPspTableName() + " WHERE " + derivedClass.getKeyPropertyName() + " IN (");
                sb.append("SELECT " + entity.getKeyPropertyName() + " FROM " + tableName + " WHERE " + whereColumnName + " IN (");
                generateSelect(pBedlProcessor, property, properties.size() - 1, sb);
                sb.append("\n  ));\n");
            }

            // Generate delete statement for this entity
            sb.append("  DELETE FROM " + tableName + " WHERE " + whereColumnName + " IN (");
            generateSelect(pBedlProcessor, property, properties.size() - 1, sb);
            sb.append("\n  );");

            return sb.toString();
        }

        private void generateSelect(BedlProcessor pBedlProcessor, BedlProperty currentProperty, int currentPropertyIndex, StringBuffer sb) {
            BedlProperty nextProperty = null;
            BedlDataEntity nextEntity = null;
            String wherePropertyName = null;
            if (currentPropertyIndex > 0) {
                nextProperty = properties.get(currentPropertyIndex - 1);
                nextEntity = pBedlProcessor.findDataEntityOrDataObjectOrDataType(nextProperty.getPropertyType());
                if (nextProperty instanceof BedlClassReferenceProperty) {  // represents a 1:1 like company->address
                    wherePropertyName = ((BedlClassReferenceProperty)nextProperty).getInverseColumnName();
                } else {
                    wherePropertyName = nextProperty.getFirstPspColumnName();
                }
            }

            sb.append("\n");
            sb.append("  ");
            sb.append(spaces(properties.size() - currentPropertyIndex));

            if (currentProperty instanceof BedlClassReferenceProperty) {
                if (nextProperty == null) {
                    sb.append("uniqueId");
                } else {
                    sb.append("SELECT " + nextEntity.getKeyPropertyName() + " FROM PSP_" + nextEntity.getPspTableName() + " WHERE " + wherePropertyName + " IN (");
                }
            } else {
                if (nextProperty == null) {
                    sb.append("uniqueId");
                } else {
                    sb.append("SELECT " + nextEntity.getKeyPropertyName() + " FROM PSP_" + nextEntity.getPspTableName() + " WHERE " + wherePropertyName + " IN (");
                }
            }

            // end the recursion if this is the last property
            if (nextProperty == null) {
                return;
            }

            generateSelect(pBedlProcessor, nextProperty, currentPropertyIndex - 1, sb);
            sb.append("\n  " + spaces(properties.size() - currentPropertyIndex) + ")");
        }

        private String spaces(int numberOfSpaces) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i <= numberOfSpaces; i++) {
                sb.append("  ");
            }
            return sb.toString();
        }

    }

    public void generateCode(BedlProcessor pBedlProcessor) throws IOException {
        BufferedWriter outputFile = new BufferedWriter(new FileWriter(pBedlProcessor.getBedlFolderName() + "prc_remove_company_fast.sql"));

        try {
            outputFile.write("CREATE OR REPLACE PROCEDURE prc_remove_company_fast(uniqueId IN VARCHAR2) IS\n");
            outputFile.write("BEGIN\n");

            BedlDataEntity company = pBedlProcessor.findDataEntityOrDataObjectOrDataType("Company");

            List<RelationPath> relationPaths = processPath(null, pBedlProcessor, company, false);

            for (RelationPath relationPath : relationPaths) {
                outputFile.write("\n  --" + relationPath.toString());
                outputFile.write("\n");

                outputFile.write(relationPath.generateDeleteStmt(pBedlProcessor));
                outputFile.write("\n");
            }

            outputFile.write("\n  DELETE FROM PSP_COMPANY WHERE COMPANY_SEQ = uniqueId;\n");
            outputFile.write("  COMMIT;\n");
            outputFile.write("END;\n");
        }
        finally {
            if (outputFile != null) outputFile.close();
        }
    }

    private List<RelationPath> processPath(RelationPath parentPath, BedlProcessor pBedlProcessor, BedlDataEntity dataEntity, Boolean followedSelfReference) {
        List<RelationPath> relationPaths = new ArrayList<RelationPath>();

        if (parentPath != null && dataEntity.getClassName().equals("Company")) {
            return relationPaths;
        }

        if (parentPath != null) {
            //System.out.println(parentPath.toString());
        }

        for (BedlCollectionProperty childProperty : dataEntity.getOneToManyRelations()) {
            RelationPath path = new RelationPath(parentPath, childProperty);

            if (!childProperty.getPropertyReferenceType().equals(dataEntity.getClassName())) {
                relationPaths.addAll(processPath(path, pBedlProcessor, pBedlProcessor.findDataEntityOrDataObjectOrDataType(childProperty.getPropertyReferenceType()), followedSelfReference));
            } else {
                if (followedSelfReference) {
                    relationPaths.add(path);
                } else {
                    relationPaths.addAll(processPath(path, pBedlProcessor, pBedlProcessor.findDataEntityOrDataObjectOrDataType(childProperty.getPropertyReferenceType()), true));
                }
            }
        }

        for (BedlCollectionProperty childProperty : dataEntity.getManyToManyRelations()) {
            RelationPath path = new RelationPath(parentPath, childProperty);
            relationPaths.add(path);
        }

        for (BedlClassReferenceProperty childProperty : dataEntity.getOneToOneRelations()) {
            if (childProperty.getHasFkColumn()) {
                RelationPath path = new RelationPath(parentPath, childProperty);

                if (!childProperty.getPropertyReferenceType().equals(dataEntity.getClassName())) {
                    relationPaths.addAll(processPath(path, pBedlProcessor, pBedlProcessor.findDataEntityOrDataObjectOrDataType(childProperty.getPropertyReferenceType()), followedSelfReference));
                } else {
                    if (followedSelfReference) {
                        relationPaths.add(path);
                    } else {
                        relationPaths.addAll(processPath(path, pBedlProcessor, pBedlProcessor.findDataEntityOrDataObjectOrDataType(childProperty.getPropertyReferenceType()), true));
                    }
                }
            }

        }

        //if (relationPaths.size() == 0) {
        if (parentPath != null) {
            relationPaths.add(parentPath);
        }
        //}
        return relationPaths;
    }
}
