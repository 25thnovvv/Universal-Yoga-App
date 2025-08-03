package com.example.universalyogaapp.dao;

import androidx.room.*;
import com.example.universalyogaapp.db.YogaClassSessionEntity;
import java.util.List;

@Dao
public interface YogaClassSessionDao {
    // Basic CRUD operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertClassSession(YogaClassSessionEntity classSessionEntity);

    @Update
    void updateClassSession(YogaClassSessionEntity classSessionEntity);

    @Delete
    void deleteClassSession(YogaClassSessionEntity classSessionEntity);

    // Query operations
    @Query("SELECT * FROM class_instances WHERE parentCourseId = :parentCourseId")
    List<YogaClassSessionEntity> getSessionsForCourse(String parentCourseId);

    @Query("SELECT * FROM class_instances WHERE cloudSyncStatus = 0")
    List<YogaClassSessionEntity> getUnsyncedSessions();

    @Query("DELETE FROM class_instances WHERE cloudDatabaseId = :cloudDatabaseId")
    void deleteByCloudId(String cloudDatabaseId);

    @Query("SELECT * FROM class_instances WHERE cloudDatabaseId = :cloudDatabaseId LIMIT 1")
    YogaClassSessionEntity getSessionByCloudId(String cloudDatabaseId);

    @Query("UPDATE class_instances SET cloudSyncStatus = 1, cloudDatabaseId = :cloudDatabaseId WHERE localDatabaseId = :localDatabaseId")
    void markSessionAsSynced(int localDatabaseId, String cloudDatabaseId);

    @Query("DELETE FROM class_instances")
    void deleteAllSessions();

    // Legacy methods for backward compatibility
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(YogaClassSessionEntity session);

    @Update
    void update(YogaClassSessionEntity session);

    @Delete
    void delete(YogaClassSessionEntity session);

    @Query("SELECT * FROM class_instances WHERE parentCourseId = :courseId")
    List<YogaClassSessionEntity> getSessionsForCourseLegacy(String courseId);

    @Query("SELECT * FROM class_instances WHERE cloudSyncStatus = 0")
    List<YogaClassSessionEntity> getUnsyncedSessionsLegacy();

    @Query("DELETE FROM class_instances WHERE cloudDatabaseId = :firebaseId")
    void deleteByFirebaseId(String firebaseId);

    @Query("SELECT * FROM class_instances WHERE cloudDatabaseId = :firebaseId LIMIT 1")
    YogaClassSessionEntity getSessionByFirebaseId(String firebaseId);

    @Query("UPDATE class_instances SET cloudSyncStatus = 1, cloudDatabaseId = :firebaseId WHERE localDatabaseId = :id")
    void markSessionAsSyncedLegacy(int id, String firebaseId);
} 