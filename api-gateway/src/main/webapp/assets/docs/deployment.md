## Software Deployment

The Gateway Application system can be deploy on a Linux box with following hardware requirements:

- At least 1G Memory
- At least 20G Hard Disk

Deployment workflow on Centos:

1. Install MongoDB
2. Install Java
3. Deploy app as a service
4. Optional - Configure HTTP / HTTPS

### 1. Install MongoDB

Configure the package management system (yum).
Create a /etc/yum.repos.d/mongodb-org-4.0.repo file so that you can install MongoDB directly using yum:

```bash
$ cat /etc/yum.repos.d/mongodb-org-4.0.repo
[mongodb-org-4.0]
name=MongoDB Repository
baseurl=https://repo.mongodb.org/yum/redhat/$releasever/mongodb-org/4.0/x86_64/
gpgcheck=1
enabled=1
gpgkey=https://www.mongodb.org/static/pgp/server-4.0.asc
```

To install the latest stable version of MongoDB, issue the following command:

```bash
$ yum install -y mongodb-org
```

### 2. Install Java 1.8.0 Headless

```bash
$ yum -y install java-1.8.0-openjdk-headless
$
$ # verify
$ java -version
openjdk version "1.8.0_191"
OpenJDK Runtime Environment (build 1.8.0_191-b12)
OpenJDK 64-Bit Server VM (build 25.191-b12, mixed mode)
```

### 3. Deploy app as a service

Copy the `target/*.jar` file into `/srv/gateway` folder.

Symlink the file to `gateway.jar`

```bash
$ ln -sf gateway-1.0.0-RELEASE.jar gateway.jar
```

Create service file for the app.

`/etc/systemd/system/gateway.service`

```
[Unit]
Description=Gateway Application
After=network-online.target mongod.service consul.service minio.service

[Service]
Environment=SPRING_PROFILES_ACTIVE=prod,swagger,deployment
WorkingDirectory=/srv/gateway
ExecStart=/usr/bin/java -jar /srv/gateway/gateway.jar
SuccessExitStatus=143
Restart=always

[Install]
WantedBy=multi-user.target
```

Reload systemctl and start the app

```bash
$ systemctl daemon-reload
$ systemctl start gateway
```

### 4 - Configure HTTP / HTTPS

By default, app run on standard Tomcat port `8080` and connect to a `gateway` MongoDB on `localhost:27017`.

This behavior can be configurable via `/srv/smartvp/config/application-deployment.yml` file:

```yaml
# Configure Database
spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017
      database: gateway_deployment
# Configure HTTP port
server:
  port: 8080
  compression:
    enabled: true
    mime-types: text/html,text/xml,text/plain,text/css, application/javascript, application/json
    min-response-size: 1024
  # Optional - configure HTTPS
  # REMOVE IF RUN ONLY HTTP
  # The certificate can be generate using following command
  # openssl pkcs12 -export -in fullchain.pem -inkey privkey.pem -out keystore.p12 -name my.domain -CAfile chain.pem -caname root
  # Export Password: my.password
  # Verify Export Password: my.password
  ssl:
    key-store: /etc/letsencrypt/live/my.domain/keystore.p12
    key-store-password: my.password
    key-store-type: PKCS12
    key-alias: my.domain
    ciphers: TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA,TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA,TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384 ,TLS_DHE_RSA_WITH_AES_128_GCM_SHA256 ,TLS_DHE_RSA_WITH_AES_256_GCM_SHA384 ,TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256,TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384,TLS_DHE_RSA_WITH_AES_128_CBC_SHA256,TLS_DHE_RSA_WITH_AES_128_CBC_SHA,TLS_DHE_RSA_WITH_AES_256_CBC_SHA256,TLS_DHE_RSA_WITH_AES_256_CBC_SHA,TLS_RSA_WITH_AES_128_GCM_SHA256,TLS_RSA_WITH_AES_256_GCM_SHA384,TLS_RSA_WITH_AES_128_CBC_SHA256,TLS_RSA_WITH_AES_256_CBC_SHA256,TLS_RSA_WITH_AES_128_CBC_SHA,TLS_RSA_WITH_AES_256_CBC_SHA,TLS_DHE_RSA_WITH_CAMELLIA_256_CBC_SHA,TLS_RSA_WITH_CAMELLIA_256_CBC_SHA,TLS_DHE_RSA_WITH_CAMELLIA_128_CBC_SHA,TLS_RSA_WITH_CAMELLIA_128_CBC_SHA
```

### 5 - Reverse Proxy via NGINX

Using nginx we can separate Backend and Frontend code using following configuration:

```nginx
server {
    listen       80 default_server;

    #access_log  /var/log/nginx/host.access.log  main;
    error_log /var/log/nginx/error.log;

    root /app/www;

    # Forward all requests to http://IP:port/assets to /app/assets
    location /assets {
        root /tango;
        try_files $uri $uri/ =404;
    }

    # For requests contains `api`, `v2`, `services` or `management`, first try to see if it a file, second forward it to backend
    location ~ (api|services|management|v2) {
        try_files $uri $uri/ @api;
    }

    # For other requests, first try to see if it a file, else open `index.html`
    location / {
	     try_files $uri $uri/ /index.html;
    }

    # Backend API
    location @api {
	      proxy_pass       http://localhost:8080;
        proxy_set_header Host            $host;
        proxy_set_header X-Forwarded-For $remote_addr;
    }
}
```

## Administration Guide

### Import & Export via Swagger API

1. Open **Administrator / API**: A Swagger UI documentation will appear, allow Administrator to manage entities within the system.
2. To Export data, navigate to entity `getAll[Entity]` function. E.g `getAllCategories`, `getAllRegions`, then run the function by press `Try it out` button.
3. TO Import data, navigate to entity `import[Entity]` function. E.g `importCategories`, `importRegions`, then paste the list of entity need to import in JSON format, then press `Try It Out`.
