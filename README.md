# xp-gradle-plugin-sitemerge
[![Build Status](https://travis-ci.org/tineikt/xp-gradle-plugin-sitemerge.svg?branch=master)](https://travis-ci.org/tineikt/xp-gradle-plugin-sitemerge)
[![License](https://img.shields.io/github/license/tineikt/xp-gradle-plugin-sitemerge.svg)](https://www.gnu.org/licenses/gpl-3.0.en.html)

This Gradle plugin does the following:

Merges site.xml(com.enonic.xp.app) from included dependencies. It will merge 
into both your build site.xml and the src site.xml. The merged result will be marked
with merged="xp-gradle-plugin-sitemerge-youtube"

```
 <input name="key" merged="xp-gradle-plugin-sitemerge-{jar}" type="TextLine">
      <label>API key (site)</label>
      <occurrences maximum="1" minimum="1"/>
 </input>
```

To use this plugin, just add the following to your build.gradle file: 

```
plugins {
  id 'com.enonic.xp.app' version '1.0.17'
  id 'no.tine.gradle.xp.SiteMerge' version '1.1.1'
}
```
NB! Needs gradle plugin com.enonic.xp.app.

Add this depends on jar in your build.gradle file. 

```
jar.dependsOn("mergeSitesXml")
```
