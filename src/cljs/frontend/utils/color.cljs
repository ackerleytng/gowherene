(ns frontend.utils.color)

(defn build-color [url]
  (let [number (mod (hash url) 0xffffff)
        hex (.toString number 16)
        color (subs hex 0 6)]
    (str "#" color)))
