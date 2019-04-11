(ns gowherene.reader.location-test
  (:require [clojure.test :refer :all]
            [gowherene.reader.location :refer :all]
            [gowherene.reader.road-name :refer [road-name]]))

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
