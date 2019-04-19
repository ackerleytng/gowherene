(ns gowherene.reader.client-test
  (:require [clojure.test :refer :all]
            [gowherene.reader.client :refer :all]))

(deftest test-retrieve
  (let [unsafe-result {:status 403 :headers {} :body ""}]
    (testing "retrieve"
      (is (= unsafe-result (retrieve "file:///etc/passwd")))
      (is (= unsafe-result (retrieve "ftp://ftp.random.com/files")))
      (is (= unsafe-result (retrieve "http://www.random.com:1234/files")))
      (is (= 200 (:status (retrieve "https://www.google.com/")))))
    (testing "exceptional cases"
      (is (= 404 (:status (retrieve "http://httpstat.us/404"))))
      (is (= nil (retrieve "http://example.invalid"))))))

(deftest test-maybe-prefix-url
  (testing "maybe-prefix-url"
    (is (= "http://www.google.com" (maybe-prefix-url "http://www.google.com")))
    (is (= "ftp://www.ftp.com" (maybe-prefix-url "ftp://www.ftp.com")))
    (is (= "https://www.https.com" (maybe-prefix-url "https://www.https.com")))
    (is (= "https://www.test.com" (maybe-prefix-url "www.test.com")))))
