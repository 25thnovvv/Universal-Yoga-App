package com.example.universalyogaapp.dao;

import com.example.universalyogaapp.db.CourseEntity;
import androidx.room.*;
import java.util.List;

@Dao
public interface CourseDao {
    // Basic CRUD operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertCourse(CourseEntity courseEntity);

    @Update
    void updateCourse(CourseEntity courseEntity);

    @Delete
    void deleteCourse(CourseEntity courseEntity);

    // Query operations
    @Query("SELECT * FROM courses WHERE cloudSyncStatus = 0")
    List<CourseEntity> getUnsyncedCourses();

    @Query("SELECT * FROM courses")
    List<CourseEntity> getAllCourses();

    @Query("DELETE FROM courses WHERE cloudDatabaseId = :cloudDatabaseId")
    void deleteByCloudId(String cloudDatabaseId);

    @Query("DELETE FROM courses")
    void deleteAllCourses();

    @Query("UPDATE courses SET cloudSyncStatus = 1, cloudDatabaseId = :cloudDatabaseId WHERE localDatabaseId = :localDatabaseId")
    void markCourseAsSynced(int localDatabaseId, String cloudDatabaseId);

    // Legacy methods for backward compatibility
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(CourseEntity course);

    @Update
    void update(CourseEntity course);

    @Delete
    void delete(CourseEntity course);

    @Query("SELECT * FROM courses WHERE cloudSyncStatus = 0")
    List<CourseEntity> getUnsyncedCoursesLegacy();

    @Query("DELETE FROM courses WHERE cloudDatabaseId = :firebaseId")
    void deleteByFirebaseId(String firebaseId);

    @Query("UPDATE courses SET cloudSyncStatus = 1, cloudDatabaseId = :firebaseId WHERE localDatabaseId = :localId")
    void markCourseAsSyncedLegacy(int localId, String firebaseId);
}