(ns shadow-test-utils.filters
  (:require
    [com.rpl.specter :as sp]))

(defn keep-first-test-case
  [namespaces]
  (sp/transform [sp/MAP-VALS :vars] #(subvec % 0 1) namespaces))

(defn has-focus-meta?
  [v]
  (-> v
      meta
      :focus))

(defn has-skip-meta?
  [v]
  (-> v
      meta
      :skip))

(defn exists-any-focus-ns?
  [namespaces]
  (->> namespaces
       (sp/select [sp/MAP-KEYS (sp/pred has-focus-meta?)])
       seq))

(defn keep-focus-ns
  [namespaces]
  (sp/setval [sp/MAP-KEYS (sp/pred (complement has-focus-meta?))]
             sp/NONE
             namespaces))

(defn remove-skipped-ns
  [namespaces]
  (sp/setval [sp/MAP-KEYS (sp/pred has-skip-meta?)]
             sp/NONE
             namespaces))

(defn exists-any-focus-case?
  [namespaces]
  (->> namespaces
       (sp/select [sp/MAP-VALS :vars sp/ALL (sp/pred has-focus-meta?)])
       seq))

(defn keep-focus-case
  [namespaces]
  (sp/setval [sp/MAP-VALS :vars sp/ALL (sp/pred (complement has-focus-meta?))]
             sp/NONE
             namespaces))

(defn remove-skipped-case
  [namespaces]
  (sp/setval [sp/MAP-VALS :vars sp/ALL (sp/pred has-skip-meta?)]
             sp/NONE
             namespaces))

(defn prune-empty-ns
  [namespaces]
  (->> namespaces
       (sp/setval [sp/MAP-VALS
                   (sp/pred #(-> %
                                 :vars
                                 empty?))]
                  sp/NONE)))

(defn tweak-test-ns
  [namespaces]
  (cond-> namespaces
    (and #?(:cljs ^boolean goog.DEBUG
            :clj true)
         (exists-any-focus-ns? namespaces))
    (keep-focus-ns)

    true
    remove-skipped-ns

    true
    prune-empty-ns))

(defn tweak-test-case
  [namespaces]
  (cond-> namespaces
    (and #?(:cljs ^boolean goog.DEBUG
            :clj true)
         (exists-any-focus-case? namespaces))
    (keep-focus-case)

    true
    remove-skipped-case

    true
    prune-empty-ns))

(comment
  (shadow.test.env/get-test-data)
  ())
