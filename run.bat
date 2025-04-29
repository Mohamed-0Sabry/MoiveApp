@echo off
echo Compiling...
javac -d bin --module-path "C:\javafx-sdk-21.0.7\lib" --add-modules javafx.controls,javafx.fxml com\movieapp\**\*.java
echo Copying resources...
xcopy /Y /I /S com\movieapp\view\*.fxml bin\com\movieapp\view\
xcopy /Y /I /S com\movieapp\styles\*.css bin\com\movieapp\styles\
xcopy /Y /I /S *.png bin\
xcopy /Y /I /S *.jpg bin\
xcopy /Y /I /S *.gif bin\
echo Running...
java -cp bin --module-path "C:\javafx-sdk-21.0.7\lib" --add-modules javafx.controls,javafx.fxml com.movieapp.Main
pause 