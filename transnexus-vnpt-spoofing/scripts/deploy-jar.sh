#!/bin/bash
# Usage: ./scripts/deploy.sh
./mvnw -Pprod clean package -DskipTests=true -Dskip.npm=true
rsync -avP target/*.jar spoofing:/juno/vnpt-spoofing/
# ssh root@$IP systemctl restart spoofing
