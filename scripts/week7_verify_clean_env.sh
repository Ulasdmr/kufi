#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
echo '=== Java and Maven ==='
java -version
mvn -version
echo '=== Java smoke and Maven verification ==='
bash week6-pure-java/run-smoke.sh
mvn --batch-mode --no-transfer-progress clean verify
bash scripts/collect_week6_evidence.sh
echo 'For PostgreSQL integration test, start the backend, then run: python3 scripts/week7_integration_smoke.py'
