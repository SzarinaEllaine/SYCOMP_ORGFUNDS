@echo off
if not exist out mkdir out
javac -d out src\*.java && copy /Y src\logo.png out\logo.png >nul && java -cp out Main
