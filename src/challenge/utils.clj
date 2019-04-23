(ns challenge.utils
  (:require
   [clojure.data.csv :as csv]
   [clojure.java.io :as io]))

(defn read-csv
  "Reads CSV file from disk into a matrix."
  [path]
  (with-open [reader (io/reader path)]
    (doall
     (csv/read-csv reader))))

(defn csv-data->maps
  "Converts the matrix representation of CSV data to a sequence
  of maps in which keys represent header fields."
  [csv-data]
  (map zipmap
       (->> (first csv-data)
            (map keyword)
            repeat)
       (rest csv-data)))


(defn contains-many? [m & ks]
  (every? #(contains? m %) ks))

(def is-time? (partial instance? org.joda.time.DateTime))
