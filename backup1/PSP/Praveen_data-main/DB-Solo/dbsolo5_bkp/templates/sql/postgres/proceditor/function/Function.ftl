CREATE OR REPLACE FUNCTION ${schema}.${name}(<#foreach arg in generator.getArgs()><#if arg_index != 0>, </#if>${arg.getName()} ${arg.getMode()} ${arg.getDataType()}</#foreach>)
--
-- Schema:   ${schema}
-- Name:     ${name}
-- Created:  ${date} at ${time}
--
-- Created by DB Solo using a template file located in:
-- ${template_file}
--
RETURNS ${returnType}
AS
$$
BEGIN
 RAISE NOTICE 'Hello World';
 RETURN NULL;
END;
$$
LANGUAGE PLPGSQL;