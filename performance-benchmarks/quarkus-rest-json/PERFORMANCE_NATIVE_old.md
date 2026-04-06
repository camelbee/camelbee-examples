

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

and in docker-compose-native: updated CPU as 2

    deploy:
      resources:
        limits:
          cpus: '2'
          memory: 256M
        reservations:
          cpus: '1'
          memory: 128M



build native executable:

./mvnw package -Dnative -Dquarkus.native.container-build=true -DskipTests

docker compose -f docker-compose-native.yml up --build -d

and then ran the k6 test script:

rest-throughput-test.js 3 times


first run:



     ✓ status is 200 or 201

     checks.........................: 100.00% ✓ 636300      ✗ 0     
     data_received..................: 953 MB  7.8 MB/s
     data_sent......................: 667 MB  5.4 MB/s
     dropped_iterations.............: 23637   192.88926/s
     http_req_blocked...............: avg=10.01µs   min=0s    med=1µs     max=66.05ms  p(90)=1µs    p(95)=2µs    
     http_req_connecting............: avg=9.01µs    min=0s    med=0s      max=61.76ms  p(90)=0s     p(95)=0s     
✓ http_req_duration..............: avg=38.25ms   min=600µs med=19.81ms max=286.72ms p(90)=81.1ms p(95)=87.65ms
{ expected_response:true }...: avg=38.25ms   min=600µs med=19.81ms max=286.72ms p(90)=81.1ms p(95)=87.65ms
http_req_failed................: 0.00%   ✓ 0           ✗ 636300
http_req_receiving.............: avg=3.77ms    min=4µs   med=837µs   max=187.8ms  p(90)=4.98ms p(95)=12.89ms
http_req_sending...............: avg=5.14µs    min=1µs   med=3µs     max=9.42ms   p(90)=6µs    p(95)=9µs    
http_req_tls_handshaking.......: avg=0s        min=0s    med=0s      max=0s       p(90)=0s     p(95)=0s     
http_req_waiting...............: avg=34.48ms   min=556µs med=17.44ms max=284.05ms p(90)=77.5ms p(95)=84.19ms
http_reqs......................: 636300  5192.513266/s
iteration_duration.............: avg=3.83s     min=2.52s med=3.81s   max=4.68s    p(90)=4.1s   p(95)=4.2s   
iterations.....................: 6363    51.925133/s
request_latency................: avg=38.311842 min=0     med=20      max=286      p(90)=81     p(95)=88     
requests_received..............: 636300  5192.513266/s
requests_sent..................: 636300  5192.513266/s
vus............................: 127     min=127       max=200
vus_max........................: 200     min=200       max=200


running (2m02.5s), 000/200 VUs, 6363 complete and 0 interrupted iterations
throughput_test ✓ [======================================] 200 VUs  2m0s  06363/30000 iters, 150 per VU

![img_4.png](docs/images/img_4.png)

second run:


     ✓ status is 200 or 201

     checks.........................: 100.00% ✓ 643500      ✗ 0     
     data_received..................: 964 MB  7.9 MB/s
     data_sent......................: 674 MB  5.5 MB/s
     dropped_iterations.............: 23565   193.193197/s
     http_req_blocked...............: avg=7.49µs    min=0s    med=1µs     max=39.07ms  p(90)=1µs     p(95)=2µs    
     http_req_connecting............: avg=6.68µs    min=0s    med=0s      max=34.01ms  p(90)=0s      p(95)=0s     
✓ http_req_duration..............: avg=37.62ms   min=671µs med=19.35ms max=269.93ms p(90)=80.35ms p(95)=86.53ms
{ expected_response:true }...: avg=37.62ms   min=671µs med=19.35ms max=269.93ms p(90)=80.35ms p(95)=86.53ms
http_req_failed................: 0.00%   ✓ 0           ✗ 643500
http_req_receiving.............: avg=3.65ms    min=4µs   med=814µs   max=168.36ms p(90)=4.69ms  p(95)=11.82ms
http_req_sending...............: avg=5.29µs    min=1µs   med=3µs     max=19.02ms  p(90)=6µs     p(95)=9µs    
http_req_tls_handshaking.......: avg=0s        min=0s    med=0s      max=0s       p(90)=0s      p(95)=0s     
http_req_waiting...............: avg=33.96ms   min=565µs med=17.23ms max=269.29ms p(90)=76.99ms p(95)=83.37ms
http_reqs......................: 643500  5275.613071/s
iteration_duration.............: avg=3.76s     min=1.97s med=3.78s   max=4.57s    p(90)=4.04s   p(95)=4.13s  
iterations.....................: 6435    52.756131/s
request_latency................: avg=37.671383 min=0     med=19      max=270      p(90)=80      p(95)=87     
requests_received..............: 643500  5275.613071/s
requests_sent..................: 643500  5275.613071/s
vus............................: 132     min=132       max=200
vus_max........................: 200     min=200       max=200


running (2m02.0s), 000/200 VUs, 6435 complete and 0 interrupted iterations
throughput_test ✓ [======================================] 200 VUs  2m0s  06435/30000 iters, 150 per VU

![img_5.png](docs/images/img_5.png)


third run:


     ✓ status is 200 or 201

     checks.........................: 100.00% ✓ 640800      ✗ 0     
     data_received..................: 960 MB  7.9 MB/s
     data_sent......................: 672 MB  5.5 MB/s
     dropped_iterations.............: 23592   193.662077/s
     http_req_blocked...............: avg=8.28µs    min=0s    med=1µs     max=41.75ms  p(90)=1µs     p(95)=2µs    
     http_req_connecting............: avg=6.65µs    min=0s    med=0s      max=35.41ms  p(90)=0s      p(95)=0s     
✓ http_req_duration..............: avg=37.77ms   min=657µs med=19.02ms max=282.45ms p(90)=81.04ms p(95)=87.37ms
{ expected_response:true }...: avg=37.77ms   min=657µs med=19.02ms max=282.45ms p(90)=81.04ms p(95)=87.37ms
http_req_failed................: 0.00%   ✓ 0           ✗ 640800
http_req_receiving.............: avg=3.73ms    min=4µs   med=821µs   max=108.34ms p(90)=4.82ms  p(95)=12.51ms
http_req_sending...............: avg=5.52µs    min=1µs   med=3µs     max=7.14ms   p(90)=6µs     p(95)=9µs    
http_req_tls_handshaking.......: avg=0s        min=0s    med=0s      max=0s       p(90)=0s      p(95)=0s     
http_req_waiting...............: avg=34.03ms   min=602µs med=17.05ms max=280.16ms p(90)=77.6ms  p(95)=83.96ms
http_reqs......................: 640800  5260.200868/s
iteration_duration.............: avg=3.78s     min=1.87s med=3.79s   max=4.48s    p(90)=4.02s   p(95)=4.11s  
iterations.....................: 6408    52.602009/s
request_latency................: avg=37.824246 min=0     med=19      max=283      p(90)=81      p(95)=87     
requests_received..............: 640800  5260.200868/s
requests_sent..................: 640800  5260.200868/s
vus............................: 138     min=138       max=200
vus_max........................: 200     min=200       max=200


running (2m01.8s), 000/200 VUs, 6408 complete and 0 interrupted iterations
throughput_test ✓ [======================================] 200 VUs  2m0s  06408/30000 iters, 150 per VU


![img_6.png](docs/images/img_6.png)


docker container stats:

![img_7.png](docs/images/img_7.png)