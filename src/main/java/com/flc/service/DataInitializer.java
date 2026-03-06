package com.flc.service;

import com.flc.model.*;

import java.util.ArrayList;

/**
 * Static helper class responsible for creating and loading all sample data
 * into the BookingSystem. Acts as a factory for initializing 48 lessons,
 * 10 members, 20+ bookings, and 20+ reviews with meaningful varied data.
 *
 * @author FLC Development Team
 */
public class DataInitializer {

        /**
         * Timetable pattern: exercise types per slot for weeks 1–4 (repeated for 5–8).
         * Columns: Sat Morning, Sat Afternoon, Sat Evening, Sun Morning, Sun Afternoon,
         * Sun Evening.
         */
        private static final ExerciseType[][] PATTERN = {
                        // Week 1 / 5
                        { ExerciseType.YOGA, ExerciseType.ZUMBA, ExerciseType.BOX_FIT,
                                        ExerciseType.AQUACISE, ExerciseType.BODY_BLITZ, ExerciseType.YOGA },
                        // Week 2 / 6
                        { ExerciseType.ZUMBA, ExerciseType.AQUACISE, ExerciseType.BODY_BLITZ,
                                        ExerciseType.BOX_FIT, ExerciseType.YOGA, ExerciseType.ZUMBA },
                        // Week 3 / 7
                        { ExerciseType.BOX_FIT, ExerciseType.BODY_BLITZ, ExerciseType.YOGA,
                                        ExerciseType.ZUMBA, ExerciseType.AQUACISE, ExerciseType.BOX_FIT },
                        // Week 4 / 8
                        { ExerciseType.AQUACISE, ExerciseType.YOGA, ExerciseType.ZUMBA,
                                        ExerciseType.BODY_BLITZ, ExerciseType.BOX_FIT, ExerciseType.AQUACISE }
        };

        private static final DayOfWeek[] DAYS = { DayOfWeek.SATURDAY, DayOfWeek.SUNDAY };
        private static final TimeSlot[] SLOTS = { TimeSlot.MORNING, TimeSlot.AFTERNOON, TimeSlot.EVENING };
        private static final String[] SLOT_CODES = { "AM", "PM", "EVE" };
        private static final String[] DAY_CODES = { "SAT", "SUN" };

        /**
         * Private constructor to prevent instantiation.
         */
        private DataInitializer() {
        }

        /**
         * Initializes the booking system with complete sample data.
         *
         * @param system the BookingSystem to populate
         */
        public static void initialize(BookingSystem system) {
                createLessons(system);
                createMembers(system);
                createBookings(system);
                createReviews(system);
        }

        /**
         * Creates 48 lessons (8 weekends × 2 days × 3 slots) and adds them to the
         * timetable.
         */
        private static void createLessons(BookingSystem system) {
                Timetable timetable = system.getTimetable();

                for (int week = 1; week <= 8; week++) {
                        int patternIndex = (week - 1) % 4;
                        ExerciseType[] exercises = PATTERN[patternIndex];

                        int exerciseIdx = 0;
                        for (int d = 0; d < DAYS.length; d++) {
                                for (int s = 0; s < SLOTS.length; s++) {
                                        String lessonId = String.format("L-W%d-%s-%s", week, DAY_CODES[d],
                                                        SLOT_CODES[s]);
                                        Lesson lesson = new Lesson(lessonId, exercises[exerciseIdx],
                                                        DAYS[d], SLOTS[s], week);
                                        timetable.addLesson(lesson);
                                        exerciseIdx++;
                                }
                        }
                }
        }

        /**
         * Creates 10 pre-registered members.
         */
        private static void createMembers(BookingSystem system) {
                system.addMember(new Member("M01", "Alice Johnson", "alice.johnson@email.com"));
                system.addMember(new Member("M02", "Bob Smith", "bob.smith@email.com"));
                system.addMember(new Member("M03", "Carol White", "carol.white@email.com"));
                system.addMember(new Member("M04", "David Brown", "david.brown@email.com"));
                system.addMember(new Member("M05", "Emma Davis", "emma.davis@email.com"));
                system.addMember(new Member("M06", "Frank Wilson", "frank.wilson@email.com"));
                system.addMember(new Member("M07", "Grace Moore", "grace.moore@email.com"));
                system.addMember(new Member("M08", "Henry Taylor", "henry.taylor@email.com"));
                system.addMember(new Member("M09", "Isla Anderson", "isla.anderson@email.com"));
                system.addMember(new Member("M10", "Jack Thomas", "jack.thomas@email.com"));
        }

        /**
         * Creates 20+ bookings distributed across members and lessons.
         * Ensures at least 3 lessons are fully booked (4/4) and some have 0 bookings.
         */
        private static void createBookings(BookingSystem system) {
                Timetable timetable = system.getTimetable();
                try {
                        Member m01 = system.getMemberById("M01");
                        Member m02 = system.getMemberById("M02");
                        Member m03 = system.getMemberById("M03");
                        Member m04 = system.getMemberById("M04");
                        Member m05 = system.getMemberById("M05");
                        Member m06 = system.getMemberById("M06");
                        Member m07 = system.getMemberById("M07");
                        Member m08 = system.getMemberById("M08");
                        Member m09 = system.getMemberById("M09");
                        Member m10 = system.getMemberById("M10");

                        // --- Fully booked lessons (3 lessons with 4/4 members) ---

                        // L-W1-SAT-AM: Yoga - fully booked
                        Lesson l1 = timetable.getLessonById("L-W1-SAT-AM");
                        system.bookLesson(m01, l1);
                        system.bookLesson(m02, l1);
                        system.bookLesson(m03, l1);
                        system.bookLesson(m04, l1);

                        // L-W1-SAT-PM: Zumba - fully booked
                        Lesson l2 = timetable.getLessonById("L-W1-SAT-PM");
                        system.bookLesson(m05, l2);
                        system.bookLesson(m06, l2);
                        system.bookLesson(m07, l2);
                        system.bookLesson(m08, l2);

                        // L-W2-SAT-AM: Zumba - fully booked
                        Lesson l3 = timetable.getLessonById("L-W2-SAT-AM");
                        system.bookLesson(m01, l3);
                        system.bookLesson(m09, l3);
                        system.bookLesson(m10, l3);
                        system.bookLesson(m05, l3);

                        // --- Partially booked lessons ---

                        // L-W1-SAT-EVE: Box Fit
                        Lesson l4 = timetable.getLessonById("L-W1-SAT-EVE");
                        system.bookLesson(m09, l4);
                        system.bookLesson(m10, l4);

                        // L-W1-SUN-AM: Aquacise
                        Lesson l5 = timetable.getLessonById("L-W1-SUN-AM");
                        system.bookLesson(m01, l5);
                        system.bookLesson(m06, l5);

                        // L-W1-SUN-PM: Body Blitz
                        Lesson l6 = timetable.getLessonById("L-W1-SUN-PM");
                        system.bookLesson(m02, l6);
                        system.bookLesson(m07, l6);
                        system.bookLesson(m03, l6);

                        // L-W1-SUN-EVE: Yoga
                        Lesson l7 = timetable.getLessonById("L-W1-SUN-EVE");
                        system.bookLesson(m04, l7);

                        // L-W2-SAT-PM: Aquacise
                        Lesson l8 = timetable.getLessonById("L-W2-SAT-PM");
                        system.bookLesson(m02, l8);
                        system.bookLesson(m03, l8);

                        // L-W2-SUN-AM: Box Fit
                        Lesson l9 = timetable.getLessonById("L-W2-SUN-AM");
                        system.bookLesson(m04, l9);
                        system.bookLesson(m06, l9);

                        // L-W3-SAT-AM: Box Fit
                        Lesson l10 = timetable.getLessonById("L-W3-SAT-AM");
                        system.bookLesson(m07, l10);
                        system.bookLesson(m08, l10);

                        // L-W3-SUN-PM: Aquacise
                        Lesson l11 = timetable.getLessonById("L-W3-SUN-PM");
                        system.bookLesson(m09, l11);

                        // L-W4-SAT-EVE: Zumba
                        Lesson l12 = timetable.getLessonById("L-W4-SAT-EVE");
                        system.bookLesson(m10, l12);
                        system.bookLesson(m08, l12);

                } catch (Exception e) {
                        System.err.println("Error initializing bookings: " + e.getMessage());
                        e.printStackTrace();
                }
        }

        /**
         * Creates 20+ reviews with varied ratings (1–5) and meaningful comments.
         */
        private static void createReviews(BookingSystem system) {
                try {
                        Member m01 = system.getMemberById("M01");
                        Member m02 = system.getMemberById("M02");
                        Member m03 = system.getMemberById("M03");
                        Member m04 = system.getMemberById("M04");
                        Member m05 = system.getMemberById("M05");
                        Member m06 = system.getMemberById("M06");
                        Member m07 = system.getMemberById("M07");
                        Member m08 = system.getMemberById("M08");
                        Member m09 = system.getMemberById("M09");
                        Member m10 = system.getMemberById("M10");

                        Timetable tt = system.getTimetable();

                        // Mark all current bookings as attended so they can be reviewed in sample data
                        for (Member m : system.getMembers()) {
                                for (Lesson l : new ArrayList<>(m.getBookedLessons())) {
                                        system.attendLesson(m, l);
                                }
                        }

                        // Reviews for L-W1-SAT-AM (Yoga) - 4 booked members
                        system.addReview(m01, tt.getLessonById("L-W1-SAT-AM"), 5,
                                        "Absolutely fantastic yoga session! The instructor was very patient and encouraging.");
                        system.addReview(m02, tt.getLessonById("L-W1-SAT-AM"), 4,
                                        "Really enjoyed the session. Good stretches and relaxation techniques.");
                        system.addReview(m03, tt.getLessonById("L-W1-SAT-AM"), 3,
                                        "It was okay. I expected more advanced poses but good for beginners.");
                        system.addReview(m04, tt.getLessonById("L-W1-SAT-AM"), 5,
                                        "Best yoga class I've ever attended. Will definitely come back.");

                        // Reviews for L-W1-SAT-PM (Zumba) - 4 booked members
                        system.addReview(m05, tt.getLessonById("L-W1-SAT-PM"), 5,
                                        "The energy was incredible! Great music and wonderful choreography.");
                        system.addReview(m06, tt.getLessonById("L-W1-SAT-PM"), 4,
                                        "Fun and engaging Zumba session. Great workout for the whole body.");
                        system.addReview(m07, tt.getLessonById("L-W1-SAT-PM"), 2,
                                        "The pace was too fast for me. Needed more time to learn the steps.");
                        system.addReview(m08, tt.getLessonById("L-W1-SAT-PM"), 4,
                                        "A really enjoyable class. The instructor kept everyone motivated.");

                        // Reviews for L-W1-SAT-EVE (Box Fit) - 2 booked members
                        system.addReview(m09, tt.getLessonById("L-W1-SAT-EVE"), 5,
                                        "Intense and exhilarating! Perfect way to end a Saturday.");
                        system.addReview(m10, tt.getLessonById("L-W1-SAT-EVE"), 3,
                                        "Good workout but quite challenging. More warm-up time would be nice.");

                        // Reviews for L-W1-SUN-AM (Aquacise) - 2 booked members
                        system.addReview(m01, tt.getLessonById("L-W1-SUN-AM"), 4,
                                        "Loved the aquacise session. The water exercises were gentle on my joints.");
                        system.addReview(m06, tt.getLessonById("L-W1-SUN-AM"), 5,
                                        "Brilliant pool session! So refreshing and the instructor was amazing.");

                        // Reviews for L-W1-SUN-PM (Body Blitz) - 3 booked members
                        system.addReview(m02, tt.getLessonById("L-W1-SUN-PM"), 1,
                                        "The session was too intense for me. I felt overwhelmed by the exercises.");
                        system.addReview(m07, tt.getLessonById("L-W1-SUN-PM"), 3,
                                        "Decent Class. Some exercises were fun but others were quite tough.");
                        system.addReview(m03, tt.getLessonById("L-W1-SUN-PM"), 4,
                                        "Great full-body workout! I felt energised after the session.");

                        // Reviews for L-W1-SUN-EVE (Yoga) - 1 booked member
                        system.addReview(m04, tt.getLessonById("L-W1-SUN-EVE"), 5,
                                        "A wonderful way to end the weekend. Very calming and restorative.");

                        // Reviews for L-W2-SAT-AM (Zumba) - 4 booked members
                        system.addReview(m01, tt.getLessonById("L-W2-SAT-AM"), 4,
                                        "This Zumba session was great fun! Good variety of songs.");
                        system.addReview(m09, tt.getLessonById("L-W2-SAT-AM"), 2,
                                        "Not my cup of tea. I prefer more structured exercise routines.");

                        // Reviews for L-W2-SAT-PM (Aquacise) - 2 booked members
                        system.addReview(m02, tt.getLessonById("L-W2-SAT-PM"), 5,
                                        "Excellent aquacise class. The pool temperature was perfect.");
                        system.addReview(m03, tt.getLessonById("L-W2-SAT-PM"), 4,
                                        "Good session overall. Nice low-impact exercises for all fitness levels.");

                        // Reviews for L-W2-SUN-AM (Box Fit)
                        system.addReview(m04, tt.getLessonById("L-W2-SUN-AM"), 3,
                                        "Decent boxing workout. I would have liked longer rounds though.");

                        // Extra review: L-W3-SAT-AM (Box Fit)
                        system.addReview(m07, tt.getLessonById("L-W3-SAT-AM"), 5,
                                        "The best box fit session yet! Felt so powerful and confident afterwards.");

                        // Set next review ID counter
                        system.setNextReviewId(23);

                } catch (Exception e) {
                        System.err.println("Error initializing reviews: " + e.getMessage());
                        e.printStackTrace();
                }
        }
}
