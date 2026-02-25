# Furzefield Leisure Centre (FLC) Booking System

A Java Swing desktop application for managing lesson bookings at the Furzefield Leisure Centre.  
Developed for **7COM1025 – Programming for Software Engineers**, University of Hertfordshire.

## Features

- **Timetable Viewer** — Browse 48 lessons across 8 weekends, searchable by day or exercise type
- **Book Lesson** — Book members into lessons with capacity and time-conflict validation
- **Change Booking** — Change an existing booking to a different lesson
- **Cancel Booking** — Cancel bookings with confirmation dialogs
- **Write Review** — Rate attended lessons (1–5) with written comments
- **Member Management** — View all members, their bookings, and add new members
- **Reports** — Generate attendance/rating and income reports with TXT export

## Technology Stack

- **Language:** Java 11+
- **GUI:** Java Swing (JFrame, JTable, JTabbedPane, etc.)
- **Build:** Maven
- **Testing:** JUnit 5 (20 test cases)
- **IDE:** NetBeans / VS Code compatible

## Project Structure

```
src/main/java/com/flc/
├── model/          — ExerciseType, TimeSlot, DayOfWeek, Lesson, Member, Review
├── service/        — BookingSystem, Timetable, DataInitializer
├── exception/      — Custom exceptions (LessonFull, TimeConflict, InvalidRating, MemberNotFound)
├── gui/            — MainFrame + 7 functional panels
└── Main.java       — Entry point
src/test/java/com/flc/
└── BookingSystemTest.java — 20 JUnit 5 tests
```

## Build & Run

```bash
# Compile and run tests
mvn clean test

# Package as JAR
mvn clean package

# Run the application
java -jar target/FLC_BookingSystem.jar
```

## Sample Data

- **48 lessons** (8 weekends × 2 days × 3 time slots)
- **10 pre-registered members**
- **20+ bookings** (including 3 fully-booked lessons)
- **22 reviews** with varied ratings and comments

## Exercise Pricing

| Exercise   | Price (£) |
|------------|-----------|
| Yoga       | 8.00      |
| Zumba      | 7.50      |
| Aquacise   | 9.00      |
| Box Fit    | 10.00     |
| Body Blitz | 9.50      |
