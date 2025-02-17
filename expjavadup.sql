WITH CTE AS (
    SELECT Id, ROLL, ROW_NUMBER() OVER (PARTITION BY ROLL ORDER BY Id) AS row_num
    FROM expjava
)
DELETE FROM expjava
WHERE Id IN (
    SELECT Id FROM CTE WHERE row_num > 1
);



update expjava set Qpcode='',Answerbookletcode='';


update expjava set UpdatedAt=NULL;
