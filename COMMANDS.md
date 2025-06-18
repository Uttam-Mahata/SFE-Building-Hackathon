

# SFE: Secure Financial Environment SDK & Sample App

[![JitPack](https://jitpack.io/v/Uttam-Mahata/SFE-Building-Hackathon.svg)](https://jitpack.io/#Uttam-Mahata/SFE-Building-Hackathon)

This repository contains the source code for the **Secure Financial Environment (SFE)**, developed for the IIEST-UCO Bank Hackathon. The project is divided into two main components:

1.  A headless **Client SDK (`csfe`)** providing core financial transaction functionalities.
2.  A **Sample Payment App (`app`)** demonstrating how to integrate and use the `csfe` SDK.

## Project Structure

-   `/csfe`: The Android library module for the Client SDK. This is the module that is published to JitPack.
-   `/app`: The Android application module that serves as a sample implementation of the SDK.

## Features of the `csfe` SDK

-   **Authentication**: Secure user login and session management.
-   **Wallet Management**: Functions to check balance, and manage wallet details.
-   **Payments**: Initiate and process payments securely.
-   **Transaction History**: Retrieve a list of past transactions.
-   **QR Code Payments**: Generate and scan QR codes for easy payments.
-   **Security**: Core security features for data protection.
-   **Fraud Detection**: Basic mechanisms for identifying potentially fraudulent activities.

---

## Getting Started

### Prerequisites

-   Android Studio (latest stable version recommended)
-   JDK 17
-   Git

### Using the SDK in Your Own App (via JitPack)

The `csfe` client SDK is published on JitPack. To add it as a dependency to your Android project, follow these steps:

1.  **Add the JitPack repository** to your root `settings.gradle.kts` file:

    ```kotlin
    // settings.gradle.kts
    dependencyResolutionManagement {
        repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
        repositories {
            google()
            mavenCentral()
            maven { url = uri("https://jitpack.io") } // Add this line
        }
    }
    ```

2.  **Add the dependency** to your app-level `build.gradle.kts` file. Replace `v1.0.0` with the desired release version.

    ```kotlin
    // app/build.gradle.kts
    dependencies {
        // ... other dependencies
        implementation("com.github.Uttam-Mahata:SFE-Building-Hackathon:v1.0.0")
    }
    ```

### Building the Project Locally

If you want to contribute to the SDK or run the sample app, you can build the project from the source.

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/Uttam-Mahata/SFE-Building-Hackathon.git
    ```

2.  **Open the project** in Android Studio. Gradle will automatically sync and download the required dependencies.

3.  **Run the sample app:** Select the `app` configuration from the dropdown and click the 'Run' button.

---

## Essential Commands

All commands should be run from the root directory of the project (`/home/uttam/IIEST-UCO Bank Hackathon/SFE-Building-Hackathon/SFE/`).

### Clean the Build

This command removes all generated files (build outputs).

```bash
./gradlew clean
```

### Build the Entire Project

This compiles both the `csfe` library and the `app` module.

```bash
./gradlew build
```

### Run Unit Tests for the SDK

This command executes all unit tests located in the `csfe` module.

```bash
./gradlew :csfe:testDebugUnitTest
```

### Install the Sample App on a Connected Device/Emulator

This builds the debug version of the sample app and installs it.

```bash
./gradlew :app:installDebug
```

### Build the SDK and Publish to Maven Local

This is a crucial command for testing the publishing process locally before pushing to GitHub. It mimics the process JitPack uses.

```bash
./gradlew :csfe:publishToMavenLocal
```

---

## How to Publish a New Version of the SDK

To publish a new version of the `csfe` SDK to JitPack, you need to create a new release on GitHub. JitPack automatically builds any new tag or release you create.

1.  **Commit all your changes** to the `main` or `master` branch.

    ```bash
    git add .
    git commit -m "feat: Add new feature for v1.0.1"
    git push origin main
    ```

2.  **Create a new Git tag.** Use semantic versioning (e.g., `v1.0.1`, `v1.1.0`).

    ```bash
    # Create an annotated tag for the new version
    git tag -a v1.0.1 -m "Release version 1.0.1"
    ```

3.  **Push the tag to GitHub.**

    ```bash
    git push origin v1.0.1
    ```

4.  **Verify on JitPack.** Go to the [JitPack page for your repository](https://jitpack.io/#Uttam-Mahata/SFE-Building-Hackathon), look for the new tag, and click "Get it" to trigger the build. Once the build log turns green, the new version is live and can be used as a dependency.

## Git Workflow Note

To avoid issues with unrelated histories, it is recommended to use a feature-branch workflow:

1.  Create a new branch for each new feature or fix:
    ```bash
    git checkout -b feature/my-new-feature
    ```
2.  Commit your work on this branch.
3.  Merge the branch back into `main` (preferably via a Pull Request on GitHub):
    ```bash
    # Switch back to the main branch
    git checkout main

    # Pull the latest changes
    git pull origin main

    # Merge your feature branch
    git merge feature/my-new-feature
    ```

## License

This project is licensed under the MIT License - see the `LICENSE` file for details.