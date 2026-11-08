#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
OUT="$(mktemp -d)"
trap 'rm -rf "$OUT"' EXIT
find "$ROOT/src/main/java" "$ROOT/src/test/java" -name '*.java' -print0 | xargs -0 javac -d "$OUT"
java -cp "$OUT" cc.fentware.fms.core.FlightPlannerSmokeTest
