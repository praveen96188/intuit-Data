DO $$
  DECLARE

    state text[];
    stateMap text[] := '{{AL,401},
   			{AK,702},
			{AZ,504},
   			{AR,405},
			{CA,606},
   			{CO,508},
			{CT,309},
   			{DE,310},
			{DC,311},
   			{FL,312},
			{GA,313},
   			{HI,715},
			{ID,516},
   			{IL,417},
			{IN,318},
   			{IA,419},
			{KS,420},
   			{KY,321},
			{LA,422},
   			{ME,323},
			{MD,324},
   			{MA,325},
			{MI,326},
   			{MN,427},
			{MS,428},
   			{MO,429},
			{MT,530},
   			{NE,431},
			{NV,632},
   			{NH,333},
			{NJ,334},
   			{NM,535},
			{NY,336},
   			{NC,337},
			{ND,438},
   			{OH,339},
			{OK,440},
   			{OR,641},
			{PA,342},
   			{RI,344},
			{SC,345},
   			{SD,446},
			{TN,347},
   			{TX,448},
			{UT,549},
   			{VT,350},
			{VA,351},
			{WA,652},
   			{WV,353},
			{WI,454},
   			{WY,555},
			{DEFAULT,999}
			}';
    sequenceName varchar(20);
    prefix varchar(3);

  BEGIN
    FOREACH state SLICE 1 IN ARRAY stateMap
      LOOP
        sequenceName := 'PSID_' || state[1];
        prefix := state[2];

        EXECUTE 'CREATE SEQUENCE IF NOT EXISTS ' || sequenceName || ' START WITH ' || prefix || '000001 INCREMENT BY 1 MAXVALUE ' || prefix || '999999 NO CYCLE';
      END LOOP;
  end;
  $$ LANGUAGE plpgsql;