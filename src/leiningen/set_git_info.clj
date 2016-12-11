(ns leiningen.set-git-info
  (:require [clj-jgit.porcelain :as p]
            [clj-jgit.querying :as q]
            [leiningen.core.main :refer [warn info]]
            [clojure.string :as s])
  (:import (java.io File)
           (clojure.lang IPersistentCollection)
           (java.util Date)
           (java.text SimpleDateFormat)))

(defn last-commit-info [root]
  (p/with-repo root
               (q/commit-info repo (first (p/git-log repo)))))

(defn canUpdate [^String path]
  (let [file (File. path)]
    (and (.canRead file) (.canWrite file))))

(defmacro with-updatable-file [path action]
  `(if (canUpdate ~path)
     ~action
     (warn (str "Failed to update: " ~path))))

(defmulti update-file (fn [mode _ _ _] mode))

(defmethod update-file :default [_ path search-regex replace-value]
  (with-updatable-file path
    (as-> path $
          (slurp $)
          (clojure.string/replace $ search-regex replace-value)
          (spit path $))))

(defmethod update-file :dry-run [_ path search-regex replace-value]
  (with-updatable-file path
                       (info (str "Will update: '" path "' replacing '" search-regex "' with '" replace-value "'"))))

(defn is-valid [args]
  (or (empty? args) (= ":dry-run" (s/join args))))

(def usage "lein set-git-info [:dry-run]")

(defmulti str-format class)

(defmethod str-format IPersistentCollection [c] (s/join ", " c))
(defmethod str-format Date [d] (.format (SimpleDateFormat. "yyyy-MM-dd'T'HH:mm:ssZ") d))
(defmethod str-format :default [s] (str s))

(defn set-git-info
  "Injects git last commit info into projects files."
  [project & args]
  (if (is-valid args)
    (let [info (last-commit-info (:root project))
          mode (when-let [mode-str (first args)] (read-string mode-str))]
      (doseq [i (:set-git-info project)]
        (update-file mode (:path i) (:search-regex i) (str-format (get info (:replace-field i))))))
    (info usage)))