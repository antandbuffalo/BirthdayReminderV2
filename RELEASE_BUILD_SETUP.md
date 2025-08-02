# 🚀 Release Build Setup Instructions

## Prerequisites

1. **Keystore File**: Place your `.jks` file in the project root directory
2. **Keystore Details**: You'll need:
   - Store password
   - Key alias
   - Key password

## Configuration Methods

### Method 1: keystore.properties File (Recommended)
1. **Copy the example file:**
   ```bash
   cp keystore.properties.example keystore.properties
   ```

2. **Edit keystore.properties with your actual values:**
   ```properties
   KEYSTORE_PASSWORD=akiyamaB1
   KEY_ALIAS=BirthdayReminderKey
   KEY_PASSWORD=akiyamaB1
   ```

3. **Build normally:**
   ```bash
   ./build-release.sh
   # OR
   ./gradlew bundleRelease
   ```

### Method 2: Environment Variables
```bash
export KEYSTORE_PASSWORD="akiyamaB1"
export KEY_ALIAS="BirthdayReminderKey"
export KEY_PASSWORD="akiyamaB1"

./gradlew bundleRelease
```

### Method 3: Interactive Script
```bash
./build-release.sh
# Script will prompt for credentials securely
```

## Building Release

### Method 1: Using Script (Recommended)
```bash
./build-release.sh
```
The script will:
- ✅ Check for keystore file
- ✅ Prompt for credentials securely  
- ✅ Clean and build signed App Bundle (.aab)
- ✅ Build signed APK for testing
- ✅ Show file locations and sizes

### Method 2: Manual Gradle Commands
```bash
# Set environment variables first
export KEYSTORE_PASSWORD="your-password"
export KEY_ALIAS="your-alias"
export KEY_PASSWORD="your-password"

# Build release bundle
./gradlew bundleRelease

# Build release APK
./gradlew assembleRelease
```

## Output Files

### App Bundle (for Play Store)
📍 **Location**: `app/build/outputs/bundle/release/app-release.aab`
🎯 **Use**: Upload to Google Play Console

### APK (for Testing)  
📍 **Location**: `app/build/outputs/apk/release/app-release.apk`
🎯 **Use**: Install directly on devices for testing

## Firebase Setup for Release

1. **Get Release SHA Fingerprint**:
   ```bash
   keytool -list -v -keystore birthday-reminder.jks -alias your-key-alias
   ```

2. **Add to Firebase Console**:
   - Copy SHA-1 and SHA-256 fingerprints
   - Add to Firebase Project Settings → Your App → Add Fingerprint

3. **Download Updated google-services.json**:
   - Replace the file in your `app/` folder
   - Rebuild the app

## Security Notes

⚠️ **NEVER commit keystore files to Git**
- The `.gitignore` has been updated to prevent this
- Keep backups of your keystore in secure locations
- Store passwords securely (use environment variables)

✅ **Keystore files are ignored**:
- `*.jks`
- `*.keystore` 
- `keystore.properties`

## Troubleshooting

### Build Fails with R8 Errors
Current configuration has R8 disabled. To enable optimization:
1. In `app/build.gradle`, change:
   ```gradle
   minifyEnabled true
   shrinkResources true
   ```
2. Fix any ProGuard rule issues

### Authentication Fails
1. Verify SHA fingerprints are added to Firebase
2. Check package name matches exactly
3. Download latest `google-services.json`

### Keystore Issues
1. Verify keystore file path and name
2. Check credentials are correct
3. Ensure keystore is not corrupted
