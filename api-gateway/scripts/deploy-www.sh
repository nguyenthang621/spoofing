#!/bin/bash
# Usage: ./scripts/deploy.sh [version]
IP=vnptspoofing
APP_VERSION=$1 npm run build
rsync -avP target/classes/static/* root@$IP:/juno/www/public/
