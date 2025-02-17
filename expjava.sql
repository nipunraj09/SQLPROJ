WITH cte AS (
    SELECT *, ROW_NUMBER() OVER (PARTITION BY ROLL ORDER BY Qpcode, Answerbookletcode) AS row_num
    FROM expjava
)
DELETE FROM expjava
WHERE ROLL IN (SELECT ROLL FROM cte WHERE row_num > 1);
