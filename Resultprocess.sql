use aspp;

select * from respro;

-- create a temprorary case to identify the duplicates in the roll number

with duplicas AS(
Select SER ,ROLL,
row_number() over(partition by ROLL) as row_num
from respro
)
select SER ,ROLL
from duplicas where row_num>1;  
-- just used create template from old table to create a new table
CREATE TABLE `respro1` (
  `SER` int DEFAULT NULL,
  `BAR` int DEFAULT NULL,
  `ROLL` text,
  `QBOOK` int DEFAULT NULL,
  `SUBJECT` int DEFAULT NULL,
  `RES` text,
   `row_num` text
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

Insert into respro1
select *,
row_number() over(partition by ROLL) as row_num
from respro;

select * from respro1;

-- Used to identify the case where we have more than one ROLL NUMBERS
Select SER,ROLL from
respro1 
where row_num>1;

-- Use to delete the duplicate roll number from SQL 
Delete 
from respro1
where row_num>1;

SET SQL_SAFE_UPDATES = 0;

Select SER,ROLL from
respro1 
where row_num>1;

select * from respro1;

SELECT SER,ROLL from 
respro1
where ROLL LIKE '%?%';
-- This code is used to detect the double markings in the OMR sheet which was captured during data processing

Select 
         ROLL,
         CASE 
             when ROLL IS NULL OR TRIM(ROLL)='' THEN 'Blank'
             when char_length(TRIM(ROLL))<10 THEN 'Less than 10 digits'
             when ROLL LIKE '%?%' THEN 'Double marking found'
             Else 'Valid'
		 end as status
         from respro1;
         
select length(trim(ROLL)),ROLL from respro1;

SELECT 
    ROLL,
    CASE 
        WHEN ROLL IS NULL OR TRIM(ROLL) = '' THEN 'Blank'
        WHEN CHAR_LENGTH(REPLACE(ROLL, ' ', '')) < 10 THEN 'Less than 10 digits'
        WHEN REPLACE(ROLL, ' ', '') LIKE '%?%' THEN 'Double marking found'
        ELSE 'Valid'
    END AS status
FROM respro1;


