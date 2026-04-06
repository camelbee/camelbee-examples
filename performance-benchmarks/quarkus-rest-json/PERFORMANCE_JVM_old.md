

NOTE after creating the micreoservoce update the application yaml like below, set all the interceptors to false

camelbee:
# when enabled registers the CamelBee event notifier to the Camel context
notifier-enabled: false
# when enabled configures stream caching, MDC logging and CamelBeeUnitOfWork for routes
route-configurer-enabled: false
# when enabled it allows the CamelBe WebGL application to fetch the topology of the Camel Context.
context-enabled: false
# when enabled intercepts/traces request and responses of all camel components and caches messages.
tracer-enabled: false
# maximum time the tracer can remain idle before deactivation tracing of messages.
tracer-max-idle-time: 60000
# maximum collected trace messages
tracer-max-messages-count: 10000
# when enabled it logs the messages exchanged between endpoints
logging-enabled: false

and in docker compoes: updated CPU as 2

    deploy:
      resources:
        limits:
          cpus: '2'
          memory: 1G
        reservations:
          cpus: '1'
          memory: 1G


chmod +x mvnw

./mvnw package -DskipTests

docker compose up --build -d

and then ran the
k6 run grpc-ttt.js  2 time to get the jvm warmed up the third run cloocedted the results:


first run:

rest

✓ status is 200 or 201

     checks.........................: 100.00% ✓ 809200      ✗ 0     
     data_received..................: 1.2 GB  10 MB/s
     data_sent......................: 848 MB  7.0 MB/s
     dropped_iterations.............: 21908   180.638056/s
     http_req_blocked...............: avg=5.96µs    min=0s    med=1µs     max=33.63ms p(90)=1µs     p(95)=2µs    
     http_req_connecting............: avg=5.06µs    min=0s    med=0s      max=33.37ms p(90)=0s      p(95)=0s     
✓ http_req_duration..............: avg=29.77ms   min=521µs med=13.09ms max=1.88s   p(90)=71.6ms  p(95)=83.73ms
{ expected_response:true }...: avg=29.77ms   min=521µs med=13.09ms max=1.88s   p(90)=71.6ms  p(95)=83.73ms
http_req_failed................: 0.00%   ✓ 0           ✗ 809200
http_req_receiving.............: avg=2.55ms    min=3µs   med=648µs   max=382.4ms p(90)=3.45ms  p(95)=5.95ms
http_req_sending...............: avg=5.93µs    min=1µs   med=3µs     max=23.57ms p(90)=6µs     p(95)=10µs   
http_req_tls_handshaking.......: avg=0s        min=0s    med=0s      max=0s      p(90)=0s      p(95)=0s     
http_req_waiting...............: avg=27.21ms   min=423µs med=11.85ms max=1.67s   p(90)=68.82ms p(95)=80.23ms
http_reqs......................: 809200  6672.097627/s
iteration_duration.............: avg=2.98s     min=1.28s med=2.5s    max=15.48s  p(90)=3.09s   p(95)=4.99s  
iterations.....................: 8092    66.720976/s
request_latency................: avg=29.845326 min=0     med=13      max=1901    p(90)=72      p(95)=84     
requests_received..............: 809200  6672.097627/s
requests_sent..................: 809200  6672.097627/s
vus............................: 82      min=82        max=200
vus_max........................: 200     min=200       max=200


running (2m01.3s), 000/200 VUs, 8092 complete and 0 interrupted iterations
throughput_test ✓ [======================================] 200 VUs  2m0s  08092/30000 iters, 150 per VU


![img.png](docs/images/img.png)



second run:


     ✓ status is 200 or 201

     checks.........................: 100.00% ✓ 997200      ✗ 0     
     data_received..................: 1.5 GB  12 MB/s
     data_sent......................: 1.0 GB  8.6 MB/s
     dropped_iterations.............: 20028   164.684629/s
     http_req_blocked...............: avg=6.12µs    min=0s    med=1µs     max=73.09ms  p(90)=1µs     p(95)=2µs    
     http_req_connecting............: avg=5.03µs    min=0s    med=0s      max=68.77ms  p(90)=0s      p(95)=0s     
✓ http_req_duration..............: avg=24.22ms   min=464µs med=12.19ms max=177.41ms p(90)=65.78ms p(95)=71.03ms
{ expected_response:true }...: avg=24.22ms   min=464µs med=12.19ms max=177.41ms p(90)=65.78ms p(95)=71.03ms
http_req_failed................: 0.00%   ✓ 0           ✗ 997200
http_req_receiving.............: avg=2.06ms    min=3µs   med=608µs   max=101.8ms  p(90)=3.01ms  p(95)=4.77ms
http_req_sending...............: avg=6.99µs    min=1µs   med=3µs     max=38.71ms  p(90)=6µs     p(95)=9µs    
http_req_tls_handshaking.......: avg=0s        min=0s    med=0s      max=0s       p(90)=0s      p(95)=0s     
http_req_waiting...............: avg=22.15ms   min=408µs med=11.07ms max=176.97ms p(90)=63.39ms p(95)=68.7ms
http_reqs......................: 997200  8199.696049/s
iteration_duration.............: avg=2.43s     min=1.61s med=2.41s   max=3.05s    p(90)=2.61s   p(95)=2.68s  
iterations.....................: 9972    81.99696/s
request_latency................: avg=24.302539 min=0     med=12      max=177      p(90)=66      p(95)=71     
requests_received..............: 997200  8199.696049/s
requests_sent..................: 997200  8199.696049/s
vus............................: 159     min=159       max=200
vus_max........................: 200     min=200       max=200


running (2m01.6s), 000/200 VUs, 9972 complete and 0 interrupted iterations
throughput_test ✓ [======================================] 200 VUs  2m0s  09972/30000 iters, 150 per VU

![img_1.png](docs/images/img_1.png)


third run:


     ✓ status is 200 or 201

     checks.........................: 100.00% ✓ 1028200     ✗ 0      
     data_received..................: 1.5 GB  13 MB/s
     data_sent......................: 1.1 GB  8.9 MB/s
     dropped_iterations.............: 19718   162.250154/s
     http_req_blocked...............: avg=9.56µs    min=0s    med=1µs     max=114.66ms p(90)=1µs     p(95)=2µs    
     http_req_connecting............: avg=8.68µs    min=0s    med=0s      max=113.41ms p(90)=0s      p(95)=0s     
✓ http_req_duration..............: avg=23.43ms   min=465µs med=12.06ms max=184.83ms p(90)=63.93ms p(95)=69.31ms
{ expected_response:true }...: avg=23.43ms   min=465µs med=12.06ms max=184.83ms p(90)=63.93ms p(95)=69.31ms
http_req_failed................: 0.00%   ✓ 0           ✗ 1028200
http_req_receiving.............: avg=1.96ms    min=3µs   med=611µs   max=157.86ms p(90)=2.98ms  p(95)=4.68ms
http_req_sending...............: avg=6.88µs    min=1µs   med=3µs     max=24.37ms  p(90)=6µs     p(95)=10µs   
http_req_tls_handshaking.......: avg=0s        min=0s    med=0s      max=0s       p(90)=0s      p(95)=0s     
http_req_waiting...............: avg=21.46ms   min=366µs med=10.9ms  max=182.27ms p(90)=61.67ms p(95)=67.19ms
http_reqs......................: 1028200 8460.574521/s
iteration_duration.............: avg=2.35s     min=1.49s med=2.34s   max=3.09s    p(90)=2.51s   p(95)=2.59s  
iterations.....................: 10282   84.605745/s
request_latency................: avg=23.516471 min=0     med=12      max=185      p(90)=64      p(95)=69     
requests_received..............: 1028200 8460.574521/s
requests_sent..................: 1028200 8460.574521/s
vus............................: 107     min=107       max=200  
vus_max........................: 200     min=200       max=200


running (2m01.5s), 000/200 VUs, 10282 complete and 0 interrupted iterations
throughput_test ✓ [======================================] 200 VUs  2m0s  10282/30000 iters, 150 per VU

![img_3.png](docs/images/img_3.png)

docker contariner stats:

![img_2.png](docs/images/img_2.png)