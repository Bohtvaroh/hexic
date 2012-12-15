(ns hexic.core
  [:require [hexic [board :as b]
                   [ui :as ui]
                   [util :as u]]]
  (:gen-class))

(defn- successive-repeats
  "Returns a set of successive recurrent values from coll."
  [coll]
  (u/reduce-with-acc
    (fn [a b acc] (if (= a b) (conj acc a) acc)) #{} coll))

(defn- random-from
  "Generates random value from supplied proposals vector."
  [proposals]
  (get proposals (rand-int (count proposals))))

(defn- random-color
  "Returns random color. Allows to filter colors using optional excluding arg."
  [& {excluding :excluding}]
  (random-from
   (filterv (complement (set excluding)) ui/colors)))

(defn generate-board
  "Generates board containing no clusters."
  [w h]
  (-> (b/create-empty-board w h)
      (b/update-board
       (fn [_ neighbor-values]
         (let [semi-clusters (successive-repeats
                              (u/make-ring neighbor-values))]
           (random-color :excluding semi-clusters))))))

(defn randomize-nils
  "Replaces nil-cells with random values."
  [board]
  (b/update-board board (fn [value _]
                          (if (nil? value) (random-color) value))))

(defn- turns-seq
  "Returns lazy seq of game turns."
  [b]
  (lazy-seq
   (let [{score :score
          b' :board
          cluster-cells :cluster-cells :as turn} (b/rotate-best b)]
     (if (pos? score)
       (cons turn
             (turns-seq
              (randomize-nils (b/ram-board b' cluster-cells))))))))

(defn- print-usage []
  (println "Options allowed:")
  (println " improved-ui - start in improved UI mode")
  (println " fallback-ui - start in fallback UI mode (default on Windows)")
  (println " usage - print this information and exit"))

(defn -main [& opts]
  (let [b (generate-board 5 17)
        turns (turns-seq b)
        start-improved #(ui/start-improved b turns)
        start-fallback #(ui/start-fallback b turns)
        start-default (if (= (u/os-type) :unix) start-improved start-fallback)]
    (condp some opts
      #{"improved-ui"} (start-improved)
      #{"fallback-ui"} (start-fallback)
      #{"usage"} (print-usage)
      (start-default))))
