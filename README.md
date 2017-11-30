# xp-gradle-plugin-sitemerge
This Gradle plugin does the following:

Merges site.xml(com.enonic.xp.app) from included dependencies. It will merge 
into both your build site.xml and the src site.xml. The merged result will be marked
with merged="xp-gradle-plugin-sitemerge"

```
 <input name="key" merged="xp-gradle-plugin-sitemerge" type="TextLine">
      <label>API key (site)</label>
      <occurrences maximum="1" minimum="1"/>
 </input>
```

To use this plugin, just add the following to your build.gradle file: 

```
plugins {
  id 'com.enonic.xp.app' version '1.0.13'
  id 'no.tine.gradle.xp.SiteMerge' version '1.0.3'
}
```
NB! Needs gradle plugin com.enonic.xp.app.

Add this depends on jar in your build.gradle file. 

```
jar.dependsOn("mergeSitesXml")
```
