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
  [gmap {:keys [position label title content]}]
  (let [marker (js/google.maps.Marker.
                (clj->js {:position position
                          :map gmap
                          ;; tooltip text
                          :title title
                          ;; label within marker
                          :label label}))
        info-window (gmap-info-window title content)]
    (.addListener
     marker "click"
     (fn [] (.open info-window gmap marker)))
    marker))
