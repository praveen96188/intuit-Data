CREATE OR REPLACE FUNCTION ${schema}.${name}<#if generator.hasArgs()>(<#foreach arg in generator.getArgs()><#if arg_index != 0>, </#if>${arg.getName()} ${arg.getMode()} ${arg.getDataType()}</#foreach>)</#if> RETURN ${returnType} 
--
-- Schema:   ${schema}
-- Name:     ${name}
-- Created:  ${date} at ${time}
--
-- Created by DB Solo using a template file located in:
-- ${template_file}
--
AS
  ret ${returnType};
BEGIN
  DBMS_OUTPUT.PUT_LINE('Hello World');

  RETURN ret;
EXCEPTION
  WHEN OTHERS THEN
    DBMS_OUTPUT.PUT_LINE('Error in ${name}, code=' || sqlcode || ', error=' || sqlerrm);
    RETURN NULL;
END;