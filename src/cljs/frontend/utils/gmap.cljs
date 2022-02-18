(ns frontend.utils.gmap
  (:require [reagent.dom.server :as rs]))

(defn gmap-latlng
  [{:keys [lat lng]}]
  (js/google.maps.LatLng. lat lng))

(defn gmap-latlng-bounds [latlngs]
  (when (seq latlngs)
    (reduce #(.extend %1 %2)
            (js/google.maps.LatLngBounds.)
            latlngs)))

(defn infowindow-content
  [header content]
  [:div
   [:b header] [:br]
   content])

(defn gmap-info-window
  [header content]
  (js/google.maps.InfoWindow.
   (clj->js {:content (rs/render-to-static-markup [infowindow-content header content])})))

(defn gmap-marker
  "Add a marker to a google maps object (gmap)
  `position` is the coordinates of the map point (google.maps.LatLng)
  `label` is the little number that appears on the map marker provided by google maps, can be nil
  `title` is the heading for this map marker, which appears when clicked
  `content` is the content of this map marker, which appears when clicked"
  [gmap {:keys [position label title content icon]}]
  (let [marker (js/google.maps.Marker.
                (clj->js {:position position
                          :map gmap
                          ;; tooltip text
                          :title title
                          ;; label within marker
                          :label label
                          :icon icon}))
        info-window (gmap-info-window title content)]
    (.addListener
     marker "click"
     (fn [] (.open info-window gmap marker)))
    marker))

(defn gmap-icon
  [color]
  ;; Thanks to https://stackoverflow.com/a/23163930/2108690 !
  {:path "M 12,2 C 8.1340068,2 5,5.1340068 5,9 c 0,5.25 7,13 7,13 0,0 7,-7.75 7,-13 0,-3.8659932 -3.134007,-7 -7,-7 z"
   :fillOpacity 1
   :fillColor color
   :strokeWeight 2
   :scale 2})
