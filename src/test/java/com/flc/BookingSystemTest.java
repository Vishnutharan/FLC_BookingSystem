package com.flc;

import com.flc.exception.InvalidRatingException;
import com.flc.exception.LessonFullException;
import com.flc.exception.TimeConflictException;
import com.flc.model.Booking;
import com.flc.model.BookingStatus;
import com.flc.model.DayOfWeek;
import com.flc.model.ExerciseType;
import com.flc.model.Lesson;
import com.flc.model.Member;
import com.flc.model.Review;
import com.flc.model.TimeSlot;
import com.flc.service.BookingSystem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * JUnit 5 test suite for FLC booking functionality.
 */
public class BookingSystemTest {

    private BookingSystem system;
    private Member testMember;

    @BeforeEach
    void setUp() {
        system = new BookingSystem();
        system.initializeSampleData();
        testMember = new Member("T01", "Test User", "test.user@email.com");
        system.addMember(testMember);
    }

    @Test
    @DisplayName("Booking succeeds when lesson has space and no conflict")
    void testBookLesson_Success() throws LessonFullException, TimeConflictException {
        Lesson lesson = system.getTimetable().getLessonById("L-W3-SUN-EVE");
        int before = lesson.getBookedMembers().size();

        system.bookLesson(testMember, lesson);

        assertEquals(before + 1, lesson.getBookedMembers().size());
        assertTrue(testMember.getBookedLessons().contains(lesson));
        assertEquals(lesson.MAX_CAPACITY - (before + 1), lesson.getAvailableSpaces());
    }

    @Test
    @DisplayName("Booking is rejected when lesson is full")
    void testBookLesson_Full_ThrowsException() {
        Lesson fullLesson = system.getTimetable().getLessonById("L-W1-SAT-AM");
        assertFalse(fullLesson.isAvailable());

        assertThrows(LessonFullException.class, () -> system.bookLesson(testMember, fullLesson));
    }

    @Test
    @DisplayName("Booking is rejected on time conflict")
    void testBookLesson_TimeConflict_ThrowsException() throws LessonFullException, TimeConflictException {
        Lesson original = system.getTimetable().getLessonById("L-W5-SAT-AM");
        system.bookLesson(testMember, original);

        Lesson conflictingLesson = new Lesson(
                "L-CONFLICT",
                ExerciseType.BODY_BLITZ,
                DayOfWeek.SATURDAY,
                TimeSlot.MORNING,
                5);

        assertThrows(TimeConflictException.class, () -> system.bookLesson(testMember, conflictingLesson));
    }

    @Test
    @DisplayName("Booking change succeeds and frees old lesson space")
    void testChangeBooking_Success() throws LessonFullException, TimeConflictException {
        Lesson oldLesson = system.getTimetable().getLessonById("L-W4-SUN-AM");
        Lesson newLesson = system.getTimetable().getLessonById("L-W4-SUN-PM");

        system.bookLesson(testMember, oldLesson);
        Booking originalBooking = testMember.getActiveBookingForLesson(oldLesson);
        int oldBefore = oldLesson.getBookedMembers().size();
        int newBefore = newLesson.getBookedMembers().size();

        system.changeBooking(testMember, oldLesson, newLesson);
        Booking changedBooking = testMember.getActiveBookingForLesson(newLesson);

        assertFalse(testMember.getBookedLessons().contains(oldLesson));
        assertTrue(testMember.getBookedLessons().contains(newLesson));
        assertEquals(oldBefore - 1, oldLesson.getBookedMembers().size());
        assertEquals(newBefore + 1, newLesson.getBookedMembers().size());
        assertNotNull(changedBooking);
        assertEquals(originalBooking.getBookingId(), changedBooking.getBookingId());
        assertEquals(BookingStatus.CHANGED, changedBooking.getStatus());
    }

    @Test
    @DisplayName("Cancel booking frees lesson space")
    void testCancelBooking_Success() throws LessonFullException, TimeConflictException {
        Lesson lesson = system.getTimetable().getLessonById("L-W4-SUN-AM");
        system.bookLesson(testMember, lesson);

        int beforeCancel = lesson.getBookedMembers().size();
        system.cancelBooking(testMember, lesson);

        assertFalse(testMember.getBookedLessons().contains(lesson));
        assertEquals(beforeCancel - 1, lesson.getBookedMembers().size());
        assertFalse(lesson.getBookedMembers().contains(testMember));
    }

    @Test
    @DisplayName("Review and rating are stored for attended lesson")
    void testReviewAndRating_Stored() throws LessonFullException, TimeConflictException, InvalidRatingException {
        Lesson lesson = system.getTimetable().getLessonById("L-W4-SUN-AM");

        system.bookLesson(testMember, lesson);
        Review review = system.attendLessonWithReview(testMember, lesson, 4, "Great instruction and pacing.");

        assertNotNull(review);
        assertEquals(4, review.getRating());
        assertEquals("Great instruction and pacing.", review.getComment());
        assertTrue(system.getReviews().contains(review));
        assertTrue(lesson.getReviews().contains(review));
        assertEquals(BookingStatus.ATTENDED, testMember.getBookingStatus(lesson));
    }

    @Test
    @DisplayName("Timetable filtering by day and weekend returns expected lessons")
    void testTimetableFilter_ByDayAndWeekend() {
        List<Lesson> week1Saturday = system.getTimetable().getLessonsByWeekAndDay(1, DayOfWeek.SATURDAY);

        assertEquals(3, week1Saturday.size());
        assertTrue(week1Saturday.stream().allMatch(l -> l.getWeekNumber() == 1));
        assertTrue(week1Saturday.stream().allMatch(l -> l.getDay() == DayOfWeek.SATURDAY));
    }

    @Test
    @DisplayName("Timetable filtering by exercise returns only selected type")
    void testTimetableFilter_ByExerciseType() {
        List<Lesson> yogaLessons = system.searchTimetableByExercise(ExerciseType.YOGA);

        assertFalse(yogaLessons.isEmpty());
        assertTrue(yogaLessons.stream().allMatch(l -> l.getExerciseType() == ExerciseType.YOGA));
    }

    @Test
    @DisplayName("Timetable filtering by day across all weeks returns all matching lessons")
    void testTimetableFilter_ByDayAcrossAllWeeks() {
        List<Lesson> saturdayLessons = system.searchTimetableByDay(DayOfWeek.SATURDAY);

        assertEquals(24, saturdayLessons.size());
        assertTrue(saturdayLessons.stream().allMatch(l -> l.getDay() == DayOfWeek.SATURDAY));
    }

    @Test
    @DisplayName("Attendance report contains correct member counts and average ratings")
    void testReport_AttendanceCountsAndAverages() {
        Lesson knownLesson = system.getTimetable().getLessonById("L-W1-SAT-AM");
        assertEquals(4, knownLesson.getBookedMembers().size());
        assertEquals(4.25, knownLesson.getAverageRating(), 0.001);

        String cycle1Report = system.generateAttendanceReportForCycle(1);

        assertTrue(cycle1Report.contains("L-W1-SAT-AM"));
        assertTrue(cycle1Report.contains("4.25"));
        assertTrue(cycle1Report.contains("Total Attended Members"));
    }

    @Test
    @DisplayName("Income report identifies highest-income exercise type correctly")
    void testReport_HighestIncomeExercise() {
        Map<ExerciseType, Double> incomeMap = system.getIncomeByExerciseForCycle(1);

        ExerciseType expectedHighest = incomeMap.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElseThrow();

        ExerciseType computedHighest = system.getHighestIncomeExerciseForCycle(1);
        assertEquals(expectedHighest, computedHighest);

        String report = system.generateIncomeReportForCycle(1);
        assertTrue(report.contains("HIGHEST-INCOME EXERCISE: " + expectedHighest.getDisplayName()));
    }

    @Test
    @DisplayName("Each 4-week cycle contains exactly 24 lessons")
    void testCycleLessonCounts() {
        assertEquals(24, system.getLessonsForCycle(1).size());
        assertEquals(24, system.getLessonsForCycle(2).size());
        assertEquals(2, system.getCycleCount());
    }

    @Test
    @DisplayName("Changing to the same lesson is rejected")
    void testChangeBooking_ToSameLessonRejected() throws LessonFullException, TimeConflictException {
        Lesson lesson = system.getTimetable().getLessonById("L-W6-SAT-AM");
        system.bookLesson(testMember, lesson);

        assertThrows(IllegalArgumentException.class, () -> system.changeBooking(testMember, lesson, lesson));
    }

    @Test
    @DisplayName("Duplicate review for same lesson/member is rejected")
    void testDuplicateReviewRejected() throws LessonFullException, TimeConflictException, InvalidRatingException {
        Lesson lesson = system.getTimetable().getLessonById("L-W6-SUN-AM");
        system.bookLesson(testMember, lesson);
        assertDoesNotThrow(() -> system.attendLessonWithReview(testMember, lesson, 5, "Excellent class."));
        assertThrows(IllegalStateException.class,
                () -> system.addReview(testMember, lesson, 4, "Second review should fail."));
    }

    @Test
    @DisplayName("Changed booking can still be cancelled and attended")
    void testChangedBooking_RemainsActionable() throws LessonFullException, TimeConflictException, InvalidRatingException {
        Lesson originalLesson = system.getTimetable().getLessonById("L-W7-SAT-AM");
        Lesson changedLesson = system.getTimetable().getLessonById("L-W7-SAT-PM");

        system.bookLesson(testMember, originalLesson);
        system.changeBooking(testMember, originalLesson, changedLesson);

        Booking changedBooking = testMember.getActiveBookingForLesson(changedLesson);
        assertNotNull(changedBooking);
        assertEquals(BookingStatus.CHANGED, changedBooking.getStatus());

        assertDoesNotThrow(() -> system.attendLessonWithReview(testMember, changedLesson, 5, "Great changed class."));
        assertEquals(BookingStatus.ATTENDED, testMember.getBookingStatus(changedLesson));

        Lesson cancelOriginal = system.getTimetable().getLessonById("L-W8-SAT-AM");
        Lesson cancelReplacement = system.getTimetable().getLessonById("L-W8-SAT-PM");
        system.bookLesson(testMember, cancelOriginal);
        system.changeBooking(testMember, cancelOriginal, cancelReplacement);
        system.cancelBooking(testMember, cancelReplacement);

        assertEquals(BookingStatus.CANCELLED, testMember.getBookingStatus(cancelReplacement));
        assertFalse(testMember.getBookedLessons().contains(cancelReplacement));
    }

    @Test
    @DisplayName("Attended bookings cannot be cancelled")
    void testCancelBooking_AfterAttendanceRejected() {
        Member member = system.getMembers().stream()
                .filter(m -> m.getMemberId().equals("M01"))
                .findFirst()
                .orElseThrow();
        Lesson attendedLesson = system.getTimetable().getLessonById("L-W1-SAT-AM");

        assertThrows(IllegalStateException.class, () -> system.cancelBooking(member, attendedLesson));
        assertTrue(member.getBookedLessons().contains(attendedLesson));
    }

    @Test
    @DisplayName("Requirement audit confirms seeded data meets assignment minimums")
    void testRequirementAudit_MeetsMinimums() {
        String audit = system.generateRequirementAuditReport();

        assertTrue(audit.contains("Registered members"));
        assertTrue(audit.contains("Lessons designed"));
        assertTrue(audit.contains("Seeded reviews"));
        assertTrue(audit.contains("OVERALL STATUS: PASS"));
    }

    @Test
    @DisplayName("Income map is sorted externally and values are non-negative")
    void testIncomeMapValues() {
        Map<ExerciseType, Double> cycleIncome = system.getIncomeByExerciseForCycle(1);
        assertEquals(ExerciseType.values().length, cycleIncome.size());
        assertTrue(cycleIncome.values().stream().allMatch(v -> v >= 0.0));

        double highest = cycleIncome.values().stream().max(Comparator.naturalOrder()).orElse(0.0);
        assertEquals(highest, cycleIncome.get(system.getHighestIncomeExerciseForCycle(1)), 0.001);
    }
}
