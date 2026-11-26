(ns gowherene.app.api
  (:require [compojure.core :refer [defroutes GET]]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [ring.middleware.json :refer [wrap-json-response]]
            [gowherene.app.handler :refer [handle]]))

(defroutes api-routes
  (GET "/parse/" request handle)
  (route/not-found "Not Found"))

(defroutes app-routes
  (wrap-json-response (wrap-defaults api-routes api-defaults)))
