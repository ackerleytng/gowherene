(ns playout.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.json :refer [wrap-json-response]]
            [ring.util.response :as resp]
            [playout.reader.core :refer [url->hickory 
                                         hickory->data
                                         cleanup-addresses
                                         data-add-geocode]]))

(defroutes site-routes
  (GET "/" [] (resp/redirect "/index.html"))
  (route/resources "/"))

(defroutes api-routes
  (GET "/parse" [url]
       (println "incoming" url) 
       (let [result (->> url
                         url->hickory
                         hickory->data
                         cleanup-addresses
                         data-add-geocode
                         (filter  #(:latlng %)))]
         (resp/response result)))
  (route/not-found "Not Found"))

(defroutes app
  (wrap-defaults site-routes site-defaults)
  (wrap-json-response (wrap-defaults api-routes site-defaults)))
