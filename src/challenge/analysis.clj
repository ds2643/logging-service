(ns challenge.analysis
  "Contains log-file analysis business logic.

  Top-level functions include `analyze` and
  associated formatters."
  (:require
   [challenge.utils :as u]
   [clj-time.core :as t]
   [clj-time.format :as tf]
   [clojure.edn :as edn]
   [clojure.java.io :as io]
   [clojure.string :as str]))

(defn time->str [t] (tf/unparse (tf/formatters :date-time) t))

(defn make-time-range-predicate
  "Closure over some time-stamp returns a predicate that determines
  if a row in the log csv file (indicating a time interval) includes
  the time."
  [t]
  (fn [{:keys [endTs timeTaken]}]
    (let [period     (t/millis (edn/read-string timeTaken))
          end-time   (tf/parse endTs)
          start-time (t/minus end-time period)
          interval   (t/interval start-time end-time)]
      ;; NOTE: test assumed not to be inclusive
      (t/within? interval t))))

(defn process-entries
  "Finds the subset of ip addresses in the log file active
  at a particular time-stamp"
  [log-entries time]
  (let [includes-time? (make-time-range-predicate time)
        included       (filter includes-time? log-entries)]
    {:time     time
     :included (mapv :ip included)
     :count    (count included)}))

(defn make-reports [log-entries times]
  (->> (map tf/parse times)
       (mapv (partial process-entries log-entries))))

(defn format-reports
  "Aids in pretty-printing the results of reports on each time-stamp."
  [reports]
  (str/join \newline
            (for [{:keys [time included count]} reports]
              (format "Time: %s\n\tActive IP addresses: %s\n\tCount: %s"
                      (time->str time)
                      (str/join ", " included)
                      count))))

(defn calculate-report-bounds
  "Finds the time-stamps with the most and fewest connections."
  [reports]
  ;; NOTE: only reports a single value
  {:fewest-connections
   (let [{:keys [time count]}
         (apply min-key :count reports)]
     {:time time :count count})
   :most-connections
   (let [{:keys [time count]} (apply max-key :count reports)]
     {:time time :count count})})

(defn calculate-uptime-statistics
  "Returns a set of descriptive statistics with respect to the provided
  log csv file. These statistics include the range of uptime, the range
  of time included in the log file, and the average number of connections.

  Tenors are noted in milliseconds."
  [log-entries]
  (let [find-duration
        (fn [{:keys [endTs timeTaken]}]
          ;; TODO: abstract away?
          (let [period     (t/millis (edn/read-string timeTaken))
                end-time   (tf/parse endTs)
                start-time (t/minus end-time period)]
            {:duration   (t/in-millis (t/interval start-time end-time))
             :end-time   end-time
             :start-time start-time}))
        time-ranges   (map find-duration log-entries)
        total-uptime  (reduce + (map :duration time-ranges))
        global-start  (apply t/min-date (map :start-time time-ranges))
        global-end    (apply t/max-date (map :end-time time-ranges))
        total-elapsed (t/in-millis (t/interval global-start global-end))]
    {:total-elapsed       total-elapsed
     :total-uptime        total-uptime
     :global-start        global-start
     :global-end          global-end
     :average-connections (float (/ total-uptime total-elapsed))}))

(defn format-statistics
  "Pretty print the results stored in the return value of `analyze`."
  [{:as combined-statistics
    :keys [total-elapsed total-uptime global-start global-end
           average-connections fewest-connections most-connections]}]
  (str/join "\n\t"
            ["Statistics:"
             (format "Total time elapsed: %s milliseconds" total-elapsed)
             (format "Total uptime: %s milliseconds" total-uptime)
             (format "Average connections: %.3f connections" average-connections)
             (let [{:keys [time count]} fewest-connections]
               (format "Fewest connections (%s) at %s" count (time->str time)))
             (let [{:keys [time count]} most-connections]
               (format "Most connections (%s) at %s" count (time->str time)))
             (format "Earliest connection: %s" global-start)
             (format "Latest disconnection: %s" global-end)]))

(defn analyze
  "Preforms top-level analysis of log-files in accordance with
  project requirements detailed in `README.md`.

  The reports field describes the set of active IP addresses at
  each time in `times`.

  The statistics field provides descriptive statistics with
  respect to the log-file found at log-path.

  Please use the `format-statistics` and `format-reports`
  functions provided in this module to aid in pretty-printing
  if desired.

  Tenors are noted in milliseconds."
  [log-path times]
  (let [log-entries (-> log-path
                        u/read-csv
                        u/csv-data->maps)
        reports     (make-reports log-entries times)
        statistics  (merge (calculate-uptime-statistics log-entries)
                           (calculate-report-bounds reports))]
    {:statistics statistics
     :reports    reports}))
