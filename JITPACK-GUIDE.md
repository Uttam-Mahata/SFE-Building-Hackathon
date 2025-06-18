# Publishing to JitPack Guide

This document outlines how to publish the SFE Client SDK to JitPack for easy distribution.

## Prerequisites

1. The code is hosted on GitHub under your account
2. The repository is public
3. The project builds successfully

## Steps Taken

1. **Maven Publishing Plugin**: Added to csfe/build.gradle.kts
   ```kotlin
   plugins {
       id("maven-publish")
   }
   ```

2. **Publishing Configuration**: Added in csfe/build.gradle.kts
   ```kotlin
   android {
       publishing {
           singleVariant("release") {
               withSourcesJar()
               withJavadocJar()
           }
       }
   }

   afterEvaluate {
       publishing {
           publications {
               create<MavenPublication>("release") {
                   from(components["release"])
                   groupId = "com.github.Uttam-Mahata"
                   artifactId = "SFE-Building-Hackathon"
                   version = "v1.0.0"
               }
           }
       }
   }
   ```

3. **JitPack Configuration**: Added jitpack.yml to the repository root
   ```yaml
   jdk:
     - openjdk11
   before_install:
     - ./gradlew clean
   install:
     - ./gradlew :csfe:build :csfe:publishToMavenLocal
   ```

4. **GitHub Integration**: Added GitHub Actions workflow to validate the build

## Publishing Steps

1. **Tag the Release**: Create a tag in your GitHub repository
   ```bash
   git tag -a v1.0.0 -m "Release 1.0.0"
   git push origin v1.0.0
   ```

2. **Activate on JitPack**: Visit [JitPack.io](https://jitpack.io) and enter your GitHub repository URL. Click "Look up" and then "Get it".

3. **Use in Projects**: The library can now be used in other projects as:
   ```gradle
   implementation 'com.github.Uttam-Mahata:SFE-Building-Hackathon:v1.0.0'
   ```

## Testing the Publication

1. **Check Build Log**: On JitPack, check the build log for any errors
2. **Try a Sample App**: Create a simple app that only depends on the published library
3. **Verify Features**: Make sure all SDK features work as expected

## Notes

- JitPack will only build the library when you create a GitHub Release or tag
- Version numbers in your build.gradle should match the tag
- Make sure all dependencies are properly declared
