(ns gowherene.reader.core-test
  (:require [clojure.test :refer :all]
            [clojure.pprint :refer [pprint]]
            [hickory.core :refer [as-hickory parse]]
            [medley.core :refer [take-upto distinct-by]]
            [gowherene.reader.core :refer :all]
            [gowherene.reader.geocodables :refer [find-postal-codes]]
            [gowherene.reader.regexes :refer :all]))

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
    (are [path expected] (let [page-zipper (->> (slurp path)
                                                hickory-zipper
                                                cleanup)]
                           (= expected
                              (->> (process-clean-zipper page-zipper)
                                   (map #(dissoc % :loc)))))
      "data/files/11-budget-buffets-in-singapore-20-and-below.html"
      '({:type :postal-code, :value "307591", :location "Address: 101 Thomson Road, Singapore 307591 (United Square Outlet) | 6254 8123 | Opening Hours for Lunch: 11:30am ~ 2:30pm (Daily Except PH) for Dinner: Sun ~ Thur, PH : 5:30pm ~ 12am Fri, Sat, Day Before PH : 5:30pm ~ 2am", :label "4. SSIKKEK Korean BBQ"} {:type :postal-code, :value "188096", :location "Address: 7 Tan Quee Lan St, Singapore 188096 | Tel number: +65 63362875 | Open daily from 12pm – 12am", :label "5. House of Steamboat"} {:type :postal-code, :value "238896", :location "Address: 181 Orchard Road, 08-09/10/11 Orchard Central, Singapore 238896 | Opening hours:  Mon – Fri: 11:00 – 15:00 and 18:00 – 22:00 Sat – Sun: 11:30 – 22:30 | ", :label "6. Shabu Sai"} {:type :postal-code, :value "227968", :location "Address: School Of The Arts Singapore, 1 Zubir Said Drive #01-04/07 Singapore 227968 (Dhoby Ghaut) | Tel: 6238 7218 | Opening hours: Mon – Fri: 11:30 – 14:30, Mon – Fri: 17:00 – 23:00 and Sat – Sun: 11:30 – 23:00", :label "8. Kim Korean Zubir"} {:type :postal-code, :value "238839", :location "Address: Plaza Singapura #03-01 68 Orchard Road Singapore 238839  | ", :label "Read Also: \r\n 5 Hawker Food Deliveries In Singapore To Skip The Queue & Satisfy Your Local Food Cravings"} {:type :postal-code, :value "307683", :location " Square #02-11/12 238 Thomson Road Singapore 307683 | ", :label "Read Also: \r\n 5 Hawker Food Deliveries In Singapore To Skip The Queue & Satisfy Your Local Food Cravings"} {:type :postal-code, :value "099253", :location "Singapore 099253 | ", :label "10. Ban Heng Harborfront center"} {:type :postal-code, :value "545724", :location "King’s Laksa Steamboat: 17 Teck Chye Terrace, Singapore 545724 | Tel: +65 62878010 | ", :label "11. King’s Laksa Steamboat"} {:type :postal-code, :value "119620", :location "Chilli Padi Nonya Café: 29 Heng Mui Keng Terrace #06-21 (Ground Level), Singapore 119620 | Tel: 6872 298 | ", :label "12. Chili Padi Nonya Cafe"} {:type :postal-code, :value "238858", :location "Makan Jen: Hotel Jen, 277 Orchard Road Singapore 238858 | Tel: 6708888 | ", :label "13. Makan @ Jen"} {:type :postal-code, :value "419882", :location "Al Jasra Restaurant: 459 Changi Road, Singapore 419882 | ", :label "14. Al Jasra Restaurant / Kasi Villa"} {:type :postal-code, :value "239571", :location "Orchard Grand Court, 131 Killiney Road, Singapore 239571 | Tel: 68302020 | ", :label "15. Crystal Cafe"} {:type :postal-code, :value "238896", :location "K.Cook Korean BBQ: Orchard Central, 181 Orchard Road, Singapore 238896 | Tel: 6884 7282 |", :label "16. K.Cook Korean BBQ"} {:type :postal-code, :value "238851", :location ", #04-01, Singapore 238851 | Tel: 6385 7854 | ", :label "17. Goro Goro Steamboat & Korean Buffet"})

      "data/files/affordable-seafood-buffets.html"
      '({:type :postal-code, :value "238896", :location ": 181 Orchard Road, #11-05, Orchard Central, Singapore 238896", :label "1. Tunglok seafood - from $26.80"} {:type :postal-code, :value "638366", :location ": 511 Upper Jurong Road, The Arena Country Club, Singapore 638366", :label "1. Tunglok seafood - from $26.80"} {:type :postal-code, :value "769198", :location ": 81 Lorong Chencharu, #01-05, ORTO, Singapore 769198 ", :label "2. Aroy Jing Jing - from $29.90"} {:type :postal-code, :value "059221", :location ": 68 Pagoda Street, Singapore 059221", :label "3. Orchid Roast Fish - from $18.80"} {:type :postal-code, :value "307591", :location ": 101 Thomson Road, #B1-01, United Square, Singapore 307591 ", :label "4. Vienna International Seafood and Teppanyaki Restaurant - from $39.90"} {:type :postal-code, :value "179103", :location ": 252 North Bridge Road, #B1-44E, Raffles City, Singapore 179103", :label "5. Buffet Town - from $26.80"} {:type :postal-code, :value "117540", :location ": 8 Port Road, Singapore 117540", :label "6. Dragon Tooth Gate - from $42"} {:type :postal-code, :value "117540", :location ": 8 Port Road, Labrador Park, Singapore 117540", :label "7. The Three Peacocks - from $50"})

      "data/files/best-burgers.html"
      '({:type :postal-code, :value "048621", :location "              Singapore 048621", :label "1. The Brat"} {:type :postal-code, :value "179103", :location " Raffles City Shopping Centre, #B1-65/66 Singapore 179103", :label "4. The Handburger"} {:type :postal-code, :value "569983", :location "510 Ang Mo Kio Avenue 1 (Car park along Sin Ming Ave, opp Blk 408), Singapore 569983", :label "5. GRUB"} {:type :postal-code, :value "238896", :location " 181 Orchard Road, Orchard Central, Singapore 238896", :label "9. EwF by Everything With Fries @ Orchard Central"} {:type :postal-code, :value "249677", :location "18A Dempsey Road, Singapore 249677", :label "10. Huber's Butchery & Bistro @ Dempsey "} {:type :postal-code, :value "078867", :location "1 Tras Link, Orchid Hotel #01-13, Singapore 078867", :label "11. Two Blur Guys"} {:type :postal-code, :value "419502", :location " 44 Jalan Eunos, Singapore 419502", :label "12. VeganBurg"} {:type :postal-code, :value "018982", :location "12 Marina Blvd #02-05, Marina Bay Financial Centre Tower 3, Singapore 018982", :label "12. VeganBurg"} {:type :postal-code, :value "287994", :location "200 Turf Club Road #01-32, Singapore 287994", :label "12. VeganBurg"} {:type :postal-code, :value "428765", :location "45 East Coast Road, Singapore 428765", :label "13. BERGS Gourmet Burgers"} {:type :postal-code, :value "189203", :location " 10 Haji Lane, Singapore 189203", :label "13. BERGS Gourmet Burgers"} {:type :postal-code, :value "049745", :location "3 Canton Street, Boat Quay, Singapore 049745", :label "13. BERGS Gourmet Burgers"} {:type :postal-code, :value "188535", :location " 8 Queen Street, Singapore 188535", :label "15. Fast Food For Thought"})

      "data/files/cheap-food-orchard.html"
      '({:type :postal-code, :value "238896", :location " Orchard Central, 181 Orchard Road, #07-10/11, S(238896)", :label "1. Shi Li Fang Hotpot"} {:type :postal-code, :value "238872", :location " Ngee Ann City, 391 Orchard Road #B201-3, Takashimaya Food Hall, S(238872) ", :label "2. Tsuru-Koshi Udon"} {:type :postal-code, :value "228213", :location " Far East Plaza,  14 Scotts Road, #05-116, S(228213)", :label "3. Hainanese Delicacy "} {:type :postal-code, :value "238872", :location " Ngee Ann City, 391 Orchard Road #B204-4-2, Takashimaya Food Hall, S(238872) ", :label "4. Fisherios "} {:type :postal-code, :value "238895", :location " 313@Somerset, 313 Orchard Road, #B3-49, S(238895)", :label "5. Dong Dae Mun"} {:type :postal-code, :value "238840", :location " #02-17/18 Concorde Hotel Singapore, 100 Orchard Road, S(238840)", :label "6. Kim Dae Mun"} {:type :postal-code, :value "238863", :location " Lucky Plaza, 304 Orchard Road, #04-25, S(238863)", :label "7. Ayam Penyet Ria"} {:type :postal-code, :value "238843", :location " The Centrepoint, 176 Orchard Road, #01-101-104, S(238843)  ", :label "8. Pies & Coffee"} {:type :postal-code, :value "238872", :location " Ngee Ann City, 391 Orchard Road #B204-1/2, Takashimaya Food Hall, S(238872) ", :label "9. Yonehachi "} {:type :postal-code, :value "228213", :location " Far East Plaza,  14 Scotts Road, #05-95, S(228213)", :label "10. New Station Snack Bar"} {:type :postal-code, :value "238895", :location " 313@Somerset, 313 Orchard Road, #01-14/15, S(238895)", :label "11. Smoothie King "} {:type :postal-code, :value "238895", :location " 313@Somerset, 313 Orchard Road, #B3-44, S(238895)", :label "12. Riverside Indonesian BBQ"} {:type :postal-code, :value "238875", :location " Orchard Towers, 400 Orchard Road, #03-23, S(238875)", :label "13. Thai Tantric"} {:type :postal-code, :value "237978", :location "#03-04 *SCAPE, 2 Orchard Link, S(237978)", :label "14. Astons Specialities"} {:type :postal-code, :value "238855", :location " Robinsons Heeren, 260 Orchard Road, #B1-02A, S(238855)", :label "15. Gyoza-Ya"} {:type :postal-code, :value "238863", :location " Lucky Plaza, 304 Orchard Road, #B1-99/101, S(238863)", :label "16. River Valley Nasi Lemak "} {:type :postal-code, :value "238896", :location " Orchard Central, 181 Orchard Road, #03-31, S(238896)", :label "17. Quiznos "} {:type :postal-code, :value "238852", :location "#B1-04/05 Midpoint Orchard, 220 Orchard Road, S(238852)", :label "18. Jtown Cafe"} {:type :postal-code, :value "239695", :location "#04-02 Cineleisure Orchard, 8 Grange Road, S(239695)", :label "19. Eighteen Chefs "} {:type :postal-code, :value "238896", :location " Orchard Central, 181 Orchard Road, #02-30, S(238896)", :label "20. Central Hong Kong Cafe"} {:type :postal-code, :value "228213", :location " Far East Plaza, 14 Scotts Road, #04-96, S(228213)", :label "21. Greenview Cafe"} {:type :postal-code, :value "228796", :location " #01-10 Cuppage Plaza, 5 Koek Road, S(228796)", :label "22. Gyoza no Ohsho"} {:type :postal-code, :value "188306", :location " 1 Selegie Road, #01-20/21, PoMo,  S(188306)", :label "Bonus: Espuma Lab "})

      "data/files/cheap-orchard-buffets.html"
      '({:type :postal-code, :value "238858", :location " 277 Orchard Road, Level 10, 238858", :label "1. [email protected] at Hotel Jen Orchard Gateway"} {:type :postal-code, :value "238879", :location " 442 Orchard Road, lobby level, Singapore 238879", :label " 2. Orchard Cafe @ Orchard Hotel "} {:type :postal-code, :value "228211", :location " 10 Scotts Road, Singapore 228211", :label " 3. Oasis @ Grand Hyatt Singapore"} {:type :postal-code, :value "238865", :location " 320 Orchard Road, Lobby level, Singapore Marriott Tang Plaza Hotel, Singapore 238865", :label " 4. Marriott Cafe "} {:type :postal-code, :value "238896", :location " 181 Orchard Road, #08-04/05, Singapore 238896", :label "5. Talay Kata"} {:type :postal-code, :value "238872", :location " 391 Orchard Road, #04-23 Ngee Ann City, Singapore 238872", :label " 6. Coca Restaurant @ Ngee Ann City"} {:type :postal-code, :value "238851", :location ", 218 Orchard road, #04-01, Singapore 238851", :label " 7. GoroGoro Steamboat and Korean Buffet "} {:type :postal-code, :value "227968", :location " 1 Zubir Said Drive #01-04 Singapore 227968", :label " 8. I’m Kim Korean BBQ @ SOTA "} {:type :postal-code, :value "229616", :location " 11 Cavenagh Road, Singapore 229616", :label "9. Tandoor @ Holiday Inn Orchard"} {:type :postal-code, :value "229616", :location " 11 Cavenagh Road #02-00, Singapore 229616", :label " 10. Window on the Park "})

      "data/files/dim-sum-restaurants-singapore.html"
      '({:type :postal-code, :value "387449", :location "\nSingapore 387449", :label nil} {:type :postal-code, :value "188720", :location "\nSingapore 188720", :label nil} {:type :postal-code, :value "099253", :location "\nSingapore 099253", :label nil} {:type :postal-code, :value "039801", :location "\nSingapore 039801", :label nil} {:type :postal-code, :value "238872", :location "\nSingapore 238872", :label nil} {:type :postal-code, :value "198759", :location "\nSingapore 198759", :label nil} {:type :postal-code, :value "238872", :location "\nSingapore 238872", :label nil} {:type :postal-code, :value "238879", :location "\nSingapore 238879", :label nil} {:type :postal-code, :value "237994", :location "\nSingapore  237994", :label nil} {:type :postal-code, :value "188966", :location "\nSingapore 188966", :label nil} {:type :postal-code, :value "228221", :location "\nSingapore 228221", :label nil} {:type :postal-code, :value "238857", :location "\nSingapore 238857", :label nil} {:type :postal-code, :value "048421", :location "\nSingapore 048421", :label nil} {:type :postal-code, :value "228209", :location "\nSingapore 228209", :label nil} {:type :postal-code, :value "160054", :location "\nSingapore 160054", :label nil} {:type :postal-code, :value "208533", :location "\nSingapore 208533", :label nil} {:type :postal-code, :value "188973", :location "\nSingapore 188973", :label nil} {:type :postal-code, :value "249715", :location "\nSingapore 249715", :label nil} {:type :postal-code, :value "310181", :location "\nSingapore 310181", :label nil} {:type :postal-code, :value "208882", :location "\nSingapore 208882", :label nil} {:type :postal-code, :value "238801", :location "\nSingapore 238801", :label nil} {:type :postal-code, :value "238839", :location "\nSingapore 238839", :label nil} {:type :postal-code, :value "307506", :location "\nSingapore 307506", :label nil} {:type :postal-code, :value "189652", :location "\nSingapore 189652", :label nil} {:type :postal-code, :value "178957", :location "\nSingapore 178957", :label nil})

      "data/files/local-breakfast-east-singapore.html"
      '({:type :postal-code, :value "429356", :location "300-302 Joo Chiat Road, Singapore 429356", :label "Mr and Mrs Mohgan's Super Crispy Roti Prata"} {:type :postal-code, :value "428903", :location "204 E Coast Rd, Singapore 428903", :label "Chin Mee Chin Confectionery"} {:type :postal-code, :value "460018", :location "18 Brewcoffee, Blk 18 Bedok South Road, Singapore 460018", :label "Yong He Bak Chor Seafood Noodles"} {:type :postal-code, :value "460016", :location "Blk 16 Bedok South Road Market & Food Centre, Singapore 460016", :label "Enak"} {:type :postal-code, :value "390051", :location "Old Airport Road Food Centre #01-37, 51 Old Airport Road, Singapore 390051", :label "Ru Ji Kitchen"} {:type :postal-code, :value "390051", :location "Old Airport Road Food Centre #01-52, Blk 51 Old Airport Road, Singapore 390051", :label "Toast Hut"} {:type :postal-code, :value "523201", :location "201C Tampines Street 21, Singapore 523201", :label "Fu Yuan Minced Pork Noodle"} {:type :postal-code, :value "521137", :location "Tampines Round Market and Food Centre, #01-14, 137 Tampines Street 11, Singapore 521137", :label "Xing Yun Kway Chap"} {:type :postal-code, :value "460216", :location "Blk 216 Bedok North St 1 Market and Food Centre #01-55, Singapore 460216", :label "Chai Chee Fried Carrot Cake"} {:type :postal-code, :value "521137", :location "Tampines Round Market and Food Centre, #01-45, 137 Tampines Street 11, Singapore 521137", :label "Yummy Sarawak Kolo Mee"} {:type :postal-code, :value "460208", :location "Blk 208B New Upper Changi Road, #01-53 Bedok Interchange Hawker Centre, Singapore 460208", :label "Bedok Chwee Kueh"} {:type :postal-code, :value "460511", :location "Blk 511 Bedok North St 3, 511 Food Centre #01-10, Singapore 460511", :label "Tian Nan Xing Minced Pork Noodle"} {:type :postal-code, :value "500002", :location "2 Changi Village Road, #01-26, Singapore 500002", :label "Mizzy Corner"} {:type :postal-code, :value "460084", :location "Blk 84 Bedok North St 4 #01-21, Singapore 460084", :label "Lek Lim Nonya Cake Confectionery"} {:type :postal-code, :value "521137", :location "Tampines Round Market and Food Centre, #01-07, 137 Tampines Street 11, Singapore 521137", :label "Song Han Carrot Cake"} {:type :postal-code, :value "520827", :location "Tampines West Foodcourt, Blk 827, Tampines Street 81, Singapore 520827", :label "Hua Zai HK Style Roasted Delight Rice/Noodle and Muslim Food"} {:type :postal-code, :value "428829", :location "139 East Coast Road, Singapore 428829", :label "Glory Catering"} {:type :postal-code, :value "460207", :location "Blk 207 Bedok North Interchange #01-18, Singapore 460207", :label "Song Zhou Carrot Cake"} {:type :postal-code, :value "402004", :location "#01-25, Eunos Market and Food Centre, Blk 4A, Eunos Crescent, Singapore 402004", :label "Hock Choon Laksa and Lor Mee"} {:type :postal-code, :value "390051", :location "51 Old Airport Road, #01-138, Old Airport Road Food Centre, Singapore 390051", :label "Dong Ji Fried Kway Teow"} {:type :postal-code, :value "390051", :location "51 Old Airport Road, #01-116, Old Airport Road Food Centre, Singapore 390051", :label "Xin Mei Xiang Lor Mee"} {:type :postal-code, :value "390051", :location "51 Old Airport Road, #01-74, Old Airport Road Food Centre, Singapore 390051", :label "Tan Beng Otah Delights"} {:type :postal-code, :value "500002", :location "Changi Village Food Centre, #01-19, 2 Changi Village Road, Singapore 500002", :label "Da Zhong Mei Shi Char Kway Teow"} {:type :postal-code, :value "500002", :location "Changi Village Food Centre, #01-40, 2 Changi Village Road, Singapore 500002", :label "Jia Mei"} {:type :postal-code, :value "460216", :location "Blk 216 Bedok North Food Centre #01-31, Singapore 460216", :label "Joo Chiat Chiap Kee"} {:type :postal-code, :value "460216", :location "Blk 216 Bedok North Street 1 #01-07, Singapore 460216", :label "Hon Ni Kitchen"} {:type :postal-code, :value "427784", :location "60/62 Joo Chiat Place, Singapore 427784/85", :label "Kim Choo Kueh Chang"})

      "data/files/no-gst-restaurants.html"
      '({:type :postal-code, :value "247781", :location " 56 Zion Road, Singapore 247781 | 6 Greenwood Avenue, Hillcrest Park, Singapore 289195", :label "1. Pepperoni Pizzeria"} {:type :postal-code, :value "179103", :location " 252 North Bridge Road, Singapore 179103", :label "2. Nam Nam Noodle Bar"} {:type :postal-code, :value "258748", :location " 10 Jalan Serene, Serene Centre #01-05, Singapore 258748", :label "3. La Petite Cuisine"} {:type :postal-code, :value "679697", :location " 30 Cashew Road, Singapore 679697", :label "4. The ART"} {:type :postal-code, :value "259569", :location " 1 Cluny Road, Tanglin Gate #B1-00, Singapore 259569", :label "5. Food For Thought"} {:type :postal-code, :value "169339", :location " 1 Kampung Bahru Road, Singapore 169339", :label "6. OKB"} {:type :postal-code, :value "419896", :location " 484 Changi Road, Singapore 419896", :label "7. Rice & Fries"} {:type :postal-code, :value "459114", :location " 922 East Coast Road, Singapore 459114", :label "8. Jag's Gastropub"} {:type :postal-code, :value "597626", :location " 2 Pandan Valley, Singapore 597626", :label "9. The French Ladle"} {:type :postal-code, :value "069043", :location " 2 McCallum Street, Singapore 069043", :label "10. SPRMRKT"} {:type :postal-code, :value "228213", :location " 14 Scotts Road, Far East Plaza #03-89, Singapore 228213", :label "11. The Sushi Bar"} {:type :postal-code, :value "259569", :location " 1 Cluny Road, Singapore 259569", :label "12. Casa Verde"} {:type :postal-code, :value "188017", :location "#01-01 89 Victoria Street, Singapore 188017", :label "13. Steakout "} {:type :postal-code, :value "188392", :location " 36 Seah Street, Singapore 188392", :label "14. Third and Sixth"} {:type :postal-code, :value "187967", :location " 96 Waterloo Street, Singapore 187967", :label "15. Torte "} {:type :postal-code, :value "427677", :location " 465 Joo Chiat Road, Singapore 427677", :label "17. Fatboys the Burger Bar "} {:type :postal-code, :value "247964", :location " 56 Tanglin Road, Tanglin Post Office #01-03, Singapore 247964", :label "18. Nassim Hill Bakery, Bistro & Bar "})

      "data/files/snippets-the-best-buffets-in-singapore-the-definitive-guide-because-you-shouldnt-waste-your-calories-and-moolah.html"
      '({:type :postal-code, :value "039595", :location "Where: Level 3, Pan Pacific Singapore, 7 Raffles Boulevard, Singapore 039595", :label nil} {:type :postal-code, :value "188966", :location "Where: Level 1, InterContinental Singapore, 80 Middle Road, Singapore 188966", :label nil} {:type :postal-code, :value "169631", :location "401 Havelock Road, Singapore 169631", :label nil} {:type :postal-code, :value "238857", :location "Where: Level 4, Grand Park Orchard, 270 Orchard Road, Singapore 238857", :label nil} {:type :postal-code, :value "249715", :location "Where: Regent Singapore, Level 2, 1 Cuscaden Rd, Singapore 249715", :label nil} {:type :postal-code, :value "238867", :location " Where: Mandarin Orchard Singapore, 333 Orchard Rd, 238867", :label nil} {:type :postal-code, :value "238883", :location "Lobby Level, Hilton Singapore, 581 Orchard Road, Singapore 238883", :label nil} {:type :postal-code, :value "039797", :location "Where: Mandarin Oriental Singapore, 5 Raffles Avenue, Marina Square, Singapore 039797", :label nil} {:type :postal-code, :value "247911", :location "Where: The St. Regis Singapore, Level 1U, 29 Tanglin Road, Singapore 247911", :label nil})

      "data/files/the-ultimate-guide-to-local-breakfast-in-singapore.html"
      '({:type :postal-code, :value "428903", :location " 204 East Coast Road, Singapore 428903, (+65) 6354 0419", :label "Social Media"} {:type :postal-code, :value "289876", :location ", 2 Adam Road, Singapore 289876, (+65) 9843 4509, ", :label "Social Media"} {:type :postal-code, :value "570024", :location " Blk 24, #01-51 Sin Ming Road, Singapore 570024, (+65) 6453 3893", :label "Social Media"} {:type :postal-code, :value "188211", :location ", 2 Short Street, Singapore 188211, (+65) 6741 7358, ", :label "Social Media"} {:type :postal-code, :value "460207", :location ", Blk 207, New Upper Changi Road, #01-53, Singapore 460207", :label "Social Media"} {:type :postal-code, :value "402001", :location "1 Geylang Serai, #02-114, Singapore 402001", :label "Social Media"} {:type :postal-code, :value "311125", :location ", Blk 127, Toa Payoh Food Centre, #02-30, Singapore 311125, (+65) 9816 9412", :label "Social Media"} {:type :postal-code, :value "207671", :location ", 95 Syed Alwi Road, Singapore 207671, ", :label "Social Media"} {:type :postal-code, :value "199660", :location ", 21 Baghdad Street, Singapore 199660", :label "Social Media"} {:type :postal-code, :value "390034", :location ", 34 Cassia Crescent, #01-86, Singapore 390034, (+65) 8115 2747", :label "Social Media"})

      "data/files/tiffany-singapore.html"
      '({:type :postal-code, :value "238872", :location "Ngee Ann City 238872", :label "Ngee Ann City"} {:type :postal-code, :value "238801", :location "Singapore 238801", :label "Singapore"} {:type :postal-code, :value "819643", :location "Singapore 819643", :label "Singapore"} {:type :postal-code, :value "819663", :location "Singapore 819663", :label "Singapore"} {:type :postal-code, :value "018972", :location "Singapore 018972", :label "Singapore"} {:type :postal-code, :value "098269", :location "Singapore 098269", :label "Singapore"} {:type :postal-code, :value "228220", :location "Singapore 228220", :label "Singapore"}))))

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
      (is (= 0 (count (filter (comp nil? :latlng) response)))
          "All the latlngs after processing were geocoded")
      (is (= (range 1 23)
             (sort (->> response
                        (map (comp get-index :label))
                        (filter identity))))
          "All headers were parsed, beginning from 1. to 22. (23rd one is a bonus)"))))

(deftest integration-test-cheap-food-orchard
  (testing "no-gst-restaurants"
    (let [page (slurp "data/files/no-gst-restaurants.html")
          response (process page)]
      (is (= 18 (count response))
          "Check number of items parsed")
      (is (= 0 (count (filter (comp nil? :latlng) response)))
          "All the latlngs after processing were geocoded")
      (is (= (range 1 19)
             (sort (->> response
                        (map (comp get-index :label))
                        (filter identity))))
          "All headers were parsed, beginning from 1. to 18."))))

(comment


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


  (defn generate
    [file]
    (let [page-zipper (->> (slurp file)
                           hickory-zipper
                           cleanup)
          results (->> (process-clean-zipper page-zipper)
                       (map #(dissoc % :loc)))]
      results))

  (let [files ["data/files/11-budget-buffets-in-singapore-20-and-below.html"
               "data/files/affordable-seafood-buffets.html"
               "data/files/best-burgers.html"
               "data/files/no-gst-restaurants.html"
               "data/files/cheap-orchard-buffets.html"
               "data/files/dim-sum-restaurants-singapore.html"
               "data/files/local-breakfast-east-singapore.html"
               "data/files/no-gst-restaurants.html"
               "data/files/snippets-the-best-buffets-in-singapore-the-definitive-guide-because-you-shouldnt-waste-your-calories-and-moolah.html"
               "data/files/the-ultimate-guide-to-local-breakfast-in-singapore.html"
               "data/files/tiffany-singapore.html"]]
    (interleave files (map generate files)))
  )
