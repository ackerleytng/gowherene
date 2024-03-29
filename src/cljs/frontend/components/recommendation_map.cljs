(ns frontend.components.recommendation-map
  (:require [clojure.set :as set]
            [clojure.string :as str]
            [reagent.core :as r]
            [reagent.dom :as rdom]
            [re-frame.core :as re-frame]
            [frontend.subs :as subs]
            [frontend.utils.scrolling :refer [scroll-to-id]]
            [frontend.utils.gmap :refer [gmap-marker gmap-latlng gmap-latlng-bounds gmap-icon]]
            [frontend.utils.color :refer [build-color]]))

(def singapore-bounds
  (gmap-latlng-bounds (map gmap-latlng [{:lat 1.365035 :lng 103.644231}
                                        {:lat 1.355701 :lng 104.036779}
                                        {:lat 1.471183 :lng 103.810720}
                                        {:lat 1.236344 :lng 103.832829}])))

(defn render-location
  [{:keys [postal-code unit-number road-name building-number]}]
  (str/join
   " "
   [(or building-number "")
    (or road-name "")
    (or unit-number "")
    (if postal-code (str "S(" postal-code ")") "")]))

(defn marker
  [gmap {:keys [latlng label location color]}]
  (gmap-marker
   gmap
   {:position (gmap-latlng latlng)
    :label (when label (second (re-find #"^\s*(\d+)" label)))
    :title label
    :content (render-location location)
    :icon (gmap-icon color)}))

(defn update-map
  [gmap pairs-atom recommendations]
  (let [pairs               @pairs-atom
        existing-recs       (keys pairs)
        existing-recs-set   (set existing-recs)
        recommendations-set (set recommendations)
        to-add              (set/difference recommendations-set existing-recs-set)
        to-remove           (set/difference existing-recs-set recommendations-set)]
    (doseq [r to-remove]
      (.setMap (get pairs r) nil)
      (swap! pairs-atom dissoc r))
    (doseq [r to-add]
      (let [m (marker gmap r)]
        (swap! pairs-atom assoc r m)))
    (let [latlngs (map (comp gmap-latlng :latlng) (keys @pairs-atom))
          bounds (or (gmap-latlng-bounds latlngs)
                     singapore-bounds)]
      (.fitBounds gmap bounds))))

(defn gmap []
  (let [gmap-atom (atom nil)
        pairs-atom (atom {})]
    (r/create-class
     {:display-name "recommendation-map"
      :component-did-mount
      (fn [this]
        (let [map-canvas (rdom/dom-node this)
              map-options #js {:disableDefaultUI true
                               :zoomControl true}
              gmap (js/google.maps.Map. map-canvas map-options)]
          (reset! gmap-atom gmap)
          (.fitBounds gmap singapore-bounds)))
      :component-did-update
      (fn [this]
        (let [{:keys [recommendations]} (r/props this)
              gmap @gmap-atom]
          (update-map gmap pairs-atom recommendations)
          (scroll-to-id "recommendation-map")))
      :reagent-render
      (fn []
        [:div#recommendation-map {:style {:height "100vh"}}])})))

(defn add-color [url recommendations]
  (let [color (build-color url)]
    (map #(assoc % :color color) recommendations)))

(defn add-offset [latlng url]
  (let [offset (/ (mod (hash url) 250) 1000000)
        {:keys [lat lng]} latlng]
    {:lat (+ lat offset) :lng (+ lng offset)}))

(defn add-offset-to-recommendations
  "Add random offset to lat and lng, to try to make completely overlapping
  markers slightly distinct"
  [url recommendations]
  (map #(update % :latlng add-offset url) recommendations))

(defn build-recommendations
  "Given results, add color for each of the results"
  [results]
  (->> results
       (map (fn [[k v]] (->> v
                             (add-color k)
                             (add-offset-to-recommendations k))))
       (apply concat)))

(defn recommendation-map []
  (let [results @(re-frame/subscribe [::subs/results])
        recommendations (build-recommendations results)]
    [:div.container.is-widescreen.space-out-top
     [gmap {:recommendations recommendations}]]))
