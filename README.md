Web interface that converts your YAML workflows to Kotlin.

## Start with Why?

The library [github-workflows-kt](https://github.com/typesafegithub/github-workflows-kt) allows you to author GitHub Actions workflows in Kotlin instead of YAML.

And it's great once you are into it, but there is a bootstrapping issue.

If you already have working GitHub workflows in YAML, the initial work of rewriting everything in Kotlin seems tedious and error-prone.

We are lazy developers, so we asked ourself: 

> What if you could automatically generate from the YAML the Kotlin script that would then generate the YAML?

## Status

Only for early adopters. 

If that's you, see [[CONTRIBUTING.md]]