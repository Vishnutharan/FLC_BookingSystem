package com.flc.service;

import com.flc.exception.*;
import com.flc.model.*;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Main service/controller class for the FLC Booking System.
 * Acts as a single source of truth for the system, managing members,
 * the timetable, and reviews. Provides all business logic for booking,
 * cancellation, changes, reviews, and report generation.
 *
 * @author FLC Development Team
 */
public class BookingSystem {

    private Timetable timetable;
    private List<Member> members;
    private List<Review> reviews;
    private int nextReviewId;

    /**
     * Constructs a new BookingSystem with empty data.
     */
    public BookingSystem() {
        this.timetable = new Timetable();
        this.members = new ArrayList<>();
        this.reviews = new ArrayList<>();
        this.nextReviewId = 1;
    }

    /**
     * Initializes the system with sample data using the DataInitializer.
     */
    public void initializeSampleData() {
        DataInitializer.initialize(this);
    }

    /**
     * Adds a member to the system.
     *
     * @param member the member to add
     */
    public void addMember(Member member) {
        members.add(member);
    }

    /**
     * Finds a member by their unique ID.
     *
     * @param id the member ID
     * @return the member
     * @throws MemberNotFoundException if no member with the given ID is found
     */
    public Member getMemberById(String id) throws MemberNotFoundException {
        return members.stream()
                .filter(m -> m.getMemberId().equals(id))
                .findFirst()
                .orElseThrow(() -> new MemberNotFoundException("Member with ID " + id + " not found."));
    }

    /**
     * Finds a member by name (case-insensitive partial match).
     *
     * @param name the name to search for
     * @return the first matching member
     * @throws MemberNotFoundException if no member with the given name is found
     */
    public Member getMemberByName(String name) throws MemberNotFoundException {
        return members.stream()
                .filter(m -> m.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow(() -> new MemberNotFoundException("Member with name '" + name + "' not found."));
    }

    /**
     * Books a lesson for a member with full validation.
     *
     * @param member the member to book
     * @param lesson the lesson to book into
     * @throws LessonFullException   if the lesson is at capacity
     * @throws TimeConflictException if the member has a time conflict
     */
    public void bookLesson(Member member, Lesson lesson)
            throws LessonFullException, TimeConflictException {
        member.bookLesson(lesson);
    }

    /**
     * Changes a booking from one lesson to another with full validation.
     *
     * @param member    the member
     * @param oldLesson the lesson to cancel
     * @param newLesson the lesson to book
     * @throws LessonFullException   if the new lesson is full
     * @throws TimeConflictException if the new lesson causes a time conflict
     */
    public void changeBooking(Member member, Lesson oldLesson, Lesson newLesson)
            throws LessonFullException, TimeConflictException {
        member.changeBooking(oldLesson, newLesson);
    }

    /**
     * Cancels a booking for a member.
     *
     * @param member the member
     * @param lesson the lesson to cancel
     */
    public void cancelBooking(Member member, Lesson lesson) {
        member.cancelBooking(lesson);
    }

    /**
     * Adds a review for a lesson from a member. Validates that the member
     * is booked into the lesson and that no duplicate review exists.
     *
     * @param member  the reviewing member
     * @param lesson  the lesson being reviewed
     * @param rating  the rating (1–5)
     * @param comment the comment text
     * @return the created Review
     * @throws InvalidRatingException if the rating is outside 1–5
     * @throws IllegalStateException  if the member is not booked in the lesson
     *                                or has already reviewed it
     */
    public Review addReview(Member member, Lesson lesson, int rating, String comment)
            throws InvalidRatingException {
        // Validate member is booked in the lesson
        if (!member.getBookedLessons().contains(lesson)) {
            throw new IllegalStateException("Member " + member.getName()
                    + " is not booked in lesson " + lesson.getLessonId()
                    + ". Only booked members can write reviews.");
        }

        // Check for duplicate review
        for (Review r : reviews) {
            if (r.getMember().equals(member) && r.getLesson().equals(lesson)) {
                throw new IllegalStateException("Member " + member.getName()
                        + " has already reviewed lesson " + lesson.getLessonId() + ".");
            }
        }

        String reviewId = "R" + String.format("%03d", nextReviewId++);
        Review review = new Review(reviewId, member, lesson, rating, comment, LocalDate.now());
        reviews.add(review);
        lesson.addReview(review);
        return review;
    }

    /**
     * Searches the timetable by day.
     *
     * @param day the day to search
     * @return list of lessons on that day
     */
    public List<Lesson> searchTimetableByDay(DayOfWeek day) {
        return timetable.getLessonsByDay(day);
    }

    /**
     * Searches the timetable by exercise type.
     *
     * @param type the exercise type
     * @return list of lessons of that type
     */
    public List<Lesson> searchTimetableByExercise(ExerciseType type) {
        return timetable.getLessonsByExercise(type);
    }

    /**
     * Generates a formatted attendance and rating report for all lessons.
     * Sorted by week, then day, then time slot.
     *
     * @return the formatted report text
     */
    public String generateAttendanceReport() {
        DecimalFormat df = new DecimalFormat("0.00");
        StringBuilder sb = new StringBuilder();
        sb.append("==========================================================\n");
        sb.append("     ATTENDANCE & RATING REPORT\n");
        sb.append("     Furzefield Leisure Centre\n");
        sb.append("==========================================================\n\n");
        sb.append(String.format("%-14s %-10s %-12s %-12s %-8s %-10s\n",
                "Lesson ID", "Week", "Day", "Time Slot", "Exercise", "Booked  Avg Rating"));
        sb.append("--------------------------------------------------------------------------\n");

        List<Lesson> sorted = timetable.getAllLessons().stream()
                .sorted(Comparator.comparingInt(Lesson::getWeekNumber)
                        .thenComparing(l -> l.getDay().ordinal())
                        .thenComparing(l -> l.getTimeSlot().ordinal()))
                .collect(Collectors.toList());

        int totalBookings = 0;
        for (Lesson lesson : sorted) {
            int booked = lesson.getBookedMembers().size();
            totalBookings += booked;
            double avgRating = lesson.getAverageRating();
            String ratingStr = avgRating == 0.0 ? "N/A" : df.format(avgRating);
            sb.append(String.format("%-14s Week %-4d %-10s %-12s %-12s %-6d %s\n",
                    lesson.getLessonId(),
                    lesson.getWeekNumber(),
                    lesson.getDay().getDisplayName(),
                    lesson.getTimeSlot().getDisplayName(),
                    lesson.getExerciseType().getDisplayName(),
                    booked,
                    ratingStr));
        }

        sb.append("--------------------------------------------------------------------------\n");
        sb.append(String.format("\nGrand Total Bookings: %d\n", totalBookings));
        sb.append(String.format("Total Lessons: %d\n", sorted.size()));
        return sb.toString();
    }

    /**
     * Generates a formatted income report grouped by exercise type.
     * Sorted in descending order by total income. The highest income
     * exercise type is clearly labelled.
     *
     * @return the formatted report text
     */
    public String generateIncomeReport() {
        DecimalFormat df = new DecimalFormat("0.00");
        StringBuilder sb = new StringBuilder();
        sb.append("==========================================================\n");
        sb.append("     INCOME REPORT BY EXERCISE TYPE\n");
        sb.append("     Furzefield Leisure Centre\n");
        sb.append("==========================================================\n\n");

        // Build data per exercise type
        Map<ExerciseType, List<Lesson>> grouped = new LinkedHashMap<>();
        for (ExerciseType type : ExerciseType.values()) {
            grouped.put(type, timetable.getLessonsByExercise(type));
        }

        // Calculate income per exercise type
        List<Map.Entry<ExerciseType, Double>> incomeList = new ArrayList<>();
        Map<ExerciseType, Integer> totalMembersMap = new HashMap<>();
        Map<ExerciseType, Integer> totalLessonsMap = new HashMap<>();

        for (Map.Entry<ExerciseType, List<Lesson>> entry : grouped.entrySet()) {
            double totalIncome = 0;
            int totalMembers = 0;
            for (Lesson lesson : entry.getValue()) {
                totalIncome += lesson.getIncome();
                totalMembers += lesson.getBookedMembers().size();
            }
            incomeList.add(new AbstractMap.SimpleEntry<>(entry.getKey(), totalIncome));
            totalMembersMap.put(entry.getKey(), totalMembers);
            totalLessonsMap.put(entry.getKey(), entry.getValue().size());
        }

        // Sort descending by income
        incomeList.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        ExerciseType highest = incomeList.get(0).getKey();

        for (Map.Entry<ExerciseType, Double> entry : incomeList) {
            ExerciseType type = entry.getKey();
            double income = entry.getValue();
            int totalLessons = totalLessonsMap.get(type);
            int totalMembers = totalMembersMap.get(type);

            String marker = type == highest ? "  ★ HIGHEST INCOME ★" : "";
            sb.append(String.format("%-12s%s\n", type.getDisplayName(), marker));
            sb.append(String.format("  Price per session:   £%s\n", df.format(type.getPrice())));
            sb.append(String.format("  Total lessons run:   %d\n", totalLessons));
            sb.append(String.format("  Total members:       %d\n", totalMembers));
            sb.append(String.format("  Total income:        £%s\n", df.format(income)));
            sb.append("\n");
        }

        double grandTotal = incomeList.stream().mapToDouble(Map.Entry::getValue).sum();
        sb.append("--------------------------------------------------------------------------\n");
        sb.append(String.format("GRAND TOTAL INCOME:    £%s\n", df.format(grandTotal)));
        return sb.toString();
    }

    // --- Getters ---

    /** @return the timetable */
    public Timetable getTimetable() {
        return timetable;
    }

    /** @return the list of all members */
    public List<Member> getMembers() {
        return members;
    }

    /** @return the list of all reviews */
    public List<Review> getReviews() {
        return reviews;
    }

    /**
     * Gets the next available member ID based on existing members.
     *
     * @return a new unique member ID
     */
    public String getNextMemberId() {
        int max = 0;
        for (Member m : members) {
            String numPart = m.getMemberId().replace("M", "");
            try {
                int num = Integer.parseInt(numPart);
                if (num > max)
                    max = num;
            } catch (NumberFormatException ignore) {
            }
        }
        return String.format("M%02d", max + 1);
    }

    /**
     * Sets the next review ID counter (used by DataInitializer).
     *
     * @param id the next review ID number
     */
    public void setNextReviewId(int id) {
        this.nextReviewId = id;
    }
}
