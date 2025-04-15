#!/bin/bash
echo "Starting Movie Night App in VIEWER mode..."

# Navigate to project directory - adjust this path to your Linux path
cd "$(dirname "$0")"

echo "Cleaning target directory..."
if [ -d "target/classes" ]; then
  rm -rf "target/classes"
fi

echo "Compiling classes..."
mkdir -p "target/classes/com/movienight"
javac -d target/classes --module-path "/path/to/javafx-sdk/lib" --add-modules javafx.controls,javafx.fxml,javafx.media src/com/movienight/*.java

echo "Copying resources..."
cp "src/com/movienight/style.css" "target/classes/com/movienight/style.css"

echo "Running application..."
java --module-path "/path/to/javafx-sdk/lib" --add-modules javafx.controls,javafx.fxml,javafx.media -cp target/classes com.movienight.Main --viewer 