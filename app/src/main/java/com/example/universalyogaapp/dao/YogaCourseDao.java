package com.example.universalyogaapp.dao;

import com.example.universalyogaapp.db.YogaCourseEntity;
import androidx.room.*;
import java.util.List;

@Dao
public interface YogaCourseDao {
    // Basic CRUD operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertCourse(YogaCourseEntity courseEntity);

    @Update
    void updateCourse(YogaCourseEntity courseEntity);

    @Delete
    void deleteCourse(YogaCourseEntity courseEntity);

    // Query operations
    @Query("SELECT * FROM courses WHERE cloudSyncStatus = 0")
    List<YogaCourseEntity> getUnsyncedCourses();

    @Query("SELECT * FROM courses")
    List<YogaCourseEntity> getAllCourses();

    @Query("SELECT * FROM courses WHERE cloudDatabaseId = :cloudDatabaseId")
    YogaCourseEntity getCourseByCloudId(String cloudDatabaseId);

    @Query("SELECT * FROM courses WHERE courseName = :courseName")
    List<YogaCourseEntity> getCoursesByName(String courseName);

    @Query("DELETE FROM courses WHERE cloudDatabaseId = :cloudDatabaseId")
    void deleteByCloudId(String cloudDatabaseId);

    @Query("DELETE FROM courses")
    void deleteAllCourses();

    @Query("UPDATE courses SET cloudSyncStatus = 1, cloudDatabaseId = :cloudDatabaseId WHERE localDatabaseId = :localDatabaseId")
    void markCourseAsSynced(int localDatabaseId, String cloudDatabaseId);

    // Legacy methods for backward compatibility
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(YogaCourseEntity course);

    @Update
    void update(YogaCourseEntity course);

    @Delete
    void delete(YogaCourseEntity course);

    @Query("SELECT * FROM courses WHERE cloudSyncStatus = 0")
    List<YogaCourseEntity> getUnsyncedCoursesLegacy();

    @Query("DELETE FROM courses WHERE cloudDatabaseId = :firebaseId")
    void deleteByFirebaseId(String firebaseId);

    @Query("UPDATE courses SET cloudSyncStatus = 1, cloudDatabaseId = :firebaseId WHERE localDatabaseId = :localId")
    void markCourseAsSyncedLegacy(int localId, String firebaseId);
} 