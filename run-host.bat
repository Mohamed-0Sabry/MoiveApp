@echo off
echo Starting Movie Night App in HOST mode...
d:
cd "d:\Git clones\Project"

echo Cleaning target directory...
if exist "target\classes" rmdir /s /q "target\classes"

echo Compiling classes...
if not exist "target\classes\com\movienight" mkdir "target\classes\com\movienight"
javac -d target/classes --module-path "C:\javafx-sdk-21.0.7\lib" --add-modules javafx.controls,javafx.fxml,javafx.media src/com/movienight/*.java

echo Copying resources...
copy "src\com\movienight\style.css" "target\classes\com\movienight\style.css"

echo Running application...
"C:\Program Files\Java\jdk-21\bin\java.exe" --module-path "C:\javafx-sdk-21.0.7\lib" --add-modules javafx.controls,javafx.fxml,javafx.media -cp target/classes com.movienight.Main --host 