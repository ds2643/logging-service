(ns challenge.analysis-test
  "A set of example and (modest) property based assertions aiming
  to test functions supporting top-level `analyze` procedure"
  (:require
   [clojure.test :refer :all]
   [challenge.analysis :refer :all]
   [challenge.utils :as u]
   [clj-time.core :as t]
   [challenge.fixtures :refer :all]))

(use-fixtures :once create-example-log-context)

(deftest check-bounds
  (let [included-ex1 "2017-10-23T12:00:00.100"
        included-ex2 "2017-10-23T12:00:00.100"
        excluded-ex1 "2017-10-23T11:00:00.900"]
    (testing "Intervals fall into example timestamps included in interval set"
      (let [reports (make-reports *log-entries* [included-ex1 included-ex2])]
        (is (every? #(pos? (:count %)) reports))))

    (testing "No entries fall into times excluded from occupied intervals"
      (let [reports (make-reports *log-entries* [excluded-ex1])]
        (is (every? #(zero? (:count %)) reports))))))

(deftest reports-adhere-to-inplicit-schema
  (let [included-ex1 "2017-10-23T12:00:00.100"
        included-ex2 "2017-10-23T12:00:00.100"
        excluded-ex1 "2017-10-23T11:00:00.900"]
    (testing "Reports include relevant fields"
      (let [times   [included-ex1 included-ex2 excluded-ex1]
            reports (make-reports *log-entries* times)
            fields  [:count :included :time]]
        (is (every? #(apply u/contains-many? % fields) reports))))

    ;; NOTE: this method of checking types is best replaced by
    ;;       implementing run-time schema enforcement, either
    ;;       through prismatic/schema or spec
    (testing "Values conform at a pseudo type-level"
      (let [times   [included-ex1 included-ex2 excluded-ex1]
            reports (make-reports *log-entries* times)]
        (is (and (every? (comp u/is-time? :time) reports)
                 (every? (comp integer? :count) reports)
                 (every? (comp coll? :included) reports)))))))

(deftest report-bounds-are-valid
  (let [included-ex1 "2017-10-23T12:00:00.100"
        included-ex2 "2017-10-23T12:00:00.100"
        excluded-ex1 "2017-10-23T11:00:00.900"
        times        [included-ex1 included-ex2 excluded-ex1]
        reports      (make-reports *log-entries* times)
        {:keys [fewest-connections most-connections] :as bounds}
        (calculate-report-bounds reports)]
    (testing "Expected top-level keys present"
      (is (and most-connections fewest-connections)))

    (testing "Bounds include valid times"
      (is (every? (comp u/is-time? :time) (vals bounds))))))

(deftest statistics-check
  (let [{:keys [global-start global-end] :as ex-statistics}
        (calculate-uptime-statistics *log-entries*)]
    (testing "Time expressed as integers representing milliseconds"
      (is (every? (every-pred integer? pos?)
                  ((juxt :total-elapsed :total-uptime) ex-statistics))))

    (testing "Global boundries are valid times"
      (is (every? u/is-time? ((juxt :global-start :global-end) ex-statistics))))

    (testing "Global end follows start"
      (is (t/after? global-end global-start)))))
