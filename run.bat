@echo off
echo Compiling...
javac -cp "C:\Users\a_sal\OneDrive\Desktop\MovieApp\MoiveApp\lib\opencv-4110.jar" ^
    -d bin ^
    --module-path "C:\Users\a_sal\OneDrive\Desktop\Modern Gas\javafx-sdk-24.0.1\lib" ^
    --add-modules javafx.controls,javafx.fxml ^
    com\movieapp\*.java

echo Copying resources...
xcopy /Y /I /S "com\movieapp\view\*.fxml" "bin\com\movieapp\view\"
xcopy /Y /I /S "com\movieapp\view\image\*.*" "bin\com\movieapp\view\image\"
xcopy /Y /I /S "com\movieapp\styles\*.css" "bin\com\movieapp\styles\"

echo Running...
java -cp "bin;C:\Users\a_sal\OneDrive\Desktop\MovieApp\MoiveApp\lib\opencv-4110.jar" ^
    --module-path "C:\Users\a_sal\OneDrive\Desktop\Modern Gas\javafx-sdk-24.0.1\lib" ^
    --add-modules javafx.controls,javafx.fxml ^
    -Djava.library.path="C:\Users\a_sal\OneDrive\Desktop\MovieApp\MoiveApp\lib" ^
    com.movieapp.Main

pause
