(ns challenge.main
  "Exposes command-line interface for application that
  allows queries over log-files and collections of time-stamps.

  Defaults to using the log-file stored in `resources/` if no
  override is used at the command-line.

  Attempts to fail fast at parse-time if data fails to conform
  to validation rules (e.g., in the case of an invalid time stamp)."
  (:require
   [challenge.analysis :as a]
   [clojure.tools.cli :refer [parse-opts]]
   [clojure.string :as str]
   [clj-time.format :as tf]
   [clojure.java.io :as io])
  (:gen-class))

(defn report-errors [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (str/join \newline errors)))

(def cli-options
  [[nil "--log-file PATH" "Path to log csv file"
    :default (io/resource "log.csv")
    :parse-fn io/file
    :validate [#(and (.exists %) (not (.isDirectory %))) "Log file must exist"]]
   ["-t" "--times TIMES" "Comma-separated list of times upon which to run query"
    :parse-fn
    #(->> (str/split % #",")
          (remove str/blank?))
    :validate [#(->> (map tf/parse %) (not-any? nil?))
               "Must enter one more valid time-stamps"]]
   ["-h" "--help"]])

(defn -main [& args]
  (let [{:keys [errors options]}
        (parse-opts args cli-options)
        {:keys [times log-file]} options]
    (when (seq errors)
      (println (report-errors errors))
      (System/exit 1))

    (let [msg (format "Analyzing: %s"
                      (cond-> log-file
                        (instance? java.io.File log-file) (.getPath)))]
      (println msg))

    (let [{:keys [statistics reports]}
          (a/analyze log-file times)]
      (println (a/format-reports reports))
      (println (a/format-statistics statistics)))))
