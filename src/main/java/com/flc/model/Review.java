package com.flc.model;

import com.flc.exception.InvalidRatingException;
import java.time.LocalDate;

/**
 * Represents a review submitted by a member for a specific lesson.
 * Includes a rating (1–5) and a written comment.
 *
 * @author FLC Development Team
 */
public class Review {

    private String reviewId;
    private Member member;
    private Lesson lesson;
    private int rating;
    private String comment;
    private LocalDate reviewDate;

    /**
     * Constructs a new Review with validation on the rating.
     *
     * @param reviewId   unique review ID
     * @param member     the member who wrote the review
     * @param lesson     the lesson being reviewed
     * @param rating     the rating (must be 1–5)
     * @param comment    the written comment
     * @param reviewDate the date the review was submitted
     * @throws InvalidRatingException if the rating is not between 1 and 5
     */
    public Review(String reviewId, Member member, Lesson lesson, int rating,
            String comment, LocalDate reviewDate) throws InvalidRatingException {
        if (rating < 1 || rating > 5) {
            throw new InvalidRatingException("Rating must be between 1 and 5. Got: " + rating);
        }
        this.reviewId = reviewId;
        this.member = member;
        this.lesson = lesson;
        this.rating = rating;
        this.comment = comment;
        this.reviewDate = reviewDate;
    }

    // --- Getters and Setters ---

    /** @return the review ID */
    public String getReviewId() {
        return reviewId;
    }

    /** @param reviewId the review ID to set */
    public void setReviewId(String reviewId) {
        this.reviewId = reviewId;
    }

    /** @return the member who wrote the review */
    public Member getMember() {
        return member;
    }

    /** @param member the member to set */
    public void setMember(Member member) {
        this.member = member;
    }

    /** @return the lesson being reviewed */
    public Lesson getLesson() {
        return lesson;
    }

    /** @param lesson the lesson to set */
    public void setLesson(Lesson lesson) {
        this.lesson = lesson;
    }

    /** @return the rating (1–5) */
    public int getRating() {
        return rating;
    }

    /**
     * Sets the rating with validation.
     *
     * @param rating the rating to set (must be 1–5)
     * @throws InvalidRatingException if the rating is not between 1 and 5
     */
    public void setRating(int rating) throws InvalidRatingException {
        if (rating < 1 || rating > 5) {
            throw new InvalidRatingException("Rating must be between 1 and 5. Got: " + rating);
        }
        this.rating = rating;
    }

    /** @return the comment */
    public String getComment() {
        return comment;
    }

    /** @param comment the comment to set */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /** @return the review date */
    public LocalDate getReviewDate() {
        return reviewDate;
    }

    /** @param reviewDate the review date to set */
    public void setReviewDate(LocalDate reviewDate) {
        this.reviewDate = reviewDate;
    }

    @Override
    public String toString() {
        return String.format("Review[%s] by %s for %s - Rating: %d/5 - \"%s\"",
                reviewId, member.getName(), lesson.getLessonId(), rating, comment);
    }
}
