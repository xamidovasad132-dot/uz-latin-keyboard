#!/bin/bash
# Emulatorda Zafaron ilovasini sinaydi: ekranlar, video yasash, xato bermasligi.
PKG=uz.zafron.app
OUT=ci-out
mkdir -p $OUT
S=$OUT/summary.txt
: > $S
log() { echo "$*" | tee -a $S; }

dump() { adb shell uiautomator dump /sdcard/u.xml >/dev/null 2>&1; adb pull /sdcard/u.xml $OUT/u.xml >/dev/null 2>&1; }
shot() { adb exec-out screencap -p > $OUT/$1.png; }
tap() {
  dump
  python3 - "$1" <<'PY'
import sys, re, subprocess
t = sys.argv[1].lower()
x = open('ci-out/u.xml', encoding='utf8').read()
for m in re.finditer(r'<node[^>]*>', x):
    n = m.group(0)
    tx = re.search(r'text="([^"]*)"', n)
    b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
    if tx and b and t in tx.group(1).lower():
        x1, y1, x2, y2 = map(int, b.groups())
        subprocess.call(['adb', 'shell', 'input', 'tap', str((x1 + x2) // 2), str((y1 + y2) // 2)])
        print("tapped:", tx.group(1)[:40])
        sys.exit(0)
print("NOT FOUND:", sys.argv[1])
sys.exit(1)
PY
  r=$?
  if [ $r -eq 0 ]; then log "OK tap '$1'"; else log "FAIL tap '$1'"; fi
  sleep 1.5
}
has() { dump; if grep -qi "$1" $OUT/u.xml; then log "OK ekranda bor: $1"; else log "FAIL ekranda yo'q: $1"; fi; }

adb install -r zafron-debug.apk >/dev/null 2>&1 && log "OK o'rnatildi" || log "FAIL o'rnatilmadi"
adb shell pm grant $PKG android.permission.POST_NOTIFICATIONS 2>/dev/null
adb shell settings put secure show_ime_with_hard_keyboard 0
for ime in $(adb shell ime list -s | tr -d "\r"); do adb shell ime disable "$ime" >/dev/null 2>&1; done
adb logcat -c

adb shell am start -n $PKG/.MainActivity >/dev/null; sleep 5
shot 01_setup; has "Kalit olish"
tap "Kalitni shu"
adb shell input text "AIzaSyFAKEFAKEFAKEFAKEFAKE1234567890"
tap "Saqlash"; sleep 2
shot 02_chat; has "Salom"; has "Ulash"

tap "≡"; sleep 1
shot 03_drawer; has "Yangi suhbat"; has "Video yasash"
tap "Video yasash"; sleep 1
shot 04_video
tap "Har qatorga"
adb shell input text "Salom"
adb shell input keyevent 66
adb shell input text "Zafaron"
adb shell input keyevent 66
adb shell input text "Telemasterlik"
tap "Videoni yasash"
sleep 30
shot 05_video_done
adb shell ls -l /sdcard/Movies/Zafaron 2>&1 | tee -a $S
mkdir -p $OUT/video; adb pull /sdcard/Movies/Zafaron $OUT/video >/dev/null 2>&1
adb shell am start -n $PKG/.MainActivity >/dev/null; sleep 3
shot 05b_back
has "Tayyor"
tap "Orqaga"; sleep 1

tap "≡"; sleep 1
tap "Ulash"; sleep 1
shot 06_ulash; has "Gmail"; has "Telefon raqam"
tap "Orqaga"; sleep 1

tap "Savol yozing"
adb shell input text "salom"
tap "↑"
sleep 20
shot 07_reply
dump; log "Javob ekranidagi matn:"; grep -o 'text="[^"]*"' $OUT/u.xml | head -12 | tee -a $S

adb logcat -d -s AndroidRuntime:E > $OUT/crash.txt
n=$(grep -c "FATAL" $OUT/crash.txt)
log "Qulashlar soni: $n"
head -30 $OUT/crash.txt | tee -a $S
exit 0
