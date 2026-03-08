# PROJECT CONTEXT: SmartSplit Backend

We are building a backend service for an expense splitting application similar to Splitwise called "SmartSplit".

## TECH STACK

* Java
* Spring Boot
* Spring Data JPA
* PostgreSQL
* Flyway (for database migrations)
* Lombok
* REST APIs

## ARCHITECTURE
We are following a modular monolith architecture.
The project is organized by modules such as:

* user module
* group module
* expense module (to be implemented later)
* settlement module (future)

Each module follows this structure:

* entity
* repository
* service
* controller

We are building the backend step-by-step in a clean layered architecture:
Controller → Service → Repository → Database.

## DATABASE STRATEGY
We use PostgreSQL with the database name: `smartsplit`.

Database rules:

* Schema: public
* Tables are created via Flyway migrations
* UUID is used as the primary key for entities
* Developers do NOT manually modify the database
* Any schema change must be done through Flyway migration scripts

Example tables planned:

* users
* groups
* group_members
* expenses (later)
* expense_splits (later)
* settlements (later)

## COLLABORATION RULES
There are two developers working on this project.

**Developer 1 responsibilities:**

* Authentication
* User module
* User entity
* UserRepository
* Signup/login logic
* JWT security (future)

**Developer 2 responsibilities (current work):**

* Group module
* Group entity
* GroupMember entity
* GroupRepository
* GroupMemberRepository
* GroupService
* GroupController

## GIT WORKFLOW
We follow a simplified GitFlow:

* `main` → production stable branch
* `develop` → integration branch
* `feature` branches → development work

Workflow:

1. Always pull latest `develop`
2. Create `feature` branch
3. Work and commit
4. Push `feature` branch
5. Create Pull Request → `develop`

Example branch names:

* `feature/group-module`
* `feature/expense-module`

## CURRENT IMPLEMENTATION STATUS

**Implemented:**

* Spring Boot project setup
* PostgreSQL connection
* Flyway setup
* User entity (created by Developer 1)
* `groups` table migration
* `group_members` table migration
* `Group` entity
* `GroupRepository`
* `GroupMemberRepository`
* `GroupService` with `createGroup()`

**Currently working on:**

* `GroupController`
* API: `POST /groups`

**Remaining in group module:**

* Add member to group
* Validate user existence
* Prevent duplicate membership
* List groups for a user

**Future modules:**

* Expense module
* Expense split logic
* Settlement algorithm
* Balance calculation
* AI features

## CODING GUIDELINES

* Use Lombok for getters/setters
* Use UUID for IDs
* Follow Spring Boot best practices
* Keep controllers thin
* Put business logic in services
* Avoid writing SQL in services (use repositories)

## API Design: DTOs, Validation, and API Contracts

A critical system design improvement is the use of **Data Transfer Objects (DTOs)** to define clean, stable API contracts.

**Problem:** Directly exposing JPA entities (`@Entity`) in REST controllers is a common but problematic practice. It tightly couples your API to your database schema and can lead to security vulnerabilities.

**Solution:** Use DTOs for all data exchange between the client and the server.

### What are DTOs?

DTOs are simple objects that define the shape of data for API requests and responses. They are not tied to the database and contain only the fields relevant to a specific API endpoint.

### Architectural Flow with DTOs

The data flow is updated to include a mapping step in the service layer.

**Controller (DTO) → Service (maps DTO to Entity) → Repository (saves Entity)**

1.  **Controller:** Receives a request DTO (`CreateGroupRequest`). It uses `@Valid` to trigger validation.
2.  **Service:** Receives the DTO from the controller. It maps the DTO to a JPA `Group` entity before saving it. For responses, it maps the `Group` entity to a `GroupResponse` DTO.
3.  **Repository:** Works exclusively with JPA entities.

### Benefits of Using DTOs

1.  **Stable API Contract:** You can change your internal database entities without breaking your public API.
2.  **Security:** Prevents accidental exposure of sensitive entity fields (like password hashes or internal timestamps). You only expose what is necessary.
3.  **Validation:** DTOs are the perfect place for input validation using annotations like `@NotBlank`, `@Size`, and `@Email`.
4.  **Clarity & Documentation:** The DTO classes themselves form a clear contract of what your API expects as input and what it will return as output.

### Example: Create Group

Instead of passing the `Group` entity to the controller, we use a `CreateGroupRequest` DTO.

**`CreateGroupRequest.java`**
```java
public record CreateGroupRequest(
    @NotBlank(message = "Group name cannot be empty")
    @Size(min = 3, max = 50, message = "Group name must be between 3 and 50 characters")
    String name,

    @NotNull(message = "Creator ID cannot be null")
    UUID createdBy
) {}
```

**`GroupController.java`**
```java
@RestController
@RequestMapping("/api/groups")
public class GroupController {

    private final GroupService groupService;

    // ... constructor ...

    @PostMapping
    public ResponseEntity<GroupResponse> createGroup(@Valid @RequestBody CreateGroupRequest request) {
        GroupResponse response = groupService.createGroup(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
```

This approach leads to a more robust, secure, and maintainable system. All new endpoints should follow this pattern.

## TASK
Continue implementing the Group module following the architecture above.
Do not break existing structure and follow the same coding style.
