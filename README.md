# set-git-info

[![Build Status](https://travis-ci.org/displacement-activity/lein-set-git-info.png?branch=master)](https://travis-ci.org/displacement-activity/lein-set-git-info)

A Leiningen plugin to inject git commit information into project files.

## Usage

Put `[set-git-info "1.0.0"]` into the `:plugins` vector of your `:user`
profile.

Provide details of files to update in `project.clj` e.g.

```clj
:set-git-info [{:path "src/myapp/buildinfo.clj" :search-regex #"::id::" :replace-field :id}
               {:path "credits.txt" :search-regex #"::author::" :replace-field :author}]
```

Then run with

    $ lein set-git-info
    
## Dry Run

To see what changes would be made without actually actioning them run as

    $ lein set-git-info :dry-run

## License

Copyright © 2016 Displacement Activity

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
