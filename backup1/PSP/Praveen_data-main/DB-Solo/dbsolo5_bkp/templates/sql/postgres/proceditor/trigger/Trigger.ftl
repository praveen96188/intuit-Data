CREATE TRIGGER ${name} 
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
EXECUTE PROCEDURE ${tableschema}.${function}();