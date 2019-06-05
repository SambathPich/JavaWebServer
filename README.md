# Java Web Server
This is a sample web server implementation using Java

Keys:
* HTTP Protocol
* Client-Server Programming
* Socket Prograamming

How it works:
* Start TCP port and listen to the port
* Accept incoming HTTP request
* Log request
* Spawn a thread to process the request

Log contains:
* remote hostname
* time requested
* first line of request
* status code of the request
* number of bytes sent
* User-agent header sent by the client

Supported features:
* Get
* Conditional Get
* Persistent connection
* Device detection
* Cookie
