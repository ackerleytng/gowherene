(ns playout.reader.tagger-test
  (:require [clojure.test :refer :all]
            [clojure.pprint :refer [pprint]]
            [hickory.core :refer [parse parse-fragment as-hickory]]
            [hickory.zip :refer [hickory-zip]]
            [clojure.zip :as zip]
            [playout.reader.tagger :refer :all]))

(deftest test-find
  (let [addresses ["201 Henderson Road #07-05 Apex Singapore 159545"
                   "Orchard Central, 181 Orchard Road, #07-10/11, S(238896)"]]
    (testing "find-postal-code"
      (is (= "588179" (find-postal-code "588179")))
      (is (= "Singapore 159545" (find-postal-code (first addresses))))
      (is (= nil (find-postal-code "Singapore159545")))
      (is (= "S(238896)" (find-postal-code (second addresses))))
      (is (= "S (238896)" (find-postal-code "Orchard Road S (238896)"))))
    
    (testing "find-road-name"
      (is (= "Henderson Road" (find-road-name (first addresses))))
      (is (let [found (find-road-name (second addresses))]
            ;; Could depend on laziness? or maybe set
            ;;   TODO check why it is not deterministic
            (or (= found "Orchard Central") (= found "Orchard Road"))))
      (is (= "Bukit Purmei" (find-road-name "Bukit Purmei")))
      (is (= "Jln Besar" (find-road-name "Jln Besar")))
      (is (= nil (find-road-name "The Road")))
      (is (= "Themed Magic Road" (find-road-name "Themed Magic Road")))
      (is (= "Rather Road" (find-road-name "Rather Road")))
      (is (= "Bathe Road" (find-road-name "Bathe Road")))
      (is (= "Lorong Themed" (find-road-name "Lorong Themed")))
      (is (= "Lorong Rather" (find-road-name "Lorong Rather")))
      (is (= "Lorong Bathe" (find-road-name "Lorong Bathe")))
      (is (= "Lorong 1 Geylang" (find-road-name "Lorong 1 Geylang")))
      (is (= nil (find-road-name "Lorong 1zz Geylang")))
      (is (= nil (find-road-name "Lorong ab12n Geylang")))
      (is (= "Lengkok Bahru has" (find-road-name "Lengkok Bahru has the best")))
      (is (= nil (find-road-name "Taman")))
      (is (= "First Road" (find-road-name "First Road")))
      (is (= "Sesame Street" (find-road-name "112 Sesame Street")))
      (is (= "Woodlands Drive 14" (find-road-name "Woodlands Drive 14")))
      (is (= "The Inglewood" (find-road-name "nothing The Inglewood random")))
      (is (= "bishopsgate" (find-road-name "whatever bishopsgate nothing")))
      (is (= "lengkong tujoh" (find-road-name "whatever lengkong tujoh nothing")))
      (is (= "Saint Michael's Road" (find-road-name "07–03, Saint Michael's Road 328005"))))
    
    (testing "find-unit-number"
      (is (= "#07-05" (find-unit-number (first addresses))))
      (is (= "#07-10/11" (find-unit-number (second addresses))))
      (is (= "#01-05/06" (find-unit-number "#01-05/06")))
      (is (= "#01- 05/06" (find-unit-number "#01- 05/06")))
      (is (= "#01-05" (find-unit-number "#01-05")))
      (is (= "#1234-1123" (find-unit-number "#1234-1123")))
      (is (= "#01-05/06 and  #01-05/06" (find-unit-number "#01-05/06 and  #01-05/06")))
      (is (= "#01-05/06 And  #01-05/06" (find-unit-number "#01-05/06 And  #01-05/06")))
      (is (= "#01-05/06 & #01-05/06" (find-unit-number "#01-05/06 & #01-05/06")))
      ;; Allow some room for error here
      (is (= "#01-3//3" (find-unit-number "#01-3//3"))))
    
    (testing "find-house-number"
      (is (= "201" (find-house-number (first addresses))))
      (is (= "181" (find-house-number (second addresses))))
      (is (= "blk 31" (find-house-number "blk 31")))
      (is (= "Block 312" (find-house-number "Block 312")))
      (is (= nil (find-house-number "Block 12345")))
      (is (= "34" (find-house-number "34")))
      (is (= "34 a" (find-house-number "34 a Super Drive")))
      (is (= "34a" (find-house-number "34a Super Drive"))))
    )
  )

(deftest test-tagger-cheap-food
  (testing "stuff"
    (let [zipper (->> "<p><span style=\"color: #d47978;\"><strong>Address:</strong></span> Orchard Central, 181 Orchard Road, #07-10/11, S(238896)</p>"
                      parse-fragment
                      (map as-hickory)
                      first
                      hickory-zip
                      )]
      
      ;; (pprint (loc->path (zip/down (zip/down zipper))))
      ;; (reduce tag [] (get-all-locs zipper))
      )))

(deftest test-tagger-tiffany  
  (testing "stuff"
    (let [zipper (->> "<tr><td><span id=\"rptGroup_ctl01_dtlStoreList_ctl00_lblStoreName\" class=\"storeName\">ION Orchard</span><br> <span id=\"rptGroup_ctl01_dtlStoreList_ctl00_lblAddress1\">2 Orchard Turn<br>#01-21 and #02-11<br>Singapore 238801<br>+65 6884 4880<br>Open Daily: 10am- 10pm<br></span><div id=\"rptGroup_ctl01_dtlStoreList_ctl00_MapLink\"><ul id=\"contentLinkList\" class=\"storeList\" style=\"clear:both;margin-left:0\" visible=\"true\"><li class=\"bullet\"><a href=\"http://international.tiffany.com/jewelry-stores/ion-orchard-street\" onclick=\"window.location.href='https://international.tiffany.com/jewelry-stores/map/ion-orchard-street';return false\">View on Map</a></li></ul></div><br></td><td><span id=\"rptGroup_ctl01_dtlStoreList_ctl01_lblStoreName\" class=\"storeName\">Singapore Changi Airport Terminal 2</span><br> <span id=\"rptGroup_ctl01_dtlStoreList_ctl01_lblAddress1\">Departure/Transit Lounge South<br>Unit 026-078 Terminal 2<br>Singapore 819643<br>+65 6543 2443<br>Open Daily: 6:00am – 1:00am <br><br></span><div id=\"rptGroup_ctl01_dtlStoreList_ctl01_MapLink\"><ul id=\"contentLinkList\" class=\"storeList\" style=\"clear:both;margin-left:0\" visible=\"true\"><li class=\"bullet\"><a href=\"http://international.tiffany.com/jewelry-stores/singapore-changi-airport-terminal-2\" onclick=\"window.location.href='https://international.tiffany.com/jewelry-stores/map/singapore-changi-airport-terminal-2';return false\">View on Map</a></li></ul></div><br></td><td><span id=\"rptGroup_ctl01_dtlStoreList_ctl02_lblStoreName\" class=\"storeName\">Singapore Changi Airport Terminal 3</span><br> <span id=\"rptGroup_ctl01_dtlStoreList_ctl02_lblAddress1\">Departure/Transit Lounge South<br>Unit 02-18 Terminal 3<br>Singapore 819663<br>+65 6441 0018<br>Open Daily: 6:00am – 1:00am <br><br></span><div id=\"rptGroup_ctl01_dtlStoreList_ctl02_MapLink\"><ul id=\"contentLinkList\" class=\"storeList\" style=\"clear:both;margin-left:0\" visible=\"true\"><li class=\"bullet\"><a href=\"http://international.tiffany.com/jewelry-stores/singapore-changi-airport-terminal-3\" onclick=\"window.location.href='https://international.tiffany.com/jewelry-stores/map/singapore-changi-airport-terminal-3';return false\">View on Map</a></li></ul></div><br></td><td><span id=\"rptGroup_ctl01_dtlStoreList_ctl03_lblStoreName\" class=\"storeName\">The Shoppes at Marina Bay Sands</span><br> <span id=\"rptGroup_ctl01_dtlStoreList_ctl03_lblAddress1\">2 Bayfront Avenue #B2-66/67/68<br>Singapore 018972<br>+65 6688 7728<br><br>Sun to Thu: 10:30-23:00<br>Fri to Sat: 10:30-23:30<br></span><div id=\"rptGroup_ctl01_dtlStoreList_ctl03_MapLink\"><ul id=\"contentLinkList\" class=\"storeList\" style=\"clear:both;margin-left:0\" visible=\"true\"><li class=\"bullet\"><a href=\"http://international.tiffany.com/jewelry-stores/shoppes-marina-bay-sands\" onclick=\"window.location.href='https://international.tiffany.com/jewelry-stores/map/shoppes-marina-bay-sands';return false\">View on Map</a></li></ul></div><br></td></tr>"
                      parse-fragment
                      (map as-hickory)
                      hickory-zip
                      )]
      
      ;;(pprint zipper)
      )))
  

