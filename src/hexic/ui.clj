(ns hexic.ui
  [:require [hexic [board :as b]
                   [format :as f]]
            [hexic [util :as u]]]
  [:import [java.nio.charset Charset]
           [com.googlecode.lanterna.input Key$Kind]
           [com.googlecode.lanterna.terminal
            Terminal Terminal$Color Terminal$SGR]
           [com.googlecode.lanterna.terminal.swing SwingTerminal]
           [com.googlecode.lanterna.terminal.text UnixTerminal]])

(def colors [0 1 2 3 4])

(defn- ^Terminal$Color get-color [^long i]
  (case i
    0 Terminal$Color/RED
    1 Terminal$Color/GREEN
    2 Terminal$Color/BLUE
    3 Terminal$Color/YELLOW
    4 Terminal$Color/CYAN
    Terminal$Color/DEFAULT))

(def ^:private last-turn-time (atom nil))
(def ^:private min-turn-duration "Minimum turn duration in ms." 2000)
(defn- update-last-turn-time []
  #(swap! last-turn-time (constantly (System/currentTimeMillis))))
(defn- wait-turn []
  (let [current-time (System/currentTimeMillis)]
   (do
     (let [prev-time @last-turn-time
           time-spent (if prev-time (- current-time prev-time) 0)
           time-left (- min-turn-duration time-spent)]
       (if (pos? time-left)
         (Thread/sleep time-left)))
     (update-last-turn-time))))

(defn ^Terminal create-terminal []
  (let [create-unix #(UnixTerminal.
                      System/in System/out (Charset/forName "UTF-8"))]
    (case (u/os-type)
     :unix (create-unix)
     (SwingTerminal.))))

(defn- reset-terminal [^Terminal t]
  (.moveCursor t 0 0)
  (.clearScreen t))

(defn- crlf [^Terminal t current-line]
  (let [next-line (inc current-line)]
    (do (.moveCursor t 0 next-line)
      next-line)))

(defn- print-line [^Terminal t s current-line]
  (do (doseq [c s] (.putCharacter t c))
      (crlf t current-line)))

(defn- apply-style [^Terminal t v is-cell-value is-in-triple is-in-cluster]
  (letfn [(applySGR [sgr]
            (.applySGR t (into-array [sgr])))]
   (do (if is-cell-value
         (.applyForegroundColor t (get-color v))
         (.applyForegroundColor t Terminal$Color/DEFAULT))
       (if is-in-triple (applySGR Terminal$SGR/ENTER_REVERSE))
       (if is-in-cluster (applySGR Terminal$SGR/ENTER_BLINK))
       (if-not (or is-cell-value is-in-triple is-in-cluster)
         (applySGR Terminal$SGR/RESET_ALL)))))

(defn- print-board [^Terminal t board-seq current-line]
  (loop [s board-seq current-line current-line]
    (if (seq s)
      (let [{:keys [value is-cell-value is-in-triple is-in-cluster]} (first s)]
        (if (= value \newline)
          (do (crlf t current-line)
              (recur (rest s) (inc current-line)))
          (do (apply-style t value is-cell-value is-in-triple is-in-cluster)
              (.putCharacter t (if is-in-cluster \o
                                   (if is-cell-value \* value)))
              (recur (rest s) current-line))))
      (crlf t current-line))))

(def ^:private paused (atom false))
(defn- check-hotkeys [^Terminal t]
  (let [key (.readInput t)]
    (if (and key (= (.getKind key) Key$Kind/NormalKey))
      (case (.getCharacter key)
        \q (do (.exitPrivateMode t)
               (System/exit 1))
        \space (if @paused
                 (do (swap! paused (constantly false))
                     (update-last-turn-time))
                 (do (swap! paused (constantly true))
                     (recur t)))
        nil)
      (if @paused
        (do (Thread/sleep 1000)
            (recur t))))))

(defn start-improved [initial-board turns]
  (let [t (create-terminal)]
    (do
      (.enterPrivateMode t)
      (reset-terminal t)
      (->> 0
           (print-board t (f/format-board initial-board))
           (crlf t)
           (print-line t "Initial board")
           (print-line t "Starting..."))
      (loop [turns turns total-score 0]
        (if-not (seq turns)
          (do (reset-terminal t)
              (->> 0
                   (print-line t "Total score:" total-score)
                   (print-line t "No more turns possible. Exiting.")))
          (let [{score :score
                 board :board
                 triple :triple
                 rotation :rotation
                 cluster-cells :cluster-cells} (first turns)
                 total-score' (+ total-score score)]
            (do (reset-terminal t)
                (->> 0
                     (print-board
                      t (f/format-board board
                                        :triple-cells (set triple)
                                        :cluster-cells cluster-cells))
                     (crlf t)
                     (print-line t (str "Triple: " triple))
                     (print-line t (str "Rotated: " (name rotation)))
                     (print-line t (str "Score achieved: " score))
                     (print-line t (str "Total score: " total-score))
                     (crlf t)
                     (print-line t (str "Press space to pause, q to quit.")))
                (wait-turn)
                (check-hotkeys t)
                (recur (rest turns) (long total-score'))))))
      (.exitPrivateMode t))))

(defn start-fallback [initial-board turns]
  (do
    (println "Initial board:")
    (println (f/format-board-simple initial-board))
    (println "Starting...")
    (wait-turn)
    (loop [turns turns total-score 0]
      (if-not (seq turns)
        (println "No more turns possible. Exiting...")
        (let [{score :score
               board :board
               triple :triple
               rotation :rotation} (first turns)
               total-score' (+ total-score score)]
          (do
            (println (f/format-board-simple board))
            (println)
            (println "Triple:" triple)
            (println "Rotated:" (name rotation))
            (println "Score achieved:" score)
            (println "Total score:" total-score')
            (wait-turn)
            (recur (rest turns) (long total-score'))))))))
