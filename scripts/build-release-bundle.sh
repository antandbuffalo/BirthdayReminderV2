#!/usr/bin/env bash
#
# Builds the signed production Android App Bundle (.aab) for the Play Store.
#
#   ./scripts/build-release-bundle.sh [--skip-clean]
#
# Signing credentials come from keystore.properties at the repo root (copy
# keystore.properties.example and fill it in), or from the BR_STORE_FILE /
# BR_STORE_PASSWORD / BR_KEY_ALIAS / BR_KEY_PASSWORD environment variables,
# which take precedence. Neither the keystore nor keystore.properties is
# tracked by git.

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$REPO_ROOT"

SKIP_CLEAN=0
for arg in "$@"; do
    case "$arg" in
        --skip-clean) SKIP_CLEAN=1 ;;
        -h|--help) sed -n '2,13p' "${BASH_SOURCE[0]}" | sed 's/^# \{0,1\}//'; exit 0 ;;
        *) echo "unknown option: $arg" >&2; exit 2 ;;
    esac
done

fail() { echo "error: $*" >&2; exit 1; }

# --- Toolchain -------------------------------------------------------------
# Gradle 6.7.1 / AGP 4.2.2 refuse to run on JDK 16+, and macOS usually has a
# much newer default JDK on PATH, so pin JDK 11 explicitly.
is_jdk11() { [ -x "$1/bin/java" ] && "$1/bin/java" -version 2>&1 | grep -q '"11\.'; }

find_jdk11() {
    if [ -n "${JAVA_HOME:-}" ] && is_jdk11 "$JAVA_HOME"; then
        echo "$JAVA_HOME"; return 0
    fi
    if [ -x /usr/libexec/java_home ]; then
        local home
        home="$(/usr/libexec/java_home -v 11 2>/dev/null || true)"
        if [ -n "$home" ] && is_jdk11 "$home"; then echo "$home"; return 0; fi
    fi
    local candidate
    for candidate in /opt/homebrew/opt/openjdk@11 /usr/local/opt/openjdk@11 \
                     /Library/Java/JavaVirtualMachines/*/Contents/Home; do
        if is_jdk11 "$candidate"; then echo "$candidate"; return 0; fi
    done
    return 1
}

JAVA_HOME="$(find_jdk11)" || fail "no JDK 11 found. Install one with: brew install openjdk@11"
export JAVA_HOME
echo "==> JDK      $JAVA_HOME"

# --- Signing credentials ---------------------------------------------------
prop() { [ -f keystore.properties ] && sed -n "s/^$1=//p" keystore.properties | head -1 || true; }

STORE_FILE="${BR_STORE_FILE:-$(prop storeFile)}"
STORE_PASSWORD="${BR_STORE_PASSWORD:-$(prop storePassword)}"
KEY_ALIAS="${BR_KEY_ALIAS:-$(prop keyAlias)}"
KEY_PASSWORD="${BR_KEY_PASSWORD:-$(prop keyPassword)}"

[ -n "$STORE_FILE" ] || fail "no signing config. Copy keystore.properties.example to keystore.properties and fill it in."
[ -f "$STORE_FILE" ] || fail "keystore not found at: $STORE_FILE"
[ -n "$STORE_PASSWORD" ] || fail "storePassword is empty in keystore.properties"
[ -n "$KEY_ALIAS" ] || fail "keyAlias is empty in keystore.properties"
[ -n "$KEY_PASSWORD" ] || fail "keyPassword is empty in keystore.properties"
echo "==> Keystore $STORE_FILE (alias: $KEY_ALIAS)"

# --- Version ---------------------------------------------------------------
VERSION_CODE="$(sed -n 's/^ *versionCode *\([0-9]*\).*/\1/p' app/build.gradle | head -1)"
VERSION_NAME="$(sed -n 's/^ *versionName *"\(.*\)".*/\1/p' app/build.gradle | head -1)"
echo "==> Version  $VERSION_NAME (versionCode $VERSION_CODE)"
echo
echo "    Play rejects an upload whose versionCode already exists. Bump"
echo "    versionCode in app/build.gradle if $VERSION_CODE is already live."
echo

# --- Build -----------------------------------------------------------------
if [ "$SKIP_CLEAN" -eq 0 ]; then
    echo "==> Cleaning"
    ./gradlew clean --console=plain -q
fi

echo "==> Building release bundle"
./gradlew bundleRelease --console=plain

AAB="app/build/outputs/bundle/release/app-release.aab"
[ -f "$AAB" ] || fail "expected bundle not produced at $AAB"

# --- Verify ----------------------------------------------------------------
# An unsigned bundle uploads fine and is then rejected by Play, so confirm the
# signature here rather than finding out in the Play Console.
echo
echo "==> Verifying signature"
"$JAVA_HOME/bin/jarsigner" -verify "$AAB" >/dev/null \
    || fail "bundle is not signed correctly"
"$JAVA_HOME/bin/keytool" -printcert -jarfile "$AAB" \
    | grep -E '^(Owner|Valid from):|SHA256:' | head -3

echo
echo "==> Done"
echo "    bundle   $REPO_ROOT/$AAB"
echo "    size     $(du -h "$AAB" | cut -f1)"
echo "    sha256   $(shasum -a 256 "$AAB" | cut -d' ' -f1)"
echo
echo "    Upload at https://play.google.com/console -> Production -> Create new release."
echo "    Confirm the SHA256 above matches the upload certificate Play expects."
