package com.flc.model;

import com.flc.exception.LessonFullException;
import com.flc.exception.TimeConflictException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a member registered at the Furzefield Leisure Centre.
 * A member can book, cancel, and change lesson bookings. Time conflict
 * detection ensures a member cannot have two bookings at the same
 * time slot on the same day and week.
 *
 * @author FLC Development Team
 */
public class Member {

    private String memberId;
    private String name;
    private String email;
    private List<Booking> bookings;

    /**
     * Constructs a new Member.
     *
     * @param memberId unique member ID (e.g., "M01")
     * @param name     the member's full name
     * @param email    the member's email address
     */
    public Member(String memberId, String name, String email) {
        this.memberId = memberId;
        this.name = name;
        this.email = email;
        this.bookings = new ArrayList<>();
    }

    /**
     * Books this member into the specified lesson after validating
     * there is no time conflict.
     *
     * @param lesson the lesson to book
     * @throws TimeConflictException if the member already has a booking
     *                               at the same day, week, and time slot
     * @throws LessonFullException   if the lesson is at capacity
     */
    public void bookLesson(Lesson lesson) throws TimeConflictException, LessonFullException {
        if (lesson == null) {
            throw new IllegalArgumentException("Lesson cannot be null.");
        }

        if (hasActiveBookingForLesson(lesson)) {
            throw new TimeConflictException("Member " + name + " is already booked into lesson "
                    + lesson.getLessonId() + ".");
        }

        if (hasTimeConflict(lesson)) {
            throw new TimeConflictException("Member " + name + " already has a booking on "
                    + lesson.getDay().getDisplayName() + " " + lesson.getTimeSlot().getDisplayName()
                    + " in Week " + lesson.getWeekNumber() + ".");
        }

        lesson.bookMember(this);
        bookings.add(new Booking(this, lesson));
    }

    /**
     * Cancels a booking for the specified lesson.
     *
     * @param lesson the lesson to cancel
     */
    public void cancelBooking(Lesson lesson) {
        Booking booking = findActiveBooking(lesson);
        if (booking == null) {
            return;
        }

        if (booking.getStatus() == BookingStatus.ATTENDED) {
            throw new IllegalStateException("Cannot cancel lesson " + lesson.getLessonId()
                    + " because it has already been marked as attended.");
        }

        booking.cancel();
        lesson.removeMember(this);
    }

    /**
     * Changes a booking from one lesson to another.
     * Cancels the old lesson and books the new one.
     *
     * @param oldLesson the current booking to cancel
     * @param newLesson the new lesson to book
     * @throws LessonFullException   if the new lesson is full
     * @throws TimeConflictException if the new lesson causes a time conflict
     */
    public void changeBooking(Lesson oldLesson, Lesson newLesson)
            throws LessonFullException, TimeConflictException {
        if (oldLesson == null || newLesson == null) {
            throw new IllegalArgumentException("Old lesson and new lesson must both be provided.");
        }

        if (oldLesson.equals(newLesson)) {
            throw new IllegalArgumentException("Cannot change booking to the same lesson.");
        }

        Booking oldBooking = findActiveBooking(oldLesson);
        if (oldBooking == null) {
            throw new IllegalStateException("Cannot change booking. Member " + name
                    + " is not currently booked into lesson " + oldLesson.getLessonId() + ".");
        }

        if (oldBooking.getStatus() == BookingStatus.ATTENDED) {
            throw new IllegalStateException("Cannot change an attended booking.");
        }

        oldBooking.cancel();
        oldLesson.removeMember(this);

        try {
            if (hasTimeConflictExcluding(newLesson, oldLesson)) {
                throw new TimeConflictException("Member " + name + " already has a booking on "
                        + newLesson.getDay().getDisplayName() + " " + newLesson.getTimeSlot().getDisplayName()
                        + " in Week " + newLesson.getWeekNumber() + ".");
            }
            newLesson.bookMember(this);
            bookings.add(new Booking(this, newLesson));
        } catch (LessonFullException | TimeConflictException e) {
            // Rollback to preserve original booking state
            try {
                oldLesson.bookMember(this);
                oldBooking.setStatus(BookingStatus.BOOKED);
            } catch (LessonFullException ex) {
                throw new IllegalStateException("Rollback failed: " + ex.getMessage(), ex);
            }
            throw e;
        }
    }

    /**
     * Marks a lesson as attended.
     *
     * @param lesson the lesson attended
     */
    public void attendLesson(Lesson lesson) {
        Booking booking = findActiveBooking(lesson);
        if (booking != null) {
            booking.markAttended();
        }
    }

    /**
     * Gets the status of a specific booking.
     *
     * @param lesson the lesson to check
     * @return the status, or null if not booked
     */
    public BookingStatus getBookingStatus(Lesson lesson) {
        Booking booking = findLatestBooking(lesson);
        return booking == null ? null : booking.getStatus();
    }

    /**
     * Checks if the member has attended a lesson.
     *
     * @param lesson the lesson to check
     * @return true if attended, false otherwise
     */
    public boolean hasAttended(Lesson lesson) {
        Booking booking = findActiveBooking(lesson);
        return booking != null && booking.getStatus() == BookingStatus.ATTENDED;
    }

    /**
     * Checks whether booking the given lesson would cause a time conflict
     * with the member's existing bookings. A conflict occurs when the member
     * already has a booking at the same day, time slot, and week.
     *
     * @param lesson the lesson to check
     * @return true if there is a conflict, false otherwise
     */
    public boolean hasTimeConflict(Lesson lesson) {
        for (Booking booking : bookings) {
            if (!booking.isActive()) {
                continue;
            }

            Lesson booked = booking.getLesson();
            if (booked.getWeekNumber() == lesson.getWeekNumber()
                    && booked.getDay() == lesson.getDay()
                    && booked.getTimeSlot() == lesson.getTimeSlot()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the list of lessons this member is currently booked into.
     *
     * @return active booked lessons (BOOKED or ATTENDED)
     */
    public List<Lesson> getBookedLessons() {
        return bookings.stream()
                .filter(Booking::isActive)
                .map(Booking::getLesson)
                .collect(Collectors.toList());
    }

    /**
     * Gets all booking records (active and cancelled).
     *
     * @return immutable list of bookings
     */
    public List<Booking> getBookings() {
        return Collections.unmodifiableList(bookings);
    }

    private boolean hasTimeConflictExcluding(Lesson lesson, Lesson excludedLesson) {
        for (Booking booking : bookings) {
            if (!booking.isActive()) {
                continue;
            }
            Lesson booked = booking.getLesson();
            if (booked.equals(excludedLesson)) {
                continue;
            }
            if (booked.getWeekNumber() == lesson.getWeekNumber()
                    && booked.getDay() == lesson.getDay()
                    && booked.getTimeSlot() == lesson.getTimeSlot()) {
                return true;
            }
        }
        return false;
    }

    private boolean hasActiveBookingForLesson(Lesson lesson) {
        return findActiveBooking(lesson) != null;
    }

    private Booking findActiveBooking(Lesson lesson) {
        for (int i = bookings.size() - 1; i >= 0; i--) {
            Booking booking = bookings.get(i);
            if (booking.getLesson().equals(lesson) && booking.isActive()) {
                return booking;
            }
        }
        return null;
    }

    private Booking findLatestBooking(Lesson lesson) {
        for (int i = bookings.size() - 1; i >= 0; i--) {
            Booking booking = bookings.get(i);
            if (booking.getLesson().equals(lesson)) {
                return booking;
            }
        }
        return null;
    }

    // --- Getters and Setters ---

    /** @return the member ID */
    public String getMemberId() {
        return memberId;
    }

    /** @param memberId the member ID to set */
    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    /** @return the member's name */
    public String getName() {
        return name;
    }

    /** @param name the name to set */
    public void setName(String name) {
        this.name = name;
    }

    /** @return the member's email */
    public String getEmail() {
        return email;
    }

    /** @param email the email to set */
    public void setEmail(String email) {
        this.email = email;
    }

    /** @param bookedLessons the list of booked lessons to set */
    public void setBookedLessons(List<Lesson> bookedLessons) {
        this.bookings.clear();
        if (bookedLessons == null) {
            return;
        }
        for (Lesson lesson : bookedLessons) {
            this.bookings.add(new Booking(this, lesson));
        }
    }

    @Override
    public String toString() {
        return name + " (" + memberId + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Member member = (Member) obj;
        return memberId.equals(member.memberId);
    }

    @Override
    public int hashCode() {
        return memberId.hashCode();
    }
}
