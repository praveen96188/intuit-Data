CREATE OR REPLACE TRIGGER ${schema}.${name} 
--
-- Schema:   ${schema}
-- Name:     ${name}
-- Created:  ${date} at ${time}
--
-- Created by DB Solo using a template file located in:
-- ${template_file}
--

${order} ${events} ON ${tableschema}.${table}
${foreachrow}
BEGIN
  DBMS_OUTPUT.PUT_LINE('Hello World');
EXCEPTION
  WHEN OTHERS THEN
    DBMS_OUTPUT.PUT_LINE('Error in ${name}, code=' || sqlcode || ', error=' || sqlerrm);
END;