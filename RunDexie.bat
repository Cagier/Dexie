REM Endless loop to run the app over and over again so if it crashes it will start again.
REM A bit of a fudge until I get the error handling a bit more robust but it works for the moment!
:Loophere
"C:\Program Files\Java\jdk1.7.0_80\bin\java" -cp .;mongodb-driver-core-3.0.4.jar;bson-3.0.4.jar;mongodb-driver-3.0.4.jar;slf4j-nop-1.7.12.jar;slf4j-api-1.7.12.jar Dexie
GOTO Loophere
:YouWontGetHere