(defproject challenge "0.2.0-SNAPSHOT"
  :description
  "Given a set of valid time-stamps, this CLI application 
   returns the set of active IP addresses in addition to
   some descriptive statistics."
  :dependencies
  [[clj-time "0.15.0"]
   [clojure.java-time "0.3.2"]
   [org.clojure/clojure "1.9.0"]
   [org.clojure/data.csv "0.1.4"]
   [org.clojure/tools.cli "0.4.2"]]
  :main ^:skip-aot challenge.main
  :target-path "target/%s"
  :profiles
  {:uberjar {:aot :all}
   :dev     {:resource-paths
             ["dev-resources"
              "resources"]}})
