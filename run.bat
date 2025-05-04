@echo off
echo Compiling...
javac -cp "D:\Study\ENGINEERING\25\Second Term\Advanced Programming\Java Projects\MovieNight\lib\opencv-4110.jar" ^
    -d bin ^
    --module-path "C:\javafx-sdk-21.0.7\lib" ^
    --add-modules javafx.controls,javafx.fxml ^
    com\movieapp\*.java

echo Copying resources...
xcopy /Y /I /S "com\movieapp\view\*.fxml" "bin\com\movieapp\view\"
xcopy /Y /I /S "com\movieapp\view\image\*.*" "bin\com\movieapp\view\image\"
xcopy /Y /I /S "com\movieapp\styles\*.css" "bin\com\movieapp\styles\"

echo Running...
java -cp "bin;D:\Study\ENGINEERING\25\Second Term\Advanced Programming\Java Projects\MovieNight\lib\opencv-4110.jar;C:\javafx-sdk-21.0.7\lib\*" ^
    --module-path "C:\javafx-sdk-21.0.7\lib" ^
    --add-modules javafx.controls,javafx.fxml ^
    -Djava.library.path="D:\Study\ENGINEERING\25\Second Term\Advanced Programming\Java Projects\MovieNight\lib" ^
    com.movieapp.Main
pause
