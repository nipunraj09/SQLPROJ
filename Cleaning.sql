Select * from layoofs;

create table layoffs_staging
like layoffs;

select * from layoffs_staging;

Insert layoffs_staging
select * 
From layoffs;



select *,
ROW_number() over(partition by company,location,industry,total_laid_off,percentage_laid_off,'date',stage,country,funds_raised_millions) AS row_num
FROM layoffs_staging;


with duplicates_cte as(
select *,
ROW_number() over(partition by company,location,industry,total_laid_off,percentage_laid_off,'date',stage,country,funds_raised_millions) AS row_num
FROM layoffs_staging)


select * from duplicates_cte 
where row_num>1;





CREATE TABLE `layoffs_staging2` (
  `company` text,
  `location` text,
  `industry` text,
  `total_laid_off` int DEFAULT NULL,
  `percentage_laid_off` text,
  `date` text,
  `stage` text,
  `country` text,
  `funds_raised_millions` int DEFAULT NULL,
   `row_num` INT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

Select * from layoffs_staging2;

insert into layoffs_staging2
select *,
ROW_number() over(partition by company,location,industry,total_laid_off,percentage_laid_off,'date',stage,country,funds_raised_millions) AS row_num
FROM layoffs_staging;

select * from layoffs_staging2;


delete from layoffs_staging2
where row_num>1;

-- Standardizing data
select distinct(trim(company))
from layoffs_staging2;

update layoffs_staging2
set company = trim(company);

select distinct industry
from layoffs_staging2
order by 1;

update layoffs_staging2
set industry = 'Crypto'
where industry like 'Crypto%';

select *
from layoffs_staging2
where country LIKE 'UNITED STATES%'
Order by 1;

update layoffs_staging2
set country=Trim(Trailing '.' from country)
where country like 'United States%';



select `date` ,
STR_TO_DATE(`date`,'%m/%d/%Y')
from layoffs_staging2;

update layoffs_staging2
set `date`= str_to_date(`date`,'%m/%d/%Y');

alter table layoffs_staging2
modify column `date` DATE;

SELECT * FROM
layoffs_staging2 
WHERE 
total_laid_off is NULL
AND percentage_laid_off IS NULL;

DELETE FROM
layoffs_staging2 
WHERE 
total_laid_off IS NULL
AND percentage_laid_off IS NULL;

select *
from layoffs_staging2 t1
join layoffs_staging2 t2
          on t1.company=t2.company
          and t1.location=t2.location 
where (t1.industry IS null or t1.industry='')
and t2.industry IS not null ;


Update layoffs_staging2 t1
join layoffs_staging2 t2
          on t1.company=t2.company
set t1.industry=t2.industry
where t1.industry IS null 
and t2.industry IS not null ;

DELETE FROM
layoffs_staging2 
WHERE 
total_laid_off IS NULL or total_laid_off=' '
AND percentage_laid_off IS NULL or percentage_laid_off = '';

select * from layoffs_staging2
where percentage_laid_off is NULL;

Delete 
from layoffs_staging2
where percentage_laid_off is NULL;

Alter table layoffs_staging2
drop column row_num;

