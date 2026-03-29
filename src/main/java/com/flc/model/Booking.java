package com.flc.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents a booking made by a member for a specific lesson.
 */
public class Booking {

    private static final AtomicInteger COUNTER = new AtomicInteger(1);

    private final String bookingId;
    private final Member member;
    private Lesson lesson;
    private BookingStatus status;
    private final LocalDateTime bookedAt;

    /**
     * Creates a new booking with an auto-generated booking ID.
     *
     * @param member the member making the booking
     * @param lesson the booked lesson
     */
    public Booking(Member member, Lesson lesson) {
        this(String.format("B%04d", COUNTER.getAndIncrement()),
                member,
                lesson,
                BookingStatus.BOOKED,
                LocalDateTime.now());
    }

    /**
     * Creates a new booking with full details.
     *
     * @param bookingId booking identifier
     * @param member    booked member
     * @param lesson    booked lesson
     * @param status    current status
     * @param bookedAt  date/time of booking
     */
    public Booking(String bookingId, Member member, Lesson lesson, BookingStatus status, LocalDateTime bookedAt) {
        this.bookingId = Objects.requireNonNull(bookingId, "bookingId cannot be null");
        this.member = Objects.requireNonNull(member, "member cannot be null");
        this.lesson = Objects.requireNonNull(lesson, "lesson cannot be null");
        this.status = Objects.requireNonNull(status, "status cannot be null");
        this.bookedAt = Objects.requireNonNull(bookedAt, "bookedAt cannot be null");
    }

    /**
     * Marks this booking as attended.
     */
    public void markAttended() {
        if (status == BookingStatus.BOOKED || status == BookingStatus.CHANGED) {
            status = BookingStatus.ATTENDED;
        }
    }

    /**
     * Moves this booking to a new lesson while keeping the same booking ID.
     *
     * @param newLesson the replacement lesson
     */
    public void changeLesson(Lesson newLesson) {
        this.lesson = Objects.requireNonNull(newLesson, "newLesson cannot be null");
        status = BookingStatus.CHANGED;
    }

    /**
     * Marks this booking as cancelled.
     */
    public void cancel() {
        status = BookingStatus.CANCELLED;
    }

    /**
     * Checks whether this booking is still active in the timetable.
     *
     * @return true if not cancelled
     */
    public boolean isActive() {
        return status != BookingStatus.CANCELLED;
    }

    public String getBookingId() {
        return bookingId;
    }

    public Member getMember() {
        return member;
    }

    public Lesson getLesson() {
        return lesson;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = Objects.requireNonNull(status, "status cannot be null");
    }

    public LocalDateTime getBookedAt() {
        return bookedAt;
    }

    @Override
    public String toString() {
        return String.format("%s: %s -> %s [%s]",
                bookingId,
                member.getMemberId(),
                lesson.getLessonId(),
                status.getDisplayName());
    }
}
