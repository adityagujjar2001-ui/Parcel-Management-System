package projectBL;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import projectBL.model.Booking;
import projectBL.model.Result;
import projectBL.model.User;
import projectBL.service.AppService;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class WebServer {
    private static final Path WEB_ROOT = Paths.get("web");
    private static final AppService service = new AppService();

    public static void main(String[] args) throws IOException {
        int[] ports = {9090, 9091, 9092};
        HttpServer server = null;
        int port = -1;
        for (int candidate : ports) {
            try {
                server = HttpServer.create(new InetSocketAddress(candidate), 0);
                port = candidate;
                break;
            } catch (IOException e) {
                if (!(e instanceof java.net.BindException)) {
                    throw e;
                }
            }
        }
        if (server == null) {
            throw new IOException("All configured ports are busy: 9090, 9091, 9092");
        }

        server.createContext("/", WebServer::serveStatic);
        server.createContext("/api/register", WebServer::handleRegister);
        server.createContext("/api/login", WebServer::handleLogin);
        server.createContext("/api/book", WebServer::handleBook);
        server.createContext("/api/track", WebServer::handleTrack);
        server.createContext("/api/invoice", WebServer::handleInvoice);
        server.createContext("/api/parcel-status", WebServer::handleParcelStatus);
        server.createContext("/api/dashboard", WebServer::handleDashboard);
        server.createContext("/api/officer/update-time", WebServer::handleUpdateTime);
        server.createContext("/api/officer/update-status", WebServer::handleUpdateStatus);
        server.setExecutor(null);

        System.out.println("Starting local UI server at http://localhost:" + port);
        server.start();
    }

    private static void serveStatic(HttpExchange exchange) throws IOException {
        URI requestUri = exchange.getRequestURI();
        String rawPath = requestUri.getPath();
        if (rawPath.equals("/")) {
            rawPath = "/index.html";
        }
        Path filePath = WEB_ROOT.resolve(rawPath.substring(1)).normalize();
        if (!filePath.startsWith(WEB_ROOT) || Files.notExists(filePath) || Files.isDirectory(filePath)) {
            sendResponse(exchange, 404, "text/plain", "404 Not Found");
            return;
        }
        String contentType = switch (getFileExtension(filePath.getFileName().toString())) {
            case "css" -> "text/css; charset=UTF-8";
            case "js" -> "application/javascript; charset=UTF-8";
            case "html" -> "text/html; charset=UTF-8";
            default -> "application/octet-stream";
        };
        byte[] bytes = Files.readAllBytes(filePath);
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static void handleRegister(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            sendResponse(exchange, 405, "application/json", "{\"ok\":false,\"message\":\"Method not allowed\"}");
            return;
        }
        Map<String, String> data = parseForm(readBody(exchange));
        Result result = service.register(data.get("customerName"), data.get("email"), data.get("countryCode"),
                data.get("mobileNumber"), data.get("address"), data.get("userName"), data.get("password"),
                data.get("confirmPassword"), data.get("preferences"));
        sendJson(exchange, result.isOk(), result.getMessage(), result.isOk() ? 200 : 400);
    }

    private static void handleLogin(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            sendResponse(exchange, 405, "application/json", "{\"ok\":false,\"message\":\"Method not allowed\"}");
            return;
        }
        Map<String, String> data = parseForm(readBody(exchange));
        Result.LoginResult result = service.login(data.get("userName"), data.get("password"));
        String json = String.format("{\"ok\":%b,\"message\":\"%s\",\"officer\":%b}", result.isOk(),
                escapeJson(result.getMessage()), result.isOfficer());
        sendResponse(exchange, result.isOk() ? 200 : 401, "application/json", json);
    }

    private static void handleBook(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            sendResponse(exchange, 405, "application/json", "{\"ok\":false,\"message\":\"Method not allowed\"}");
            return;
        }
        Map<String, String> data = parseForm(readBody(exchange));
        Result.BookingResult result = service.book(data.get("userName"), data.get("recipientName"),
                data.get("recipientAddress"), data.get("recipientPin"), data.get("recipientMobile"),
                data.get("parcelWeightGram"), data.get("parcelContentsDescription"),
                data.get("parcelDeliveryType"), data.get("parcelPackingPreference"), data.get("pickupTime"),
                data.get("dropoffTime"), data.get("serviceCost"));
        String json = String.format("{\"ok\":%b,\"message\":\"%s\",\"bookingId\":%d}", result.isOk(),
                escapeJson(result.getMessage()), result.getBookingId());
        sendResponse(exchange, result.isOk() ? 200 : 400, "application/json", json);
    }

    private static void handleTrack(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            sendResponse(exchange, 405, "application/json", "{\"ok\":false,\"message\":\"Method not allowed\"}");
            return;
        }
        Map<String, String> data = parseForm(readBody(exchange));
        Result.TrackResult result = service.track(data.get("bookingId"));
        if (!result.isOk()) {
            sendJson(exchange, false, result.getMessage(), 400);
            return;
        }
        Booking booking = result.getBooking();
        String json = String.format(
                "{\"ok\":true,\"message\":\"%s\",\"bookingId\":%d,\"recipientName\":\"%s\",\"recipientAddress\":\"%s\",\"parcelStatus\":\"%s\",\"pickupTime\":\"%s\",\"dropoffTime\":\"%s\",\"serviceCost\":%.2f,\"paymentTime\":\"%s\"}",
                escapeJson(result.getMessage()), booking.getBookingId(), escapeJson(booking.getRecipientName()),
                escapeJson(booking.getRecipientAddress()), escapeJson(booking.getStatus()),
                escapeJson(booking.getPickupTime()), escapeJson(booking.getDropoffTime()),
                booking.getServiceCost(), booking.getPaymentTime().toString());
        sendResponse(exchange, 200, "application/json", json);
    }

    private static void handleInvoice(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            sendResponse(exchange, 405, "application/json", "{\"ok\":false,\"message\":\"Method not allowed\"}");
            return;
        }
        Map<String, String> data = parseForm(readBody(exchange));
        Result.InvoiceResult result = service.invoice(data.get("bookingId"));
        if (!result.isOk()) {
            sendJson(exchange, false, result.getMessage(), 400);
            return;
        }
        Booking booking = result.getBooking();
        User user = result.getUser();
        String json = String.format(
                "{\"ok\":true,\"message\":\"%s\",\"bookingId\":%d,\"customerName\":\"%s\",\"email\":\"%s\",\"address\":\"%s\",\"recipientName\":\"%s\",\"recipientAddress\":\"%s\",\"parcelStatus\":\"%s\",\"pickupTime\":\"%s\",\"dropoffTime\":\"%s\",\"serviceCost\":%.2f,\"paymentTime\":\"%s\"}",
                escapeJson(result.getMessage()), booking.getBookingId(), escapeJson(user.getCustomerName()),
                escapeJson(user.getEmail()), escapeJson(user.getAddress()),
                escapeJson(booking.getRecipientName()), escapeJson(booking.getRecipientAddress()),
                escapeJson(booking.getStatus()), escapeJson(booking.getPickupTime()),
                escapeJson(booking.getDropoffTime()), booking.getServiceCost(),
                booking.getPaymentTime().toString());
        sendResponse(exchange, 200, "application/json", json);
    }

    private static void handleParcelStatus(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            sendResponse(exchange, 405, "application/json", "{\"ok\":false,\"message\":\"Method not allowed\"}");
            return;
        }
        Map<String, String> data = parseForm(readBody(exchange));
        Result.TrackResult result = service.track(data.get("bookingId"));
        if (!result.isOk()) {
            sendJson(exchange, false, result.getMessage(), 400);
            return;
        }
        Booking booking = result.getBooking();
        String json = String.format(
                "{\"ok\":true,\"message\":\"%s\",\"bookingId\":%d,\"parcelStatus\":\"%s\",\"pickupTime\":\"%s\",\"dropoffTime\":\"%s\"}",
                escapeJson(result.getMessage()), booking.getBookingId(), escapeJson(booking.getStatus()),
                escapeJson(booking.getPickupTime()), escapeJson(booking.getDropoffTime()));
        sendResponse(exchange, 200, "application/json", json);
    }

    private static void handleDashboard(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            sendResponse(exchange, 405, "application/json", "{\"ok\":false,\"message\":\"Method not allowed\"}");
            return;
        }
        String query = exchange.getRequestURI().getQuery();
        Map<String, String> params = parseQuery(query);
        Result.DashboardResult result = service.dashboard(params.get("userName"));
        if (!result.isOk()) {
            sendJson(exchange, false, result.getMessage(), 400);
            return;
        }
        Booking booking = result.getBooking();
        String bookingJson = "null";
        if (booking != null) {
            bookingJson = String.format(
                    "{\"bookingId\":%d,\"recipientName\":\"%s\",\"recipientAddress\":\"%s\",\"parcelStatus\":\"%s\",\"pickupTime\":\"%s\",\"dropoffTime\":\"%s\",\"serviceCost\":%.2f}",
                    booking.getBookingId(), escapeJson(booking.getRecipientName()),
                    escapeJson(booking.getRecipientAddress()), escapeJson(booking.getStatus()),
                    escapeJson(booking.getPickupTime()), escapeJson(booking.getDropoffTime()),
                    booking.getServiceCost());
        }
        User user = result.getUser();
        String json = String.format(
                "{\"ok\":true,\"message\":\"%s\",\"customerName\":\"%s\",\"email\":\"%s\",\"countryCode\":\"%s\",\"mobileNumber\":\"%s\",\"address\":\"%s\",\"booking\":%s}",
                escapeJson(result.getMessage()), escapeJson(user.getCustomerName()), escapeJson(user.getEmail()),
                escapeJson(user.getCountryCode()), escapeJson(user.getMobileNumber()),
                escapeJson(user.getAddress()), bookingJson);
        sendResponse(exchange, 200, "application/json", json);
    }

    private static void handleUpdateTime(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            sendResponse(exchange, 405, "application/json", "{\"ok\":false,\"message\":\"Method not allowed\"}");
            return;
        }
        Map<String, String> data = parseForm(readBody(exchange));
        Result result = service.updatePickup(data.get("bookingId"), data.get("pickupTime"),
                data.get("dropoffTime"));
        sendJson(exchange, result.isOk(), result.getMessage(), result.isOk() ? 200 : 400);
    }

    private static void handleUpdateStatus(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            sendResponse(exchange, 405, "application/json", "{\"ok\":false,\"message\":\"Method not allowed\"}");
            return;
        }
        Map<String, String> data = parseForm(readBody(exchange));
        Result result = service.changeParcelStatus(data.get("bookingId"), data.get("status"));
        sendJson(exchange, result.isOk(), result.getMessage(), result.isOk() ? 200 : 400);
    }

    private static void sendJson(HttpExchange exchange, boolean ok, String message, int statusCode) throws IOException {
        String json = String.format("{\"ok\":%b,\"message\":\"%s\"}", ok, escapeJson(message));
        sendResponse(exchange, statusCode, "application/json", json);
    }

    private static void sendResponse(HttpExchange exchange, int responseCode, String contentType, String body)
            throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(responseCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static String readBody(HttpExchange exchange) throws IOException {
        try (InputStream input = exchange.getRequestBody()) {
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static Map<String, String> parseForm(String body) throws IOException {
        Map<String, String> result = new HashMap<>();
        if (body == null || body.isBlank()) {
            return result;
        }
        String[] pairs = body.split("&");
        for (String pair : pairs) {
            String[] parts = pair.split("=", 2);
            String key = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
            String value = parts.length > 1 ? URLDecoder.decode(parts[1], StandardCharsets.UTF_8) : "";
            result.put(key, value);
        }
        return result;
    }

    private static Map<String, String> parseQuery(String query) {
        Map<String, String> result = new HashMap<>();
        if (query == null || query.isBlank()) {
            return result;
        }
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] parts = pair.split("=", 2);
            String key = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
            String value = parts.length > 1 ? URLDecoder.decode(parts[1], StandardCharsets.UTF_8) : "";
            result.put(key, value);
        }
        return result;
    }

    private static String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    private static String getFileExtension(String name) {
        int index = name.lastIndexOf('.');
        return index < 0 ? "" : name.substring(index + 1);
    }
}
