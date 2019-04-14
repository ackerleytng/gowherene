(ns gowherene.core
  (:require [environ.core :refer [env]]
            [ring.adapter.jetty :refer [run-jetty]]
            [compojure.handler :as handler]
            [compojure.core :refer [routes]]
            [gowherene.app.api :refer [app-routes]]))

(defn start-gowherene []
  (-> (routes app-routes)
      handler/site
      (run-jetty {:join? false
                  :port (env :port)})))

(defn -main []
  (start-gowherene))
