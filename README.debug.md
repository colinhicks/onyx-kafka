
[//]: # ({:display :header, :valid-structure? true, :all-params ({:display :catalog-entry, :model :onyx.plugin.kafka/read-messages, :width 80, :merge-additions {:onyx/plugin :onyx.plugin.kafka/read-messages, :onyx/medium :kafka, :kafka/force-reset? true, :onyx/type :input, :onyx/name :read-messages, :onyx/max-peers <<number-of-partitions>>, :onyx/min-peers <<number-of-partitions>>, :kafka/consumer-opts :gen-doc-ignore, :onyx/doc "Reads messages from a Kafka topic", :kafka/start-offsets {p1 offset1, p2 offset2}, :onyx/batch-size 100}} {:display :lifecycle-entry, :model :onyx.plugin.kafka/read-messages} {:display :attribute-table, :model :onyx.plugin.kafka/read-messages, :columns :columns/default} {:display :catalog-entry, :model :onyx.plugin.kafka/write-messages, :width 200, :merge-additions {:kafka/zookeeper "127.0.0.1:2181", :onyx/plugin :onyx.plugin.kafka/write-messages, :onyx/medium :kafka, :kafka/request-size 307200, :onyx/type :output, :onyx/name :write-messages, :kafka/topic "topic", :kafka/serializer-fn :my.ns/serializer-fn, :onyx/doc "Writes messages to a Kafka topic", :onyx/batch-size batch-size}} {:display :lifecycle-entry, :model :onyx.plugin.kafka/write-messages} {:display :attribute-table, :model :onyx.plugin.kafka/write-messages, :columns :columns/default})})
## onyx-kafka

Onyx plugin providing read and write facilities for Kafka. This plugin automatically discovers broker locations from ZooKeeper and updates the consumers when there is a broker failover.

This plugin version is *only compatible with Kafka 0.9+*. Please use [onyx-kafka-0.8](https://github.com/onyx-platform/onyx-kafka-0.8) with Kafka 0.8.

#### Installation

In your project file:

```clojure
[org.onyxplatform/onyx-kafka "0.9.10.1"]
```

In your peer boot-up namespace:

```clojure
(:require [onyx.plugin.kafka])
```

#### Functions

##### read-messages

Reads segments from a Kafka topic. Peers will automatically be assigned to each
of the topics partitions, unless `:kafka/partition` is supplied in which case
only one partition will be read from. `:onyx/min-peers` and `:onyx/max-peers`
must be used to fix the number of the peers for the task to the number of
partitions read by the task.

NOTE: The `:done` sentinel (i.e. batch processing) is not supported if more
than one partition is auto-assigned i.e. the topic has more than one partition
and `:kafka/partition` is not fixed. An exception will be thrown if a `:done`
is read under this circumstance.

Catalog entry:


[//]: # ({:display :catalog-entry, :model :onyx.plugin.kafka/read-messages, :width 80, :merge-additions {:onyx/plugin :onyx.plugin.kafka/read-messages, :onyx/medium :kafka, :kafka/force-reset? true, :onyx/type :input, :onyx/name :read-messages, :onyx/max-peers <<number-of-partitions>>, :onyx/min-peers <<number-of-partitions>>, :kafka/consumer-opts :gen-doc-ignore, :onyx/doc "Reads messages from a Kafka topic", :kafka/start-offsets {p1 offset1, p2 offset2}, :onyx/batch-size 100}})
```clojure
{:onyx/name :read-messages,
 :onyx/plugin :onyx.plugin.kafka/read-messages,
 :onyx/type :input,
 :onyx/medium :kafka,
 :kafka/topic "The topic name to read from.",
 :kafka/partition "Partition to read from if auto-assignment is not used.",
 :kafka/group-id "The consumer identity to store in ZooKeeper.",
 :kafka/zookeeper "The ZooKeeper connection string.",
 :kafka/offset-reset :earliest,
 :kafka/force-reset? true,
 :kafka/deserializer-fn :my.ns/deserializer-fn,
 :kafka/receive-buffer-bytes 65536,
 :kafka/commit-interval 2000,
 :kafka/wrap-with-metadata? false,
 :kafka/start-offsets {p1 offset1, p2 offset2},
 :onyx/min-peers <<number-of-partitions>>,
 :onyx/max-peers <<number-of-partitions>>,
 :onyx/batch-size 100,
 :onyx/doc "Reads messages from a Kafka topic"}
```

Lifecycle entry:


[//]: # ({:display :lifecycle-entry, :model :onyx.plugin.kafka/read-messages})
```clojure
[{:task.lifecycle/name :read-messages,
  :lifecycle/calls :onyx.plugin.kafka/read-messages-calls}]
```

###### Attributes


[//]: # ({:display :attribute-table, :model :onyx.plugin.kafka/read-messages, :columns :columns/default})

| Parameter                     | Type       | Optional? | Default  | Description                                                                                                                                                                                                                                                                                                                                                      |
|------------------------------ | ---------- | --------- | -------- | -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `:kafka/topic`                | `:string`  |           |          | The topic name to read from.                                                                                                                                                                                                                                                                                                                                     |
| `:kafka/partition`            | `:string`  | `true`    |          | Partition to read from if auto-assignment is not used.                                                                                                                                                                                                                                                                                                           |
| `:kafka/group-id`             | `:string`  |           |          | The consumer identity to store in ZooKeeper.                                                                                                                                                                                                                                                                                                                     |
| `:kafka/zookeeper`            | `:string`  |           |          | The ZooKeeper connection string.                                                                                                                                                                                                                                                                                                                                 |
| `:kafka/offset-reset`         | `:keyword` |           |          | Offset bound to seek to when not found - `:earliest` or `:latest`.                                                                                                                                                                                                                                                                                               |
| `:kafka/force-reset?`         | `:boolean` |           |          | Force to read from the beginning or end of the log, as specified by `:kafka/offset-reset`. If false, reads from the last acknowledged messsage if it exists.                                                                                                                                                                                                     |
| `:kafka/deserializer-fn`      | `:keyword` |           |          | A keyword that represents a fully qualified namespaced function to deserialize a message. Takes one argument, which must be a byte array.                                                                                                                                                                                                                        |
| `:kafka/receive-buffer-bytes` | `:long`    | `true`    | `65536`  | The size in the receive buffer in the Kafka consumer.                                                                                                                                                                                                                                                                                                            |
| `:kafka/commit-interval`      | `:long`    | `true`    | `2000`   | The interval in milliseconds to commit the latest acknowledged offset to ZooKeeper.                                                                                                                                                                                                                                                                              |
| `:kafka/wrap-with-metadata?`  | `:boolean` | `true`    | `false`  | Wraps message into map with keys `:offset`, `:partitions`, `:topic` and `:message` itself.                                                                                                                                                                                                                                                                       |
| `:kafka/start-offsets`        | `:map`     | `true`    |          | Allows a task to be supplied with the starting offsets for all partitions. Maps partition to offset, e.g. `{0 50, 1, 90}` will start at offset 50 for partition 0, and offset 90 for partition 1.                                                                                                                                                                |
| `:kafka/consumer-opts`        | `:map`     | `true`    |          | A map of arbitrary configuration to merge into the underlying Kafka consumer base configuration. Map should contain keywords as keys, and the valid values described in the [Kafka Docs](http://kafka.apache.org/documentation.html#newconsumerconfigs). Please note that key values such as `fetch.min.bytes` must be in keyword form, i.e. `:fetch.min.bytes`. |
| `:kafka/empty-read-back-off`  | `:long`    | `true`    | `500`    | The amount of time to back off between reads when nothing was fetched from a consumer.                                                                                                                                                                                                                                                                           |
| `:kafka/fetch-size`           | `:long`    | `true`    | `307200` | The size in bytes to request from ZooKeeper per fetch request.                                                                                                                                                                                                                                                                                                   |
| `:kafka/chan-capacity`        | `:long`    | `true`    | `1000`   | The buffer size of the Kafka reading channel.                                                                                                                                                                                                                                                                                                                    |


##### write-messages

Writes segments to a Kafka topic using the Kafka "new" producer.

Catalog entry:


[//]: # ({:display :catalog-entry, :model :onyx.plugin.kafka/write-messages, :width 200, :merge-additions {:kafka/zookeeper "127.0.0.1:2181", :onyx/plugin :onyx.plugin.kafka/write-messages, :onyx/medium :kafka, :kafka/request-size 307200, :onyx/type :output, :onyx/name :write-messages, :kafka/topic "topic", :kafka/serializer-fn :my.ns/serializer-fn, :onyx/doc "Writes messages to a Kafka topic", :onyx/batch-size batch-size}})
```clojure
{:onyx/name :write-messages,
 :onyx/plugin :onyx.plugin.kafka/write-messages,
 :onyx/type :output,
 :onyx/medium :kafka,
 :kafka/topic "topic",
 :kafka/zookeeper "127.0.0.1:2181",
 :kafka/partition "Partition to write to, if you do not wish messages to be auto allocated to partitions. Must either be supplied in the task map, or all messages should contain a `:partition` key.",
 :kafka/serializer-fn :my.ns/serializer-fn,
 :kafka/request-size 307200,
 :kafka/no-seal? false,
 :kafka/producer-opts :gen-doc-please-handle-in-merge-additions,
 :onyx/batch-size batch-size,
 :onyx/doc "Writes messages to a Kafka topic"}
```

Lifecycle entry:


[//]: # ({:display :lifecycle-entry, :model :onyx.plugin.kafka/write-messages})
```clojure
[{:task.lifecycle/name :write-messages,
  :lifecycle/calls :onyx.plugin.kafka/write-messages-calls}]
```

Segments supplied to a `:onyx.plugin.kafka/write-messages` task should be in in
the following form: `{:message message-body}` with optional partition, topic and
key values.

```clojure
{:message message-body
 :key optional-key
 :partition optional-partition
 :topic optional-topic}
```

###### Attributes


[//]: # ({:display :attribute-table, :model :onyx.plugin.kafka/write-messages, :columns :columns/default})

| Parameter              | Type       | Optional? | Default | Description                                                                                                                                                                                                                                                                                                                                               |
|----------------------- | ---------- | --------- | ------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `:kafka/topic`         | `:string`  | `true`    |         | The topic name to write to. Must either be supplied or otherwise all messages must contain a `:topic` key                                                                                                                                                                                                                                                 |
| `:kafka/zookeeper`     | `:string`  |           |         | The ZooKeeper connection string.                                                                                                                                                                                                                                                                                                                          |
| `:kafka/partition`     | `:string`  | `true`    |         | Partition to write to, if you do not wish messages to be auto allocated to partitions. Must either be supplied in the task map, or all messages should contain a `:partition` key.                                                                                                                                                                        |
| `:kafka/serializer-fn` | `:keyword` |           |         | A keyword that represents a fully qualified namespaced function to serialize a message. Takes one argument - the segment.                                                                                                                                                                                                                                 |
| `:kafka/request-size`  | `:long`    | `true`    |         | The maximum size of request messages.  Maps to the `max.request.size` value of the internal kafka producer.                                                                                                                                                                                                                                               |
| `:kafka/no-seal?`      | `:boolean` | `true`    | `false` | Do not write :done to the topic when task receives the sentinel signal (end of batch job).                                                                                                                                                                                                                                                                |
| `:kafka/producer-opts` | `:map`     | `true`    |         | A map of arbitrary configuration to merge into the underlying Kafka producer base configuration. Map should contain keywords as keys, and the valid values described in the [Kafka Docs](http://kafka.apache.org/documentation.html#producerconfigs). Please note that key values such as `buffer.memory` must be in keyword form, i.e. `:buffer.memory`. |


#### Test Utilities

A take-segments utility function is provided for use when testing the results
of jobs with kafka output tasks. take-segments reads from a topic until a :done
is reached, and then returns the results. Note, if a `:done` is never written to a
topic, this will hang forever as there is no timeout.

```clojure
(ns your-ns.a-test
  (:require [onyx.kafka.utils :as kpu]))

;; insert code to run a job here

;; retrieve the segments on the topic
(def results
  (kpu/take-segments (:zookeeper/addr peer-config) "yourtopic" your-decompress-fn))

(last results)
; :done

```

#### Embedded Kafka Server

An embedded Kafka server is included for use in test cases where jobs output to
kafka output tasks. Note, stopping the server will *not* perform a [graceful shutdown](http://kafka.apache.org/documentation.html#basic_ops_restarting) -
please do not use this embedded server for anything other than tests.

This can be used like so:

```clojure
(ns your-ns.a-test
  (:require [onyx.kafka.embedded-server :as ke]
            [com.stuartsierra.component :as component]))

(def kafka-server
  (component/start
    (ke/map->EmbeddedKafka {:hostname "127.0.0.1"
                            :port 9092
                            :broker-id 0
			    :num-partitions 1
			    ; optional log dir name - randomized dir will be created if none is supplied
			    ; :log-dir "/tmp/embedded-kafka"
			    :zookeeper-addr "127.0.0.1:2188"})))

;; insert code to run a test here

;; stop the embedded server
(component/stop kafka-server)

```

#### Development

To benchmark, start a real ZooKeeper instance (at 127.0.0.1:2181) and Kafka instance, and run the following benchmarks.

Write perf, single peer writer:
```
TIMBRE_LOG_LEVEL=info lein test onyx.plugin.output-bench-test :benchmark
```

Read perf, single peer reader:
```
TIMBRE_LOG_LEVEL=info lein test onyx.plugin.input-benchmark-test :benchmark
```

Past results are maintained in `dev-resources/benchmarking/results.txt`.

#### Contributing

Pull requests into the master branch are welcomed.

#### License

Copyright © 2015 Michael Drogalis

Distributed under the Eclipse Public License, the same as Clojure.