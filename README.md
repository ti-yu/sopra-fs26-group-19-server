# Lend-a-Hand Server - Backend

## Introduction
In our society, many individuals (especially elderly people) wish to maintain their independence at home but frequently face small, everyday hurdles,such as grocery shopping, moving heavy items, or basic garden maintenance, that become difficult to manage alone due to missing strength or ilness.
The goal of **Lend-a-Hand** is to bridge this gap by connecting individuals in need of assistance **Recipients**, with passionate local helpers **Volunteers** which help out with small everyday tasks.
---
## Technologies Used

* **Java** - Core object-oriented server programming language.
* **Spring Boot / Spring** - Primary model-view-controller web server framework.
* **Java Persistence API (JPA) / Hibernate** - Data tier relational mapping framework.
* **H2 Database** - Volatile dev-stage in-memory relational database.
* **Gradle** - Automation software packaging and build tracking environment.
* **GitHub Actions** - Automated continuous integration build pipeline runner.
* **SonarQube** - Structural code checking analytics system.
* **Google Cloud Platform (App Engine)** - Production cloud deployment server platform.
---
## High-Level Components

Our Spring Boot backend application is organized into a layered architectural pattern. Each layer has an isolated operational responsibility, ensuring data integrity, security, and a strict separation of concerns.

### 1. The Controller Layer (REST API Endpoints)
* **Role**: Acts as the external gateway of the system. It exposes the REST API endpoints that the Next.js frontend calls to interact with the platform.
* **Core Files**:
    * [`UserController.java`](./src/main/java/ch/uzh.ifi.hase/soprafs26/controller/UserController.java) — Manages profiles, sign-ups, and user sessions.
    * [`InseratController.java`](./src/main/java/ch/uzh.ifi.hase/soprafs26/controller/InseratController.java) — Exposes creation, browsing, updates, and deletion of help listings.
* **Correlation**: Receives HTTP requests from the browser, relies on the **Rest Tier** to convert data, and forwards clean parameters down to the Service Layer.

### 2. The Rest Data Tier (DTOs & Mapper)
* **Role**: Acts as a defensive data buffer and abstraction layer between our internal database models and external HTTP payloads.
* **Core Directory**: [`rest/`](./src/main/java/ch/uzh.ifi.hase/soprafs26/rest/)
* **Key Files**:
    * **DTOs** (e.g., [`InseratPostDTO.java`](./src/main/java/ch/uzh/ifi/hase/soprafs26/rest/dto/InseratPostDTO.java), [`UserGetDTO.java`](./src/main/java/ch/uzh/ifi/hase/soprafs26/rest/dto/UserGetDTO.java)) — Specialized data structures that only contain the precise fields required for a specific web request or response, keeping sensitive fields (like raw passwords) safely encapsulated inside the server.
    * [`DTOMapper.java`](./src/main/java/ch/uzh/ifi/hase/soprafs26/rest/mapper/DTOMapper.java) — The conversion engine that automatically maps properties between raw Entities and their matching DTO representations.
* **Correlation**: Used constantly by Controllers to transform incoming user input into entities, or to down-sample database entities into safe transfer data before sending them back across the internet to the frontend client.

### 3. The Service Layer
* **Role**: The core logic engine of the platform. This layer executes transaction rules, validates user status permissions, and prevents system rule violations.
* **Core Files**:
    * [`UserService.java`](./src/main/java/ch/uzh/ifi/hase/soprafs26/service/UserService.java) — Encapsulates login checking and session validation.
    * [`InseratService.java`](./src/main/java/ch/uzh/ifi/hase/soprafs26/service/InseratService.java) — Governs support matching conditions (e.g., verifying listings are empty before allowing deletion).
    * [`ReviewService.java`](./src/main/java/ch/uzh/ifi/hase/soprafs26/service/ReviewService.java) — Automatically generates system evaluation requests when events finish, handling user cool-downs (24-hour snooze timers) for screen popup reminders.
* **Correlation**: Takes pure Java domain data objects from the Controller tier, handles evaluations, and commands the Repositories to interact with the database tables.

### 4. The Entity Layer (Domain Models)
* **Role**: Represents the structural state models and relational entity associations of the application.
* **Core Files**:
    * [`User.java`](./src/main/java/ch/uzh/ifi/hase/soprafs26/entity/User.java) — Tracks profiles.
    * [`Inserat.java`](./src/main/java/ch/uzh/ifi/hase/soprafs26/entity/Inserat.java) — Holds help request variables.
    * [`Review.java`](./src/main/java/ch/uzh/ifi/hase/soprafs26/entity/Review.java) — Tracks post-interaction feedback metrics.
* **Correlation**: Mentored directly by Hibernate annotations to generate database tables. These objects represent our persistent server truth.

### 5. The Repository Layer (Data Access Abstraction)
* **Role**: Manages database reads and writes. It abstracts data access into clean object methods, entirely eliminating manual SQL string queries.
* **Core Directory**: [`repository/`](./src/main/java/ch/uzh/ifi/hase/soprafs26/repository/)
* **Key Files**: [`UserRepository.java`](./src/main/java/ch/uzh/ifi/hase/soprafs26/repository/UserRepository.java), [`InseratRepository.java`](./src/main/java/ch/uzh/ifi/hase/soprafs26/repository/InseratRepository.java), [`ReviewRepository.java`](./src/main/java/ch/uzh/ifi/hase/soprafs26/repository/ReviewRepository.java)
* **Correlation**: Invoked exclusively by the Service layer to query and modify rows inside the relational H2 database storage engine.


### The Core Application Lifecycle

Here is how data flows across the entire architecture during a common user event, such as a client submitting a new help request:

1. **Client Request**: The Next.js client transmits a JSON payload representing a new listing to `POST /help-requests`.
2. **Controller & Unpacking**: The **`InseratController`** intercepts the request body as an **`InseratPostDTO`** object.
3. **DTO Mapping**: The controller utilizes the **`DTOMapper`** to cleanly transform the `InseratPostDTO` fields into a real **`Inserat`** database entity.
4. **Business Validation**: The controller invokes the **`InseratService`** with the new entity. The service runs validation routines (such as checking address parameters via coordinates and ensuring the user account is authentic).
5. **Database Persistence**: If all business rules pass, the service fires `inseratRepository.save(inserat)`. The **`InseratRepository`** commands **Hibernate** to run SQL insert actions against the database tables.
6. **Data Return Transformation**: The newly updated database entity is returned to the controller, which passes it back through the **`DTOMapper`** to convert it into a sleek, secure **`InseratGetDTO`**, returning a clean JSON response back to the user's dashboard.

---
## Launch & Deployment

Our Spring Boot server includes an embedded Tomcat application container and a local Gradle Wrapper ecosystem, meaning no external web server installations are required to run the codebase locally.
### Getting started with Spring Boot
-   Documentation: https://docs.spring.io/spring-boot/docs/current/reference/html/index.html
-   Guides: http://spring.io/guides
    -   Building a RESTful Web Service: http://spring.io/guides/gs/rest-service/
    -   Building REST services with Spring: https://spring.io/guides/tutorials/rest/

### Prerequisites
Before launching, ensure your local workspace has:
* **Java Development Kit (JDK) 17** (Verify that your `JAVA_HOME` environment variable is set correctly).
---

### Building with Gradle

#### Standard Boot Execution
1. Clone the repository and navigate to the project directory root.
2. Build and compile the core dependency tree:
   You can use the local Gradle Wrapper to build the application.
-   macOS: `./gradlew`
-   Linux: `./gradlew`
-   Windows: `./gradlew.bat`

### Build

```bash
./gradlew build
```

### Run

```bash
./gradlew bootRun
```

You can verify that the server is running by visiting `localhost:8080` in your browser.

### Test

```bash
./gradlew test
```

### Development Mode
You can start the backend in development mode, this will automatically trigger a new build and reload the application
once the content of a file has been changed.

Start two terminal windows and run:

`./gradlew build --continuous`

and in the other one:

`./gradlew bootRun`

If you want to avoid running all tests with every change, use the following command instead:

`./gradlew build --continuous -xtest`

## Debugging
If something is not working and/or you don't know what is going on. We recommend using a debugger and step-through the process step-by-step.

To configure a debugger for SpringBoot's Tomcat servlet (i.e. the process you start with `./gradlew bootRun` command), do the following:

1. Open Tab: **Run**/Edit Configurations
2. Add a new Remote Configuration and name it properly
3. Start the Server in Debug mode: `./gradlew bootRun --debug-jvm`
4. Press `Shift + F9` or the use **Run**/Debug "Name of your task"
5. Set breakpoints in the application where you need it
6. Step through the process one step at a time

---

## Git Workflow & Collaboration Guide

To keep our codebase stable and prevent team members from accidentally overwriting each other's changes, **never write code or push commits directly to the `main` branch**. Always use the following **Feature-Branch Workflow** to isolate your work for each specific GitHub Issue:

### 1. Sync Your Local Machine with the Cloud
Before starting any new task or writing a single line of code, pull the latest, verified changes from your team to avoid merge conflicts later.
```bash
# Switch your local workspace to the main branch
git checkout main

# Download and merge the latest code from GitHub
git pull origin main 
```

### 2. Create a Dedicated Branch for Your GitHub Issue
Every feature, bug fix, or sub-task must live on its own separate branch. Name the branch clearly after the issue number or task you are tackling.
```bash
# Creates a new branch and immediately switches you onto it
git checkout -b task-<issue-number>

# Example:
git checkout -b task-104-login-screen
```
### 3. Monitor Your Work in Progress
As you edit or add files, use diagnostic commands frequently to see exactly what changes are sitting in your workspace.
```bash
# Lists which files have been modified, deleted, or are currently untracked
git status

# Shows a line-by-line comparison of your code changes since your last save
git diff
```
### 4. Save and Upload Your Progress
Once your feature is complete and working locally, bundle your changes, create a local snapshot, and publish your branch up to GitHub.
```bash
# 1. Stage all modified files to prepare them for a snapshot save
git add .

# 2. Commit the staged changes with a clear, descriptive message and the task number with a #
git commit -m "implemented screenreader aria labels for request inputs #127"

# 3. Push your local branch up to the remote GitHub repository
git push origin task-<issue-number>
```
### 5. Merge Your Code on GitHub via Pull Request
1. Navigate to the project repository page on GitHub in your web browser.

2. Click the green "Compare & pull request" banner that automatically appears at the top of the page.

3. Link the Pull Request to your original task tracking card or issue.

4. Verify that the automated GitHub Actions continuous integration pipeline tests run successfully (turn green).

5. Once reviewed and approved by a team member, click "Squash and merge" to safely absorb your completed branch back into the stable main production codebase.

---
## Roadmap
### 1. Volunteer Profile Verification
* **Trusted Badges**: Add a verification status (like `UNVERIFIED` or `VERIFIED`) to the user profile. This allows administrators to review a volunteer's identity or background check before they are allowed to apply to help requests.

### 2. Accessibility & Skill Tags for Help Requests
* **Specialized Assistance Matching**: Add an accessibility tag system to our help requests (`Inserat` entity). When a client creates a post, they will be able to tag it for specific needs, such as requiring a volunteer who knows **Sign Language** or a helper with a **valid driver's license**.
---
## Authors and Acknowledgment

This platform was designed, engineered, and maintained by:

* **Timur Yu** ([ti-yu](https://github.com/ti-yu))
* **Lisa Gehrig** ([lisgeh2](https://github.com/lisgeh2))
* **Jonathan Boggia** ([jonathanbogia](https://github.com/jonathanboggia))
* **Romeo Pestalozzi** ([romevp](https://github.com/romevp))
---
## License
MIT
