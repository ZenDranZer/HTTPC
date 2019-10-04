httpc is a curl-like application but supports HTTP protocol only.

Usage:

httpc [get/post/help] URL [-v] [-h key:value] [-d dataWithoutSpace] [-f fileName] [-o fileName]
                    get     executes a HTTP GET request and prints the response.
                    post    executes a HTTP POST request and prints the response.
                    help    prints this screen.

GET:

usage: httpc get URL [-v] [-h key:value].
Get executes a HTTP GET request for a given URL.
-v\tPrints the detail of the response such as protocol, status, and headers.
-h key:value\tAssociates headers to HTTP Request with the format 'key:value'.

POST:

usage: httpc post URL [-v] [-h key:value] [-d inline-data] [-f file].
Post executes a HTTP POST request for a given URL with inline data or from file.
-v Prints the detail of the response such as protocol, status, and headers.
-h key:value\tAssociates headers to HTTP Request with the format 'key:value'.
-d string\tAssociates an inline data to the body HTTP POST request.
-f file\tAssociates the content of a file to the body HTTP POST request.
Either [-d] or [-f] can be used but not both.

Commands:

Help:
    httpc help
    httpc help get
    httpc help post

Get:
    httpc get http://httpbin.org/get
    httpc get http://httpbin.org/get?course=networking&assignment=1 -v -h 123:123
    httpc get http://httpbin.org/get?course=networking&assignment=1 -v -h 123:123 -o output.txt

Post:
    httpc post http://httpbin.org/post
    httpc post http://httpbin.org/post -v -h 123:123 -d 456
    httpc post http://httpbin.org/post -v -h 123:123 -f input.txt -o output.txt
    httpc post http://httpbin.org/post -v -h 123:123 -d 456 -f input.txt

Redirect:
    httpc get  http://httpbin.org/redirect/n -v -h 123:123
    httpc get  http://httpbin.org/absolute-redirect/n -v -h 123:123
    httpc get  http://httpbin.org/redirect-to?url=http://httpbin.org/get -v -h 123:123
    httpc post http://httpbin.org/redirect-to?url=http://httpbin.org/post -v -h 123:123 -d 456
    httpc post http://httpbin.org/redirect-to?url=http://httpbin.org/post -v -h 123:123 -f input.txt -o output.txt