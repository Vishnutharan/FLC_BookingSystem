package com.flc.model;

import com.flc.exception.LessonFullException;
import com.flc.exception.TimeConflictException;
import java.util.ArrayList;
import java.util.List;

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
    private List<Lesson> bookedLessons;

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
        this.bookedLessons = new ArrayList<>();
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
        if (hasTimeConflict(lesson)) {
            throw new TimeConflictException("Member " + name + " already has a booking on "
                    + lesson.getDay().getDisplayName() + " " + lesson.getTimeSlot().getDisplayName()
                    + " in Week " + lesson.getWeekNumber() + ".");
        }
        lesson.bookMember(this);
        bookedLessons.add(lesson);
    }

    /**
     * Cancels a booking for the specified lesson.
     *
     * @param lesson the lesson to cancel
     */
    public void cancelBooking(Lesson lesson) {
        lesson.removeMember(this);
        bookedLessons.remove(lesson);
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
        // Cancel old booking first
        cancelBooking(oldLesson);
        try {
            // Attempt to book new lesson
            bookLesson(newLesson);
        } catch (LessonFullException | TimeConflictException e) {
            // Rollback: re-book into old lesson if new booking fails
            try {
                lesson_reBook(oldLesson);
            } catch (LessonFullException ex) {
                // Should not happen since we just cancelled
            }
            throw e;
        }
    }

    /**
     * Re-books a lesson without conflict checking (used for rollback).
     *
     * @param lesson the lesson to re-book
     * @throws LessonFullException if the lesson is full
     */
    private void lesson_reBook(Lesson lesson) throws LessonFullException {
        lesson.bookMember(this);
        bookedLessons.add(lesson);
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
        for (Lesson booked : bookedLessons) {
            if (booked.getWeekNumber() == lesson.getWeekNumber()
                    && booked.getDay() == lesson.getDay()
                    && booked.getTimeSlot() == lesson.getTimeSlot()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the list of lessons this member is booked into.
     *
     * @return the list of booked lessons
     */
    public List<Lesson> getBookedLessons() {
        return bookedLessons;
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
        this.bookedLessons = bookedLessons;
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
