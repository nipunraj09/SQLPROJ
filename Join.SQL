
use mskjoi;

select * from ordernumdata;

select * from price_data;

select * from ordernumdata 
inner join product
on ordernumdata.ordernumber = product.ordernumber;


select * from ordernumdata 
full outer join product
on ordernumdata.ordernumber = product.ordernumber;


select * from ordernumdata 
 right outer join product
on ordernumdata.ordernumber = product.ordernumber;

select * from ordernumdata 
 left outer join product
on ordernumdata.ordernumber = product.ordernumber;


