(defproject utils-common "0.1.0-SNAPSHOT"

  :description "various utilities"

  :url "https://bitbucket.org/pupcus/utils-common"

  :scm {:url "git@bitbucket.org:pupcus/utils-common"}

  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/tools.logging "0.4.0"]
                 [com.lowagie/itext "2.1.7"]
                 [commons-codec "1.11"]
                 [hiccup "1.0.5"]]

  :profiles {:dev  {:resource-paths ["dev-resources"]
                    :dependencies [[org.clojure/clojure "1.8.0"]
                                   [org.slf4j/slf4j-log4j12 "1.7.25"]]}}

  :deploy-repositories [["snapshots"
                         {:url "http://maven.pupcus.org/repository/snapshots"
                          :sign-releases false
                          :creds :gpg}]
                        ["releases"
                         {:url "http://maven.pupcus.org/repository/internal"
                          :sign-releases false
                          :creds :gpg}]]

  :release-tasks [["vcs" "assert-committed"]
                  ["change" "version" "leiningen.release/bump-version" "release"]
                  ["vcs" "commit"]
                  ["vcs" "tag" "--no-sign"]
                  ["deploy"]
                  ["change" "version" "leiningen.release/bump-version"]
                  ["vcs" "commit"]
                  ["vcs" "push"]])
