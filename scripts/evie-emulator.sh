#!/usr/bin/env bash
# Drive the HOST-side headless Android emulator (SDK at ~/Android/Sdk, AVD evie-test).
# Usage: ./scripts/evie-emulator.sh [boot|wait|status|adb|shell|install|screencap|logcat|kill]
set -euo pipefail

SDK="${HOME}/Android/Sdk"
AVD=evie-test
EMU_LOG="${HOME}/.local/share/evie-emu/emulator.log"

adb() { "$SDK/platform-tools/adb" "$@"; }
adb_e() { "$SDK/platform-tools/adb" -s emulator-5554 "$@"; }

running() { adb devices | grep -q 'emulator-\|device$'; }


case "${1:-status}" in
  boot)
    if adb devices 2>/dev/null | grep -q emulator; then echo "already running"; exit 0; fi
    mkdir -p "$(dirname "$EMU_LOG")"
    nohup "$SDK/emulator/emulator" -avd "$AVD" \
      -no-window -no-audio -gpu swiftshader_indirect \
      -camera-back none -no-boot-anim -accel on \
      > "$EMU_LOG" 2>&1 &
    echo "emulator starting (pid $!)"
    ;;
  wait)
    for _ in $(seq 1 90); do
      if adb_e shell getprop sys.boot_completed 2>/dev/null | grep -q '^1'; then
        echo "boot completed"; exit 0
      fi
      sleep 4
    done
    echo "boot timeout"; exit 1
    ;;
  status) adb devices | tail -2 ;;
  adb) shift; adb_e "$@" ;;
  shell) shift; adb_e shell "$@" ;;
  install) shift; adb_e install -r "$@" ;;
  screencap) shift; adb_e exec-out screencap -p > "${1:-/tmp/opencode/emu.png}" ;;
  logcat) shift; adb_e logcat "$@" ;;
  kill) adb_e emu kill ;;
  *) echo "usage: $0 [boot|wait|status|adb|shell|install|screencap|logcat|kill]" >&2; exit 2 ;;
esac