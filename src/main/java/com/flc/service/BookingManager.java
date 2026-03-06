package com.flc.service;

import com.flc.exception.LessonFullException;
import com.flc.exception.TimeConflictException;
import com.flc.model.Lesson;
import com.flc.model.Member;

/**
 * Singleton manager responsible for booking operations.
 */
public final class BookingManager {

    private static final BookingManager INSTANCE = new BookingManager();

    private BookingManager() {
    }

    /**
     * Gets the singleton instance.
     *
     * @return the shared booking manager
     */
    public static BookingManager getInstance() {
        return INSTANCE;
    }

    /**
     * Creates a booking for a member.
     *
     * @param member target member
     * @param lesson target lesson
     * @throws LessonFullException   if lesson is full
     * @throws TimeConflictException if member has a conflicting booking
     */
    public void bookLesson(Member member, Lesson lesson) throws LessonFullException, TimeConflictException {
        validate(member, lesson);
        member.bookLesson(lesson);
    }

    /**
     * Changes a booking from one lesson to another.
     *
     * @param member    target member
     * @param oldLesson lesson currently booked
     * @param newLesson lesson to move into
     * @throws LessonFullException   if new lesson is full
     * @throws TimeConflictException if new lesson conflicts with existing bookings
     */
    public void changeBooking(Member member, Lesson oldLesson, Lesson newLesson)
            throws LessonFullException, TimeConflictException {
        validate(member, oldLesson);
        validate(member, newLesson);
        member.changeBooking(oldLesson, newLesson);
    }

    /**
     * Cancels a member booking.
     *
     * @param member member
     * @param lesson booked lesson
     */
    public void cancelBooking(Member member, Lesson lesson) {
        validate(member, lesson);
        member.cancelBooking(lesson);
    }

    /**
     * Marks a booking as attended.
     *
     * @param member member
     * @param lesson booked lesson
     */
    public void markAttendance(Member member, Lesson lesson) {
        validate(member, lesson);
        member.attendLesson(lesson);
    }

    private void validate(Member member, Lesson lesson) {
        if (member == null) {
            throw new IllegalArgumentException("member cannot be null");
        }
        if (lesson == null) {
            throw new IllegalArgumentException("lesson cannot be null");
        }
    }
}
