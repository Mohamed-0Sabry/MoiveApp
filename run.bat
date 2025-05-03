@echo off
echo Compiling...
javac -cp "c:\Users\mahm1\Downloads\opencv\build\java\opencv-4110.jar" ^
    -d bin ^
    --module-path "C:\javafx-sdk-21.0.7\lib" ^
    --add-modules javafx.controls,javafx.fxml ^
    com\movieapp\**\*.java

echo Copying resources...
xcopy /Y /I /S com\movieapp\view\*.fxml bin\com\movieapp\view\
xcopy /Y /I /S com\movieapp\images\*.css bin\com\movieapp\images\
xcopy /Y /I /S *.png bin\
xcopy /Y /I /S *.jpg bin\
xcopy /Y /I /S *.gif bin\

echo Running...
java -cp "bin;c:\Users\mahm1\Downloads\opencv\build\java\opencv-4110.jar" ^
    --module-path "C:\javafx-sdk-21.0.7\lib" ^
    --add-modules javafx.controls,javafx.fxml ^
    -Djava.library.path="c:\Users\mahm1\Downloads\opencv\build\java\x64" ^
    com.movieapp.Main

pause
