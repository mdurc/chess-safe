#!/bin/zsh

JAVAFX_LIB_PATH="/Users/mdurcan/java-utils/javafx"
JAVAFX_CP=$(find "$JAVAFX_LIB_PATH" -name "*.jar" | paste -sd ":" -)

BIN_DIR="bin"
SRC_DIR="src/chess"
RES_DIR="resources"

mkdir -p "$BIN_DIR"
mkdir -p "$BIN_DIR/pieces"
mkdir -p "$BIN_DIR/sounds"

cp -r "$RES_DIR/pieces/"*.png "$BIN_DIR/pieces/"
cp -r "$RES_DIR/sounds/"*.wav "$BIN_DIR/sounds/"

echo "Finding all java files"
JAVA_FILES=($SRC_DIR/**/*.java)
echo "Compiling Java files..."
javac -d "$BIN_DIR" -cp "$BIN_DIR:$JAVAFX_CP" $JAVA_FILES && java -ea --module-path "$JAVAFX_LIB_PATH" --add-modules javafx.controls,javafx.media,javafx.graphics,javafx.swing \
    --add-exports=javafx.base/com.sun.javafx.logging=ALL-UNNAMED \
    -cp "$BIN_DIR:$JAVAFX_CP" chess.Main
