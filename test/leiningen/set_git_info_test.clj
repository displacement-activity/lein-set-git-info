(ns leiningen.set-git-info-test
  (:use clojure.test)
  (:require [leiningen.set-git-info :refer :all]
            [leiningen.core.main :refer [warn info]])
  (:import (java.io File)
           (java.util Date)))


(defmacro with-tmp-file [identifier action]
  `(let [~identifier (File/createTempFile "set-git-info" "test")]
     ~action
     (.delete ~identifier)))

(defmacro expect-messages [type expected-msgs test-body]
  `(let [messages# (atom [])]
     (with-redefs [~type #(swap! messages# conj %)]
       ~test-body
       (is (= ~expected-msgs @messages#)))))


(deftest set-git-info-test

  (testing "Test updates to file"
    (with-redefs [last-commit-info (fn [_] {:id 1234 :author "me"})]
      (expect-messages
        warn []
        (with-tmp-file tmp
                       (let [tmp-path (.getCanonicalPath tmp)
                             project {:root "." :set-git-info [{:path tmp-path :search-regex #"::id::" :replace-field :id}
                                                               {:path tmp-path :search-regex #"::author::" :replace-field :author}]}]
                         (spit tmp-path "::id::, ::author::")
                         (set-git-info project)
                         (is (= "1234, me" (slurp tmp-path)))
                         )))))

  (testing "Test with a file that cannot be updated"
    (expect-messages warn
                     ["Failed to update: ./not-there"]
                     (set-git-info {:root "." :set-git-info [{:path "./not-there"}]}))))

(deftest test-dry-run
  (testing "Test dryrun with file warning"
    (expect-messages warn
                     ["Failed to update: ./not-there"]
                     (set-git-info {:root "." :set-git-info [{:path "./not-there"}]} ":dry-run")))

  (testing "Test dryrun with updates to file"
    (with-redefs [last-commit-info (fn [_] {:id 1234 :author "me"})]
      (with-tmp-file tmp
                     (let [tmp-path (.getCanonicalPath tmp)
                           project {:root "." :set-git-info [{:path tmp-path :search-regex #"::id::" :replace-field :id}
                                                             {:path tmp-path :search-regex #"::author::" :replace-field :author}
                                                             ]}
                           expected-messages [(str "Will update: '" tmp-path "' replacing '::id::' with '1234'")
                                              (str "Will update: '" tmp-path "' replacing '::author::' with 'me'")]]
                       (expect-messages
                         info expected-messages
                         (set-git-info project ":dry-run")))))))


(deftest test-usage
  (testing "invalid arg"
    (expect-messages
      info
      [usage]
      (set-git-info {:root "." :set-git-info [{:path "./not-there"}]} "foo bar"))))

(deftest test-str-format
  (is (= "1970-01-01T01:00:00+0100" (str-format (Date. 0))))
  (is (= "1, 2, 3" (str-format (take 3 [1 2 3]))))
  (is (= "1, 2, 3" (str-format [1 2 3]))))

