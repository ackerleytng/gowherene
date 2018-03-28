(ns gowherene.reader.client-test
  (:require [clojure.test :refer :all]
            [gowherene.reader.client :refer :all]))

(deftest test-safe-get
  (let [unsafe-result {:status 403 :headers {} :body ""}]
    (testing "safe-get"
      (is (= nil (safe-get nil)))
      (is (= unsafe-result (safe-get "file:///etc/passwd")))
      (is (= unsafe-result (safe-get "ftp://ftp.random.com/files")))
      (is (= unsafe-result (safe-get "http://www.random.com:1234/files")))
      (is (= 200 (:status (safe-get "https://www.google.com/")))))
    (testing "exceptional cases"
      (is (= 404 (:status (safe-get "http://httpstat.us/404"))))
      (is (= nil (safe-get "http://example.invalid"))))))

(deftest test-maybe-prefix-url
  (testing "maybe-prefix-url"
    (is (= "http://www.google.com" (maybe-prefix-url "http://www.google.com")))
    (is (= "ftp://www.ftp.com" (maybe-prefix-url "ftp://www.ftp.com")))
    (is (= "https://www.https.com" (maybe-prefix-url "https://www.https.com")))
    (is (= "http://www.test.com" (maybe-prefix-url "www.test.com")))))
