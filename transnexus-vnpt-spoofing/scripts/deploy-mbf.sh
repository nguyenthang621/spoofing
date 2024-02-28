#!/bin/bash
# Usage: ./scripts/deploy.sh
IP=10.51.20.118
./mvnw -Pprod clean package -DskipTests=true -Dskip.npm=true
rsync -avP target/*.jar root@$IP:/istt/spoofing/
ssh root@$IP systemctl restart spoofing
