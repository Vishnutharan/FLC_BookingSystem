package com.flc.service;

import com.flc.model.DayOfWeek;
import com.flc.model.ExerciseType;
import com.flc.model.Lesson;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Manages the complete timetable of lessons for the Furzefield Leisure Centre.
 * Provides methods to search and filter lessons by day, exercise type, week,
 * and availability.
 *
 * @author FLC Development Team
 */
public class Timetable {

    private List<Lesson> allLessons;

    /**
     * Constructs an empty Timetable.
     */
    public Timetable() {
        this.allLessons = new ArrayList<>();
    }

    /**
     * Adds a lesson to the timetable.
     *
     * @param lesson the lesson to add
     */
    public void addLesson(Lesson lesson) {
        allLessons.add(lesson);
    }

    /**
     * Gets all lessons scheduled on a specific day across all weeks.
     *
     * @param day the day of the week
     * @return list of lessons on that day
     */
    public List<Lesson> getLessonsByDay(DayOfWeek day) {
        return allLessons.stream()
                .filter(l -> l.getDay() == day)
                .collect(Collectors.toList());
    }

    /**
     * Gets all lessons of a specific exercise type across all weeks and days.
     *
     * @param type the exercise type
     * @return list of lessons of that exercise type
     */
    public List<Lesson> getLessonsByExercise(ExerciseType type) {
        return allLessons.stream()
                .filter(l -> l.getExerciseType() == type)
                .collect(Collectors.toList());
    }

    /**
     * Gets all lessons for a specific week and day combination.
     *
     * @param week the week number (1–8)
     * @param day  the day of the week
     * @return list of lessons for that week and day
     */
    public List<Lesson> getLessonsByWeekAndDay(int week, DayOfWeek day) {
        return allLessons.stream()
                .filter(l -> l.getWeekNumber() == week && l.getDay() == day)
                .collect(Collectors.toList());
    }

    /**
     * Finds a lesson by its unique ID.
     *
     * @param id the lesson ID
     * @return the lesson, or null if not found
     */
    public Lesson getLessonById(String id) {
        return allLessons.stream()
                .filter(l -> l.getLessonId().equals(id))
                .findFirst()
                .orElse(null);
    }

    /**
     * Gets the complete list of all lessons in the timetable.
     *
     * @return list of all lessons
     */
    public List<Lesson> getAllLessons() {
        return allLessons;
    }

    /**
     * Gets all lessons that have available spaces (less than maximum capacity).
     *
     * @return list of available lessons
     */
    public List<Lesson> getAvailableLessons() {
        return allLessons.stream()
                .filter(Lesson::isAvailable)
                .collect(Collectors.toList());
    }
}
