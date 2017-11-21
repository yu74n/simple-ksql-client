# simple-ksql-client

## Preparing
1. Download ksql repository
    ```
    git clone https://github.com/confluentinc/ksql.git
    ```
1. Set your Kafka bootstrap server in ksqlserver.properties file
1. Launch ksql-rest-app locally
    ```
    bin/ksql-server-start config/ksqlserver.properties
    ```

## How to build
```
sbt assembly
```

## Usage
```
java -jar target/scala-2.12/sbt-client-assembly-1.0.jar "SELECT pageid FROM pageviews_original LIMIT 3;"
```
