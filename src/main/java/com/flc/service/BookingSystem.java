package com.flc.service;

import com.flc.exception.InvalidRatingException;
import com.flc.exception.LessonFullException;
import com.flc.exception.MemberNotFoundException;
import com.flc.exception.TimeConflictException;
import com.flc.model.BookingStatus;
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
import java.util.Set;
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
     * Marks a lesson as attended and immediately records the required review.
     *
     * @param member reviewing member
     * @param lesson attended lesson
     * @param rating rating 1-5
     * @param comment review text
     * @return created review
     * @throws InvalidRatingException if rating invalid
     */
    public Review attendLessonWithReview(Member member, Lesson lesson, int rating, String comment)
            throws InvalidRatingException {
        String trimmedComment = comment == null ? "" : comment.trim();
        BookingStatus status = member.getBookingStatus(lesson);

        if (status != com.flc.model.BookingStatus.BOOKED && status != com.flc.model.BookingStatus.CHANGED) {
            throw new IllegalStateException("Only booked or changed lessons can be attended with a review.");
        }
        if (trimmedComment.isEmpty()) {
            throw new IllegalArgumentException("Review comment cannot be empty.");
        }

        bookingManager.markAttendance(member, lesson);
        return addReview(member, lesson, rating, trimmedComment);
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
        if (member.getBookingStatus(lesson) != com.flc.model.BookingStatus.ATTENDED) {
            throw new IllegalStateException("Member " + member.getName()
                    + " has not attended lesson " + lesson.getLessonId()
                    + ". Only attended sessions can be reviewed.");
        }

        if (hasReview(member, lesson)) {
            throw new IllegalStateException("Member " + member.getName()
                    + " has already reviewed lesson " + lesson.getLessonId() + ".");
        }

        String reviewId = "R" + String.format("%03d", nextReviewId++);
        Review review = new Review(reviewId, member, lesson, rating, comment, LocalDate.now());
        reviews.add(review);
        lesson.addReview(review);
        return review;
    }

    /**
     * Checks whether a member has already submitted a review for a lesson.
     *
     * @param member member to check
     * @param lesson lesson to check
     * @return true if a review already exists
     */
    public boolean hasReview(Member member, Lesson lesson) {
        return reviews.stream()
                .anyMatch(review -> review.getMember().equals(member) && review.getLesson().equals(lesson));
    }

    /**
     * Gets the list of attended lessons that a member can still review.
     *
     * @param member target member
     * @return sorted reviewable lessons
     */
    public List<Lesson> getReviewableLessons(Member member) {
        return member.getBookedLessons().stream()
                .filter(member::hasAttended)
                .filter(lesson -> !hasReview(member, lesson))
                .sorted(Comparator.comparingInt(Lesson::getWeekNumber)
                        .thenComparing(lesson -> lesson.getDay().ordinal())
                        .thenComparing(lesson -> lesson.getTimeSlot().ordinal()))
                .collect(Collectors.toList());
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
                String.format("MONTHLY LESSON REPORT (Month %02d: Weeks %d-%d)",
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
                String.format("MONTHLY CHAMPION EXERCISE TYPE REPORT (Month %02d: Weeks %d-%d)",
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
        int weekCount = getWeekCount();
        return weekCount == 0 ? 0 : (int) Math.ceil(weekCount / 4.0);
    }

    /**
     * Returns the number of weeks configured in the timetable.
     *
     * @return total distinct weeks
     */
    public int getWeekCount() {
        return getMaxWeekNumber();
    }

    /**
     * Gets the number of members who have attended the given lesson.
     *
     * @param lesson target lesson
     * @return attended member count
     */
    public int getAttendedCount(Lesson lesson) {
        return (int) members.stream()
                .filter(member -> member.hasAttended(lesson))
                .count();
    }

    /**
     * Generates an assignment audit report showing whether the seeded data meets
     * the minimum deliverable requirements.
     *
     * @return formatted audit report
     */
    public String generateRequirementAuditReport() {
        int memberCount = members.size();
        Set<ExerciseType> exerciseTypes = timetable.getAllLessons().stream()
                .map(Lesson::getExerciseType)
                .collect(Collectors.toSet());
        int weekendCount = getWeekCount();
        int lessonCount = timetable.getAllLessons().size();
        int reviewCount = reviews.size();

        boolean membersOk = memberCount >= 10;
        boolean exercisesOk = exerciseTypes.size() >= 4;
        boolean weekendsOk = weekendCount >= 8;
        boolean lessonsOk = lessonCount >= 48;
        boolean reviewsOk = reviewCount >= 20;
        boolean overall = membersOk && exercisesOk && weekendsOk && lessonsOk && reviewsOk;

        StringBuilder sb = new StringBuilder();
        sb.append("==========================================================\n");
        sb.append("     ASSIGNMENT REQUIREMENT AUDIT\n");
        sb.append("     Furzefield Leisure Centre\n");
        sb.append("==========================================================\n\n");
        sb.append(formatRequirementLine("Registered members", memberCount, 10, membersOk));
        sb.append(formatRequirementLine("Exercise types in timetable", exerciseTypes.size(), 4, exercisesOk));
        sb.append(formatRequirementLine("Weekends designed", weekendCount, 8, weekendsOk));
        sb.append(formatRequirementLine("Lessons designed", lessonCount, 48, lessonsOk));
        sb.append(formatRequirementLine("Seeded reviews", reviewCount, 20, reviewsOk));
        sb.append(String.format("%-30s %d\n", "4-week cycles available", getCycleCount()));
        sb.append(String.format("%-30s %d\n", "Maximum lesson capacity", Lesson.MAX_CAPACITY));
        sb.append("\n");
        sb.append("OVERALL STATUS: ").append(overall ? "PASS" : "FAIL").append("\n");
        return sb.toString();
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
        sb.append(String.format("%-14s %-12s %-12s %-10s %-9s %-9s %-8s %-11s\n",
                "Lesson ID",
                "Exercise",
                "Week/Day",
                "Time",
                "Attended",
                "Pending",
                "Reviews",
                "Avg Rating"));
        sb.append("-----------------------------------------------------------------------------------\n");

        int totalAttendance = 0;
        int totalPending = 0;
        for (Lesson lesson : lessons) {
            int attendedCount = getAttendedCount(lesson);
            int pendingCount = Math.max(lesson.getBookedMembers().size() - attendedCount, 0);
            int reviewCount = lesson.getReviews().size();
            totalAttendance += attendedCount;
            totalPending += pendingCount;
            String avgRating = lesson.getAverageRating() == 0.0
                    ? "N/A"
                    : df.format(lesson.getAverageRating());

            sb.append(String.format("%-14s %-12s W%-2d %-9s %-10s %-9d %-9d %-8d %-11s\n",
                    lesson.getLessonId(),
                    lesson.getExerciseType().getDisplayName(),
                    lesson.getWeekNumber(),
                    lesson.getDay().getDisplayName(),
                    lesson.getTimeSlot().getDisplayName(),
                    attendedCount,
                    pendingCount,
                    reviewCount,
                    avgRating));
        }

        sb.append("-----------------------------------------------------------------------------------\n");
        sb.append(String.format("Total Lessons: %d\n", lessons.size()));
        sb.append(String.format("Total Attended Members: %d\n", totalAttendance));
        sb.append(String.format("Total Pending Members: %d\n", totalPending));
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

    private String formatRequirementLine(String label, int actual, int minimum, boolean passed) {
        return String.format("%-30s %d (minimum %d) [%s]\n",
                label,
                actual,
                minimum,
                passed ? "PASS" : "FAIL");
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
