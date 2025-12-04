#!/usr/bin/env bash
set -euo pipefail

cd ../server/

echo "1) Finding duplicate filenames"
find . -type f -name '*.java' -printf "%f\t%p\n" | sort | uniq -d -w 200 || true

echo
echo "2) Finding classes with same simple name"
python3 - <<'PY'
import re,os,sys
classes={}
for root,dirs,files in os.walk('.'):
    for f in files:
        if f.endswith('.java'):
            p=os.path.join(root,f)
            with open(p,'r',encoding='utf-8',errors='ignore') as fh:
                for line in fh:
                    m=re.match(r'^\s*(public\s+)?(class|interface|enum)\s+([A-Za-z0-9_]+)', line)
                    if m:
                        name=m.group(3)
                        classes.setdefault(name,[]).append(p)
                        break
for name,paths in classes.items():
    if len(paths)>1:
        print("CLASS DUP:",name)
        for p in paths:
            print("  ",p)
PY

echo
echo "3) SHA1 duplicate file bodies (exact duplicates)"
find . -type f -name '*.java' -exec sha1sum {} + | sort | awk '{print $1}' | uniq -D || true

echo
echo "4) Run gradle check (including checkstyle) and unit tests"

./gradlew clean test jacocoTestReport --no-daemon

echo
echo "5) Run static analyzers (if installed):"
echo " - sonar: run in CI or with sonar-scanner"
echo " - spotbugs: ./gradlew spotbugsMain (if added)"
