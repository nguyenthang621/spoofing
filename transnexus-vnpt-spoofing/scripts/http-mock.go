package main

import (
	"flag"
	"io/ioutil"
	"time"
	"fmt"
	"net/http"
)

type HttpEchoParameters struct {
	Host				string
	Path				string
	Payload     string
	Status      int
	Delay				string
}

var params *HttpEchoParameters
var response string
var duration time.Duration
var hasDelay error


func mock(w http.ResponseWriter, req *http.Request) {
	duration, hasDelay := time.ParseDuration(params.Delay)
	if hasDelay == nil {
		time.Sleep(duration)
	}
	http.Error(w, response, params.Status)
}

func main() {
	params = ParseOpts()
	dat, err := ioutil.ReadFile(params.Payload)
	if err != nil {
      response = "OK"
  } else {
		response = string(dat)
	}
	http.HandleFunc(params.Path, mock)
  http.ListenAndServe(params.Host, nil)
}



func ParseOpts() *HttpEchoParameters {
	o := &HttpEchoParameters{Host: ":8080", Path: "/", Status: 200}
	flag.StringVar(&o.Host, "http", o.Host, "The HTTP listening address")
	flag.StringVar(&o.Path, "path", o.Path, "The listening path")
	flag.StringVar(&o.Payload, "payload", o.Payload, "The file with response paylaod to return to client")
	flag.IntVar(&o.Status, "status", 200, "Http Status Code")
	flag.StringVar(&o.Delay, "delay", o.Delay, "Delay duration, e.g 1s")
	flag.Usage = func() {
		fmt.Printf("Options:\n")
		flag.PrintDefaults()
	}
	flag.Parse()
	return o
}
