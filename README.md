
### Config Map
```yaml
kind: ConfigMap
apiVersion: v1
metadata:
  name: redis-config
  namespace: redisdemo
data:
  REDIS_CONNECTION_CONFIG: ACL/PASSWORDONLY/''
  REDIS_HOST: redis-18421.redis.quasys.com.tr
  REDIS_PASS: Password
  REDIS_PORT: '18421'
  REDIS_PREFIX: 'TEST:'
  REDIS_USERNAME: testuser1
  REDIS_USE_PREFIX: 'true'
  REDIS_WRITE_PERIOD: '100'
  REDIS_THREAD_COUNT: '10'
```

### Deployment
```yaml
kind: Deployment
apiVersion: apps/v1
metadata:
  name: redisapp
  namespace: redisdemo
spec:
  replicas: 1
  selector:
    matchLabels:
      app: redisapp
  template:
    metadata:
      creationTimestamp: null
      labels:
        app: redisapp
    spec:
      containers:
        - name: container
          image: 'quasys/redisdemo:1.0.7'
          ports:
            - containerPort: 8080
              protocol: TCP
          envFrom:
            - configMapRef:
                name: redis-config
          resources: {}
          terminationMessagePath: /dev/termination-log
          terminationMessagePolicy: File
          imagePullPolicy: IfNotPresent
      restartPolicy: Always
      terminationGracePeriodSeconds: 30
      dnsPolicy: ClusterFirst
      securityContext: {}
      schedulerName: default-scheduler
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxUnavailable: 25%
      maxSurge: 25%
  revisionHistoryLimit: 10
  progressDeadlineSeconds: 600
```
### Komutlar
```shell
# Redis Acl yarat
+@all ~TEST:*


# Add Data

curl --location 'http://localhost:8080/api' \
--header 'Content-Type: application/json' \
--data '{
    "key": "test2",
    "value": "value2"
}'

# Read data

curl --location --request GET 'http://localhost:8080/api'



# Read All Data

curl --location --request GET 'http://localhost:8080/api/getAllData'



# Start writing time

curl --location --request GET 'http://localhost:8080/api/start'

# Stop Writing time 
curl --location --request GET 'http://localhost:8080/api/stop'

# ReadTime
curl --location --request GET 'http://localhost:8080/api/readTime'

curl -w "dns_resolution: %{time_namelookup}, tcp_established: %{time_connect}, ssl_handshake_done: %{time_appconnect}, TTFB: %{time_starttransfer}\n" -o /dev/null -s "http://localhost:8080/api/readTime"

curl --location --request GET 'http://localhost:8080/api/readTime' -w "\nResponse Time: %{time_total} seconds\n"
```
