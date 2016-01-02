(ns repo-stats.core
  (:require [tentacles.repos :as repos]
            [tentacles.orgs :as orgs]))


(defn rate-limited? [resp]
  (or (= (:status resp) 403)
      (= (get-in resp [:headers "X-RateLimit-Remaining"]) "0")))

(defn with-rate-limit-retry
  "Retries rate limit errors up to 10 times. Takes a zero arg function that
   must represent an HTTP call"
  ([f] (with-rate-limit-retry f 0))
  ([f n]
   (if (< n 30)
     (let [result (f)]
       (if (rate-limited? result)
         (do (Thread/sleep (* 1000 60 5))
             (with-rate-limit-retry f (inc n)))
         result))
     (throw (Exception. "Gave up retrying")))))

(defn org-member-ids
  "Return a collection of members of an organization"
  [org opts]
  (map :id (with-rate-limit-retry #(orgs/members org (merge opts {:all-pages true})))))

(defn org-repo-names
  "Return a collection of all repos for an organization excluding forks"
  [org opts]
  (map
   :name
   (filter (comp not :fork)
           (with-rate-limit-retry #(orgs/repos org (merge opts {:all-pages true}))))))

(defn datetime-str->hour
  "Takes an isoformat datetimes string and returns the hour.

   Usage:
   (datetime-str->hour \"2015-12-18T22:31:23Z\")"
  [s]
  (-> s
      (clojure.string/split #"T")
      second
      (clojure.string/split #":")
      first
      (Long/parseLong)))

(defn remove-nil [hm] (dissoc hm nil))

(defn append-additions-deletions
  "For each commit, associates :stats to the map with additions,
   deletions, total.

   WARNING: This performs one HTTP request per commit and will be slow"
  [commits org repo opts]
  (for [c commits
        :when (map? c)
        :let [f #(repos/specific-commit org repo (:sha c) opts)
              resp (with-rate-limit-retry f)]]
    (assoc c :stats (:stats resp))))

(defn commit-frequency-by-user
  "Return a histogram of commit count by user"
  [commits]
  (remove-nil (frequencies (map #(-> % :author :login) commits))))

(defn commit-time-frequency-by-user
  "Return a histogram of commit time by user"
  [commits]
  (remove-nil
   (reduce
    (fn [accum el]
      (update accum (-> el :author :login)
              #(merge-with + % {(-> el :commit :author :date datetime-str->hour) 1})))
    {}
    commits)))

(defn commit-time-frequency
  "Return a histogram of commit time by user"
  [commits]
  (frequencies
   (map #(-> % :commit :author :date datetime-str->hour) commits)))

(defn adds-vs-deletes-by-user
  "Return a histogram of additions vs deletions by user"
  [commits-with-add-deletes]
  (remove-nil
   (reduce
    (fn [accum el]
      (update accum (-> el :author :login) #(merge-with + % (:stats el))))
    {}
    commits-with-add-deletes)))

(defn repo-stats
  [username password org repo start end]
  (let [opts {:auth (str username ":" password)
              :all-pages true}
        commits (-> (with-rate-limit-retry #(repos/commits org repo (merge opts {:since start :until end})))
                    (append-additions-deletions org repo opts))]
    (when-not (zero? (count commits))
      (println repo "\n")

      (println "Total Commits" (count commits))
      (println "")

      (println "Commit Count by User")
      (clojure.pprint/print-table
       [:name :commits]
       (map (fn [[k v]] {:name k :commits v}) (commit-frequency-by-user commits)))
      (println "")

      (println "Additions vs Deletions")
      (clojure.pprint/print-table
       [:name :additions :deletions :total]
       (map (fn [[k v]] (assoc v :name k)) (adds-vs-deletes-by-user commits)))
      (println "")

      (println "Commits by Hour Overall")
      (clojure.pprint/print-table
       (range 24)
       [(commit-time-frequency commits)])
      (println "")

      (println "Commits by Hour by User")
      (clojure.pprint/print-table
       (conj (range 24) :name)
       (map (fn [[k v]] (assoc v :name k)) (commit-time-frequency-by-user commits)))
      (println ""))))

(defn -main
  "Prints outs repo stats for each repo in the organization for the
   designated date range. Optionally pass in a repo as the last argument"
  [username password org start end & [repo]]
  (let [opts {:auth (str username ":" password)}
        repos (if repo [repo] (org-repo-names org opts))]
    (println "Repo stats for" org "from" start "to" end "\n")
    (doseq [repo repos]
      (repo-stats username password org repo start end))
    (System/exit 0)))
