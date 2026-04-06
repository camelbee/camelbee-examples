

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

grpc-throughput-test.js 23 times for jmv to warm up


first run:

grpc % k6 run grpc-throughput-test.js



     ✓ status is OK

     █ teardown

     checks...............: 100.00% ✓ 742000      ✗ 0    
     data_received........: 335 MB  2.7 MB/s
     data_sent............: 332 MB  2.7 MB/s
     dropped_iterations...: 22580   185.61488/s
✓ grpc_req_duration....: avg=32.38ms   min=368.83µs med=17.96ms max=313.96ms p(90)=74.17ms p(95)=82.51ms
iteration_duration...: avg=3.26s     min=7.79µs   med=3.27s   max=4.06s    p(90)=3.54s   p(95)=3.61s  
iterations...........: 7420    60.994792/s
request_latency......: avg=32.617605 min=0        med=18      max=314      p(90)=74      p(95)=83     
requests_received....: 742000  6099.479218/s
requests_sent........: 742000  6099.479218/s
vus..................: 134     min=134       max=200
vus_max..............: 200     min=200       max=200


running (2m01.6s), 000/200 VUs, 7420 complete and 0 interrupted iterations
throughput_test ✓ [======================================] 200 VUs  2m0s  07420/30000 iters, 150 per VU

![img_4.png](docs/images/img_4.png)


second run:


     ✓ status is OK

     █ teardown

     checks...............: 100.00% ✓ 738300      ✗ 0    
     data_received........: 333 MB  2.7 MB/s
     data_sent............: 330 MB  2.7 MB/s
     dropped_iterations...: 22617   185.673036/s
✓ grpc_req_duration....: avg=32.54ms   min=415.08µs med=18.15ms max=302.91ms p(90)=74.22ms p(95)=82.8ms
iteration_duration...: avg=3.28s     min=8.45µs   med=3.28s   max=4.23s    p(90)=3.58s   p(95)=3.68s
iterations...........: 7383    60.610338/s
request_latency......: avg=32.814529 min=0        med=18      max=303      p(90)=74      p(95)=83    
requests_received....: 738300  6061.033847/s
requests_sent........: 738300  6061.033847/s
vus..................: 142     min=142       max=200
vus_max..............: 200     min=200       max=200


running (2m01.8s), 000/200 VUs, 7383 complete and 0 interrupted iterations
throughput_test ✓ [======================================] 200 VUs  2m0s  07383/30000 iters, 150 per VU



![img_5.png](docs/images/img_5.png)


third run:


     ✓ status is OK

     █ teardown

     checks...............: 100.00% ✓ 761500      ✗ 0    
     data_received........: 343 MB  2.8 MB/s
     data_sent............: 341 MB  2.8 MB/s
     dropped_iterations...: 22385   183.589014/s
✓ grpc_req_duration....: avg=31.6ms    min=421.91µs med=17.4ms max=368.91ms p(90)=73.11ms p(95)=81.67ms
iteration_duration...: avg=3.18s     min=8.12µs   med=3.19s  max=3.99s    p(90)=3.48s   p(95)=3.55s  
iterations...........: 7615    62.453891/s
request_latency......: avg=31.851404 min=0        med=18     max=369      p(90)=73      p(95)=82     
requests_received....: 761500  6245.389053/s
requests_sent........: 761500  6245.389053/s
vus..................: 149     min=149       max=200
vus_max..............: 200     min=200       max=200


running (2m01.9s), 000/200 VUs, 7615 complete and 0 interrupted iterations
throughput_test ✓ [======================================] 200 VUs  2m0s  07615/30000 iters, 150 per VU

![img_6.png](docs/images/img_6.png)

full docker stats:

![img_7.png](docs/images/img_7.png)