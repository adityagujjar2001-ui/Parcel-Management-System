# projectBL Web UI Integration

## Overview
This project now includes a lightweight browser UI for parcel booking, tracking, and officer actions.

## How to run
From the outer project root `c:\Users\user\Downloads\projectBL`:

1. Run the helper script:
   ```powershell
   .\build-web-ui.cmd
   ```

If you prefer to run manually, change into the nested folder first:

1. Open a terminal in `c:\Users\user\Downloads\projectBL`
2. Run:
   ```powershell
   cd projectBL
   javac -d bin -sourcepath src src\projectBL\*.java
   java -cp bin projectBL.WebServer
   ```
3. Open the browser at:
   - `http://localhost:9090`
   - or if 9090 is busy, the server will fall back to `http://localhost:9091`

## Project flow
1. The browser loads static UI pages from `web/`.
2. UI actions send HTTP requests to `projectBL.WebServer` at endpoints such as `/api/login`, `/api/register`, `/api/book`, `/api/track`, `/api/invoice`, `/api/parcel-status`, `/api/dashboard`, `/api/officer/update-time`, and `/api/officer/update-status`.
3. `WebServer` parses request data and delegates processing to `projectBL.service.AppService`.
4. `AppService` validates input, applies business rules, and chooses the correct data access operation.
5. `AppService` uses `projectBL.dao.UserDao` and `projectBL.dao.BookingDao`, with JDBC managed by `projectBL.dao.Database`, to read and write MySQL data.
6. The DAO layer persists users and bookings in the MySQL database `addydb`.
7. Service results are returned to `WebServer`, which sends JSON responses back to the UI.

## MVC architecture
- **Controller**: `WebServer.java` handles HTTP requests and responses.
- **Service**: `AppService.java` contains business logic and validation.
- **DAO**: `Database.java`, `UserDao.java`, `BookingDao.java` manage data persistence.
- **Model**: `User.java`, `Booking.java`, `Result.java` represent data structures.

## Notes
- The web UI uses `web/index.html`, `web/login.html`, `web/register.html`, `web/home.html`, `web/book.html`, `web/track.html`, `web/invoice.html`, `web/parcel-status.html`, `web/update-pickup.html`, `web/style.css`, and `web/app.js`.
- Active Java source files are `src/projectBL/WebServer.java`, `src/projectBL/service/AppService.java`, `src/projectBL/dao/Database.java`, `src/projectBL/dao/UserDao.java`, `src/projectBL/dao/BookingDao.java`, and model classes in `src/projectBL/model/`.
- The server listens on port `9090` by default.
- If `9090` is busy, the server will fall back to `http://localhost:9091` or `http://localhost:9092`.
- The backend persists user registrations and bookings into MySQL database `addydb`.
- A required schema script is available in `addydb-schema.sql`.
- Ensure the MySQL JDBC driver (for example `mysql-connector-java`) is on the runtime classpath when launching the server.
- Server logs are output to the console where the application is running.
