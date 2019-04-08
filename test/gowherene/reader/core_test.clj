(ns gowherene.reader.core-test
  (:require [clojure.test :refer :all]
            [clojure.pprint :refer [pprint]]
            [hickory.core :refer [as-hickory parse]]
            [medley.core :refer [take-upto distinct-by]]
            [gowherene.reader.core :refer :all]
            [gowherene.reader.geocodables :refer [find-postal-codes]]
            [gowherene.reader.regexes :refer :all]))

(deftest integration-test-find-postal-codes
  (testing "find results in no-gst-restaurants"
    (are [path expected] (let [page-zipper (->> (slurp path)
                                                hickory-zipper
                                                cleanup)]
                           (= expected
                              (->> (find-postal-codes page-zipper)
                                   (map #(dissoc % :loc)))))
      "data/files/11-budget-buffets-in-singapore-20-and-below.html"
      '({:type :postal-code, :value "307591"}
        {:type :postal-code, :value "188096"}
        {:type :postal-code, :value "238896"}
        {:type :postal-code, :value "227968"}
        {:type :postal-code, :value "238839"}
        {:type :postal-code, :value "307683"}
        {:type :postal-code, :value "099253"}
        {:type :postal-code, :value "545724"}
        {:type :postal-code, :value "119620"}
        {:type :postal-code, :value "238858"}
        {:type :postal-code, :value "419882"}
        {:type :postal-code, :value "239571"}
        {:type :postal-code, :value "238896"}
        {:type :postal-code, :value "238851"})

      "data/files/affordable-seafood-buffets.html"
      '({:type :postal-code, :value "238896"}
        {:type :postal-code, :value "638366"}
        {:type :postal-code, :value "769198"}
        {:type :postal-code, :value "059221"}
        {:type :postal-code, :value "307591"}
        {:type :postal-code, :value "179103"}
        {:type :postal-code, :value "117540"}
        {:type :postal-code, :value "117540"})

      "data/files/best-burgers.html"
      '({:type :postal-code, :value "048621"}
        {:type :postal-code, :value "179103"}
        {:type :postal-code, :value "569983"}
        {:type :postal-code, :value "238896"}
        {:type :postal-code, :value "249677"}
        {:type :postal-code, :value "078867"}
        {:type :postal-code, :value "419502"}
        {:type :postal-code, :value "018982"}
        {:type :postal-code, :value "287994"}
        {:type :postal-code, :value "428765"}
        {:type :postal-code, :value "189203"}
        {:type :postal-code, :value "049745"}
        {:type :postal-code, :value "188535"})

      "data/files/cheap-food-orchard.html"
      '({:type :postal-code, :value "238896"}
        {:type :postal-code, :value "238872"}
        {:type :postal-code, :value "228213"}
        {:type :postal-code, :value "238872"}
        {:type :postal-code, :value "238895"}
        {:type :postal-code, :value "238840"}
        {:type :postal-code, :value "238863"}
        {:type :postal-code, :value "238843"}
        {:type :postal-code, :value "238872"}
        {:type :postal-code, :value "228213"}
        {:type :postal-code, :value "238895"}
        {:type :postal-code, :value "238895"}
        {:type :postal-code, :value "238875"}
        {:type :postal-code, :value "237978"}
        {:type :postal-code, :value "238855"}
        {:type :postal-code, :value "238863"}
        {:type :postal-code, :value "238896"}
        {:type :postal-code, :value "238852"}
        {:type :postal-code, :value "239695"}
        {:type :postal-code, :value "238896"}
        {:type :postal-code, :value "228213"}
        {:type :postal-code, :value "228796"}
        {:type :postal-code, :value "188306"})

      "data/files/no-gst-restaurants.html"
      ;; find-postal-codes will miss 16. One Place Western Bistro and Bar
      ;;   because its address is malformed
      '({:type :postal-code, :value "247781"}
        {:type :postal-code, :value "179103"}
        {:type :postal-code, :value "258748"}
        {:type :postal-code, :value "679697"}
        {:type :postal-code, :value "259569"}
        {:type :postal-code, :value "169339"}
        {:type :postal-code, :value "419896"}
        {:type :postal-code, :value "459114"}
        {:type :postal-code, :value "597626"}
        {:type :postal-code, :value "069043"}
        {:type :postal-code, :value "228213"}
        {:type :postal-code, :value "259569"}
        {:type :postal-code, :value "188017"}
        {:type :postal-code, :value "188392"}
        {:type :postal-code, :value "187967"}
        {:type :postal-code, :value "427677"}
        {:type :postal-code, :value "247964"})

      "data/files/dim-sum-restaurants-singapore.html"
      '({:type :postal-code, :value "387449"}
        {:type :postal-code, :value "188720"}
        {:type :postal-code, :value "099253"}
        {:type :postal-code, :value "039801"}
        {:type :postal-code, :value "238872"}
        {:type :postal-code, :value "198759"}
        {:type :postal-code, :value "238872"}
        {:type :postal-code, :value "238879"}
        {:type :postal-code, :value "237994"}
        {:type :postal-code, :value "188966"}
        {:type :postal-code, :value "228221"}
        {:type :postal-code, :value "238857"}
        {:type :postal-code, :value "048421"}
        {:type :postal-code, :value "228209"}
        {:type :postal-code, :value "160054"}
        {:type :postal-code, :value "208533"}
        {:type :postal-code, :value "188973"}
        {:type :postal-code, :value "249715"}
        {:type :postal-code, :value "310181"}
        {:type :postal-code, :value "208882"}
        {:type :postal-code, :value "238801"}
        {:type :postal-code, :value "238839"}
        {:type :postal-code, :value "307506"}
        {:type :postal-code, :value "189652"}
        {:type :postal-code, :value "178957"})

      "data/files/local-breakfast-east-singapore.html"
      '({:type :postal-code, :value "429356"}
        {:type :postal-code, :value "428903"}
        {:type :postal-code, :value "460018"}
        {:type :postal-code, :value "460016"}
        {:type :postal-code, :value "390051"}
        {:type :postal-code, :value "390051"}
        {:type :postal-code, :value "523201"}
        {:type :postal-code, :value "521137"}
        {:type :postal-code, :value "460216"}
        {:type :postal-code, :value "521137"}
        {:type :postal-code, :value "460208"}
        {:type :postal-code, :value "460511"}
        {:type :postal-code, :value "500002"}
        {:type :postal-code, :value "460084"}
        {:type :postal-code, :value "521137"}
        {:type :postal-code, :value "520827"}
        {:type :postal-code, :value "428829"}
        {:type :postal-code, :value "460207"}
        {:type :postal-code, :value "402004"}
        {:type :postal-code, :value "390051"}
        {:type :postal-code, :value "390051"}
        {:type :postal-code, :value "390051"}
        {:type :postal-code, :value "500002"}
        {:type :postal-code, :value "500002"}
        {:type :postal-code, :value "460216"}
        {:type :postal-code, :value "460216"}
        {:type :postal-code, :value "427784"})

      "data/files/no-gst-restaurants.html"
      '({:type :postal-code, :value "247781"}
        {:type :postal-code, :value "179103"}
        {:type :postal-code, :value "258748"}
        {:type :postal-code, :value "679697"}
        {:type :postal-code, :value "259569"}
        {:type :postal-code, :value "169339"}
        {:type :postal-code, :value "419896"}
        {:type :postal-code, :value "459114"}
        {:type :postal-code, :value "597626"}
        {:type :postal-code, :value "069043"}
        {:type :postal-code, :value "228213"}
        {:type :postal-code, :value "259569"}
        {:type :postal-code, :value "188017"}
        {:type :postal-code, :value "188392"}
        {:type :postal-code, :value "187967"}
        {:type :postal-code, :value "427677"}
        {:type :postal-code, :value "247964"})

      "data/files/snippets-the-best-buffets-in-singapore-the-definitive-guide-because-you-shouldnt-waste-your-calories-and-moolah.html"
      '({:type :postal-code, :value "039595"}
        {:type :postal-code, :value "188966"}
        {:type :postal-code, :value "169631"}
        {:type :postal-code, :value "238857"}
        {:type :postal-code, :value "249715"}
        {:type :postal-code, :value "238867"}
        {:type :postal-code, :value "238883"}
        {:type :postal-code, :value "039797"}
        {:type :postal-code, :value "247911"})

      "data/files/the-ultimate-guide-to-local-breakfast-in-singapore.html"
      '({:type :postal-code, :value "428903"}
        {:type :postal-code, :value "289876"}
        {:type :postal-code, :value "570024"}
        {:type :postal-code, :value "188211"}
        {:type :postal-code, :value "460207"}
        {:type :postal-code, :value "402001"}
        {:type :postal-code, :value "311125"}
        {:type :postal-code, :value "207671"}
        {:type :postal-code, :value "199660"}
        {:type :postal-code, :value "390034"})

      "data/files/tiffany-singapore.html"
      '({:type :postal-code, :value "238872"}
        {:type :postal-code, :value "238801"}
        {:type :postal-code, :value "819643"}
        {:type :postal-code, :value "819663"}
        {:type :postal-code, :value "018972"}
        {:type :postal-code, :value "098269"}
        {:type :postal-code, :value "228220"}))))

(deftest test-retain-longer-names
  (testing "retain longer names"
    (is (= [{:place "Unrelated"
             :address "address"}

            {:place "Weirdzzzz"
             :address "address"}

            {:place "Totally Different"
             :address "address"}
            {:place "Short Name Longer Complete"
             :address "address"}]
           (retain-longer-names [{:place "Unrelated"
                                  :address "address"}
                                 {:place "Weirdzzzz"
                                  :address "address"}
                                 {:place "Short Name"
                                  :address "address"}
                                 {:place "Short Name Longer"
                                  :address "address"}
                                 {:place "Totally Different"
                                  :address "address"}
                                 {:place "Short Name Longer Complete"
                                  :address "address"}])))))

(deftest test-reader-cheap-food-orchard
  (testing "cheap-food-orchard"
    (let [page (slurp "test/gowherene/reader/fixtures/cheap-food-orchard")
          response (process page)]

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
    (let [page (slurp "test/gowherene/reader/fixtures/no-gst-restaurants")
          hickory (->> page
                       parse
                       as-hickory)
          response (process page)

          tags-removed (remove-tags uninteresting-tags hickory)
          postal-code-locs (get-postal-code-locs tags-removed)

          loc (first postal-code-locs)

          data (gather-address-info hickory)]

      ;; There shouldn't be any blank addresses
      (is (= (count (filter (comp clojure.string/blank? :address) data)) 0))

      ;; Check that remove-tags has some effect
      ;;   More tags appear here than in uninteresting-tags because
      ;;     child tags of uninteresting-tags are also removed
      (is (= #{:hr :script :iframe :ins :footer :header :title :style :head :link
               :noscript :base :nav :img}
             (clojure.set/difference (get-all-tags hickory)
                                     (get-all-tags tags-removed))))

      (is (= 18 (count response)))

      (is (= 0 (count (filter (comp nil? :latlng) response))))

      (is (= (range 1 19)
             (sort (->> response
                        (map :place)
                        (map get-index)
                        (filter identity))))))))

(deftest test-reader-the-ultimate-guide-to-local-breakfast-in-singapore
  (testing "the-ultimate-guide-to-local-breakfast-in-singapore"
    (let [page (slurp "test/gowherene/reader/fixtures/the-ultimate-guide-to-local-breakfast-in-singapore")
          response (process page)]
      (is (= 10 (count response)))

      (is (= 0 (count (filter (comp nil? :latlng) response))))

      (is (= '("188211" "199660" "207671" "289876" "311125"
               "390034" "402001" "428903" "460207" "570024")
             (sort (->> response
                        (map :address)
                        (map (partial re-find re-postal-code)))))))))

(deftest test-reader-local-breakfast-east-singapore
  (testing "local-breakfast-east-singapore"
    (let [page (slurp "test/gowherene/reader/fixtures/local-breakfast-east-singapore")
          response (process page)]
      (is (= 27 (count response)))

      (is (= 0 (count (filter (comp nil? :latlng) response))))

      (is (= '("Bedok Chwee Kueh"
               "Chai Chee Fried Carrot Cake"
               "Chin Mee Chin Confectionery"
               "Da Zhong Mei Shi Char Kway Teow"
               "Dong Ji Fried Kway Teow"
               "Enak"
               "Fu Yuan Minced Pork Noodle"
               "Glory Catering"
               "Hock Choon Laksa and Lor Mee"
               "Hon Ni Kitchen"
               "Hua Zai HK Style Roasted Delight Rice/Noodle and Muslim Food"
               "Jia Mei Wanton Mee"
               "Joo Chiat Chiap Kee"
               "Kim Choo Kueh Chang"
               "Lek Lim Nonya Cake Confectionery"
               "Mizzy Corner"
               "Mr and Mrs Mohgan’s Super Crispy Roti Prata"
               "Ru Ji Kitchen"
               "Song Han Carrot Cake"
               "Song Zhou Carrot Cake"
               "Tan Beng Otah Delights"
               "Tian Nan Xing Minced Pork Noodle"
               "Toast Hut"
               "Xin Mei Xiang Lor Mee"
               "Xing Yun Kway Chap"
               "Yong He Bak Chor Seafood Noodles"
               "Yummy Sarawak Kolo Mee")
             (->> response
                  (map :place)
                  sort))))))

(deftest test-reader-no-gst-cafes
  (testing "no-gst-cafes"
    (let [page (slurp "test/gowherene/reader/fixtures/no-gst-cafes")
          hickory (->> page
                       parse
                       as-hickory)
          response (process page)

          tags-removed (remove-tags uninteresting-tags hickory)
          postal-code-locs (get-postal-code-locs tags-removed)

          data (gather-address-info hickory)]

      ;; There shouldn't be any blank addresses
      (is (= (count (filter (comp clojure.string/blank? :address) data)) 0))

      ;; Check that remove-tags has some effect
      ;;   More tags appear here than in uninteresting-tags because
      ;;     child tags of uninteresting-tags are also removed
      (is (= #{:hr :script :iframe :ins :footer :header :title :style :head :link
               :noscript :base :nav :img}
             (clojure.set/difference (get-all-tags hickory)
                                     (get-all-tags tags-removed))))

      (is (= 15 (count response)))

      (is (= 0 (count (filter (comp nil? :latlng) response))))

      (is (= (range 1 16)
             (sort (->> response
                        (map :place)
                        (map get-index)
                        (filter identity))))))))

(deftest test-reader-tiffany
  (testing "tiffany-singapore"
    (let [hickory (->> "test/gowherene/reader/fixtures/tiffany-singapore"
                       slurp
                       parse
                       as-hickory)

          ;; gather-address-info, split up
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
    (let [page (slurp "test/gowherene/reader/fixtures/best-burgers")
          hickory (->> page
                       parse
                       as-hickory)

          tags-removed (remove-tags uninteresting-tags hickory)

          raw-result-before-geocoding (->> hickory
                                           gather-address-info
                                           (distinct-by (fn [d] [(:place d) (:address d)])))

          response (process page)]

      (testing "remove-tags has some effect"
        (is (= #{:script :iframe :ins :footer :header :title :style :head
                 :link :noscript :base :nav :img}
               (clojure.set/difference (get-all-tags hickory)
                                       (get-all-tags tags-removed)))))

      (is (= (count response) 24))

      ;; All the latlngs after processing were geocoded
      (is (= (count (filter (comp nil? :latlng) response)) 0))

      ;; All headers were parsed in raw result
      ;;   (Ensures no missing headers)
      (is (= (sort (->> raw-result-before-geocoding
                        (map :place)
                        (map get-index)
                        (filter identity)))
             '(1 1 2 3 4 5 6 7 8 8 8 8 9 10 11 12 12 12 13 13 13 13 14 15)))

      ;; All headers were parsed
      ;;   (Ensures no missing headers)
      (is (= (sort (->> response
                        (map :place)
                        (map get-index)
                        (filter identity)))
             '(1 1 2 3 4 5 6 7 8 8 8 8 9 10 11 12 12 12 13 13 13 13 14 15))))))
