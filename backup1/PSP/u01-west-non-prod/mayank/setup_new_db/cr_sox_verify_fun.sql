  CREATE OR REPLACE EDITIONABLE FUNCTION sys.SOX_VERIFY_FUNCTION
(username varchar2,
  password varchar2,
  old_password varchar2)
  RETURN boolean IS
   n boolean;
   m integer;
   differ integer;
   isdigit boolean;
   ischar  boolean;
   ispunct boolean;
   iscap boolean;
   digitarray varchar2(32);
   punctarray varchar2(25);
  capchararray varchar2(26);
  lowchararray varchar2(26);

BEGIN
--   digitarray:= '0123456789';
   capchararray:= 'ABCDEFGHIJKLMNOPQRSTUVWXYZ';
   lowchararray:= 'abcdefghijklmnopqrstuvwxyz';
   digitarray:='1234567890!"#$%&()``*+,-/:;<=>?_';

   -- Check if the password is same as the username
   IF NLS_LOWER(password) = NLS_LOWER(username) THEN
     raise_application_error(-20001, 'Password same as or similar to user');
   END IF;

   -- Check for the minimum length of the password
   IF length(password) < 8 THEN
      raise_application_error(-20002, 'Password length less than 8');
   END IF;
   -- Check if the password contains at least one low case letter,
   -- one capital case letter and one non-alphabetic character
   -- 1. Check for the non-alphabetic character
   isdigit:=FALSE;
   m := length(password);
   FOR i IN 1..length(digitarray) LOOP
      FOR j IN 1..m LOOP
         IF substr(password,j,1) = substr(digitarray,i,1) THEN
            isdigit:=TRUE;
             GOTO findchar;
         END IF;
      END LOOP;
   END LOOP;
   IF isdigit = FALSE THEN
      raise_application_error(-20003, 'Password should contain at least one \
        non-alphabetic character, one low case and one upper case character');
   END IF;
   -- 2. Check for the low case character
   <<findchar>>
   ischar:=FALSE;
   FOR i IN 1..length(lowchararray) LOOP
      FOR j IN 1..m LOOP
         IF substr(password,j,1) = substr(lowchararray,i,1) THEN
            ischar:=TRUE;
             GOTO findcap;
         END IF;
      END LOOP;
   END LOOP;
   IF ischar = FALSE THEN
      raise_application_error(-20003, 'Password should contain at least one \
              non-alphabetic, one low case and one upper case character');
   END IF;
   -- 3. Check for the capital character
   <<findcap>>
   iscap:=FALSE;
   FOR i IN 1..length(capchararray) LOOP
      FOR j IN 1..m LOOP
         IF substr(password,j,1) = substr(capchararray,i,1) THEN
            iscap:=TRUE;
             GOTO endsearch;
         END IF;
      END LOOP;
   END LOOP;
   IF iscap = FALSE THEN
      raise_application_error(-20003, 'Password should contain at least one \
              non-alphabetic, one low case and one upper case character');
   END IF;

   <<endsearch>>
   -- Check if the password differs from the previous password by at least
   -- 3 letters
   IF old_password IS NOT NULL THEN
     differ := length(old_password) - length(password);

     IF abs(differ) < 3 THEN
       IF length(password) < length(old_password) THEN
         m := length(password);
       ELSE
         m := length(old_password);
       END IF;

       differ := abs(differ);
       FOR i IN 1..m LOOP
         IF substr(password,i,1) != substr(old_password,i,1) THEN
           differ := differ + 1;
         END IF;
       END LOOP;

       IF differ < 3 THEN
         raise_application_error(-20004, 'Password should differ by at \
         least 3 characters');
       END IF;
     END IF;
   END IF;
   -- Everything is fine; return TRUE ;
   RETURN(TRUE);
END;
/

