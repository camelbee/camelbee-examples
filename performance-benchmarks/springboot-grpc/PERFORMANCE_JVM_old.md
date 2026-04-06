

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

and then ran the  grpc-throughput-test.js 3 times to get the jvm warmed up.


first run:

grpc % k6 run grpc-throughput-test.js


     ✓ status is OK

     █ teardown

     checks...............: 100.00% ✓ 1127900     ✗ 0    
     data_received........: 508 MB  4.2 MB/s
     data_sent............: 505 MB  4.2 MB/s
     dropped_iterations...: 18721   154.771175/s
✓ grpc_req_duration....: avg=21.12ms   min=372.87µs med=14.82ms max=695.68ms p(90)=43.47ms p(95)=55.43ms
iteration_duration...: avg=2.13s     min=7.83µs   med=1.97s   max=8.69s    p(90)=2.23s   p(95)=2.62s  
iterations...........: 11279   93.246305/s
request_latency......: avg=21.351064 min=0        med=15      max=696      p(90)=44      p(95)=56     
requests_received....: 1127900 9324.630543/s
requests_sent........: 1127900 9324.630543/s
vus..................: 200     min=200       max=200
vus_max..............: 200     min=200       max=200


running (2m01.0s), 000/200 VUs, 11279 complete and 0 interrupted iterations
throughput_test ✓ [======================================] 200 VUs  2m0s  11279/30000 iters, 150 per VU

![img_1.png](docs/images/img_1.png)


second run:


     ✓ status is OK

     █ teardown

     checks...............: 100.00% ✓ 1205100     ✗ 0    
     data_received........: 543 MB  4.5 MB/s
     data_sent............: 539 MB  4.5 MB/s
     dropped_iterations...: 17949   148.435672/s
✓ grpc_req_duration....: avg=19.73ms   min=406.25µs med=14.93ms max=179.14ms p(90)=40.54ms p(95)=49.47ms
iteration_duration...: avg=2s        min=8.12µs   med=1.98s   max=2.6s     p(90)=2.2s    p(95)=2.26s  
iterations...........: 12051   99.660053/s
request_latency......: avg=19.984954 min=0        med=15      max=180      p(90)=41      p(95)=50     
requests_received....: 1205100 9966.005262/s
requests_sent........: 1205100 9966.005262/s
vus..................: 200     min=200       max=200
vus_max..............: 200     min=200       max=200


running (2m00.9s), 000/200 VUs, 12051 complete and 0 interrupted iterations
throughput_test ✓ [======================================] 200 VUs  2m0s  12051/30000 iters, 150 per VU

![img_2.png](docs/images/img_2.png)



third run:



     ✓ status is OK

     █ teardown

     checks...............: 100.00% ✓ 1236400      ✗ 0    
     data_received........: 557 MB  4.6 MB/s
     data_sent............: 553 MB  4.6 MB/s
     dropped_iterations...: 17636   145.604313/s
✓ grpc_req_duration....: avg=19.31ms   min=445.95µs med=14.57ms max=291.21ms p(90)=39.84ms p(95)=48.64ms
iteration_duration...: avg=1.95s     min=8.7µs    med=1.95s   max=2.49s    p(90)=2.12s   p(95)=2.17s  
iterations...........: 12364   102.078233/s
request_latency......: avg=19.526816 min=0        med=15      max=291      p(90)=40      p(95)=49     
requests_received....: 1236400 10207.823324/s
requests_sent........: 1236400 10207.823324/s
vus..................: 87      min=87         max=200
vus_max..............: 200     min=200        max=200


running (2m01.1s), 000/200 VUs, 12364 complete and 0 interrupted iterations
throughput_test ✓ [======================================] 200 VUs  2m0s  12364/30000 iters, 150 per VU


![img_3.png](docs/images/img_3.png)



docker container stats:

![img_4.png](docs/images/img_4.png)