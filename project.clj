(defproject gowherene "0.1.0-SNAPSHOT"
  :description "gowherene plots addresses from recommendation pages in Singapore on a map!"
  :url "https://gowherene.herokuapp.com"
  :min-lein-version "2.0.0"
  :jvm-opts ["--add-modules" "java.xml.bind"]
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [compojure "1.6.0"]
                 [ring/ring-defaults "0.3.1"]
                 [ring/ring-json "0.4.0" :exclusions [cheshire
                                                      com.fasterxml.jackson.core/jackson-core]]
                 [environ "1.1.0"]
                 ;; Logging
                 [heroku-database-url-to-jdbc "0.2.2"]
                 [org.clojure/core.async "0.4.474"]
                 [org.clojure/java.jdbc "0.7.5"]
                 [org.postgresql/postgresql "42.2.2"]
                 [korma "0.4.3"]
                 [com.novemberain/monger "3.1.0" :exclusions [com.google.guava/guava]]
                 ;; For reader
                 [medley "1.0.0"]
                 [clj-http "3.8.0"]
                 [xtreak/clj-http-ssrf "0.2.2"]
                 [slingshot "0.12.2"]
                 [hickory "0.7.1"]
                 ;; For client-side
                 [org.clojure/clojurescript "1.10.238"]
                 [reagent "0.8.0-alpha2"]
                 [cljs-ajax "0.7.3" :exclusions [com.fasterxml.jackson.core/jackson-core]]
                 [cljsjs/google-maps "3.18-1"]
                 [com.cemerick/url "0.1.1"]
                 [binaryage/devtools "0.9.9"]]
  :plugins [[lein-ring "0.12.4"]
            [lein-figwheel "0.5.15"]
            [lein-cljsbuild "1.1.7" :exclusions [[org.clojure/clojure]]]]
  :ring {:handler gowherene.handler/app
         :nrepl   {:start? true}}
  :cljsbuild {:builds [{:id           "dev"
                        :source-paths ["src"]
                        :figwheel     true
                        :compiler     {:main            "app.core"
                                       :asset-path      "js/out"
                                       :output-to       "resources/public/js/main.js"
                                       :output-dir      "resources/public/js/out"
                                       :source-map      true
                                       :preloads        [devtools.preload]
                                       :external-config {:devtools/config
                                                         {:features-to-install :all}}
                                       :optimizations   :none}}
                       ;; For production: lein cljsbuild once min
                       {:id           "min"
                        :source-paths ["src"]
                        ;; This output directory is part of a hack
                        :compiler     {:output-to     "target/cljs-output/public/js/main.js"
                                       :output-dir    "target/cljs-output/public/js"
                                       ;; Uncomment to debug compilation
                                       ;; :source-map "target/cljs-output/public/js/main.js.map"
                                       :main          "app.core"
                                       :optimizations :advanced
                                       :pretty-print  false}}]}
  :aliases {"accesses-cleanup" ["run" "-m" "gowherene.db-utils/cleanup-accesses"]
            "accesses-show"    ["run" "-m" "gowherene.logging/show-accesses"]
            "requests-cleanup" ["run" "-m" "gowherene.db-utils/cleanup-requests"]
            "requests-show"    ["run" "-m" "gowherene.logging/show-requests"]}
  :profiles  {:uberjar {:aot            :all
                        ;; hack: By including this, lein will copy the production main.js output
                        ;;   over the one from figwheel. The js/out directory still gets copied in,
                        ;;   so that's not that great. The client won't load it though, so that's
                        ;;   not so bad.
                        :resource-paths ["target/cljs-output"]
                        :prep-tasks     [["cljsbuild" "once" "min"]]}
              :dev     {:dependencies [[javax.servlet/servlet-api "2.5"]
                                       [ring/ring-mock "0.3.0"]]}})
