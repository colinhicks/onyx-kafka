(ns onyx.plugin.input-test
  (:require [clojure.core.async :refer [<!! go pipe]]
            [clojure.core.async.lab :refer [spool]]
            [clojure.test :refer [deftest is]]
            [com.stuartsierra.component :as component]
            [franzy.admin.zookeeper.client :as k-admin]
            [franzy.admin.cluster :as k-cluster]

            ;; added
            [franzy.admin.topics :as k-topics]
            [franzy.clients.consumer.protocols :refer [poll! assign-partitions!]]
            [franzy.clients.consumer.client :as c]

            [franzy.admin.configuration :as k-config]
            [franzy.serialization.serializers :refer [byte-array-serializer]]
            [franzy.serialization.deserializers :refer [byte-array-deserializer]]
            [franzy.clients.producer.client :as producer]
            [franzy.clients.producer.protocols :refer [send-sync! send-async!]]
            [onyx.test-helper :refer [with-test-env]]
            [onyx.job :refer [add-task]]
            [onyx.kafka.embedded-server :as ke]
            [onyx.kafka.utils :refer [take-until-done]]
            [onyx.tasks.kafka :refer [consumer]]
            [onyx.tasks.core-async :as core-async]
            [onyx.plugin.core-async :refer [get-core-async-channels]]
            [onyx.plugin.test-utils :as test-utils]
            [onyx.plugin.kafka]
            [onyx.api])
  (:import [franzy.clients.producer.types ProducerRecord]))

(defn build-job [zk-address topic batch-size batch-timeout]
  (let [batch-settings {:onyx/batch-size batch-size :onyx/batch-timeout batch-timeout}
        base-job (merge {:workflow [[:read-messages :identity]
                                    [:identity :out]]
                         :catalog [(merge {:onyx/name :identity
                                           :onyx/fn :clojure.core/identity
                                           :onyx/type :function}
                                          batch-settings)]
                         :lifecycles []
                         :windows []
                         :triggers []
                         :flow-conditions []
                         :task-scheduler :onyx.task-scheduler/balanced})]
    (-> base-job
        (add-task (consumer :read-messages
                            (merge {:kafka/topic topic
                                    :kafka/group-id "onyx-consumer"
                                    :kafka/zookeeper zk-address
                                    :kafka/offset-reset :smallest
                                    :kafka/force-reset? false
                                    :kafka/deserializer-fn :onyx.tasks.kafka/deserialize-message-edn
                                    :onyx/min-peers 2
                                    :onyx/max-peers 2}
                                   batch-settings)))
        (add-task (core-async/output :out batch-settings)))))

(defn mock-kafka
  "Use a custom version of mock-kafka as opposed to the one in test-utils
  because we need to spawn 2 producers in order to write to each partition"
  [topic zookeeper embedded-kafka?]
  (let [kafka-server (component/start
                      (ke/embedded-kafka {:advertised.host.name "127.0.0.1"
                                          :port 9092
                                          :broker.id 1
                                          :server? embedded-kafka?
                                          :log.dir (str "/tmp/embedded-kafka" (java.util.UUID/randomUUID))
                                          :zookeeper.connect zookeeper
                                          :controlled.shutdown.enable false}))
        zk-utils (k-admin/make-zk-utils {:servers [zookeeper]} false)
        #_ (k-topics/create-topic! zk-utils topic 2 1
                                  {;;cover tracks
                                   :cleanup.policy      :delete
                                   ;;24 hours to abuse user trust
                                   :delete.retention.ms 40000
                                   }
                                  )

        producer-config {:bootstrap.servers ["127.0.0.1:9092"]}
        key-serializer (byte-array-serializer)
        value-serializer (byte-array-serializer)]

    (with-open [producer1 (producer/make-producer producer-config key-serializer value-serializer)]
      (with-open [producer2 (producer/make-producer producer-config key-serializer value-serializer)]
        (doseq [x (range 3000)] ;0 1 2
          (send-sync! producer1 (ProducerRecord. topic nil nil (.getBytes (pr-str {:n x})))))
        (doseq [x (range 3000)] ;3 4 5
          (send-sync! producer2 (ProducerRecord. topic nil nil (.getBytes (pr-str {:n (+ 3 x)})))))))
    kafka-server))

(comment 
 (def topictest "wheee3")

 (let [zk-utils (k-admin/make-zk-utils {:servers ["127.0.0.1:2181"]} false)
       topic-name topictest
       partition-count 1 ;;obviously
       replication-factor 1 ;;tri-force of replicas
       topic-config         {;;cover tracks
                             :cleanup.policy      :delete
                             ;;24 hours to abuse user trust
                             :delete.retention.ms 86400000
                             ;;fsync after every 10 messages, living dangerously
                             :flush.messages      10
                             ;;fsync on some nerdy interval
                             :flush.ms            32768}]
   (k-topics/create-topic! zk-utils topic-name partition-count replication-factor topic-config))

 (k-config/update-topic-config! (k-admin/make-zk-utils {:servers ["127.0.0.1:2181"]} false)
                                topictest
                                {:delete.retention.ms 100000})

 (let [zk-utils (k-admin/make-zk-utils {:servers ["127.0.0.1:2181"]} false)
       producer-config {:bootstrap.servers ["127.0.0.1:9092"]}
       key-serializer (byte-array-serializer)
       value-serializer (byte-array-serializer)]
   (with-open [producer1 (producer/make-producer producer-config key-serializer value-serializer)]
     (doseq [x (range 300000)] ;0 1 2
       (send-sync! producer1 (ProducerRecord. topictest nil nil (.getBytes (pr-str {:n x})))))))

 (defn- make-consumer
   []
   (c/make-consumer
    {:bootstrap.servers ["127.0.0.1:9092"]
     :group.id "onyx-consumer"
     :auto.offset.reset :earliest
     :receive.buffer.bytes 65536
     :enable.auto.commit false}
    (byte-array-deserializer)
    (byte-array-deserializer)))

 (def consss (make-consumer))
 (assign-partitions! consss [{:topic topictest :partition 0}])
 (into [] (poll! consss {:poll-timeout-ms 5000}))) 

(deftest kafka-input-test
  (let [test-topic (str "onyx-test-aiijkss" #_(java.util.UUID/randomUUID))
        _ (println "Using topic" test-topic)
        {:keys [test-config env-config peer-config]} (onyx.plugin.test-utils/read-config)
        tenancy-id ;(str (java.util.UUID/randomUUID)) 
        "aaadess"
        _ (println peer-config)
        env-config (assoc env-config :onyx/tenancy-id tenancy-id)
        peer-config (assoc peer-config :onyx/tenancy-id tenancy-id)
        zk-address (get-in peer-config [:zookeeper/address])
        job (build-job zk-address test-topic 10 1000)
        {:keys [out read-messages]} (get-core-async-channels job)
        mock (atom {})]
    (try
      (with-test-env [test-env [4 env-config peer-config]]
        (onyx.test-helper/validate-enough-peers! test-env job)
        (println (:embedded-kafka? test-config))
        (reset! mock (mock-kafka test-topic zk-address (:embedded-kafka? test-config)))
        (let [job-id (:job-id (onyx.api/submit-job peer-config job))]
          (is (= 15
                 (reduce + (mapv :n (onyx.plugin.core-async/take-segments! out 10000)))))
          (onyx.api/kill-job peer-config job-id)
          ))
      (finally (swap! mock component/stop)))))
