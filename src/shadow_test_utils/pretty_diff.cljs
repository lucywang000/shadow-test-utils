(ns shadow-test-utils.pretty-diff
  "Show a prettified diff when tests fails for cljs-test-display."
  (:require
    [applied-science.js-interop :as j]
    [cljs-test-display.core :as display]
    [cljs.pprint :as pp]
    [cljs.test :refer-macros [run-tests] :refer [empty-env]]
    [clojure.data :as data]
    [clojure.string :as str]
    [goog.dom :as gdom]
    [goog.dom.classlist :as classlist]
    [lambdaisland.deep-diff2 :as ddiff]
    [lambdaisland.deep-diff2.printer-impl :as printer-impl]
    [lambdaisland.deep-diff2.puget.color.html]
    [lambdaisland.deep-diff2.puget.printer :as puget-printer]
    [meander.epsilon :as me]
    [pjstadig.macro]
    [pjstadig.print :as p]
    [pjstadig.util :as util]))

(def dummy-env (empty-env))

(defn convert-event
  [{:keys [actual expected]
    :as event}]
  (let [diffs
        (when (and (seq? actual)
                   (seq actual)
                   (= 'not (first actual))
                   (seq? (second actual))
                   (seq (second actual))
                   (#{'clojure.core/= '= 'cljs.core/=} (first (second actual)))
                   (< 2 (count (second actual))))
          (let [a (nth (second actual) 1)
                more (drop 2 (second actual))]
            (map vector
              more
              (map #(take 2 (data/diff a %)) more))))

        expected (if (seq diffs)
                   (nth (second actual) 1)
                   expected)]
    (assoc event
      :diffs diffs
      :expected expected)))

(def html-printer (ddiff/printer {:color-markup :html-classes}))

(defn pprint-str
  [fipp-doc]
  (with-out-str
    (printer-impl/print-doc fipp-doc html-printer)))

(defn get-diff-html
  [a b]
  (pprint-str
    (puget-printer/format-doc
      html-printer
      (ddiff/diff a b))))

(defn collect-diffs
  [{:keys [actual expected]
    :as event}]
  (when-let [[a more] (me/match actual
                        (not (= ?expected . !more ...))
                        [?expected !more]

                        _
                        nil)]
    (when (seq more)
      (mapv #(get-diff-html a %) more))))

(defn append-style
  []
  (let
    [id "shadow-pretty-differ"
     node (j/call js/document :querySelector (str "#" id))
     exists (boolean node)
     style-node (or node
                    (-> (js/document.createElement "style")
                        (j/assoc! :id id)))
     css
     "code .class-delimiter {color: #a3685a;} code .class-name {color: #a3685a;} code .nil {color: #4d4d4c;} code .boolean {color: #4d4d4c;} code .number {color: #4271ae;} code .character {color: #a3685a;} code .string {color: #3e999f;} code .keyword {color: #4271ae;} code .symbol {color: #3e999f;} code .delimiter {color: #8959a8;} code .function-symbol {color: #8959a8;} code .tag {color: #a3685a;} code .insertion {color: #718c00;} code .deletion {color: #c82829;}"]
    (j/assoc! style-node :innerText css)
    (when-not exists
      (j/call-in js/document [:head :appendChild] style-node))))

;; Reporting with diffs
(set!
  display/add-fail-node!
  (fn add-fail-node! [m]
    (append-style)
    (let [out (binding [cljs.test/*current-env* dummy-env] ;; we don't want
                                                           ;; `humane-test-output`
                                                           ;; to modify the env
                (with-out-str
                  (util/report- (convert-event m))))
          new-diffs-nodes (->> (collect-diffs m)
                               (mapv
                                 (fn [html]
                                   (let [wrapper (js/document.createElement "div")
                                         node (js/document.createElement "code")]
                                     (j/assoc! wrapper :id "shadow-pretty-differ")
                                     (j/assoc! wrapper :style "padding: 10px 24px")
                                     (j/assoc! node :innerHTML html)
                                     (j/call wrapper :appendChild node)
                                     wrapper))))
          clean-out (->> (str/split out #"\n")
                         (drop-while #(not (str/starts-with? % "expected")))
                         (str/join "\n")
                         (str (with-out-str (pp/pprint (:expected m))) "\n"))

          node (display/div :test-fail
                            (display/contexts-node)
                            (display/div :fail-body
                                         (when-let [message (:message m)]
                                           (display/div :test-message message))
                                         (display/n
                                           :pre
                                           {}
                                           (display/n :code {} clean-out))))
          curr-node (display/current-node)]
      (classlist/add curr-node "has-failures")
      (classlist/add (display/current-node-parent) "has-failures")
      (gdom/appendChild curr-node node)
      (doseq [n new-diffs-nodes]
        (gdom/appendChild node n))
    )))
