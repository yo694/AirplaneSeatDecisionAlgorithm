# Airplane Seat Assignment Algorithm

This project is a Java-based airplane seat assignment algorithm.

The goal is to assign seats to passengers using seat availability, passenger preferences, booking groups, and airline rules.

## Problem Statement

Airlines need to assign seats in a way that is safe, fair, and practical.

The system should not simply pick the first available seat. It should consider:

- Seat availability
- Passenger age category
- Ticket class
- Seat preferences
- Booking groups
- Safety rules
- Group seating

## Approach Used

This project uses a constraint-based scoring approach.

First, the algorithm checks hard rules that cannot be broken.  
Then, it uses scoring to choose better seats based on passenger preferences and group seating.

## Hard Rules

The algorithm checks:

- Seat must be available
- A child or infant cannot sit in an exit row
- An infant must sit only in an infant-allowed seat
- Passenger ticket class must match the seat class
- One seat cannot be assigned to multiple passengers

## Versions

### Version 1: Basic Seat Assignment

In Version 1, the algorithm assigned the first valid available seat.

It handled:

- Seat data
- Passenger data
- Basic validation rules
- Avoiding duplicate seat assignment

Limitation:

- It picked the first valid seat, not the best seat.

### Version 2: Preference-Based Seat Selection

In Version 2, the algorithm was improved to choose the best valid seat.

It checks all valid seats and gives scores:

- Correct class: +500
- Preference matched: +200

This helps passengers get preferred seats like window or aisle.

### Version 3: Group-Based Seating

In Version 3, the algorithm tries to keep passengers from the same booking group in the same row.

Example:

```text
P1 -> 10A
P2 -> 10C
P3 -> 10B
```

All passengers are seated in row 10.

## Project Structure

```text
AirplaneSeatAssignment/
|
|-- Seat.java
|-- Passenger.java
|-- Assignment.java
|-- AirplaneSeatAssignment.java
|-- .gitignore
|-- README.md
```

## How To Run

Compile the Java files:

```bash
javac *.java
```

Run the program:

```bash
java AirplaneSeatAssignment
```

## Sample Output

```text
Final Seat Assignments:
-----------------------
P1 -> 10A | window preference matched.
P2 -> 10C | aisle preference matched.
P3 -> 10B | Valid seat assigned based on availability and rules.
```

## Classes Used

### Seat

Stores seat details such as:

- Seat number
- Row
- Seat type
- Seat class
- Availability
- Exit row status
- Infant allowed status

### Passenger

Stores passenger details such as:

- Passenger ID
- Age category
- Ticket class
- Seat preference
- Group ID

### Assignment

Stores the final result:

- Passenger
- Seat
- Reason for assignment

### AirplaneSeatAssignment

Contains the main algorithm logic.

## Future Improvements

Possible future improvements:

- Adjacent row group seating
- Paid seat selection
- Loyalty priority
- Real-time seat locking
- Manual airline staff override
- Better scoring for large groups
- Reading input from files or database

## Summary

This project starts with a simple working solution and improves step by step.

The final goal is to build a seat assignment algorithm that is rule-based, preference-aware, group-friendly, and explainable.