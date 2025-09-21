(ns gowherene.core
  (:require [environ.core :refer [env]]
            [ring.middleware.cors :refer [wrap-cors]]
            [ring.middleware.defaults :refer [wrap-defaults secure-api-defaults api-defaults]]
            [org.httpkit.server :as server]
            [compojure.core :refer [routes]]
            [gowherene.app.api :refer [app-routes]])
  (:gen-class))

(defn start-gowherene []
  (let [port (if-let [p (env :port)]
               (Integer. p)
               3000)]
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
        (server/run-server {:port port}))))

(defn -main []
  (start-gowherene))
