(defproject org.clojars.displacement-activity/lein-set-git-info "1.0.0"
  :description "A Leiningen plugin to inject git commit information into project files."
  :url "https://github.com/displacement-activity/lein-set-git-info"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :eval-in-leiningen true
  :dependencies [[clj-jgit "0.8.9"]]
  :lein-release {:deploy-via :clojars})
