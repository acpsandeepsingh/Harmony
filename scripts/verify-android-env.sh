#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
LOCAL_PROPS="$ROOT_DIR/local.properties"

fail() { echo "❌ $1"; exit 1; }
warn() { echo "⚠️  $1"; }
pass() { echo "✅ $1"; }

sdk_dir=""

if [[ -f "$LOCAL_PROPS" ]]; then
  sdk_dir=$(awk -F= '/^sdk\.dir=/{print substr($0,index($0,$2))}' "$LOCAL_PROPS" | sed 's#\\:#:#g' | tail -n1)
fi

if [[ -z "$sdk_dir" ]]; then
  sdk_dir="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-}}"
fi

[[ -n "$sdk_dir" ]] || fail "Android SDK location not found. Set sdk.dir in local.properties or export ANDROID_HOME/ANDROID_SDK_ROOT."
[[ -d "$sdk_dir" ]] || fail "sdk.dir path does not exist: $sdk_dir"
pass "Android SDK found at: $sdk_dir"

if java -version 2>&1 | head -n1 | rg -q '"2[5-9]|"[3-9][0-9]'; then
  pass "Java runtime is compatible (requires Java 25+)"
else
  warn "Java 25+ not detected. Build may fail toolchain checks."
fi

if [[ -d "$sdk_dir/platforms/android-36" ]]; then
  pass "Android SDK Platform 36 found"
elif [[ -d "$sdk_dir/platforms/android-35" ]]; then
  warn "Platform 36 missing; Platform 35 found (use -Pandroid.compileSdk=35 -Pandroid.targetSdk=35)"
else
  fail "Neither Android SDK Platform 36 nor 35 is installed"
fi

if [[ -d "$sdk_dir/build-tools/35.0.0" ]]; then
  pass "Build-Tools 35.0.0 found"
else
  fail "Android Build-Tools 35.0.0 not found"
fi

if command -v curl >/dev/null 2>&1; then
  if curl -fsSI --max-time 10 https://dl.google.com/android/repository/repository2-1.xml >/dev/null; then
    pass "dl.google.com reachable for SDK downloads"
  else
    warn "dl.google.com is not reachable from this environment. SDK auto-download will fail."
  fi
fi

pass "Environment check completed."
