# HTTP Mock

Simulate the behavior of HTTP endpoint

## Usage

1. Create the payload . Example, a file with following content:

__`response-payload.json`__


```json
{"errorCode":"1","error":"UNKNOWN","remoteGT":"84900406"}
```

2. Run the application

```bash
$ ./http-mock --help
Options:
  -delay string
    	Delay duration, e.g 1s
  -http string
    	The HTTP listening address (default ":8080")
  -path string
    	The listening path (default "/")
  -payload string
    	The file with response paylaod to return to client
  -status int
    	Http Status Code (default 200)

$ # Example usage
$ ./http-mock -http :8089 -payload ./response-payload.json -status 510 -path /api/public/sms-server/perform-sri-sm -delay 1s
```

3. Test the app by curl

```bash
$  curl -vv 'http://localhost:8089/api/public/sms-server/perform-sri-sm?msisdn=84900153149'
*   Trying ::1:8089...
* Connected to localhost (::1) port 8089 (#0)
> GET /api/public/sms-server/perform-sri-sm?msisdn=84900153149 HTTP/1.1
> Host: localhost:8089
> User-Agent: curl/7.69.1
> Accept: */*
> 
* Mark bundle as not supporting multiuse
< HTTP/1.1 510 Not Extended
< Content-Type: text/plain; charset=UTF-8
< Date: Sat, 29 Aug 2020 08:46:39 GMT
< Content-Length: 58
< 
{"errorCode":"1","error":"UNKNOWN","remoteGT":"84900406"}

```



