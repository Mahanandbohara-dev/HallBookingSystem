# Hall Booking Management System — Hall Symphony Inc.

A Java Swing desktop application implementing the coursework brief exactly:
4 roles (Scheduler, Customer, Administrator, Manager), hall booking with
payment + receipt, cancellation with the 3-day rule, maintenance/availability
scheduling, issue handling, and a sales dashboard — all backed by plain
`.txt` files (no database, per the assignment's General Requirements).

## How to compile and run

**Requires a JDK (11+) — the JRE alone is not enough.**

From the project root (the folder containing `src/` and `data/`):

```bash
# Compile
mkdir -p bin
javac -d bin -encoding UTF-8 $(find src -name "*.java")

# Run (must run from the project root so it finds/creates the data/ folder)
java -cp bin Main
```

Or in an IDE (IntelliJ / Eclipse / NetBeans): mark `src` as the sources
root, set `Main` as the main class, and set the **working directory to
the project root** (not `bin`) so the `data/` folder is found.

On first run, if `data/` is empty, the app auto-seeds:
- **admin / admin123** (Administrator)
- **manager / manager123** (Manager)
- **scheduler / scheduler123** (Scheduler)
- **customer / customer123** (Customer, demo account)
- One of each hall type: Auditorium, Banquet Hall, Meeting Room (rates
  from Table 1 of the brief)

New customers can also self-register from the login screen.

## Project structure

```
src/
  Main.java                      entry point
  model/                         User hierarchy, Hall hierarchy, Booking, Issue, HallSchedule
  exception/                     custom checked exceptions
  io/FileManager.java            all .txt file reading/writing (the only I/O layer)
  gui/                           Swing UI: Login, Register, and one dashboard per role
data/                            .txt data files (created/seeded automatically)
```

## Where each OOP concept lives (for your report)

- **Abstraction**: `User` and `Hall` are abstract classes — code that works
  with "a user" or "a hall" never needs to know the concrete subclass.
- **Inheritance**: `Scheduler / Customer / Administrator / Manager extends User`;
  `Auditorium / BanquetHall / MeetingRoom extends Hall`.
- **Polymorphism**: `LoginFrame.openDashboardFor()` and `Hall.calculateCost()`
  behave differently per concrete subclass through the same method call.
  `getRole()` / `getHallType()` are overridden per subclass.
- **Encapsulation**: all model fields are `private` with controlled getters/setters.
- **Exception handling**: `AuthenticationException`, `InvalidBookingException`,
  `CancellationNotAllowedException` are custom checked exceptions used for
  login failures, invalid/overlapping bookings, and the 3-day cancellation rule.
- **File I/O**: `FileManager` is the single place that reads/writes `.txt` files.

## What's intentionally NOT included

Per your instruction to build only what the assignment asks for, no extra
"creative features" beyond Table 2 were added. You're free to layer your
own on top before submission — the brief explicitly invites that.

## Before you submit

- Read through every file with your teammates — you'll need to explain
  design decisions in the report/viva.
- Take the required screenshots (program output + the `.txt` files in `data/`).
- Draw your own Use Case and Class diagrams based on this design (or adjust
  the design first, then diagram it) — don't skip this even though the code exists.
- Add input validation edge cases your team wants beyond what's here.
