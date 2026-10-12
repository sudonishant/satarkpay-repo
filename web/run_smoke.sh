#!/usr/bin/env bash
# SatarkPay M2 · smoke test runner
#   ./run_smoke.sh          -> jsdom install (agar nahi hai) + 65 checks
#   HTML_PATH=... ./run_smoke.sh
set -e
HERE="$(cd "$(dirname "$0")" && pwd)"
JLIB="${JSDOM_DIR:-/tmp/domtest}"
if [ ! -d "$JLIB/node_modules/jsdom" ]; then
  echo "→ jsdom install ho raha hai ($JLIB)…"
  mkdir -p "$JLIB" && (cd "$JLIB" && npm install jsdom --no-audit --no-fund --silent)
fi
cd "$HERE"
NODE_PATH="$JLIB/node_modules" node smoke_test.js
