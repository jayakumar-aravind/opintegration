# Basic Auth Array API

A tiny Java REST API protected by HTTP Basic Authentication that returns JSON arrays.
Uses only the JDK — no external dependencies.

## Requirements
- Java 17+
- Maven (optional)

## Run

With Maven:
```bash
mvn package
java -jar target/basic-auth-array-api-1.0.0.jar
```

Without Maven (Java 17+ single-file launch):
```bash
cd src/main/java
java com/example/api/App.java
```

Default credentials: `admin` / `secret`. Override with environment variables:
```bash
API_USER=myuser API_PASSWORD=mypass PORT=9090 java -jar target/basic-auth-array-api-1.0.0.jar
```

## Endpoints

| Method | Path            | Auth  | Returns                 |
|--------|-----------------|-------|-------------------------|
| GET    | `/api/users`    | Basic | JSON array of users     |
| GET    | `/api/products` | Basic | JSON array of products  |
| GET    | `/health`       | None  | `{"status":"UP"}`       |

## Try it
```bash
curl -u admin:secret http://localhost:8080/api/users
curl -u admin:secret http://localhost:8080/api/products
curl -i -u admin:wrong http://localhost:8080/api/users   # 401 Unauthorized
```

Sample response from `/api/users`:
```json
[
  {"id":1,"name":"Alice Johnson","email":"alice@example.com","role":"admin"},
  {"id":2,"name":"Bob Smith","email":"bob@example.com","role":"editor"}
]
```
