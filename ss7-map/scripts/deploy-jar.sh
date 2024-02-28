#!/bin/bash
# Usage: ./scripts/deploy.sh
IP=spoofing
./mvnw -Pprod -DskipTests=true clean package
rsync -avP target/*.jar $IP:/istt/ss7-isup/
