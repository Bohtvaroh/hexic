(ns hexic.core-test
  [:use clojure.test hexic.core]
  [:require [hexic [board :as b]]])

(deftest generate-board-test
  (testing "generate-board produces no clusters."
    (is
     (not-any? #(seq (b/find-clusters %))
               (for [size (range 3 20)] (generate-board size size))))))
