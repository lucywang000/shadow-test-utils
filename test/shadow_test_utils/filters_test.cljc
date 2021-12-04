(ns shadow-test-utils.filters-test
  (:require [shadow-test-utils.filters :refer [tweak-test-ns tweak-test-case]]
            [clojure.test :refer [deftest is are use-fixtures testing are]]))

(defn v1 [])
(defn v2 [])
(defn ^:focus vf1 [])
(defn ^:focus vf2 [])
(defn ^:skip vs1 [])
(defn ^:skip vs2 [])

(deftest test-filter-test-case
  (are [args]
    (let [{:keys [before-vars after-vars]} args
          ns 'foo
          namespaces {ns {:vars before-vars}}
          after-tweak (tweak-test-case namespaces)
          vars (get-in after-tweak [ns :vars])]
      (is (= vars after-vars))
      true)

    {:before-vars [#'v1 #'vf1 #'vf2 #'vs1 #'vs2]
     :after-vars [#'vf1 #'vf2]}

    {:before-vars [#'v1 #'vs1 #'vs2]
     :after-vars [#'v1]}

    {:before-vars [#'vs1]
     :after-vars nil}

  ))

(deftest test-filter-test-ns
  (let [ns1   'ns1
        ns2   'ns2
        ns-f1 (vary-meta 'ns-f1 assoc :focus true)
        ns-f2 (vary-meta 'ns-f2 assoc :focus true)
        ns-s1 (vary-meta 'ns-s1 assoc :skip true)
        ns-s2 (vary-meta 'ns-s2 assoc :skip true)]
    (are [args]
      (let [{:keys [before-ns-keys after-ns-keys]} args
            namespaces (->> before-ns-keys
                            (map (fn [x]
                                   [x {:vars [#'v1]}]))
                            (into {}))
            after-tweak (tweak-test-ns namespaces)]
        (is (= (keys after-tweak) after-ns-keys))
        true)

      {:before-ns-keys [ns1 ns-f1 ns-s1]
       :after-ns-keys  [ns-f1]}

      {:before-ns-keys [ns1 ns-f1 ns-f2 ns-s1 ns-s2]
       :after-ns-keys  [ns-f1 ns-f2]}

      {:before-ns-keys [ns1 ns-s1 ns-s2]
       :after-ns-keys  [ns1]}

      {:before-ns-keys [ns1 ns2]
       :after-ns-keys  [ns1 ns2]}

      {:before-ns-keys [ns-s1 ns-s2]
       :after-ns-keys  nil}

      )))
