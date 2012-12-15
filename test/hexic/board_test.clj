(ns hexic.board-test
  [:use clojure.test hexic.board])

(deftest find-clusters-test
  (testing "find-cluster"
;
; no clusters
;       _       _       _       _
;   _ / 1 \ _ / 2 \ _ / 3 \ _ / 4 \
; / 5 \ _ / 6 \ _ / 7 \ _ / 8 \ _ /
; \ _ / 9 \ _ / 0 \ _ / 9 \ _ / 8 \
; / 7 \ _ / 6 \ _ / 5 \ _ / 4 \ _ /
; \ _ /   \ _ /   \ _ /   \ _ /
    (is (not (seq
              (find-clusters [[1 2 3 4] [5 6 7 8] [9 0 9 8] [7 6 5 4]]))))
;
; single cluster at [0 0] [0 1] [0 2]
;       _       _       _       _
;   _ / 1 \ _ / 2 \ _ / 3 \ _ / 4 \
; / 1 \ _ / 6 \ _ / 7 \ _ / 8 \ _ /
; \ _ / 1 \ _ / 0 \ _ / 9 \ _ / 8 \
; / 7 \ _ / 6 \ _ / 5 \ _ / 4 \ _ /
; \ _ /   \ _ /   \ _ /   \ _ /
    (is (= #{[0 0] [0 1] [0 2]}
           (first (find-clusters [[1 2 3 4] [1 6 7 8] [1 0 9 8] [7 6 5 4]]))))
;
; single big cluster
;       _       _       _       _
;   _ / 1 \ _ / 1 \ _ / 1 \ _ / 1 \
; / 1 \ _ / 1 \ _ / 1 \ _ / 1 \ _ /
; \ _ / 1 \ _ / 1 \ _ / 1 \ _ / 1 \
; / 1 \ _ / 1 \ _ / 1 \ _ / 1 \ _ /
; \ _ /   \ _ /   \ _ /   \ _ /
    (let [clusters (find-clusters[[1 1 1 1] [1 1 1 1] [1 1 1 1] [1 1 1 1]])]
      (is (and (= 1 (count clusters))
               (= 16 (count (first clusters))))))))
