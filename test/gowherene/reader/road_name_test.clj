(ns gowherene.reader.road-name-test
  (:require [clojure.test :refer :all]
            [gowherene.reader.road-name :refer :all]))

(deftest choose-best-test
  (testing "choose-best applies choosers correctly"
    (is (= 3 (choose-best [1 4 3] [#(first (sort > (filter odd? %)))])))
    (is (= 4 (choose-best [1 4 3] [#(first (sort > (filter even? %)))]))))

  (testing "choose-best applies choosers in order"
    (is (= 3 (choose-best [1 4 3]
                          [;; Pick the biggest odd number
                           #(first (sort > (filter odd? %)))
                           ;; Otherwise pick the biggest even number
                           #(first (sort > (filter even? %)))])))
    ;; Now flip the priority
    (is (= 4 (choose-best [1 4 3]
                          [#(first (sort > (filter even? %)))
                           #(first (sort > (filter odd? %)))]))))

  (testing "choose-best picks the first one if none of the earlier ones work out"
    (is (= 1 (choose-best [1 9 3] [])))
    (is (= 1 (choose-best [1 9 3]
                          [#(first (sort > (filter even? %)))
                           (fn [v] (first (sort > (filter #(zero? (mod % 5)) v))))])))))

(deftest has-building-number-before-test
  (testing "has-building-number-before"
    (is (boolean (has-building-number-before "Orchard Road" "123 Orchard Road")))
    (is (boolean (has-building-number-before "Orchard Road" "123   Orchard Road")))
    (is (boolean (has-building-number-before "Orchard Road" "#02-238 3 Orchard Road")))
    (is (not (has-building-number-before "Orchard Road" "#02-238 Orchard Road")))))

(deftest road-name-test
  (testing "case and space should not matter"
    (is (= "collyer quay" (road-name "collyer quay")))
    (is (= "Collyer Quay" (road-name "Collyer Quay")))
    (is (= "Collyer  Quay" (road-name "Collyer  Quay")))
    (is (= "collyer quay" (road-name "12 collyer quay")))
    (is (nil? (road-name "some other content that does not contain name")))
    (is (nil? (road-name ""))))
  (testing "will get the first road name if there is more than one"
    (is (= "Raffles Place" (road-name "Raffles Place")))
    ;; Does not exactly match "Collyer Quay", but that's cos this is just a heuristic
    (is (= "To see Collyer Quay" (road-name "You could take a little trip around Singapore town
In Singapore city bus
To see Collyer Quay and Raffles Place
The Esplanade and all of us"))))
  (testing "match whole words in road names"
    ;; This should not return "A dempsey road"
    (is (= "dempsey road" (road-name "18A dempsey road"))))
  (testing "match whole words for suffixes"
    ;; This should not return "singapore cl"
    (is (nil? (road-name "singapore clubs"))))
  (testing "match whole words for prefixes"
    ;; This should not return "mt test"
    (is (nil? (road-name "filamt test"))))
  (testing "other characters in names"
    (is (= "St Andrew’s Road" (road-name "St Andrew’s Road")))
    (is (= "St. Andrew’s Road" (road-name "St. Andrew’s Road"))))
  (testing "Preferentially selects road names that has a building number in front of it"
    (is (= "Orchard Road" (road-name "#08-01/02/03, Orchard Central, 181 Orchard Road |")))))

(deftest english-road-names
  (are [name*] (= name* (road-name name*))
    ;; Road names mentioned in https://en.wikipedia.org/wiki/Road_names_in_Singapore
    "Sembawang Alley"
    "Bedok North Avenue 1"
    "Laurel Wood Avenue"
    "Pearl Bank"
    "Siglap Bank"
    "Raffles Boulevard"
    "Stadium Boulevard"
    "Compassvale Bow"
    "Bukit Merah Central"
    "Jurong West Central 1"
    "Joo Koon Circle"
    "Sunbird Circle"
    "Tuas View Circuit"
    "Newton Circus"
    "Pioneer Circus"
    "Anchorvale Close"
    "Seletar Close"
    "Tampines Concourse"
    "Seletar Court"
    "Dakota Crescent"
    "Marine Crescent"
    "Rhu Cross"
    "Woodlands Crossing"
    "Choa Chu Kang Drive"
    "Woodlands Drive 60"
    "Punggol East"
    "Toa Payoh East"
    "Mandai Estate"
    "Swiss Cottage Estate"
    "Kallang-Paya Lebar Expressway"
    "Pan Island Expressway"
    "Murai Farmway"
    "Pasir Ris Farmway 1"
    "Punggol Field"
    "Eng Kong Garden"
    "Pandan Gardens"
    "Eng Kong Garden"
    "Pandan Gardens"
    "Hyde Park Gate"
    "Sultan Gate"
    "one-north Gateway"
    "Sentosa Gateway"
    "Tampines Grande"
    "Buangkok Green"
    "River Valley Green"
    "Faber Grove"
    "Tukang Innovation Grove"
    "Springleaf Height"
    "Telok Blangah Heights"
    "Springleaf Height"
    "Telok Blangah Heights"
    "Nicoll Highway"
    "West Coast Highway"
    "Ann Siang Hill"
    "Paterson Hill"
    "Coral Island"
    "Sandy Island"
    "Bedok Junction"
    "Kallang Junction"
    "Chai Chee Lane"
    "Lim Chu Kang Lane 1"
    "Buangkok Link"
    "Tuas Link 1"
    "Kranji Loop"
    "Senoko Loop"
    "Cuppage Mall"
    "Marina Mall"
    "Sentosa East Mall"
    "Mount Elizabeth"
    "Mount Sophia"
    "Choa Chu Kang North 5"
    "Toa Payoh North"
    "Draycott Park"
    "Ming Teck Park"
    "East Coast Parkway"
    "Chinese Cemetery Path 1"
    "Muslim Cemetery Path 1"
    "Boon Lay Place"
    "Sims Place"
    "Edgedale Plains"
    "Lentor Plain"
    "Edgedale Plains"
    "Lentor Plain"
    "Goldhill Plaza"
    "Tanjong Pagar Plaza"
    "Kim Seng Promenade"
    "Collyer Quay"
    "North Boat Quay"
    "Tanah Merah Kechil Ridge"
    "Thomson Ridge"
    "Stagmont Ring"
    "Changi North Rise"
    "Simei Rise"
    "Bishan Road"
    "Upper Jurong Road"
    "Kallang Sector"
    "Pioneer Sector 1"
    "Sector A Sin Ming Industrial Estate"
    "Whampoa South"
    "Ellington Square"
    "Sengkang Square"
    "Bishan Street 11"
    "Victoria Street"
    "Joo Chiat Terrace"
    "Pearl's Hill Terrace"
    "Mandai Lake Road - Track 9"
    "Turut Track"
    "Engku Aman Turn"
    "Orchard Turn"
    "Clifton Vale"
    "Sunset Vale"
    "Nanyang Valley"
    "Pandan Valley"
    "Tanjong Rhu View"
    "Ubi View"
    "Changi Business Park Vista"
    "Keppel Bay Vista"
    "Compassvale Walk"
    "Paya Lebar Walk"
    "Canberra Way"
    "Shenton Way"
    "Toa Payoh West"
    "Whampoa West"
    "Saint Anne's Wood"
    "Bukit Ayer Molek"
    "Bukit Purmei"
    "Jalan Besar"
    "Jalan Jurong Kechil"
    "Kampong Bugis"
    "Kampong Sireh"
    "Lengkok Angsa"
    "Lengkok Merak"
    "Lengkong Dua"
    "Lengkong Lima"
    "Lorong 1 Toa Payoh"
    "Lorong Chuan"
    "Lorong Halus"
    "Padang Chancery"
    "Padang Jeringau"
    "Taman Ho Swee"
    "Taman Warna"
    "Tanjong Penjuru"
    ;; Road names with modifiers
    "Happy Avenue Central"
    "Happy Avenue East"
    "Happy Avenue North"
    "Happy Avenue West"
    "Bartley Road East"
    "Lower Delta Road"
    "New Upper Changi Road"
    "Sturdee Road North"
    "Old Choa Chu Kang Road"
    "Tanjong Katong Road South"
    "Upper Bukit Timah Road"
    "Admiralty Road West"
    ;; Road names without any generic element
    "Geylang Bahru"
    "Geylang Serai"
    "Kallang Bahru"
    "Kallang Tengah"
    "Wholesale Centre"
    ;; Road names with the definite article "the"
    "The Inglewood"
    "The Knolls"
    "The Oval"
    ;; Road names that consist of a single word
    "Bishopsgate"
    "Causeway"
    "Piccadilly"
    "Queensway"))
