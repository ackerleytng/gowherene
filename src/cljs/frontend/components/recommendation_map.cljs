(ns frontend.components.recommendation-map
  (:require [clojure.set :as set]
            [clojure.string :as str]
            [re-frame.core :as re-frame]
            [frontend.subs :as subs]
            [frontend.utils.scrolling :refer [scroll-to-id]]
            [frontend.utils.color :refer [build-color]]
            [leaflet]
            [react]))

(def singapore-bounds
  (js/L.latLngBounds (js/L.latLng 1.236344 103.640231)
                     (js/L.latLng 1.471183 104.036779)))

(defn render-location
  [{:keys [postal-code unit-number road-name building-number]}]
  (str/join
   " "
   [(or building-number "")
    (or road-name "")
    (or unit-number "")
    (if postal-code (str "S(" postal-code ")") "")]))

(defn icon [color label-num]
  (let [svg (str "<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 32 32\" fill=\"none\">"
                 "<path fill=\"" color "\" d=\"M16 0c-5.523 0-10 4.477-10 10 0 10 10 22 10 22s10-12 10-22c0-5.523-4.477-10-10-10z\"/>"
                 "<circle fill=\"#fff\" cx=\"16\" cy=\"10\" r=\"3\"/>"
                 "<text x=\"16\" y=\"10\" font-family=\"Arial, sans-serif\" font-size=\"8\" fill=\"#333\" text-anchor=\"middle\" dominant-baseline=\"middle\">"
                 (or label-num "")
                 "</text>"
                 "</svg>")]
    (js/L.divIcon
     (clj->js {:html svg
               :opacity 0.5
               :className "recommendation-map-marker"
               :iconSize [32 32]
               :iconAnchor [16 32]}))))

(defn popup [title content]
  (str "<h4>" title "</h4>" content))

(defn marker
  [{:keys [latlng label location color]}]
  (let [{lat :lat lng :lng} latlng
        label-num (when label (second (re-find #"^\s*(\d+)" label)))
        options (clj->js {:title label
                          :icon (icon color label-num)})
        m (js/L.marker (js/L.latLng lat lng) options)
        p (popup label (render-location location))]
    (.bindPopup m p)))

(defn update-map
  [lmap pairs-ref recommendations]
  (let [pairs               (.-current pairs-ref)
        existing-recs-set   (set (keys pairs))
        recommendations-set (set recommendations)
        to-add              (set/difference recommendations-set existing-recs-set)
        to-remove           (set/difference existing-recs-set recommendations-set)]
    (doseq [r to-remove]
      (.remove (get pairs r))
      (set! (.-current pairs-ref) (dissoc (.-current pairs-ref) r)))
    (doseq [r to-add]
      (let [m (marker r)]
        (.addTo m lmap)
        (set! (.-current pairs-ref) (assoc (.-current pairs-ref) r m))))
    (let [latlngs (map (fn [{:keys [lat lng]}] (js/L.latLng lat lng))
                       (keep :latlng (keys (.-current pairs-ref))))
          bounds  (if (seq latlngs)
                    (js/L.latLngBounds (clj->js latlngs))
                    singapore-bounds)]
      (.fitBounds lmap bounds))))

(defn render-map [el]
  (let [lmap-options #js {:preferCanvas true
                          :fadeAnimation true}
        tile-layer (js/L.tileLayer
                    "https://www.onemap.gov.sg/maps/tiles/GreyLite/{z}/{x}/{y}.png"
                    (clj->js {:minZoom 11
                              :maxZoom 19
                              :attribution "<img src=\"https://www.onemap.gov.sg/web-assets/images/logo/om_logo.png\" style=\"height:20px;width:20px;\"/>&nbsp;<a href=\"https://www.onemap.gov.sg/\" target=\"_blank\" rel=\"noopener noreferrer\">OneMap</a>&nbsp;&copy;&nbsp;contributors&nbsp;&#124;&nbsp;<a href=\"https://www.sla.gov.sg/\" target=\"_blank\" rel=\"noopener noreferrer\">Singapore Land Authority</a>"}))
        lmap (js/L.map el lmap-options)]
    (.addLayer lmap tile-layer)
    (.setPrefix (.-attributionControl lmap) "<a href=\"https://www.leafletjs.com\">Leaflet</a>")
    (.fitBounds lmap singapore-bounds)
    lmap))

(defn the-map [{:keys [recommendations]}]
  (let [map-el-ref (react/useRef nil)
        map-ref    (react/useRef nil)
        pairs-ref  (react/useRef {})]
    (react/useEffect
     (fn []
       (when-let [el (.-current map-el-ref)]
         (set! (.-current map-ref) (render-map el)))
       (fn []
         (when-let [m (.-current map-ref)]
           (.remove m)
           (set! (.-current map-ref) nil)
           (set! (.-current pairs-ref) {}))))
     #js [])
    (react/useEffect
     (fn []
       (when-let [m (.-current map-ref)]
         (update-map m pairs-ref recommendations)
         (when (seq recommendations)
           (scroll-to-id "recommendation-map")))
       js/undefined)
     #js [recommendations])
    [:div#recommendation-map {:ref map-el-ref :style #js {:height "100vh"}}]))

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
     [:f> the-map {:recommendations recommendations}]]))
