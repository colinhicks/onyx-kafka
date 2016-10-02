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

::::::::::::::::::::::::::::::::::::::::::::::::::
:display `:catalog-entry`
:model :onyx.plugin.kafka/read-messages
:width 80
:merge-additions
```clojure
{:onyx/name :read-messages
 :onyx/plugin :onyx.plugin.kafka/read-messages
 :onyx/type :input
 :onyx/medium :kafka
 :kafka/topic "my topic"
 :kafka/group-id "onyx-consumer"
 :kafka/receive-buffer-bytes 65536
 :kafka/zookeeper "127.0.0.1:2181"
 :kafka/offset-reset :smallest
 :kafka/force-reset? true
 :kafka/commit-interval 500
 :kafka/deserializer-fn :my.ns/deserializer-fn
 :kafka/wrap-with-metadata? false
 :onyx/batch-timeout 50
 :onyx/min-peers "<<NUMBER-OF-PARTITIONS>>"
 :onyx/max-peers "<<NUMBER-OF-PARTITIONS>>"
 :onyx/batch-size 100
 :onyx/doc "Reads messages from a Kafka topic"}
```
::::::::::::::::::::::::::::::::::::::::::::::::::

Lifecycle entry:

::::::::::::::::::::::::::::::::::::::::::::::::::
:display :lifecycle-entry
:model :onyx.plugin.kafka/read-messages
::::::::::::::::::::::::::::::::::::::::::::::::::


###### Attributes

::::::::::::::::::::::::::::::::::::::::::::::::::
:display :attribute-table
:model :onyx.plugin.kafka/read-messages
:columns :columns/default
::::::::::::::::::::::::::::::::::::::::::::::::::

##### write-messages

Writes segments to a Kafka topic using the Kafka "new" producer.

Catalog entry:

::::::::::::::::::::::::::::::::::::::::::::::::::
:display :catalog-entry
:model :onyx.plugin.kafka/write-messages
:width 200
:merge-additions
```clojure
{:onyx/name :write-messages
 :onyx/plugin :onyx.plugin.kafka/write-messages
 :onyx/type :output
 :onyx/medium :kafka
 :kafka/topic "topic"
 :kafka/zookeeper "127.0.0.1:2181"
 :kafka/serializer-fn :my.ns/serializer-fn
 :kafka/request-size 307200
 :onyx/batch-size batch-size
 :onyx/doc "Writes messages to a Kafka topic"}
```
::::::::::::::::::::::::::::::::::::::::::::::::::

Lifecycle entry:

::::::::::::::::::::::::::::::::::::::::::::::::::
:display :lifecycle-entry
:model :onyx.plugin.kafka/write-messages
::::::::::::::::::::::::::::::::::::::::::::::::::

Segments supplied to a `:onyx.plugin.kafka/write-messages` task should be in in
the following form: `{:message message-body}` with optional partition, topic and
key values.

``` clj
{:message message-body
 :key optional-key
 :partition optional-partition
 :topic optional-topic}
```

###### Attributes

::::::::::::::::::::::::::::::::::::::::::::::::::
:display :attribute-table
:model :onyx.plugin.kafka/write-messages
:columns :columns/default
::::::::::::::::::::::::::::::::::::::::::::::::::

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
