(ns user
  "Tools for interactive development with the REPL. This file should
  not be included in a production build of the application."
  (:require
   [clojure.java.io :as io]
   [clojure.java.javadoc :refer [javadoc]]
   [clojure.pprint :refer [pprint]]
   [clojure.reflect :refer [reflect]]
   [clojure.repl :refer [apropos dir doc find-doc pst source]]
   [clojure.set :as set]
   [clojure.zip :as zip]
   [clojure.string :as string]
   [clojure.test :as test]
   [clojure.tools.namespace.repl :refer [clear]]
   [alembic.still :as alembic]
   [dev :refer [start stop refresh refresh-all reset]]

   [hickory.zip :refer [hickory-zip]]
   [hickory.convert :refer [hickory-to-hiccup]]
   [gowherene.reader.core :refer :all]
   [gowherene.reader.geocoding :refer [geocode add-latlng]]
   [gowherene.reader.regexes :refer [re-postal-code re-address, re-spaces]]
   [gowherene.reader.geocodables :refer [geocodables]]
   [gowherene.reader.location :refer [add-location]]
   [gowherene.reader.label :refer :all]
   [gowherene.reader.utils :refer [content subtree]]))


(comment
  (def page (slurp "data/files/cheap-food-orchard.html"))
  (def page (slurp "data/files/11-budget-buffets-in-singapore-20-and-below.html"))
  (def page (slurp "data/files/dim-sum-restaurants-singapore.html"))

  (->> page
       hickory-zipper
       cleanup
       geocodables
       (map add-location)
       (map add-label)
       (pmap add-latlng)
       (map #(update % :loc zip/node)))

  (->> page
       hickory-zipper
       cleanup
       geocodables
       first
       :loc
       label)


  )
