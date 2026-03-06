package com.flc.service;

import com.flc.exception.InvalidRatingException;
import com.flc.exception.LessonFullException;
import com.flc.exception.MemberNotFoundException;
import com.flc.exception.TimeConflictException;
import com.flc.model.DayOfWeek;
import com.flc.model.ExerciseType;
import com.flc.model.Lesson;
import com.flc.model.Member;
import com.flc.model.Review;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Main service/controller class for the FLC Booking System.
 */
public class BookingSystem {

    private final Timetable timetable;
    private final List<Member> members;
    private final List<Review> reviews;
    private final BookingManager bookingManager;
    private int nextReviewId;

    /**
     * Constructs a new BookingSystem with empty data.
     */
    public BookingSystem() {
        this.timetable = new Timetable();
        this.members = new ArrayList<>();
        this.reviews = new ArrayList<>();
        this.bookingManager = BookingManager.getInstance();
        this.nextReviewId = 1;
    }

    /**
     * Initializes the system with sample data.
     */
    public void initializeSampleData() {
        DataInitializer.initialize(this);
    }

    /**
     * Adds a member to the system.
     *
     * @param member member to add
     */
    public void addMember(Member member) {
        members.add(member);
    }

    /**
     * Finds a member by ID.
     *
     * @param id member ID
     * @return member
     * @throws MemberNotFoundException if not found
     */
    public Member getMemberById(String id) throws MemberNotFoundException {
        return members.stream()
                .filter(m -> m.getMemberId().equals(id))
                .findFirst()
                .orElseThrow(() -> new MemberNotFoundException("Member with ID " + id + " not found."));
    }

    /**
     * Finds a member by exact name (case-insensitive).
     *
     * @param name member name
     * @return member
     * @throws MemberNotFoundException if not found
     */
    public Member getMemberByName(String name) throws MemberNotFoundException {
        return members.stream()
                .filter(m -> m.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow(() -> new MemberNotFoundException("Member with name '" + name + "' not found."));
    }

    /**
     * Books a lesson for a member.
     */
    public void bookLesson(Member member, Lesson lesson) throws LessonFullException, TimeConflictException {
        bookingManager.bookLesson(member, lesson);
    }

    /**
     * Changes a member booking from one lesson to another.
     */
    public void changeBooking(Member member, Lesson oldLesson, Lesson newLesson)
            throws LessonFullException, TimeConflictException {
        bookingManager.changeBooking(member, oldLesson, newLesson);
    }

    /**
     * Cancels a member booking.
     */
    public void cancelBooking(Member member, Lesson lesson) {
        bookingManager.cancelBooking(member, lesson);
    }

    /**
     * Marks a lesson as attended for a member.
     */
    public void attendLesson(Member member, Lesson lesson) {
        bookingManager.markAttendance(member, lesson);
    }

    /**
     * Adds a review for an attended booking.
     *
     * @param member reviewing member
     * @param lesson reviewed lesson
     * @param rating rating 1-5
     * @param comment review text
     * @return created review
     * @throws InvalidRatingException if rating invalid
     */
    public Review addReview(Member member, Lesson lesson, int rating, String comment)
            throws InvalidRatingException {
        if (!member.getBookedLessons().contains(lesson)) {
            throw new IllegalStateException("Member " + member.getName()
                    + " is not booked in lesson " + lesson.getLessonId()
                    + ". Only booked members can write reviews.");
        }

        if (!member.hasAttended(lesson)) {
            throw new IllegalStateException("Member " + member.getName()
                    + " has not attended lesson " + lesson.getLessonId()
                    + ". Only attended sessions can be reviewed.");
        }

        for (Review existing : reviews) {
            if (existing.getMember().equals(member) && existing.getLesson().equals(lesson)) {
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
     * Searches timetable by day across all weeks.
     */
    public List<Lesson> searchTimetableByDay(DayOfWeek day) {
        return timetable.getLessonsByDay(day);
    }

    /**
     * Searches timetable by exercise type across all weeks.
     */
    public List<Lesson> searchTimetableByExercise(ExerciseType type) {
        return timetable.getLessonsByExercise(type);
    }

    /**
     * Generates attendance report across all weeks.
     */
    public String generateAttendanceReport() {
        return generateAttendanceReportForWeeks(1, getMaxWeekNumber(), "ATTENDANCE AND RATING REPORT (All Weeks)");
    }

    /**
     * Generates income report across all weeks.
     */
    public String generateIncomeReport() {
        return generateIncomeReportForWeeks(1, getMaxWeekNumber(), "INCOME REPORT BY EXERCISE TYPE (All Weeks)");
    }

    /**
     * Generates attendance report for a 4-week cycle.
     *
     * @param cycleNumber cycle starting at 1
     * @return formatted report
     */
    public String generateAttendanceReportForCycle(int cycleNumber) {
        int[] range = getWeekRangeForCycle(cycleNumber);
        return generateAttendanceReportForWeeks(
                range[0],
                range[1],
                String.format("ATTENDANCE AND RATING REPORT (Cycle %d: Weeks %d-%d)",
                        cycleNumber,
                        range[0],
                        range[1]));
    }

    /**
     * Generates income report for a 4-week cycle.
     *
     * @param cycleNumber cycle starting at 1
     * @return formatted report
     */
    public String generateIncomeReportForCycle(int cycleNumber) {
        int[] range = getWeekRangeForCycle(cycleNumber);
        return generateIncomeReportForWeeks(
                range[0],
                range[1],
                String.format("INCOME REPORT BY EXERCISE TYPE (Cycle %d: Weeks %d-%d)",
                        cycleNumber,
                        range[0],
                        range[1]));
    }

    /**
     * Returns sorted lessons for a cycle.
     *
     * @param cycleNumber cycle number
     * @return cycle lessons
     */
    public List<Lesson> getLessonsForCycle(int cycleNumber) {
        int[] range = getWeekRangeForCycle(cycleNumber);
        return getLessonsInWeekRange(range[0], range[1]);
    }

    /**
     * Returns income totals by exercise type for a cycle.
     *
     * @param cycleNumber cycle number
     * @return income map
     */
    public Map<ExerciseType, Double> getIncomeByExerciseForCycle(int cycleNumber) {
        int[] range = getWeekRangeForCycle(cycleNumber);
        return calculateIncomeByExercise(getLessonsInWeekRange(range[0], range[1]));
    }

    /**
     * Returns highest-income exercise type for a cycle.
     *
     * @param cycleNumber cycle number
     * @return highest-income exercise type
     */
    public ExerciseType getHighestIncomeExerciseForCycle(int cycleNumber) {
        return getIncomeByExerciseForCycle(cycleNumber).entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .orElseThrow(() -> new IllegalStateException("No lessons available for cycle " + cycleNumber))
                .getKey();
    }

    /**
     * Returns number of 4-week cycles present in the timetable.
     *
     * @return cycle count
     */
    public int getCycleCount() {
        int maxWeek = getMaxWeekNumber();
        return maxWeek == 0 ? 0 : (int) Math.ceil(maxWeek / 4.0);
    }

    /** @return timetable */
    public Timetable getTimetable() {
        return timetable;
    }

    /** @return all members */
    public List<Member> getMembers() {
        return members;
    }

    /** @return all reviews */
    public List<Review> getReviews() {
        return reviews;
    }

    /**
     * Computes next member ID.
     *
     * @return next member ID like M11
     */
    public String getNextMemberId() {
        int max = 0;
        for (Member m : members) {
            String numPart = m.getMemberId().replace("M", "");
            try {
                int num = Integer.parseInt(numPart);
                if (num > max) {
                    max = num;
                }
            } catch (NumberFormatException ignore) {
            }
        }
        return String.format("M%02d", max + 1);
    }

    /**
     * Sets next review ID counter (used by DataInitializer).
     *
     * @param id next numeric review ID
     */
    public void setNextReviewId(int id) {
        this.nextReviewId = id;
    }

    private String generateAttendanceReportForWeeks(int startWeek, int endWeek, String title) {
        DecimalFormat df = new DecimalFormat("0.00");
        List<Lesson> lessons = getLessonsInWeekRange(startWeek, endWeek);

        StringBuilder sb = new StringBuilder();
        sb.append("==========================================================\n");
        sb.append("     ").append(title).append("\n");
        sb.append("     Furzefield Leisure Centre\n");
        sb.append("==========================================================\n\n");
        sb.append(String.format("%-14s %-12s %-12s %-10s %-7s %-11s\n",
                "Lesson ID",
                "Exercise",
                "Week/Day",
                "Time",
                "Booked",
                "Avg Rating"));
        sb.append("--------------------------------------------------------------------------\n");

        int totalBookings = 0;
        for (Lesson lesson : lessons) {
            int bookedCount = lesson.getBookedMembers().size();
            totalBookings += bookedCount;
            String avgRating = lesson.getAverageRating() == 0.0
                    ? "N/A"
                    : df.format(lesson.getAverageRating());

            sb.append(String.format("%-14s %-12s W%-2d %-9s %-10s %-7d %-11s\n",
                    lesson.getLessonId(),
                    lesson.getExerciseType().getDisplayName(),
                    lesson.getWeekNumber(),
                    lesson.getDay().getDisplayName(),
                    lesson.getTimeSlot().getDisplayName(),
                    bookedCount,
                    avgRating));
        }

        sb.append("--------------------------------------------------------------------------\n");
        sb.append(String.format("Total Lessons: %d\n", lessons.size()));
        sb.append(String.format("Total Bookings: %d\n", totalBookings));
        return sb.toString();
    }

    private String generateIncomeReportForWeeks(int startWeek, int endWeek, String title) {
        DecimalFormat df = new DecimalFormat("0.00");
        List<Lesson> lessons = getLessonsInWeekRange(startWeek, endWeek);
        Map<ExerciseType, Double> incomeByExercise = calculateIncomeByExercise(lessons);
        Map<ExerciseType, Integer> bookingsByExercise = calculateBookingsByExercise(lessons);

        List<Map.Entry<ExerciseType, Double>> ranked = incomeByExercise.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .collect(Collectors.toList());

        ExerciseType highest = ranked.isEmpty() ? null : ranked.get(0).getKey();

        StringBuilder sb = new StringBuilder();
        sb.append("==========================================================\n");
        sb.append("     ").append(title).append("\n");
        sb.append("     Furzefield Leisure Centre\n");
        sb.append("==========================================================\n\n");

        for (Map.Entry<ExerciseType, Double> entry : ranked) {
            ExerciseType type = entry.getKey();
            String marker = type == highest ? "  <-- HIGHEST INCOME" : "";
            sb.append(String.format("%-12s%s\n", type.getDisplayName(), marker));
            sb.append(String.format("  Price:             GBP %s\n", df.format(type.getPrice())));
            sb.append(String.format("  Total bookings:    %d\n", bookingsByExercise.get(type)));
            sb.append(String.format("  Total income:      GBP %s\n\n", df.format(entry.getValue())));
        }

        double grandTotal = ranked.stream().mapToDouble(Map.Entry::getValue).sum();
        sb.append("--------------------------------------------------------------------------\n");
        sb.append(String.format("GRAND TOTAL INCOME: GBP %s\n", df.format(grandTotal)));
        if (highest != null) {
            sb.append(String.format("HIGHEST-INCOME EXERCISE: %s\n", highest.getDisplayName()));
        }
        return sb.toString();
    }

    private List<Lesson> getLessonsInWeekRange(int startWeek, int endWeek) {
        return timetable.getAllLessons().stream()
                .filter(lesson -> lesson.getWeekNumber() >= startWeek && lesson.getWeekNumber() <= endWeek)
                .sorted(Comparator.comparingInt(Lesson::getWeekNumber)
                        .thenComparing(lesson -> lesson.getDay().ordinal())
                        .thenComparing(lesson -> lesson.getTimeSlot().ordinal()))
                .collect(Collectors.toList());
    }

    private int[] getWeekRangeForCycle(int cycleNumber) {
        int cycleCount = getCycleCount();
        if (cycleCount == 0) {
            throw new IllegalArgumentException("No lessons available in the timetable.");
        }
        if (cycleNumber < 1 || cycleNumber > cycleCount) {
            throw new IllegalArgumentException("Cycle number must be between 1 and " + cycleCount + ".");
        }

        int startWeek = ((cycleNumber - 1) * 4) + 1;
        int endWeek = Math.min(startWeek + 3, getMaxWeekNumber());
        return new int[] { startWeek, endWeek };
    }

    private int getMaxWeekNumber() {
        return timetable.getAllLessons().stream()
                .mapToInt(Lesson::getWeekNumber)
                .max()
                .orElse(0);
    }

    private Map<ExerciseType, Double> calculateIncomeByExercise(List<Lesson> lessons) {
        Map<ExerciseType, Double> totals = new LinkedHashMap<>();
        for (ExerciseType type : ExerciseType.values()) {
            double value = lessons.stream()
                    .filter(lesson -> lesson.getExerciseType() == type)
                    .mapToDouble(Lesson::getIncome)
                    .sum();
            totals.put(type, value);
        }
        return totals;
    }

    private Map<ExerciseType, Integer> calculateBookingsByExercise(List<Lesson> lessons) {
        Map<ExerciseType, Integer> totals = new LinkedHashMap<>();
        for (ExerciseType type : ExerciseType.values()) {
            int value = lessons.stream()
                    .filter(lesson -> lesson.getExerciseType() == type)
                    .mapToInt(lesson -> lesson.getBookedMembers().size())
                    .sum();
            totals.put(type, value);
        }
        return totals;
    }
}
