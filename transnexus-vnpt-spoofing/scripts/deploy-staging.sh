#!/bin/bash
# Usage: ./scripts/deploy.sh
IP=172.16.16.146;
./mvnw -Pprod clean package -DskipTests=true -Dskip.npm=true
rsync -avP target/*.jar root@$IP:/istt/vnpt-spoofing/
ssh root@$IP systemctl restart vnpt-spoofing
