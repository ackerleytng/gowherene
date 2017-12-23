(defproject playout "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [compojure "1.5.1"]
                 [ring/ring-defaults "0.2.1"]
                 [ring/ring-json "0.4.0"]
                 [environ "1.1.0"]
                 ;; For reader
                 [medley "1.0.0"]
                 [clj-http "3.7.0"]
                 [slingshot "0.12.2"]
                 [hickory "0.7.1"]]
  :plugins [[lein-ring "0.9.7"]]
  :ring {:handler playout.handler/app}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring/ring-mock "0.3.0"]]}})
