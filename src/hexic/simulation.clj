(ns hexic.simulation
  [:require [hexic [core :as c]
                   [board :as b]
                   [format :as f]]])

(defn- print-board [b]
  (println (f/format-board b)))

(defn- run-simulation []
  (let [board (c/generate-board 5 5)]
    (do (println "Starting simulation.")
        (print-board board))
    (loop [b board total-score 0N]
      (let [{score :score
             b' :board
             triple :triple
             rotations :rotations
             cluster-cells :cluster-cells} (b/rotate-best b)]
        (if (pos? score)
          (let [new-board (c/randomize-nils (b/ram-board b cluster-cells))]
            (do (println "Rotating triple" triple
                         (case (int rotations) 1 "once" 2 "twice"))
                (print-board new-board)
                (println "Points achieved: " score
                         "Total score" (str total-score))
                (recur new-board (+ total-score score))))
          (println "No more rotations possible. Exiting."))))))
