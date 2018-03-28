(defproject gowherene "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :jvm-opts ["--add-modules" "java.xml.bind"]

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [compojure "1.5.1"]
                 [ring/ring-defaults "0.2.1"]
                 [ring/ring-json "0.4.0"]
                 [environ "1.1.0"]
                 ;; For reader
                 [medley "1.0.0"]
                 [clj-http "3.7.0"]
                 [xtreak/clj-http-ssrf "0.2.2"]
                 [slingshot "0.12.2"]
                 [hickory "0.7.1"]
                 ;; For client-side
                 [org.clojure/clojurescript "1.9.946"]
                 ;; Had to downgrade to 0.7.0 because
                 ;; https://github.com/reagent-project/reagent/issues/307
                 [reagent "0.7.0"]
                 [cljs-ajax "0.7.3"]
                 [com.cemerick/url "0.1.1"]
                 [binaryage/devtools "0.9.9"]]
  :plugins [[lein-ring "0.9.7"]
            [lein-figwheel "0.5.14"]
            [lein-cljsbuild "1.1.7" :exclusions [[org.clojure/clojure]]]]
  :ring {:handler gowherene.handler/app}
  :cljsbuild {:builds [{:id "dev"
                        :source-paths ["src"]
                        :figwheel true
                        :compiler {:main "app.core"
                                   :asset-path "js/out"
                                   :output-to "resources/public/js/main.js"
                                   :output-dir "resources/public/js/out"
                                   :source-map true
                                   :preloads [devtools.preload]
                                   :external-config {:devtools/config {:features-to-install :all}}
                                   :optimizations :none}}
                       ;; For production: lein cljsbuild once min
                       {:id "min"
                        :source-paths ["src"]
                        ;; This output directory is part of a hack
                        :compiler {:output-to "target/cljs-output/public/js/main.js"
                                   :output-dir "target/cljs-output/public/js"
                                   ;; Uncomment to debug compilation
                                   ;; :source-map "target/cljs-output/public/js/main.js.map"
                                   :externs ["src/app/externs/google-maps.js"]
                                   :main "app.core"
                                   :optimizations :advanced
                                   :pretty-print false}}]}
  :profiles  {:uberjar {:aot :all
                        ;; hack: By including this, lein will copy the production main.js output
                        ;;   over the one from figwheel. The js/out directory still gets copied in,
                        ;;   so that's not that great. The client won't load it though, so that's
                        ;;   not so bad.
                        :resource-paths ["target/cljs-output"]
                        :prep-tasks [["cljsbuild" "once" "min"]]}
              :dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                                   [ring/ring-mock "0.3.0"]]}})
