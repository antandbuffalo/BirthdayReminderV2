#!/bin/bash

# Birthday Reminder - Release Build Script
# This script builds a signed release Android App Bundle (.aab)

set -e  # Exit on any error

echo "🚀 Birthday Reminder - Release Build Script"
echo "============================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
JKS_FILE="BirthdayReminderKeyStore.jks"
OUTPUT_DIR="app/build/outputs"

# Check if keystore exists
if [ ! -f "$JKS_FILE" ]; then
    echo -e "${RED}❌ Error: Keystore file '$JKS_FILE' not found!${NC}"
    echo "Please make sure your .jks file is in the project root directory."
    exit 1
fi

echo -e "${BLUE}📋 Keystore file found: $JKS_FILE${NC}"

# Get keystore credentials
echo -e "${YELLOW}🔐 Please enter your keystore credentials:${NC}"
read -p "Store Password: " -s STORE_PASSWORD
echo
read -p "Key Alias: " KEY_ALIAS
read -p "Key Password: " -s KEY_PASSWORD
echo

# Export environment variables for Gradle
export KEYSTORE_PASSWORD="$STORE_PASSWORD"
export KEY_ALIAS="$KEY_ALIAS"
export KEY_PASSWORD="$KEY_PASSWORD"

echo -e "${BLUE}🧹 Cleaning project...${NC}"
./gradlew clean

echo -e "${BLUE}📦 Building signed release App Bundle...${NC}"
./gradlew bundleRelease

# Check if build was successful
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✅ Build successful!${NC}"
    echo
    echo -e "${GREEN}📱 Release App Bundle created:${NC}"
    echo -e "${BLUE}   Location: $OUTPUT_DIR/bundle/release/app-release.aab${NC}"
    echo
    
    # Show file size
    if [ -f "$OUTPUT_DIR/bundle/release/app-release.aab" ]; then
        SIZE=$(du -h "$OUTPUT_DIR/bundle/release/app-release.aab" | cut -f1)
        echo -e "${GREEN}   Size: $SIZE${NC}"
    fi
    
    echo
    echo -e "${YELLOW}📋 Next steps:${NC}"
    echo "1. Test the app bundle on a device"
    echo "2. Upload to Google Play Console"
    echo "3. Create internal/alpha test release"
    echo
    echo -e "${BLUE}💡 To install for testing:${NC}"
    echo "   bundletool install-apks --apks=app-release.apks"
    
else
    echo -e "${RED}❌ Build failed!${NC}"
    echo "Check the output above for errors."
    exit 1
fi

# Also build APK for testing
echo -e "${BLUE}📱 Building signed release APK for testing...${NC}"
./gradlew assembleRelease

if [ $? -eq 0 ] && [ -f "$OUTPUT_DIR/apk/release/app-release.apk" ]; then
    APK_SIZE=$(du -h "$OUTPUT_DIR/apk/release/app-release.apk" | cut -f1)
    echo -e "${GREEN}✅ Release APK also created:${NC}"
    echo -e "${BLUE}   Location: $OUTPUT_DIR/apk/release/app-release.apk${NC}"
    echo -e "${BLUE}   Size: $APK_SIZE${NC}"
fi

echo
echo -e "${GREEN}🎉 Release build completed successfully!${NC}"
