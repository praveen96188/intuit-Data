ALTER TABLE "$it.tableName$"
ADD CONSTRAINT "$it.foreignKeyConstraintName$"
FOREIGN KEY ("$it.foreignKeyColumnName$","REALM_ID")
REFERENCES "$it.referenceTableName$"

/