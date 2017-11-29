# xp-gradle-plugin-sitemerge
This Gradle plugin does the following:

Merges site.xml(com.enonic.xp.app) from included dependencies.

NB! Needs gradle plugin com.enonic.xp.app.


To use this plugin, just add the following to your build.gradle file:

```
plugins {
  id 'com.enonic.xp.app' version '1.0.13'
  id 'no.tine.gradle.xp.SiteMerge' version '1.0.0'
}
```

Add a task. E.g 

```
task merge(type: SiteMerge) {	
  siteXml = 'src/main/resources/site/site.xml'
  target = 'build/resources/main/site/site.xml'
}
```
