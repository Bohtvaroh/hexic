(ns hexic.board
  [:require [hexic [util :as u]]]
  [:require [clojure [set :as set]]]
  [:require [clojure.core [reducers :as r]]])

(defn get-board-width [b]
  (count (first b))) ; first row size determines board width

(defn get-board-height [b]
  (count b))

(defn create-empty-board [w h]
  (vec (repeat h (vec (repeat w nil)))))

(defn- neighbor-cells-coords
  "Returns coordinates of neighbor cells starting from top-left neighbor
  and continuing CW."
  [[x y]]
  (let [even-diffs [[0 -1] [0 -2] [1 -1] [1 1] [0 2] [0 1]]
        odd-diffs [[-1 -1] [0 -2] [0 -1] [0 1] [0 2] [-1 1]]]
    (for [[dx dy] (if (even? y) even-diffs odd-diffs)] [(+ x dx) (+ y dy)])))

(defn- value-at-cell [board [x y]]
  (get-in board [y x]))

(defn- set-cell-value [board coord value]
  (update-in board (reverse coord) (constantly value)))

(defn- set-cell-values [board & pairs]
  (reduce (fn [b [coord value]]
            (set-cell-value b coord value)) board (partition 2 pairs)))

(defn- neighbor-cells
  "Returns lazy seq of pairs of (value coord) of neighbor cells
  starting from top-left neighbor and continuing CW."
  [board coord]
  (map #(vector (value-at-cell board %) %)
       (neighbor-cells-coords coord)))

(defn- neighbor-cells-values [board coord]
  (map first (neighbor-cells board coord)))

(defn- cells-seq
  "Returns a seq on board cells."
  [board]
  (let [w (get-board-width board)
        h (get-board-height board)]
    (for [y (range h) x (range w)] [x y])))

(defn update-board
  "Traverses through the board left to right row by row applying f to the cell
  value and neighbor cells values each iteration. f must
  return a new cell value which will be visible to subsequent calls to f
  within the board argument."
  [board f]
  (loop [b board cells (cells-seq board)]
    (if-let [coords (seq cells)]
      (let [coord (first coords)
            new-value (f (value-at-cell b coord)
                         (neighbor-cells-values b coord))]
        (recur (set-cell-value b coord new-value) (next cells)))
      b)))

(defn- find-cluster
  "Returns a set of cluster coordinates around coord or nil
  if no cluster was found."
  [board coord]
  (let [value (value-at-cell board coord)
        neighbors (neighbor-cells board coord)
        ring (u/make-ring neighbors)
        cluster-coords (u/reduce-with-acc
                         (fn [[a ac] [b bc] acc]
                           (if (= a b value) (conj acc ac bc) acc)) #{} ring)]
    (if (seq cluster-coords)
      (conj cluster-coords coord) nil)))

(defn find-clusters
  "Returns a seq of found clusters coordinates sets."
  [board]
  (letfn
      [(has-super-set? [s x]
         (some (partial set/subset? x) s))
       (adjacents [s x]
         (filter #(> (count (set/intersection x %)) 1) s))
       (f [acc [x y :as coord]]
         (if-let [cluster (find-cluster board coord)]
           (if (seq acc)
             (if (or (contains? acc cluster)
                     (has-super-set? acc cluster)) acc
               (if-let [adjs (seq (adjacents acc cluster))]
                 (conj (apply disj acc adjs)
                       (apply set/union cluster adjs))
                 (conj acc cluster)))
             (conj acc cluster))
           acc))]
    (reduce f #{} (cells-seq board))))

(defn- get-score
  "Returns game score achieved when clusters are removed."
  [clusters]
  (reduce (fn [acc cluster]
            (+ acc (apply * (repeat (- (count cluster) 2) 3))))
          0 clusters))

(defn- get-all-triples
  "Returns all triples of coordinates which can be rotated."
  [board]
  (letfn [(within-board? [[x y]]
            (and (< -1 x (get-board-width board))
                 (< -1 y (get-board-height board))))
          (f [coord]
            (let [[_ _ _ c4 c5 c6] (neighbor-cells-coords coord)]
              (filterv (partial every? within-board?)
                       [[coord c5 c6] [coord c4 c5]])))]
    (mapcat f (cells-seq board))))

(defn- rotate-triple
  "Rotates supplied cells updating board values accordingly."
  [board [a b c :as cells]]
  (let [[a-value b-value c-value]
        (mapv (partial value-at-cell board) cells)]
    (set-cell-values board
                     a c-value b a-value c b-value)))

(defn- get-all-possible-rotations
  "Returns a seq of all possible board alterations
  in form of [board triple rotation]."
  [board]
  (mapcat (fn [triple]
                  (let [board' (rotate-triple board triple)
                        board'' (rotate-triple board' triple)]
                    [[board' triple :right] [board'' triple :left]]))
                (get-all-triples board)))

(defn rotate-best
  "Finds and returns best turn resulting in highest score gained."
  [board]
  (let [candidates (vec (get-all-possible-rotations board))
        reducef (fn ([] {:score 0})
                    ([a b] (if (> (:score a) (:score b)) a b)))
        mapf (fn [[b t r]]
               (let [clusters (find-clusters b)
                     score (get-score clusters)]
                 {:score score
                  :board b
                  :triple t
                  :rotation r
                  :cluster-cells (set (apply concat clusters))}))]
    (r/fold reducef (r/map mapf candidates))))

(defn- get-column-index
  "Returns virtual column index."
  [[x y]]
  (let [i (* 2 x)] (if (even? y) (inc i) i)))

(defn- get-bottommost-cluster-cells
  "Returns the bottommost cluster cell for each column."
  [cluster-cells]
  (letfn [(lowest-cell [cells]
            (reduce
             (fn [[_ y1 :as a] [_ y2 :as b]](if (> y2 y1) b a))
             cells))]
    (map (comp lowest-cell second)
         (group-by get-column-index cluster-cells))))

(defn- above-cells-coords
  "Returns lazy seq of cells coordinates straight above coord.
  Stops when top board edge is reached."
  [coord]
  (lazy-seq
   (let [[x y :as cell-above] (second (neighbor-cells-coords coord))]
     (when (not (neg? y))
       (cons cell-above (above-cells-coords cell-above))))))

(defn- nils-to-end
  "Takes coll and returns a lazy seq on that collection with
  nil-elements moved to the end with order preserved."
  [coll]
  (let [coll' (remove nil? coll)]
    (take (count coll) (concat coll' (repeat nil)))))

(defn ram-board
  "Rams board. It means that all clusters are filled with cells
  falling from above. Nils are placed in empty cells."
  [board cluster-cells]
  (let [start-cells (get-bottommost-cluster-cells cluster-cells)
        cells-to-ram (map #(cons % (above-cells-coords %)) start-cells)
        get-cell-value (fn [cell]
                         (if-not (contains? cluster-cells cell)
                           (value-at-cell board cell)))
        new-cell-values (map #(nils-to-end (map get-cell-value %))
                             cells-to-ram)
        f (fn [b [cells values]]
            (apply set-cell-values b (interleave cells values)))]
    (reduce f
            board
            (map vector cells-to-ram new-cell-values))))
