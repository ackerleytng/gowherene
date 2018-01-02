(ns playout.reader.core-test
  (:require [clojure.test :refer :all]
            [clojure.pprint :refer [pprint]]
            [hickory.core :refer [as-hickory parse]]
            [playout.reader.core :refer :all]))

(deftest test-reader-cheap-food-orchard
  (testing "cheap-food-orchard"
    (let [hickory (->> (slurp "test/playout/reader/fixtures/cheap-food-orchard")
                       parse
                       as-hickory)
          response (process hickory)]

      ;; There are 23 items parsed
      (is (= (count response) 23))

      ;; There are 23 postal codes
      (is (= (count (filter (comp (partial re-find re-postal-code) :address) response)) 23))

      ;; All the latlngs after processing were geocoded
      (is (= (count (filter (comp nil? :latlng) response)) 0))

      ;; All headers were parsed, beginning from 1. to 22.
      ;;   (The 23rd one is a "Bonus")
      ;;   (Ensures no missing headers)
      (is (= (sort (->> response
                        (map :place)
                        (map get-index)
                        (filter identity)))
             (range 1 23)))

      ;; All addresses contain postal codes (true for this site)
      (is (= 23 (->> response
                     (map :address)
                     (map #(re-find re-postal-code %))
                     count))))))

(deftest test-reader-no-gst-restaurants
  (testing "no-gst-restaurants"
    (let [hickory (->> (slurp "test/playout/reader/fixtures/no-gst-restaurants")
                       parse
                       as-hickory)
          response (process hickory)

          tags-removed (remove-tags uninteresting-tags hickory)
          postal-code-locs (get-postal-code-locs tags-removed)

          loc (first postal-code-locs)

          data (hickory->data hickory)]

      ;; There shouldn't be any blank addresses
      (is (= (count (filter (comp clojure.string/blank? :address) data)) 0))

      ;; Check that remove-tags has some effect
      ;;   More tags appear here than in uninteresting-tags because
      ;;     child tags of uninteresting-tags are also removed
      (is (= #{:hr :script :iframe :ins :footer :header :title :style :head :link
               :noscript :base :nav :img}
             (clojure.set/difference (get-all-tags hickory)
                                     (get-all-tags tags-removed))))

      (is (= (count response) 18))

      (is (= (count (filter (comp nil? :latlng) response)) 0))

      (is (= (sort (->> response
                        (map :place)
                        (map get-index)
                        (filter identity)))
             (range 1 19))))))

(deftest test-reader-no-gst-cafes
  (testing "no-gst-cafes"
    (let [hickory (->> (slurp "test/playout/reader/fixtures/no-gst-cafes")
                       parse
                       as-hickory)
          response (process hickory)

          tags-removed (remove-tags uninteresting-tags hickory)
          postal-code-locs (get-postal-code-locs tags-removed)

          data (hickory->data hickory)]

      ;; There shouldn't be any blank addresses
      (is (= (count (filter (comp clojure.string/blank? :address) data)) 0))

      ;; Check that remove-tags has some effect
      ;;   More tags appear here than in uninteresting-tags because
      ;;     child tags of uninteresting-tags are also removed
      (is (= #{:hr :script :iframe :ins :footer :header :title :style :head :link
               :noscript :base :nav :img}
             (clojure.set/difference (get-all-tags hickory)
                                     (get-all-tags tags-removed))))

      (is (= (count response) 15))

      (is (= (count (filter (comp nil? :latlng) response)) 0))

      (is (= (sort (->> response
                        (map :place)
                        (map get-index)
                        (filter identity)))
             (range 1 16))))))

(deftest test-reader-tiffany
  (testing "tiffany-singapore"
    (let [hickory (->> "test/playout/reader/fixtures/tiffany-singapore"
                       slurp
                       parse
                       as-hickory)

          ;; hickory->data, split up
          tags-removed (remove-tags uninteresting-tags hickory)
          postal-code-locs (get-postal-code-locs tags-removed)

          data-pc-locs (map (partial tag-with :postal-code-loc) postal-code-locs)
          data-h-pc-locs (map (partial update-with-tag
                                       :header-loc :postal-code-loc get-earlier-header)
                              data-pc-locs)
          data-locs (filter :header-loc data-h-pc-locs)
          data-places (map (partial update-with-tag :place :header-loc loc->place) data-locs)
          data-places-addrs (mapcat (partial update-with-tag-seq
                                             :address :postal-code-loc
                                             loc->addresses)
                                    data-places)]

      ;; Check that remove-tags has some effect
      (is (= #{:script :iframe :footer :head :link :noscript :img}
             (clojure.set/difference (get-all-tags hickory)
                                     (get-all-tags tags-removed))))

      (is (= 6 (count postal-code-locs)))

      (is (= 6 (count (map :postal-code-loc data-pc-locs))))

      ;; Managed to find 6 headers
      (is (= 6 (count (filter :header-loc data-h-pc-locs))))
      (is (= 6 (count (filter :postal-code-loc data-h-pc-locs))))

      (is (= 6 (count data-locs)))

      (is (= 6 (count (filter :place data-places))))

      (is (= 6 (count (filter (comp (partial re-find re-postal-code) :address)
                              data-places-addrs)))))))

(deftest test-reader-best-burgers
  (testing "best-burgers"
    (let [hickory (->> (slurp "test/playout/reader/fixtures/best-burgers")
                       parse
                       as-hickory)

          tags-removed (remove-tags uninteresting-tags hickory)

          response (process hickory)]

      ;; Check that remove-tags has some effect
      (is (= #{:script :iframe :ins :footer :header :title :style :head
               :link :noscript :base :nav :img}
             (clojure.set/difference (get-all-tags hickory)
                                     (get-all-tags tags-removed))))

      (is (= (count response) 24))

      ;; All the latlngs after processing were geocoded
      (is (= (count (filter (comp nil? :latlng) response)) 0))

      ;; All headers were parsed
      ;;   (Ensures no missing headers)
      (is (= (sort (->> response
                        (map :place)
                        (map get-index)
                        (filter identity)))
             '(1 1 2 3 4 5 6 7 8 8 8 8 9 10 11 12 12 12 13 13 13 13 14 15))))))
