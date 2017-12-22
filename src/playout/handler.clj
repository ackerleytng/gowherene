(ns playout.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.json :refer [wrap-json-response]]
            [ring.util.response :as resp]))

(defroutes site-routes
  (GET "/" [] (resp/redirect "/index.html"))
  (route/resources "/"))

(defroutes api-routes
  (GET "/parse" [url]
       (println "incoming" url) 
       (resp/response url))
  (route/not-found "Not Found"))

(defroutes app
  (wrap-defaults site-routes site-defaults)
  (wrap-json-response (wrap-defaults api-routes site-defaults)))
