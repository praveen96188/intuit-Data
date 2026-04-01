CREATE FUNCTION ${schema}.${name}(<#foreach arg in generator.getArgs()><#if arg_index != 0>, </#if>${arg.getName()} ${arg.getDataType()}</#foreach>) RETURNS ${returnType} 
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
  DECLARE @ret ${returnType}
  SET @ret = NULL

  RETURN @ret
END