#!/bin/bash

# Font download script for localgrok
# This script downloads JetBrains Mono and Space Grotesk fonts

FONT_DIR="../app/src/main/res/font"

# Create font directory
mkdir -p "$FONT_DIR"

echo "Downloading JetBrains Mono..."
# Download JetBrains Mono
curl -L "https://github.com/JetBrains/JetBrainsMono/releases/download/v2.304/JetBrainsMono-2.304.zip" -o /tmp/jetbrains_mono.zip
unzip -o /tmp/jetbrains_mono.zip -d /tmp/jetbrains_mono

cp "/tmp/jetbrains_mono/fonts/ttf/JetBrainsMono-Regular.ttf" "$FONT_DIR/jetbrains_mono_regular.ttf"
cp "/tmp/jetbrains_mono/fonts/ttf/JetBrainsMono-Medium.ttf" "$FONT_DIR/jetbrains_mono_medium.ttf"
cp "/tmp/jetbrains_mono/fonts/ttf/JetBrainsMono-Bold.ttf" "$FONT_DIR/jetbrains_mono_bold.ttf"

echo "Downloading Space Grotesk..."
# Download Space Grotesk from Google Fonts
curl -L "https://fonts.google.com/download?family=Space%20Grotesk" -o /tmp/space_grotesk.zip
unzip -o /tmp/space_grotesk.zip -d /tmp/space_grotesk

# Find and copy the font files (Google Fonts uses variable fonts)
cp /tmp/space_grotesk/SpaceGrotesk-VariableFont_wght.ttf "$FONT_DIR/space_grotesk_bold.ttf" 2>/dev/null || \
cp /tmp/space_grotesk/static/SpaceGrotesk-Bold.ttf "$FONT_DIR/space_grotesk_bold.ttf" 2>/dev/null

cp /tmp/space_grotesk/SpaceGrotesk-VariableFont_wght.ttf "$FONT_DIR/space_grotesk_medium.ttf" 2>/dev/null || \
cp /tmp/space_grotesk/static/SpaceGrotesk-Medium.ttf "$FONT_DIR/space_grotesk_medium.ttf" 2>/dev/null

# Cleanup
rm -rf /tmp/jetbrains_mono.zip /tmp/jetbrains_mono /tmp/space_grotesk.zip /tmp/space_grotesk

echo "Fonts downloaded successfully!"
echo "Font files are in: $FONT_DIR"
ls -la "$FONT_DIR"

