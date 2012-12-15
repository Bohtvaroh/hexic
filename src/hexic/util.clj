(ns hexic.util
  [:require [clojure [string :as string]]])

(defn ^String str-join-repeat
  "Returns a string formed by repeating s n times, separating with separator.
  Supports optional prefix and suffix."
  [n separator s & {:keys [prefix suffix]}]
  (str prefix (string/join separator (repeat n s)) suffix))

(defn reduce-with-acc
  "Applies f to first two coll elements and acc producing new acc value for
  subsequent f calls. Returns acc. On singleton or empty collection returns
  acc immediately."
  [f acc coll]
  (loop [a (first coll) acc acc s (next coll)]
    (if (seq s)
      (let [b (first s)]
        (recur b (f a b acc) (next s)))
      acc)))

(defn flip
  "Flips function's argument list."
  [f] (fn [& args] (apply f (reverse args))))

(defn make-ring [coll]
  (concat coll (list (first coll)))) ; (a b c) -> (a b c a)

(defn os-type []
  (let [contains-str? (fn [^String s1 ^String s2] (.contains s1 s2))
        os (System/getProperty "os.name")]
    (condp contains-str? os
      "Mac OS X" :unix
      "Linux" :unix
      "Windows" :windows
      :other)))

(defmulti to-char class)
(defmethod to-char Long [l] (.charAt (str l) 0))
(defmethod to-char Character [c] c)
