(ns playout.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.json :refer [wrap-json-response]]
            [ring.util.response :as resp]
            [playout.reader.core :as reader]))

(defroutes site-routes
  (GET "/" [] (resp/redirect "/index.html"))
  (route/resources "/"))

(defroutes api-routes
  (GET "/parse" [url]
       (resp/response (reader/handle url)))
  (route/not-found "Not Found"))

(defroutes app
  (wrap-defaults site-routes site-defaults)
  (wrap-json-response (wrap-defaults api-routes site-defaults)))
