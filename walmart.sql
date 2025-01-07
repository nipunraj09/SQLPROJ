use mydb;

select * from walmart;

select time,
          (CASE
                 WHEN `time` BETWEEN "00:00:00" AND "12:00:00" THEN "Morning"
                 WHEN `time` BETWEEN "12:01:00" AND "16:00:00" THEN "Afternoon"
                 ELSE "Evening"
			  END 
              ) As time_of_date
              From walmart;  
    
    
  
    SET SQL_SAFE_UPDATES = 0;
    
update walmart
set time_of_day = (
    CASE
                              WHEN `time` BETWEEN "00:00:00" AND "12:00:00" THEN "Morning"
                              WHEN `time` BETWEEN "12:01:00" AND "16:00:00" THEN "Afternoon"
                              ELSE "Evening"
    END
);

                 
                 
select 
date,Dayname(date)
from walmart;

-- alter table walmart add column day_name varchar(20);

update walmart 
set day_name= DAYNAME(date);

select * from walmart;

select 
date,monthname(date)
from walmart;

alter table walmart add column month_day Varchar(20);

update walmart
set month_day = monthname(date);

select * from walmart ;
-------------------------------------------------------------------------------
-------- -- Generic ques
-- Lessgo

select DISTINCT(City) FROM walmart;

select distinct city,branch FROM walmart;

select 
DISTINCT(Product)
from walmart;

select payment,count(payment) as cnt from walmart group by payment;
 
select `Product line`, count(`Product line`) as cnt
from walmart group by `Product line` order by cnt DESC;


select 
month_day as month,
sum(total) as total_revenue
from walmart
group by month_day
order by total_revenue DESC;

