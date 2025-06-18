# SFE - Secure Financial Environment

[![](https://jitpack.io/v/Uttam-Mahata/SFE-Building-Hackathon.svg)](https://jitpack.io/#Uttam-Mahata/SFE-Building-Hackathon)

> **🚀 Hackathon Project**: This repository contains the Secure Financial Environment (SFE) project developed for the IIEST-UCO Bank Hackathon.

## Overview

This repository contains two main components:

1. **Client SDK (`csfe` module)**: A headless, secure financial transaction layer for Android applications
2. **Sample Payment App (`app` module)**: A demo application showcasing the integration of the Client SDK

## Getting Started

### Using the Client SDK in Your Project

Add JitPack repository to your root `build.gradle`:

```gradle
allprojects {
    repositories {
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

Add the dependency to your app `build.gradle`:

```gradle
dependencies {
    implementation 'com.github.Uttam-Mahata:SFE-Building-Hackathon:v1.0.0'
}
```

### Running the Sample Payment App

1. Clone this repository
2. Open in Android Studio
3. Build and run the `app` module

## Documentation

- [Client SDK Documentation](csfe/CLIENT-SDK-README.md)
- [Sample Payment App Documentation](app/SAMPLE-PAYMENT-APP-README.md)

## License

This project is licensed under the MIT License.

## Hackathon Project Notice

**⚠️ This is a prototype developed for hackathon demonstration. For production use, please ensure proper security audits and compliance validation.**
