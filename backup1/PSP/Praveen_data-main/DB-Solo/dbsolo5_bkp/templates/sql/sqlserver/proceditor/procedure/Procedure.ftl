CREATE PROCEDURE ${schema}.${name} <#foreach arg in generator.getArgs()><#if arg_index != 0>, </#if>${arg.getName()} ${arg.getDataType()} <#if arg.getMode().isOut()>${arg.getMode()}</#if></#foreach>
--
-- Schema:   ${schema}
-- Name:     ${name}
-- Created:  ${date} at ${time}
--
-- Created by DB Solo using a template file located in:
-- ${template_file}
--
AS
  Print 'Hello World'
