#!/usr/bin/env bash
set -euo pipefail
mkdir -p /tmp/kufi-week6-smoke
javac -d /tmp/kufi-week6-smoke src/main/java/com/kufi/backend/appointment/BookingTimeRules.java week6-pure-java/BookingTimeRulesCheck.java
java -cp /tmp/kufi-week6-smoke BookingTimeRulesCheck
