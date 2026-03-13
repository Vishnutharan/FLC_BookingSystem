# Furzefield Leisure Centre (FLC) Booking System

Self-contained Java Swing booking application for weekend group exercise lessons.

## Requirement Coverage

- 10 pre-registered members (`M01` to `M10`)
- 5 exercise types with fixed per-type pricing
- 8 weekends x 2 days x 3 slots = 48 lessons
- Lesson capacity enforced at 4 members
- Assignment audit report confirming dataset minimums are satisfied
- Timetable filtering:
  - by Saturday/Sunday across all weeks
  - by specific week + Saturday/Sunday
  - by exercise type
- Booking validation:
  - reject full lessons
  - reject same week/day/slot conflicts
- Booking change with rollback safety and space release
- Booking cancellation frees capacity
- Attended bookings cannot be cancelled
- Attendance tracking before review submission
- Reviews: one per member per attended lesson, rating 1-5
- 20+ seeded reviews with varied ratings
- 4-week cycle reports:
  - Report 1: per-lesson booked count, attended count, review count, and average rating
  - Report 2: income by exercise + highest-income exercise type
- Executable JAR output
- JUnit tests for required scenarios

## Design Notes

- Core entities: `Member`, `Lesson`, `Booking`, `Review`, `Timetable`
- Service layer: `BookingSystem`, `BookingManager`, `DataInitializer`
- Applied pattern: `BookingManager` Singleton
- UML class diagram with multiplicities:
  - `docs/UML_Class_Diagram.md`

## UI

- Rich light-theme dashboard (no dark theme)
- Card-based navigation
- Animated fade+slide panel transitions
- Validation feedback via dialog messages

## Build and Run

### Option 1 (Maven)

```bash
mvn clean test
mvn clean package
java -jar target/FLC_BookingSystem.jar
```

### Console report mode

```bash
java -cp target/classes com.flc.Main --console
```

This prints the assignment requirement audit plus both 4-week cycle reports to `System.out`.

### Option 2 (No Maven, using JDK tools)

```bash
# Compile main sources
javac -d out-main @sources-main.txt

# Build executable jar
"C:\Program Files\Java\jdk-22\bin\jar.exe" --create --file target\FLC_BookingSystem.jar --main-class com.flc.Main -C out-main .

# Run
java -jar target\FLC_BookingSystem.jar
```

## Test Execution Used in This Workspace

Maven CLI was unavailable, so tests were executed with JUnit Console Standalone:

```bash
javac -d out-main @sources-main.txt
javac -cp out-main;junit-platform-console-standalone-1.10.2.jar -d out-test @sources-test.txt
java -jar junit-platform-console-standalone-1.10.2.jar --class-path out-main;out-test --scan-class-path
```

Result: 17/17 tests passed.
