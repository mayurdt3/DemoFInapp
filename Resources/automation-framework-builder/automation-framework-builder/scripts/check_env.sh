#!/bin/sh
# Usage: check_env.sh <java|playwright>
# Prints which prerequisites exist so the skill knows whether it can run the demo test.
stack="${1:-playwright}"
ok=1
chk() { if command -v "$1" >/dev/null 2>&1; then echo "OK      $1: $($1 $2 2>&1 | head -1)"; else echo "MISSING $1"; ok=0; fi; }
if [ "$stack" = "java" ]; then
  chk java -version
  chk mvn -v
  command -v google-chrome >/dev/null 2>&1 || command -v chromium >/dev/null 2>&1 || command -v firefox >/dev/null 2>&1 \
    && echo "OK      browser found" || { echo "MISSING browser (chrome/chromium/firefox)"; ok=0; }
else
  chk node -v
  chk npm -v
  [ -d "$HOME/.cache/ms-playwright" ] && echo "OK      playwright browsers cached" || echo "NOTE    run: npx playwright install"
fi
# network check for demo site
if command -v curl >/dev/null 2>&1; then
  curl -s -o /dev/null -m 8 -w "%{http_code}" https://www.saucedemo.com | grep -q '^200$' \
    && echo "OK      network: saucedemo reachable" || { echo "MISSING network access to https://www.saucedemo.com"; ok=0; }
fi
[ "$ok" = 1 ] && echo "RESULT  can run demo test" || echo "RESULT  cannot fully run demo test here: provide run instructions only"
