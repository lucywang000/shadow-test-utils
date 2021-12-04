(ns shadow-test-utils.filters-test
  (:require [shadow-test-utils.filters :refer [tweak-test-ns tweak-test-case]]
            [clojure.test :refer [deftest is are use-fixtures testing]]))

(deftest foo-test
  (is (= 1 1)))
