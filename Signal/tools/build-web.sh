#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
compiler="${KOTLIN_HOME:?Set KOTLIN_HOME to your Kotlin 2.1.21 compiler directory}"
work=$(mktemp -d)
trap 'rm -rf "$work"' EXIT
stdlib="$compiler/lib/kotlin-stdlib-js.klib"
"$compiler/bin/kotlinc-js" -Xir-produce-klib-file -libraries "$stdlib" -ir-output-dir "$work/lib" -ir-output-name signal src/*.kt
"$compiler/bin/kotlinc-js" -Xir-produce-js -Xir-dce -Xinclude="$work/lib/signal.klib" -libraries "$stdlib" -ir-output-dir "$work/js" -ir-output-name signal -main call
python3 tools/bundle.py "$work/js/signal.js" "$@"
