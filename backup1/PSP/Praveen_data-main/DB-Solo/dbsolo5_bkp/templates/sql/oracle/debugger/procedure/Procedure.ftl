CREATE OR REPLACE PROCEDURE ${schema}.${name}<#if generator.hasArgs()>(<#foreach arg in generator.getArgs()><#if arg_index != 0>, </#if>${arg.getName()} ${arg.getMode()} ${arg.getDataType()}</#foreach>)</#if>
--
-- Schema:   ${schema}
-- Name:     ${name}
-- Created:  ${date} at ${time}
--
-- Created by DB Solo using a template file located in:
-- ${template_file}
--
AS
BEGIN
  DBMS_OUTPUT.PUT_LINE('Hello World');
EXCEPTION
  WHEN OTHERS THEN
    DBMS_OUTPUT.PUT_LINE('Error in ${name}, code=' || sqlcode || ', error=' || sqlerrm);
END;