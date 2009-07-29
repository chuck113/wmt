set HSQLDB_HOME=C:\java\hsqldb_1_8_0_10
set HSQLDB_DATA_DIR=C:\data\projects\wheresmytube\hsqldbfiles

java -cp %HSQLDB_HOME%\lib\hsqldb.jar org.hsqldb.Server -database.0 file:%HSQLDB_DATA_DIR% -dbname.0 wmtdb