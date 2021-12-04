(defproject org.clojars.lucywang000/shadow-test-utils "0.0.2"
  :description "Shadow-cljs test helpers, including a kaocha-like test filter"
  :url "https://github.com/lucywang000/shadow-test-utils"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :min-lein-version "2.5.0"

  :aliases {"kaocha" ["with-profile" "+dev" "run" "-m" "kaocha.runner"]
            "test"   ["version"]}

  :dependencies [[org.clojure/clojure "1.10.3" :scope "provided"]
                 [com.rpl/specter "1.1.3"]

                 ;; deps for pretty diff
                 [applied-science/js-interop "0.3.1"]
                 [lambdaisland/deep-diff2 "2.0.108"]
                 [meander/epsilon "0.0.650"]
                 [pjstadig/humane-test-output "0.11.0"]]

  :deploy-repositories [["clojars" {:url           "https://clojars.org/repo"
                                    :username      :env/clojars_user
                                    :password      :env/clojars_token
                                    :sign-releases false}]]

  :profiles {:dev {:injections [(require 'hashp.core)
                                (require 'debux.core)]

                   :source-paths ["dev/src" "local/src"]
                   :dependencies [[hashp "0.2.1"]
                                  [org.clojure/clojurescript "1.10.896"]
                                  [philoskim/debux "0.8.1"]
                                  [lambdaisland/kaocha "1.60.945"]]}})
