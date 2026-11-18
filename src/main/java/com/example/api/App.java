package com.example.api;

import com.sun.net.httpserver.BasicAuthenticator;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Minimal REST API protected by HTTP Basic Authentication.
 * Uses only the JDK (no external dependencies).
 *
 * Endpoints:
 *   GET /api/users      -> JSON array of users
 *   GET /api/products   -> JSON array of products
 *   GET /health         -> public, no auth
 */
public class App {

    record User(int id, String name, String email, String role) {
        String toJson() {
            return String.format("{\"id\":%d,\"name\":\"%s\",\"email\":\"%s\",\"role\":\"%s\"}",
                    id, name, email, role);
        }
    }

    record Product(int id, String name, double price, boolean inStock) {
        String toJson() {
            return String.format(java.util.Locale.ROOT,
                    "{\"id\":%d,\"name\":\"%s\",\"price\":%.2f,\"inStock\":%b}",
                    id, name, price, inStock);
        }
    }

    static final List<User> USERS = List.of(
            new User(1, "Alice Johnson", "alice@example.com", "admin"),
            new User(2, "Bob Smith", "bob@example.com", "editor"),
            new User(3, "Carol White", "carol@example.com", "viewer"),
            new User(4, "David Brown", "david@example.com", "viewer")
    );

    static final List<Product> PRODUCTS = List.of(
            new Product(101, "Laptop", 1299.99, true),
            new Product(102, "Keyboard", 49.50, true),
            new Product(103, "Monitor", 289.00, false)
    );

    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(env("PORT", "8080"));
        String user = env("API_USER", "admin");
        String pass = env("API_PASSWORD", "secret");

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        BasicAuthenticator auth = new BasicAuthenticator("sample-api") {
            @Override
            public boolean checkCredentials(String u, String p) {
                return user.equals(u) && pass.equals(p);
            }
        };

        HttpContext users = server.createContext("/api/users", ex ->
                sendJson(ex, toJsonArray(USERS.stream().map(User::toJson).toList())));
        users.setAuthenticator(auth);

        HttpContext products = server.createContext("/api/products", ex ->
                sendJson(ex, toJsonArray(PRODUCTS.stream().map(Product::toJson).toList())));
        products.setAuthenticator(auth);

        server.createContext("/health", ex -> sendJson(ex, "{\"status\":\"UP\"}"));

        server.start();
        System.out.println("Server running on http://localhost:" + port);
        System.out.println("Basic Auth credentials: " + user + " / " + pass);
    }

    static String toJsonArray(List<String> items) {
        return items.stream().collect(Collectors.joining(",", "[", "]"));
    }

    static void sendJson(HttpExchange ex, String body) throws IOException {
        if (!"GET".equalsIgnoreCase(ex.getRequestMethod())) {
            ex.sendResponseHeaders(405, -1);
            ex.close();
            return;
        }
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json");
        ex.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }

    static String env(String key, String def) {
        String v = System.getenv(key);
        return (v == null || v.isBlank()) ? def : v;
    }
}
