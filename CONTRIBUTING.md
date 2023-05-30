# CONTRIBUTING

## Requesites

- Java 11
- IntelliJ

## The samples

The [samples](samples) contains dozens of YAML workflows

```bash
$ ls samples | head -4
AllTriggers.kt
AllTriggers.yml
AutoAssignPR.kt
AutoAssignPR.yml
```

You can add your own workflows there.

To create the Kotlin script, run the tests

```bash
$ ./gradlew yaml2kotlin:test
```

## Running the web interface

The web interface can be run via

```bash
$ ./gradlew web:run
```

Then open `http://0.0.0.0:8080/`

Copy-paste a YAML workflow from the `samples` folder

**The web interface is experimental**

▶️ [Have a look at the issues](https://gthub.com/typesafegithub/yaml2kotlin/issues)


