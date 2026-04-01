DROP TABLE IF EXISTS TEMP_PSP_LAW_RATE_RANGE CASCADE

;
--------------------------------------------------------
-- Create temp table                                  --
--------------------------------------------------------

CREATE TABLE TEMP_PSP_LAW_RATE_RANGE (LIKE PSP_LAW_RATE_RANGE INCLUDING ALL)
;

--------------------------------------------------------
-- Insert into temp table                             --
--------------------------------------------------------

-- Alaska
insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4117-9b77-1cdcd90c237a', 0, -1, null, null, 7, 83)
;

-- Alabama
insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4118-9b77-1cdcd90c237a', 0, -1, null, null, 7, 84)
;
insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4119-9b77-1cdcd90c237a', 0, -1, null, null, 7, 144)
;

-- Arkansas
insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4189-9b77-1cdcd90c237a', 0, -1, 0.00300, 0.14300, 7, 85)
;
insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4120-9b77-1cdcd90c237a', 0, -1, null, null, 3, 145)
;

-- Arizona
insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4180-9b77-1cdcd90c237a', 0, -1, 0.0007, 0.22930, 7, 86)
  
;
insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4121-9b77-1cdcd90c237a', 0, -1, null, null, 3, 179)
;

-- California
insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4101-9b77-1cdcd90c237a', 0, -1, 0.01500, 0.08200, 7, 87)
;
insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4102-9b77-1cdcd90c237a', 0, -1, null, null, 3, 142)
;

-- Colorado
insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4122-9b77-1cdcd90c237a', 0, -1, 0.00000, 0.10390, 7, 88)
;
insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4123-9b77-1cdcd90c237a', 0, -1, 0.0000, 0.0000, 7, 153)
;

-- Connecticut
insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4106-9b77-1cdcd90c237a', 0, -1, 0.01700, 0.06800, 7, 89)
;

-- District of Columbia
insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4124-9b77-1cdcd90c237a', 0, -1, 0.01600, 0.07400, 7, 90)
;
insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4125-9b77-1cdcd90c237a', 0, -1, null, null, 7, 188)
;

-- Delaware
insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4126-9b77-1cdcd90c237a', 0, -1, null, null, 7, 91)
;

-- Florida
insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4190-9b77-1cdcd90c237a', 0, -1, 0.00100, 0.07400, 7, 92)
;

-- Georgia
insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4127-9b77-1cdcd90c237a', 0, -1, 0.00040, 0.08100, 7, 93)
;
insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4128-9b77-1cdcd90c237a', 0, -1, null, null, 4, 154)
;

-- Hawaii
insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4129-9b77-1cdcd90c237a', 0, -1, 0.0, 0.06200, 7, 94)
;
insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4130-9b77-1cdcd90c237a', 0, -1, null, null, 4, 155)
;

-- Iowa
insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4131-9b77-1cdcd90c237a', 0, -1, null, null, 7, 95)
;
insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4132-9b77-1cdcd90c237a', 0, -1, null, null, 7, 156)
;
insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4133-9b77-1cdcd90c237a', 0, -1, null, null, 7, 186)
;

-- Idaho
insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4134-9b77-1cdcd90c237a', 0, -1, 0.0016560, 0.05400, 7, 96)
;
insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4135-9b77-1cdcd90c237a', 0, -1, 0.00000, 0.0005808, 7, 146)

;
-- Illinois
insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4103-9b77-1cdcd90c237a', 0, -1, 0.00675, 0.12975, 7, 97)
;

-- Indiana
insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4199-9b77-1cdcd90c237a', 0, -1, 0.00500, 0.12362, 7, 98)
;

-- Kansas
insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4136-9b77-1cdcd90c237a', 0, -1, 0.00000, 0.07600, 7, 99)
;

-- Kentucky
insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4137-9b77-1cdcd90c237a', 0, -1, 0.00225, 0.09500, 7, 100)
  
;

-- Louisiana
insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4138-9b77-1cdcd90c237a', 0, -1, 0.0009, 0.06200, 7, 101)
;
insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4139-9b77-1cdcd90c237a', 0, -1, null, null, 7, 183)
;

-- Massachusetts
insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4184-9b77-1cdcd90c237a', 0, -1, 0.00560, 0.14370, 7, 102)
;
insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4182-9b77-1cdcd90c237a', 0, -1, null, null, 7, 150)
;

-- Maryland
insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4109-9b77-1cdcd90c237a', 0, -1, 0.01000, 0.13500, 7, 103)
  
;

-- Maine
insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4140-9b77-1cdcd90c237a', 0, -1, 0.00490, 0.06370, 7, 104)
;
insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4141-9b77-1cdcd90c237a', 0, -1, null, null, 7, 157)
;
insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4183-9b77-1cdcd90c237a', 0, -1, null, null, 4, 189)
;

-- Michigan
insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4115-9b77-1cdcd90c237a', 0, -1, 0.00060, 0.13300, 7, 105)
;
insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4116-9b77-1cdcd90c237a', 0, -1, 0.00000, 0.00000, 7, 201)
;

-- Minnesota
insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4142-9b77-1cdcd90c237a', 0, -1, 0.00100, 0.0940, 7, 106)
;
insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4143-9b77-1cdcd90c237a', 0, -1, null, null, 3, 158)
;

-- Missouri
insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4144-9b77-1cdcd90c237a', 0, -1, 0.00000, 0.09950, 7, 107)
;

-- Mississippi
insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4145-9b77-1cdcd90c237a', 0, -1, 0.00000, 0.05640, 7, 108)
;
insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4146-9b77-1cdcd90c237a', 0, -1, 0.0, 0.00200, 4, 187)
;

-- Montana
insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4147-9b77-1cdcd90c237a', 0, -1, 0.00000, 0.09180, 7, 109)
;
insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4148-9b77-1cdcd90c237a', 0, -1, 0.00080, 0.00180, 7, 147)
;

-- North Carolina
insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4149-9b77-1cdcd90c237a', 0, -1, 0.00060, 0.05760, 7, 110)
;

-- North Dakota
insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4185-9b77-1cdcd90c237a', 0, -1, 0.0008, 0.09970, 7, 111)
;

-- Nebraska
insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4150-9b77-1cdcd90c237a', 0, -1, 0.0000, 0.05400, 7, 112)
;
insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4151-9b77-1cdcd90c237a', 0, -1, null, null, 7, 148)
;
insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4154-9b77-1cdcd90c237b', 0, -1, null, null, 7, 213)
;
-- New Hampshire
insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4152-9b77-1cdcd90c237a', 0, -1, 0.00000, 0.10500, 7, 113)
;
insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4153-9b77-1cdcd90c237a', 0, -1, 0.00000, 0.00400, 7, 185)
;

-- New Jersey
insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4110-9b77-1cdcd90c237a', 0, -1, 0.001175, 0.068825, 7, 114)
;
insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4181-9b77-1cdcd90c237a', 0, -1, null, null, 7, 73)
;
insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4111-9b77-1cdcd90c237a', 0, -1, null, null, 5, 167)
;
insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4112-9b77-1cdcd90c237a', 0, -1, null, null, 7, 169)
;
insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4113-9b77-1cdcd90c237a', 0, -1, null, null, 7, 171)
;
insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4114-9b77-1cdcd91c237a', 0, -1, 0.00000, 0.0014, 7, 194)
;

-- New Mexico
insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4154-9b77-1cdcd90c237a', 0, -1, 0.00330, 0.06400, 7, 115)
;
insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4155-9b77-1cdcd90c237a', 0, -1, null, null, 7, 190)
;
insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4156-9b77-1cdcd90c237a', 0, -1, null, null, 7, 192)
;

 -- Nevada
insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4157-9b77-1cdcd90c237a', 0, -1, 0.00250, 0.05400, 7, 116)
;
insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4158-9b77-1cdcd90c237a', 0, -1, null, null, 4, 159)
;

-- New York
insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4159-9b77-1cdcd90c237a', 0, -1, 0.02025, 0.0990, 7, 117)
;
insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4160-9b77-1cdcd90c237a', 0, -1, null, null, 5, 152)
;

-- Ohio
insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
     'e3d65988-9ec8-4161-9b77-1cdcd90c237a', 0, -1, 0.00800, 0.129, 7, 118)
;

-- Oklahoma
insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4186-9b77-1cdcd90c237a', 0, -1, 0.000300, 0.09200, 7, 119)
;

-- Oregon
insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4164-9b77-1cdcd90c237a', 0, -1, 0.00900, 0.05400, 7, 120)
;
insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4162-9b77-1cdcd90c237a', 0, -1, null, null, 7, 174)
;
insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4165-9b77-1cdcd90c237a', 0, -1, null, null, 7, 175)
;
insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4163-9b77-1cdcd90c237a', 0, -1, null, null, 7, 176)
;

-- Pennsylvania
insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4104-9b77-1cdcd90c237a', 0, -1, 0.012905, 0.136494, 7, 121)
;

-- Rhode Island
insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4166-9b77-1cdcd90c237a', 0, -1, 0.00950, 0.11490, 7, 123)
;
insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4167-9b77-1cdcd90c237a', 0, -1, null, null, 4, 160)
;
insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4168-9b77-1cdcd90c237a', 0, -1, null, null, 7, 199)
;

-- South Carolina
insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4169-9b77-1cdcd90c237a', 0, -1, 0.00000, 0.0546, 7, 124)
;
insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4170-9b77-1cdcd90c237a', 0, -1, 0, 0.0006, 7, 161)
;

-- South Dakota
insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4171-9b77-1cdcd90c237a', 0, -1, 0.00000, 0.09500, 7, 125)
;

-- South Dakota surcharge
insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  '02786bf9-09db-430f-afd9-cbd5e369ec7d', 0, -1, 0.00000, 0.00020, 7, 204)
;

insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4172-9b77-1cdcd90c237a', 0, -1, 0.00000, 0.00550, 7, 162)
;

-- Tennessee
insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4187-9b77-1cdcd90c237a', 0, -1, 0.0001, 0.1, 7, 126)
;
insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4173-9b77-1cdcd90c237a', 0, -1, null, null, 7, 151)
;

-- Texas
insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4107-9b77-1cdcd90c237a', 0, -1, 0.0, 0.06310, 7, 127)
;
insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4108-9b77-1cdcd90c237a', 0, -1, null, null, 7, 163)
;

-- Utah
insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4174-9b77-1cdcd90c237a', 0, -1, 0.00020, 0.08300, 7, 128)
;

-- Virginia
insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4175-9b77-1cdcd90c237a', 0, -1, 0.0010, 0.0643, 7, 129)
;

--  Vermont
insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4176-9b77-1cdcd90c237a', 0, -1, 0.00400, 0.05400, 7, 130)
;

-- Washington
insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4188-9b77-1cdcd90c237a', 0, -1, 0.002, 0.10330, 7, 131)
;
insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4105-9b77-1cdcd90c237a', 0, -1, null, null, 4, 164)
;

-- Wisconsin
insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4114-9b77-1cdcd90c237a', 0, -1, 0.00000, 0.12000, 7, 132)
;

-- West Virginia
insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4177-9b77-1cdcd90c237a', 0, -1, 0.01500, 0.08500, 7, 133)
;

-- Wyoming
insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4178-9b77-1cdcd90c237a', 0, -1, null, null, 7, 134)
;
insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4179-9b77-1cdcd90c237a', 0, -1, null, null, 7, 184)
;

insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4191-9b77-1cdcd90c237a', 0, -1, null, null, 7, 208)
;
--PSP-17930 START
insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4192-9b77-1cdcd90c237a', 0, -1, null, null, 7, 209)
;

insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4193-9b77-1cdcd90c237a', 0, -1, null, null, 7, 210)
;

insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4194-9b77-1cdcd90c237a', 0, -1, null, null, 7, 211)
;

insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4195-9b77-1cdcd90c237a', 0, -1, null, null, 7, 212)
;

insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4195-9b77-1cdcd90c247a', 0, -1, null, null, 7, 218)
;

insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4195-9b77-1cdcd90c337a', 0, -1, null, null, 7, 219)
;

insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4195-9b77-1cdcd90c437a', 0, -1, 0.00000, 0.10896, 7, 220)
;

insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4196-9b77-1cdcd90c237a', 0, -1, null, null, 7, 221)
;
insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4196-9b77-1cdcd90c637a', 0, -1, null, null, 7, 222)
;
insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4196-9b77-1cdcd90c737a', 0, -1, null, null, 7, 223)
;
insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4196-9b77-1cdcd90c837a', 0, -1, null, null, 7, 224)
;
insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4196-9b77-1cdcd90c937a', 0, -1, null, null, 7, 225)
;

insert into temp_psp_law_rate_range (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk) values (
  'e3d65988-9ec8-4196-9b77-1cdcd90c947a', 0, -1, null, null, 7, 226)
;

--------------------------------------------------------
-- Synchronize temp table and real table by            --
-- inserting, deleting, and updating as necessary     --
--------------------------------------------------------

INSERT INTO psp_law_rate_range
   (law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk)
SELECT 
   law_rate_range_id, version, realm_id, min_rate, max_rate, precision, law_fk
FROM 
   temp_psp_law_rate_range tmp
WHERE 
   tmp.law_rate_range_id NOT IN (SELECT law_rate_range_id FROM psp_law_rate_range)
;

DELETE FROM psp_law_rate_range
WHERE law_rate_range_id NOT IN (SELECT law_rate_range_id FROM temp_psp_law_rate_range)
;

UPDATE psp_law_rate_range lrr
SET  (version, realm_id, min_rate, max_rate, precision, law_fk) =
(SELECT version, realm_id, min_rate, max_rate, precision, law_fk
 FROM temp_psp_law_rate_range temp WHERE temp.law_rate_range_id = lrr.law_rate_range_id)
;

--------------------------------------------------------
-- Drop temp table                                  --
--------------------------------------------------------
DROP TABLE temp_psp_law_rate_range

;
COMMIT
 

 
