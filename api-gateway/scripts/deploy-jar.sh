#!/bin/bash
# Usage: ./scripts/deploy.sh
IP=172.16.16.122;
./mvnw -Pprod clean package -DskipTests=true -Dskip.npm=true
rsync -avP target/*.jar root@$IP:/transnexus/api-gateway/
ssh root@$IP systemctl restart api-gateway
