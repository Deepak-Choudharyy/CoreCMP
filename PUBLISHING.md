# Maven Central Publish Checklist

## Requirements

Sonatype account
GPG key
GitHub repository
Library version ready

---

## Step 1 - group id

Published via JitPack:

group: `com.corecmp`

artifact: `coreCmp`

---

## Step 2 - gradle config

group = "com.corecmp"

version = "1.0.03-alpha-11"

---

## Step 3 - add plugins

plugins {

id("maven-publish")

id("signing")

}

---

## Step 4 - configure publishing

publishing {

publications {

create<MavenPublication>("release") {

groupId = "com.aj"

artifactId = "corecmp"

version = "1.0.0"

}

}

}

---

## Step 5 - sign artifacts

signing {

sign(publishing.publications)

}

---

## Step 6 - publish

./gradlew publish

---

## Step 7 - release in Sonatype

login:

https://central.sonatype.com

release repository

---

## Step 8 - verify dependency

implementation("com.aj:corecmp:1.0.0")

---

Checklist

[ ] version updated
[ ] changelog updated
[ ] readme updated
[ ] build successful
[ ] sample tested