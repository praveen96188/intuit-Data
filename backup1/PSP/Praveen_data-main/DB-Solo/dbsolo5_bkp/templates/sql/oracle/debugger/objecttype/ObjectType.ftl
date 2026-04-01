CREATE OR REPLACE TYPE ${schema}.${name} AS OBJECT 
--
-- Schema:   ${schema}
-- Name:     ${name}
-- Created:  ${date} at ${time}
--
-- Created by DB Solo using a template file located in:
-- ${template_file}
--
(
  X NUMBER,
  MEMBER PROCEDURE HELLO(ARG DATE)
);