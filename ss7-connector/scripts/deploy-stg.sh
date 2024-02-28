#!/bin/bash
./mvnw -Pprod clean package -DskipTests=true

rsync -avP target/*.jar root@172.16.16.88:/juno/isup/
rsync -avP target/*.jar root@172.16.16.84:/juno/isup/
ssh root@172.16.16.88 systemctl restart juno-isup
ssh root@172.16.16.84 systemctl restart juno-isup
