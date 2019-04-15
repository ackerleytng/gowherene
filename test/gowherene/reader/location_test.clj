(ns gowherene.reader.location-test
  (:require [clojure.test :refer :all]
            [clojure.zip :as zip]
            [hickory.zip :refer [hickory-zip]]
            [hickory.convert :refer [hiccup-to-hickory]]
            [gowherene.reader.location :refer :all]
            [gowherene.reader.road-name :refer [road-name]]
            [gowherene.reader.geocodables :refer [labelled-info]]))

(deftest test-postal-code
  (testing "postal-code"
    (are [string expected] (= expected (postal-code string))
      "258 Tanjong Katong Road |"
      nil

      "#08-01/02/03, Orchard Central, 181 Orchard Road |"
      nil

      "Refer"
      nil

      "101 Thomson Road, Singapore 307591 (United Square Outlet) | 6254 8123 | Opening Hours for Lunch: 11:30am ~ 2:30pm (Daily Except PH) for Dinner: Sun ~ Thur, PH : 5:30pm ~ 12am Fri, Sat, Day Before PH : 5:30pm ~ 2am"
      "307591"

      "7 Tan Quee Lan St, Singapore 188096 | Tel number: +65 63362875 | Open daily from 12pm – 12am"
      "188096"

      "181 Orchard Road, 08-09/10/11 Orchard Central, Singapore 238896 | Opening hours: Mon – Fri: 11:00 – 15:00 and 18:00 – 22:00 Sat – Sun: 11:30 – 22:30 |"
      "238896"

      ""
      nil

      "School Of The Arts Singapore, 1 Zubir Said Drive #01-04/07 Singapore 227968 (Dhoby Ghaut) | Tel: 6238 7218 | Opening hours: Mon – Fri: 11:30 – 14:30, Mon – Fri: 17:00 – 23:00 and Sat – Sun: 11:30 – 23:00"
      "227968"

      "Plaza Singapura #03-01 68 Orchard Road Singapore 238839 |"
      "238839"

      "1 Maritime Square #04-01 HarbourFront Centre"
      nil)))

(deftest test-unit-number
  (testing "unit-number"
    (are [string expected] (= expected (unit-number string))
      "258 Tanjong Katong Road |"
      nil

      "#08-01/02/03, Orchard Central, 181 Orchard Road |"
      "#08-01/02/03"

      "Refer"
      nil

      "101 Thomson Road, Singapore 307591 (United Square Outlet) | 6254 8123 | Opening Hours for Lunch: 11:30am ~ 2:30pm (Daily Except PH) for Dinner: Sun ~ Thur, PH : 5:30pm ~ 12am Fri, Sat, Day Before PH : 5:30pm ~ 2am"
      nil

      "7 Tan Quee Lan St, Singapore 188096 | Tel number: +65 63362875 | Open daily from 12pm – 12am"
      nil

      "181 Orchard Road, 08-09/10/11 Orchard Central, Singapore 238896 | Opening hours: Mon – Fri: 11:00 – 15:00 and 18:00 – 22:00 Sat – Sun: 11:30 – 22:30 |"
      "08-09/10/11"

      ""
      nil

      "School Of The Arts Singapore, 1 Zubir Said Drive #01-04/07 Singapore 227968 (Dhoby Ghaut) | Tel: 6238 7218 | Opening hours: Mon – Fri: 11:30 – 14:30, Mon – Fri: 17:00 – 23:00 and Sat – Sun: 11:30 – 23:00"
      "#01-04/07"

      "Plaza Singapura #03-01 68 Orchard Road Singapore 238839 |"
      "#03-01"

      "1 Maritime Square #04-01 HarbourFront Centre"
      "#04-01")))

(deftest test-road-name
  (testing "road-name, tested with real data"
    (are [string expected] (= expected (road-name string))
      "258 Tanjong Katong Road |"
      "Tanjong Katong Road"

      "#08-01/02/03, Orchard Central, 181 Orchard Road |"
      "Orchard Road"

      "Refer"
      nil

      "101 Thomson Road, Singapore 307591 (United Square Outlet) | 6254 8123 | Opening Hours for Lunch: 11:30am ~ 2:30pm (Daily Except PH) for Dinner: Sun ~ Thur, PH : 5:30pm ~ 12am Fri, Sat, Day Before PH : 5:30pm ~ 2am"
      "Thomson Road"

      "7 Tan Quee Lan St, Singapore 188096 | Tel number: +65 63362875 | Open daily from 12pm – 12am"
      "Tan Quee Lan St"

      "181 Orchard Road, 08-09/10/11 Orchard Central, Singapore 238896 | Opening hours: Mon – Fri: 11:00 – 15:00 and 18:00 – 22:00 Sat – Sun: 11:30 – 22:30 |"
      "Orchard Road"

      ""
      nil

      "School Of The Arts Singapore, 1 Zubir Said Drive #01-04/07 Singapore 227968 (Dhoby Ghaut) | Tel: 6238 7218 | Opening hours: Mon – Fri: 11:30 – 14:30, Mon – Fri: 17:00 – 23:00 and Sat – Sun: 11:30 – 23:00"
      "Zubir Said Drive"

      "Plaza Singapura #03-01 68 Orchard Road Singapore 238839 |"
      "Orchard Road"

      "1 Maritime Square #04-01 HarbourFront Centre"
      "Maritime Square")))

(deftest test-building-number
  (testing "building-number"
    (are [string road-name expected] (= expected (building-number string road-name))
      "258 Tanjong Katong Road |"
      "Tanjong Katong Road"
      "258"

      "#08-01/02/03, Orchard Central, 181 Orchard Road |"
      "Orchard Road"
      "181"

      "101 Thomson Road, Singapore 307591 (United Square Outlet) | 6254 8123 | Opening Hours for Lunch: 11:30am ~ 2:30pm (Daily Except PH) for Dinner: Sun ~ Thur, PH : 5:30pm ~ 12am Fri, Sat, Day Before PH : 5:30pm ~ 2am"
      "Thomson Road"
      "101"

      "7 Tan Quee Lan St, Singapore 188096 | Tel number: +65 63362875 | Open daily from 12pm – 12am"
      "Tan Quee Lan St"
      "7"

      "181 Orchard Road, 08-09/10/11 Orchard Central, Singapore 238896 | Opening hours: Mon – Fri: 11:00 – 15:00 and 18:00 – 22:00 Sat – Sun: 11:30 – 22:30 |"
      "Orchard Road"
      "181"

      "School Of The Arts Singapore, 1 Zubir Said Drive #01-04/07 Singapore 227968 (Dhoby Ghaut) | Tel: 6238 7218 | Opening hours: Mon – Fri: 11:30 – 14:30, Mon – Fri: 17:00 – 23:00 and Sat – Sun: 11:30 – 23:00"
      "Zubir Said Drive"
      "1"

      "Plaza Singapura #03-01 68 Orchard Road Singapore 238839 |"
      "Orchard Road"
      "68"

      "1 Maritime Square #04-01 HarbourFront Centre"
      "Maritime Square"
      "1")))

(deftest test-address-parts
  (testing "address-parts"
    (are [string expected] (= expected (address-parts string))
      "258 Tanjong Katong Road |"
      {:postal-code nil
       :unit-number nil
       :road-name "Tanjong Katong Road"
       :building-number "258"}

      "#08-01/02/03, Orchard Central, 181 Orchard Road |"
      {:postal-code nil
       :unit-number "#08-01/02/03"
       :road-name "Orchard Road"
       :building-number "181"}

      "Refer"
      {:postal-code nil
       :unit-number nil
       :road-name nil
       :building-number nil}

      "101 Thomson Road, Singapore 307591 (United Square Outlet) | 6254 8123 | Opening Hours for Lunch: 11:30am ~ 2:30pm (Daily Except PH) for Dinner: Sun ~ Thur, PH : 5:30pm ~ 12am Fri, Sat, Day Before PH : 5:30pm ~ 2am"
      {:postal-code "307591"
       :unit-number nil
       :road-name "Thomson Road"
       :building-number "101"}

      "7 Tan Quee Lan St, Singapore 188096 | Tel number: +65 63362875 | Open daily from 12pm – 12am"
      {:postal-code "188096"
       :unit-number nil
       :road-name "Tan Quee Lan St"
       :building-number "7"}

      "181 Orchard Road, 08-09/10/11 Orchard Central, Singapore 238896 | Opening hours: Mon – Fri: 11:00 – 15:00 and 18:00 – 22:00 Sat – Sun: 11:30 – 22:30 |"
      {:postal-code "238896"
       :unit-number "08-09/10/11"
       :road-name "Orchard Road"
       :building-number "181"}

      ""
      {:postal-code nil
       :unit-number nil
       :road-name nil
       :building-number nil}

      "School Of The Arts Singapore, 1 Zubir Said Drive #01-04/07 Singapore 227968 (Dhoby Ghaut) | Tel: 6238 7218 | Opening hours: Mon – Fri: 11:30 – 14:30, Mon – Fri: 17:00 – 23:00 and Sat – Sun: 11:30 – 23:00"
      {:postal-code "227968"
       :unit-number "#01-04/07"
       :road-name "Zubir Said Drive"
       :building-number "1"}

      "Plaza Singapura #03-01 68 Orchard Road Singapore 238839 |"
      {:postal-code "238839"
       :unit-number "#03-01"
       :road-name "Orchard Road"
       :building-number "68"}

      "1 Maritime Square #04-01 HarbourFront Centre"
      {:postal-code nil
       :unit-number "#04-01"
       :road-name "Maritime Square"
       :building-number "1"})))

(deftest test-handle-labelled
  (testing "that when address info cannot be found, handle-labelled will go a level up"
    (let [address-loc (->> (hickory-zip
                            (hiccup-to-hickory
                             [[:p [:strong {:style "line-height: 1.3em;"}
                                   [:span {:style "color: #d47978;"} "Address:"]
                                   [:span {:style "line-height: 1.3em;"}
                                    "&nbsp;273 Jalan Kayu, Singapore 7995"]]]]))
                           (iterate zip/next)
                           (take 8)
                           last)
          input (labelled-info address-loc)]
      (is (= {:type :labelled, :value ""}
             (dissoc input :loc))
          "Sanity check to make sure labelled-info works")
      (is (= {:type :labelled,
              :value "273 Jalan Kayu, Singapore 7995",
              :location
              {:postal-code nil,
               :unit-number nil,
               :road-name "Jalan Kayu",
               :building-number "273"}}
             (dissoc (handle-labelled input) :loc))))))
