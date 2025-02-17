CREATE PROCEDURE dbo.InsertLogProcedure
    @ROLL VARCHAR(50),
    @NAME VARCHAR(100),
    @DOB DATE,
    @Qpcode VARCHAR(50),
    @Answerbookletcode VARCHAR(50),
    @Updatereason VARCHAR(255),
    @Timelog DATETIME
AS
BEGIN
    INSERT INTO expjava_log (ROLL, NAME, DOB, Qpcode, Answerbookletcode, Updatereason, UpdatedAt, Timelog)
    VALUES (@ROLL, @NAME, @DOB, @Qpcode, @Answerbookletcode, @Updatereason, GETDATE(), @Timelog);
END
