(ns hexic.format
  [:require [hexic [board :as b]
                   [util :as u]]]
  [:require [clojure [string :as string]]])

(defrecord Element [value
                    ^boolean is-cell-value
                    ^boolean is-in-triple
                    ^boolean is-in-cluster])
(defn make-element
  [value & {:keys [is-cell-value is-in-triple is-in-cluster]
            :or {is-cell-value false is-in-triple false is-in-cluster false}}]
  (->Element value is-cell-value is-in-triple is-in-cluster))

(defn- create-element
  [c & kvs]
  (let [elt (make-element c)]
    (if (seq kvs)
      (apply assoc elt kvs)
      elt)))

(defn- prepare-board [b triple-cells cluster-cells]
  (map-indexed
   (fn [row-idx row]
     (map-indexed
      (fn [col-idx value]
        (let [coord [col-idx row-idx]
              is-in-triple (contains? triple-cells coord)
              is-in-cluster (contains? cluster-cells coord)]
          (->Element value true is-in-triple is-in-cluster)))
      row))
   b))

(defn- gen-format
  "Simple generalization of format function. Returns a seq on pattern elements
  with each non-special character wrapped in f1 call, each special character
  substitution wrapped in f2 call. Special character: ~."
  [pattern f1 f2 & args]
  (loop [pattern pattern args args acc []]
    (if-not (seq pattern)
      acc
      (let [c (first pattern)]
        (if (= c \~)
          (recur (rest pattern) (rest args) (conj acc (f2 (first args))))
          (recur (rest pattern) args (conj acc (f1 c))))))))

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
           create-element identity
           (map nil-to-space row))))

(defn- get-header [size]
  (u/str-join-repeat size "     " " _ " :prefix "     "))

(defn- get-footer [idx size]
  (let [prefix (if (even? idx) "" "    ")]
    (u/str-join-repeat size "   " "\\ _ /" :prefix prefix)))

(defn- add-header-and-footer [size rows]
  (let [header (map create-element (get-header size))
        footer (map create-element (get-footer (count rows) size))]
    (conj (into [header] rows) footer)))

(defn format-board
  "Returns board representation as a seq of Element records."
  [b & {:keys [triple-cells cluster-cells]}]
  (->>
   (prepare-board b triple-cells cluster-cells)  ; prepare board
   (map-indexed format-row)                      ; format board rows
   (add-header-and-footer (b/get-board-width b)) ; add header and footer
   (interpose [(create-element \newline)])       ; interpose with \n
   (apply concat)))                              ; join rows with new line


(defn format-board-simple
  "Returns board representation as a string simulating ASCII graphics."
  [b]
  (apply str (map :value (format-board b))))
