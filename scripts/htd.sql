select to_timestamp
 (  to_char (to_number (substr ('&1', 1, 2), 'xx') - 100, 'fm00')
 || to_char (to_number (substr ('&&1', 3, 2), 'xx') - 100, 'fm00')
 || to_char (to_number (substr ('&&1', 5, 2), 'xx'), 'fm00')
 || to_char (to_number (substr ('&&1', 7, 2), 'xx'), 'fm00')
 || to_char (to_number (substr ('&&1', 9, 2), 'xx') - 1, 'fm00')
 || to_char (to_number (substr ('&&1', 11, 2), 'xx') - 1, 'fm00')
 || to_char (to_number (substr ('&&1', 13, 2), 'xx') - 1, 'fm00')
 || to_char (nvl (to_number (substr ('&&1', 15, 8), 'xxxxxxxx'), 0), 'fm000000000')
 , 'yyyymmddhh24missff'
 )
 from   dual;            

