(ns hexic.format
  [:require [hexic [board :as b]
                   [util :as u]]]
  [:require [clojure [string :as string]]])

(defn- lift
  [c & kvs]
  (let [lifted (hash-map :char c)]
    (if (seq kvs)
      (apply assoc lifted kvs)
      lifted)))

(defn- prepare-board [b triple-cells cluster-cells]
  (map-indexed
   (fn [row-idx row]
     (map-indexed
      (fn [col-idx value]
        (let [coord [col-idx row-idx]
              is-triple (contains? triple-cells coord)
              is-cluster-cell (contains? cluster-cells coord)]
         (lift value
               :is-cell true
               :is-triple is-triple
               :is-cluster-cell is-cluster-cell)))
      row))
   b))

(defn- gen-format
  "Simple generalization of format function. Returns a seq on pattern elements
  with each non-special character wrapped in f2 call, each special character
  substitution wrapped in f1 call. Special character: ~."
  [pattern f1 f2 & args]
  (loop [pattern pattern args args acc []]
    (if-not (seq pattern)
      acc
      (let [c (first pattern)]
        (if (= c \~)
          (recur (rest pattern) (rest args) (conj acc (f1 (first args))))
          (recur (rest pattern) args (conj acc (f2 c))))))))

(defn- decorate-row [idx row]
  (let [even-line-prefix (if (zero? idx) "  _ " "\\ _ ")
        odd-line-suffix " _ /"]
    (if (even? idx)
      (str even-line-prefix row)
      (str row odd-line-suffix))))

(defn- get-row-format [idx size]
  (->> (repeat size "/ ~ \\")
       (string/join " _ ")
       (decorate-row idx)))

(defn- format-row [idx row]
  (let [size (count row)
        nil-to-space #(if (nil? %) " " %)]
    (apply gen-format
           (get-row-format idx size)
           identity lift
           (map nil-to-space row))))

(defn- get-header [size]
  (u/str-join-repeat size "     " " _ " :prefix "     "))

(defn- get-footer [idx size]
  (let [prefix (if (even? idx) "" "    ")]
    (u/str-join-repeat size "   " "\\ _ /" :prefix prefix)))

(defn- add-header-and-footer [size rows]
  (let [header (map lift (get-header size))
        footer (map lift (get-footer (count rows) size))]
    (conj (into [header] rows) footer)))

(defn format-board
  "Returns board representation as a seq of {:char c :cluster? b} maps."
  [b & {:keys [triple-cells cluster-cells]}]
  (->>
   (prepare-board b triple-cells cluster-cells)  ; prepare board
   (map-indexed format-row)                      ; format board rows
   (add-header-and-footer (b/get-board-width b)) ; add header and footer
   (interpose [(lift \newline)])                 ; interpose with \n
   (apply concat)))                              ; join rows with new line


(defn format-board-simple
  "Returns board representation as a string simulating ASCII graphics."
  [b]
  (apply str (map :char (format-board b))))
