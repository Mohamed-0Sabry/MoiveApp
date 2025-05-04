@echo off
echo Compiling...
javac -cp "lib\opencv-4110.jar" ^
    -d bin ^
    --module-path "C:\javafx-sdk-21.0.7\lib" ^
    --add-modules javafx.controls,javafx.fxml ^
    com\movieapp\*.java

echo Copying resources...
xcopy /Y /I /S "com\movieapp\view\*.fxml" "bin\com\movieapp\view\"
xcopy /Y /I /S "com\movieapp\view\image\*.*" "bin\com\movieapp\view\image\"
xcopy /Y /I /S "com\movieapp\styles\*.css" "bin\com\movieapp\styles\"

echo Running...
java ^
    --module-path "C:\javafx-sdk-21.0.7\lib" ^
    --add-modules javafx.controls,javafx.fxml ^
    -cp "bin;lib\opencv-4110.jar" ^
    -Djava.library.path="lib" ^
    com.movieapp.Main

pause
