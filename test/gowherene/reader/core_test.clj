(ns gowherene.reader.core-test
  (:require [clojure.test :refer :all]
            [clojure.pprint :refer [pprint]]
            [hickory.core :refer [as-hickory parse]]
            [gowherene.reader.core :refer :all]
            [gowherene.reader.geocodables :refer [find-postal-codes]]
            [gowherene.reader.regexes :refer :all]))

(deftest test-location-subset?
  (testing "location-subset?"
    (is (location-subset?
         {:postal-code nil :unit-number nil :road-name nil :building-number nil}
         {:postal-code nil :unit-number nil :road-name nil :building-number nil}))
    (is (location-subset?
         {:postal-code "048621" :unit-number nil :road-name nil :building-number nil}
         {:postal-code "048621" :unit-number nil :road-name nil :building-number nil}))
    (is (location-subset?
         {:postal-code "048621" :unit-number nil :road-name nil :building-number nil}
         {:postal-code "048621" :unit-number "#B1-K1" :road-name "Raffles Place" :building-number "24"}))
    (is (not (location-subset?
              {:postal-code "048621" :unit-number "#B1-K1" :road-name "Raffles Place" :building-number "24"}
              {:postal-code "048621" :unit-number nil :road-name nil :building-number nil})))
    (is (location-subset?
         {:postal-code nil :unit-number "#B1-K1" :road-name nil :building-number nil}
         {:postal-code "048621" :unit-number "#B1-K1" :road-name "Raffles Place" :building-number "24"}))
    (is (not (location-subset?
              {:postal-code "048621" :unit-number "#B1-K1" :road-name "Raffles Place" :building-number "24"}
              {:postal-code "013432" :unit-number "#B1-K1" :road-name "Raffles Place" :building-number "24"})))
    (is (not (location-subset?
              {:postal-code "012342" :unit-number nil :road-name nil :building-number nil}
              {:postal-code "048621" :unit-number "#B1-K1" :road-name "Raffles Place" :building-number "24"})))
    (is (not (location-subset?
              {:postal-code "048621" :unit-number "#B1-K1" :road-name "Raffles Place" :building-number "24"}
              {:postal-code nil :unit-number "#B2-39" :road-name nil :building-number nil})))
    (is (not (location-subset?
              {:postal-code "048621" :unit-number nil :road-name nil :building-number nil}
              {:postal-code nil :unit-number "#B2-39" :road-name nil :building-number nil})))))

(deftest test-dedupe-by-location
  (testing "sanity"
    (is (= []
           (dedupe-by-location [])))
    (is (= [{:label "1. The Brat", :location {:postal-code "048621", :unit-number "#B1-K1", :road-name "Raffles Place", :building-number "24"}, :latlng {:lat 1.2838559, :lng 103.85213}, :index 1}]
           (dedupe-by-location [{:label "1. The Brat", :location {:postal-code "048621", :unit-number "#B1-K1", :road-name "Raffles Place", :building-number "24"}, :latlng {:lat 1.2838559, :lng 103.85213}, :index 1}]))))

  (testing "exactly the same location"
    (is (= [{:type :labelled, :value "18A Dempsey Road, Singapore 249677 Opening Hours: Tues to Fri: 11am - 7pm | Sat, Sun & Public Holidays: 9.30am - 7pm (Closed on Mondays)", :location {:postal-code "249677", :unit-number nil, :road-name "Dempsey Road", :building-number nil}, :label "10. Huber's Butchery & Bistro @ Dempsey"}]
           (dedupe-by-location
            [{:type :postal-code, :value "249677", :location {:postal-code "249677", :unit-number nil, :road-name "Dempsey Road", :building-number nil}, :label "10. Huber's Butchery & Bistro @ Dempsey"}
             {:type :labelled, :value "18A Dempsey Road, Singapore 249677 Opening Hours: Tues to Fri: 11am - 7pm | Sat, Sun & Public Holidays: 9.30am - 7pm (Closed on Mondays)", :location {:postal-code "249677", :unit-number nil, :road-name "Dempsey Road", :building-number nil}, :label "10. Huber's Butchery & Bistro @ Dempsey"}]))))
  (testing "dedupe-by-location"
    (is (=
         (set [{:label "1. The Brat", :location {:postal-code "048621", :unit-number "#B1-K1", :road-name "Raffles Place", :building-number "24"}, :latlng {:lat 1.2838559, :lng 103.85213}, :index 1}
               {:label "1. The Brat", :location {:postal-code nil, :unit-number "#B2-39", :road-name "Orchard Road", :building-number "68"}, :latlng {:lat 1.3007475, :lng 103.84505}, :index 1}])
         (set (dedupe-by-location
               [{:label "1. The Brat", :location {:postal-code "048621", :unit-number nil, :road-name nil, :building-number nil}, :latlng {:lat 1.2838559, :lng 103.85213}, :index 1}
                {:label "1. The Brat", :location {:postal-code nil, :unit-number "#B2-39", :road-name "Orchard Road", :building-number "68"}, :latlng {:lat 1.3007475, :lng 103.84505}, :index 1}
                {:label "1. The Brat", :location {:postal-code "048621", :unit-number "#B1-K1", :road-name "Raffles Place", :building-number "24"}, :latlng {:lat 1.2838559, :lng 103.85213}, :index 1}]))))
    (is (=
         (set [{:label "1. The Brat", :location {:postal-code "048621", :unit-number "#B1-K1", :road-name "Raffles Place", :building-number "24"}, :latlng {:lat 1.2838559, :lng 103.85213}, :index 1}
               {:label "1. The Brat", :location {:postal-code nil, :unit-number "#B2-39", :road-name "Orchard Road", :building-number "68"}, :latlng {:lat 1.3007475, :lng 103.84505}, :index 1}])
         (set (dedupe-by-location
               [{:label "1. The Brat", :location {:postal-code "048621", :unit-number "#B1-K1", :road-name "Raffles Place", :building-number "24"}, :latlng {:lat 1.2838559, :lng 103.85213}, :index 1}
                {:label "1. The Brat", :location {:postal-code nil, :unit-number "#B2-39", :road-name "Orchard Road", :building-number "68"}, :latlng {:lat 1.3007475, :lng 103.84505}, :index 1}
                {:label "1. The Brat", :location {:postal-code "048621", :unit-number nil, :road-name nil, :building-number nil}, :latlng {:lat 1.2838559, :lng 103.85213}, :index 1}]))))))

(deftest integration-test-find-postal-codes
  (testing "find-postal-codes"
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

(deftest integration-test-process-clean-zipper
  (testing "process-clean-zipper"
    (are [path expected-by-type] (let [processed (->> (slurp path)
                                                      hickory-zipper
                                                      cleanup
                                                      process-clean-zipper)]
                                   (= expected-by-type
                                      (->> processed
                                           (group-by :type)
                                           (map (fn [[k v]] [k (count v)]))
                                           (into {}))))
      "data/files/the-ultimate-guide-to-local-breakfast-in-singapore.html"
      {:postal-code 10}
      "data/files/dim-sum-restaurants-singapore.html"
      {:postal-code 25}
      "data/files/affordable-seafood-buffets.html"
      {:postal-code 8}
      "data/files/singapore-cafes-with-no-gst.html"
      {:postal-code 15, :labelled 15}
      "data/files/best-burgers.html"
      {:postal-code 13, :labelled 24}
      "data/files/cheap-food-orchard.html"
      {:postal-code 23, :labelled 23}
      "data/files/cheap-orchard-buffets.html"
      {:postal-code 10, :labelled 10}
      "data/files/no-gst-restaurants.html"
      {:postal-code 17, :labelled 18}
      "data/files/snippets-the-best-buffets-in-singapore-the-definitive-guide-because-you-shouldnt-waste-your-calories-and-moolah.html"
      {:postal-code 9, :labelled 9}
      "data/files/tiffany-singapore.html"
      {:postal-code 7}
      "data/files/local-breakfast-east-singapore.html"
      {:postal-code 27, :labelled 27}
      "data/files/11-budget-buffets-in-singapore-20-and-below.html"
      {:postal-code 14, :labelled 10})))

(defn get-index
  [header]
  (and
   header
   (when-let [num (re-find #"(\d+)\." header)]
     (Integer/parseInt (get num 1)))))

(deftest integration-test-cheap-food-orchard
  (testing "cheap-food-orchard"
    (let [page (slurp "data/files/cheap-food-orchard.html")
          response (process page)]
      (is (= 23 (count response))
          "Check number of items parsed")
      (is (= (range 1 23)
             (sort (->> response
                        (map (comp get-index :label))
                        (filter identity))))
          "All headers were parsed, beginning from 1. to 22. (23rd one is a bonus)"))))

(deftest integration-test-no-gst-restaurants
  (testing "no-gst-restaurants"
    (let [page (slurp "data/files/no-gst-restaurants.html")
          response (process page)]
      (is (= 18 (count response))
          "Check number of items parsed")
      (is (= (range 1 19)
             (sort (->> response
                        (map (comp get-index :label))
                        (filter identity))))
          "All headers were parsed, beginning from 1. to 18.")      )))

(deftest integration-test-the-ultimate-guide-to-local-breakfast-in-singapore
  (testing "the-ultimate-guide-to-local-breakfast-in-singapore"
    (let [page (slurp "data/files/the-ultimate-guide-to-local-breakfast-in-singapore.html")
          response (process page)]
      (is (= 10 (count response))
          "Check number of items parsed"))))

(deftest integration-test-local-breakfast-east-singapore
  (testing "local-breakfast-east-singapore"
    (let [page (slurp "data/files/local-breakfast-east-singapore.html")
          response (process page)]
      (is (= 27 (count response))
          "Check number of items parsed"))))

(deftest integration-test-singapore-cafes-with-no-gst
  (testing "singapore-cafes-with-no-gst"
    (let [page (slurp "data/files/singapore-cafes-with-no-gst.html")
          response (process page)]
      (is (= 15 (count response))
          "Check number of items parsed")
      (is (= (range 1 16)
             (sort (->> response
                        (map (comp get-index :label))
                        (filter identity))))
          "All headers were parsed, beginning from 1. to 15."))))

(deftest integration-test-tiffany-singapore
  (testing "tiffany-singapore"
    (let [page (slurp "data/files/tiffany-singapore.html")
          response (process page)]
      (is (= 7 (count response))
          "Check number of items parsed"))))

(deftest integration-test-best-burgers
  (testing "best-burgers"
    (let [page (slurp "data/files/best-burgers.html")
          response (process page)]
      (is (= '({1 2}
               {2 1}
               {3 1}
               {4 1}
               {5 1}
               {6 1}
               {7 1}
               {8 4}
               {9 1}
               {10 1}
               {11 1}
               {12 3}
               {13 4}
               {14 1}
               {15 1})
             (->> response
                  (map #(assoc % :index (get-index (:label %))))
                  (group-by :index)
                  seq
                  (sort-by first)
                  (map (fn [[k v]] {k (count v)}))))))))
