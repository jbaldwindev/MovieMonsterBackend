# MovieMonster Backend

Spring Boot backend for MovieMonster, a social movie logging platform where users can rate movies, build movie lists, manage friends, comment on films, and view movie discovery data.

## Features

- User registration, login, refresh, logout, and current-user lookup
- JWT-backed stateless API security with HTTP-only auth cookies
- PostgreSQL persistence through Spring Data JPA
- Movie search, details, popular, top-rated, and now-playing data through TMDB
- User movie ratings and sorted profile movie lists
- Friend requests, friend lists, user search, and profile data
- Movie comments with like and unlike support
- Favorite movie management and favorite ranking
- Profile icon upload backed by AWS S3
- Unit and integration test workflows through Maven and GitHub Actions

## Tech Stack

- Java 25
- Spring Boot 3.5
- Maven
- Spring Web, WebFlux, Security, Data JPA
- PostgreSQL
- JSON Web Tokens
- AWS SDK for S3
- TMDB API integration
- Docker

## Repository Layout

```text
.
├── Dockerfile
├── README.md
└── demo
    ├── pom.xml
    ├── mvnw
    ├── system.properties
    └── src
        ├── main/java/com/MovieMonster/demo
        │   ├── Config
        │   ├── Controllers
        │   ├── Dto
        │   ├── Models
        │   ├── Repositories
        │   ├── Security
        │   └── Services
        └── test
```

The Maven project is located in `demo/`. Run Maven commands from that directory unless noted otherwise.

## Prerequisites

- JDK 25
- PostgreSQL 16 or compatible local PostgreSQL instance
- TMDB API key
- AWS S3 bucket and credentials for profile icon upload

For local development, the `local` Spring profile provides defaults for most settings. A real TMDB key is required for movie search and movie metadata calls to return useful data.

## Configuration

Production configuration is read from environment variables in `demo/src/main/resources/application.properties`.

| Variable | Purpose |
| --- | --- |
| `JDBC_DATABASE_URL` | PostgreSQL JDBC URL |
| `JDBC_DATABASE_USERNAME` | Database username |
| `JDBC_DATABASE_PASSWORD` | Database password |
| `JWT_SECRET` | Secret used to sign JWTs |
| `TMDB_KEY` | TMDB API key |
| `S3_BUCKET_NAME` | S3 bucket for profile icons |
| `S3_ACCESS_KEY` | AWS access key |
| `S3_SECRET_KEY` | AWS secret key |
| `APP_CORS_ALLOWED_ORIGINS` | Comma-separated allowed frontend origins |
| `APP_CORS_ALLOWED_ORIGIN_PATTERNS` | Comma-separated allowed origin patterns |
| `APP_COOKIE_SECURE` | Whether auth cookies should be marked secure |
| `APP_COOKIE_SAME_SITE` | Cookie SameSite value |
| `APP_COOKIE_DOMAIN` | Optional cookie domain |

Local profile overrides use `LOCAL_`-prefixed variables and default to:

```properties
LOCAL_JDBC_DATABASE_URL=jdbc:postgresql://localhost:5432/MovieMonster
LOCAL_JDBC_DATABASE_USERNAME=postgres
LOCAL_JDBC_DATABASE_PASSWORD=postgres
LOCAL_JWT_SECRET=local-dev-jwt-secret-change-me
LOCAL_TMDB_KEY=
LOCAL_S3_BUCKET_NAME=local-dev-bucket
LOCAL_S3_ACCESS_KEY=local-access-key
LOCAL_S3_SECRET_KEY=local-secret-key
```

## Local Development

1. Create a local PostgreSQL database:

   ```bash
   createdb MovieMonster
   ```

2. Start the application with the local profile:

   ```bash
   cd demo
   LOCAL_TMDB_KEY=your_tmdb_key ./mvnw spring-boot:run -Dspring-boot.run.profiles=local
   ```

3. The API will be available at:

   ```text
   http://localhost:8080
   ```

Spring JPA is configured with `spring.jpa.hibernate.ddl-auto=update`, so the application creates and updates tables automatically for local development.

## API Overview

Most endpoints require authentication. Public endpoints are:

- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/refresh`
- `POST /api/auth/logout`
- `GET /api/auth/me`
- `GET /api/user/auth/user-exists/{username}`
- `GET /api/user/icon/{username}`

Main authenticated route groups:

| Area | Routes |
| --- | --- |
| Authentication | `/api/auth/**` |
| Users and friends | `/api/user/**` |
| Movies, ratings, comments | `/api/movie/**` |
| Dashboard movie discovery | `/api/dash/**` |

Representative endpoints:

- `GET /api/movie/{id}` - fetch movie details
- `GET /api/movie/search/{title}` - search movies
- `GET /api/movie/search/{title}/{page}` - paged movie search
- `POST /api/movie/rate` - create or update a movie rating
- `GET /api/movie/list/{username}&sort={asc|desc}` - fetch a user's movie list
- `POST /api/movie/post-comment` - post a movie comment
- `POST /api/user/send-request` - send a friend request
- `POST /api/user/request-response` - accept or reject a friend request
- `GET /api/user/profile/{username}` - fetch profile information
- `POST /api/user/upload-icon/{username}` - upload a profile icon
- `GET /api/dash/popular/{page}` - popular movies
- `GET /api/dash/top/{page}` - top-rated movies
- `GET /api/dash/playing/{page}` - now-playing movies

## Testing

Run unit tests:

```bash
cd demo
./mvnw test
```

Run integration tests:

```bash
cd demo
./mvnw -DskipTests package failsafe:integration-test failsafe:verify
```

Integration tests expect a PostgreSQL database. By default, test configuration uses:

```properties
TEST_DB_URL=jdbc:postgresql://localhost:5432/moviemonster_test
TEST_DB_USERNAME=postgres
TEST_DB_PASSWORD=postgres
```

## Build

Package the application:

```bash
cd demo
./mvnw clean package
```

Skip tests while packaging:

```bash
cd demo
./mvnw clean package -DskipTests
```

Run the packaged application:

```bash
java -jar demo/target/*.jar
```

## Docker

Build the image from the repository root:

```bash
docker build -t moviemonster-backend .
```

Run the container:

```bash
docker run --rm -p 8080:8080 \
  -e JDBC_DATABASE_URL=jdbc:postgresql://host.docker.internal:5432/MovieMonster \
  -e JDBC_DATABASE_USERNAME=postgres \
  -e JDBC_DATABASE_PASSWORD=postgres \
  -e JWT_SECRET=change-me \
  -e TMDB_KEY=your_tmdb_key \
  -e S3_BUCKET_NAME=your_bucket \
  -e S3_ACCESS_KEY=your_access_key \
  -e S3_SECRET_KEY=your_secret_key \
  moviemonster-backend
```

## CI/CD

GitHub Actions is configured to:

- Run unit tests
- Package the Spring Boot application
- Build a Docker image
- Run integration tests against PostgreSQL 16
- Push Docker tags and trigger staging deployment on pushes to `main`

## Notes

- The application uses HTTP-only cookies named `accessToken` and `refreshToken`.
- CORS is configured through environment variables and allows credentials.
- Database schema management is currently handled by Hibernate auto-DDL.
- The deployed runtime targets Java 25, as declared in `demo/system.properties` and the Dockerfile.
