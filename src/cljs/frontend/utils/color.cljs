(ns frontend.utils.color)

(defn build-color [url]
  (let [number (Math/abs (hash url))
        hex (.toString number 16)
        color (subs hex 0 6)]
    (str "#" color)))
