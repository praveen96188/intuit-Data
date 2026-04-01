DECLARE

    TYPE state_prefix_type IS TABLE OF VARCHAR(3) INDEX BY VARCHAR2(10);
    state_prefix state_prefix_type;

    TYPE states_type IS TABLE OF VARCHAR2(10);
    states states_type;

    sequenceName varchar(20);
    sequenceExists number(5);
    prefix varchar(3);
    initialValue varchar(10);

BEGIN

    -- 50 States, plus DC, plus a default (PR, VI, etc.).
    states := states_type('AL', 'AK', 'AZ', 'AR', 'CA', 'CO', 'CT', 'DE', 'DC', 'FL', 'GA', 'HI', 'ID', 'IL', 'IN',
                          'IA', 'KS', 'KY', 'LA', 'ME', 'MD', 'MA', 'MI', 'MN', 'MS', 'MO', 'MT', 'NE', 'NV', 'NH',
                          'NJ', 'NM', 'NY', 'NC', 'ND', 'OH', 'OK', 'OR', 'PA', 'RI', 'SC', 'SD', 'TN', 'TX', 'UT',
                          'VT', 'VA', 'WA', 'WV', 'WI', 'WY', 'DEFAULT');

    -- Set the prefix (area code digit plus 2 digit state code) for each state.
    state_prefix('AL') := '401';
    state_prefix('AK') := '702';
    state_prefix('AZ') := '504';
    state_prefix('AR') := '405';
    state_prefix('CA') := '606';
    state_prefix('CO') := '508';
    state_prefix('CT') := '309';
    state_prefix('DE') := '310';
    state_prefix('DC') := '311';
    state_prefix('FL') := '312';
    state_prefix('GA') := '313';
    state_prefix('HI') := '715';
    state_prefix('ID') := '516';
    state_prefix('IL') := '417';
    state_prefix('IN') := '318';
    state_prefix('IA') := '419';
    state_prefix('KS') := '420';
    state_prefix('KY') := '321';
    state_prefix('LA') := '422';
    state_prefix('ME') := '323';
    state_prefix('MD') := '324';
    state_prefix('MA') := '325';
    state_prefix('MI') := '326';
    state_prefix('MN') := '427';
    state_prefix('MS') := '428';
    state_prefix('MO') := '429';
    state_prefix('MT') := '530';
    state_prefix('NE') := '431';
    state_prefix('NV') := '632';
    state_prefix('NH') := '333';
    state_prefix('NJ') := '334';
    state_prefix('NM') := '535';
    state_prefix('NY') := '336';
    state_prefix('NC') := '337';
    state_prefix('ND') := '438';
    state_prefix('OH') := '339';
    state_prefix('OK') := '440';
    state_prefix('OR') := '641';
    state_prefix('PA') := '342';
    state_prefix('RI') := '344';
    state_prefix('SC') := '345';
    state_prefix('SD') := '446';
    state_prefix('TN') := '347';
    state_prefix('TX') := '448';
    state_prefix('UT') := '549';
    state_prefix('VT') := '350';
    state_prefix('VA') := '351';
    state_prefix('WA') := '652';  -- Actual FIPS Code is 53.
    state_prefix('WV') := '353';  -- Actual FIPS Code is 54.
    state_prefix('WI') := '454';  -- Actual FIPS Code is 55.
    state_prefix('WY') := '555';  -- Actual FIPS Code is 56.
    state_prefix('DEFAULT') := '999';

  FOR state IN states.FIRST .. states.LAST LOOP

      sequenceName := 'PSID_' || states(state);

      -- See if it already exists.  We have to drop/create since there is no alter for sequence values.
      select count(*) into sequenceExists from user_sequences where sequence_name = sequenceName;
      if sequenceExists > 0 then
        begin
            EXECUTE IMMEDIATE 'DROP SEQUENCE ' || sequenceName;
        end;
      end if;

      -- Get the prefix for this state.
      prefix := state_prefix(states(state));

      -- Query for the max value for this state/prefix.
      select max(source_company_id) into initialValue
          from psp_company co
      where source_company_id like prefix || '______';

      -- If there were no values for this state, start at 1.
      if initialValue is null then
          initialValue := prefix || '000001';
      else
          -- Leave a small buffer.
          initialValue := initialValue + 5;
      end if;

      -- Create the new sequence.
      begin
        EXECUTE IMMEDIATE 'CREATE SEQUENCE ' || sequenceName ||
            ' START WITH ' || initialValue ||
            'INCREMENT BY 1 ' ||
            'MAXVALUE ' || prefix || '999999 ' ||
            'NOCACHE ' ||
            'NOCYCLE';
      end;

  END LOOP;

end;
/

