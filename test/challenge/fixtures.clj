(ns challenge.fixtures
  (:require
   [clojure.test :as t]
   [challenge.utils :as u]
   [clojure.java.io :as io]))

(def ^:dynamic *log-entries* nil)

(defn create-example-log-context [f]
  (binding [*log-entries*
            (-> "test-log.csv"
                io/resource
                u/read-csv
                u/csv-data->maps)]
    (f)))
