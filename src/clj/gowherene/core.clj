(ns gowherene.core
  (:require [environ.core :refer [env]]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.cors :refer [wrap-cors]]
            [ring.middleware.defaults :refer [wrap-defaults secure-api-defaults api-defaults]]
            [compojure.core :refer [routes]]
            [gowherene.app.api :refer [app-routes]])
  (:gen-class))

(defn start-gowherene []
  (-> (routes app-routes)
      (wrap-cors :access-control-allow-origin [#"https://gowherene.ackerleytng.com"
                                               #"https://gowherene.netlify.app"
                                               #"https?://localhost:?\d+"]
                 :access-control-allow-methods [:get])
      (wrap-defaults (assoc
                      (if (env :gowherene-debug)
                        api-defaults
                        secure-api-defaults)
                      :proxy true))
      (run-jetty {:join? false
                  :port (if-let [port (env :port)] (Integer. port) 3000)})))

(defn -main []
  (start-gowherene))
