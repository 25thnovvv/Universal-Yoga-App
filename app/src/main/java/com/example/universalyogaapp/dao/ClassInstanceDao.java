package com.example.universalyogaapp.dao;

import androidx.room.*;
import com.example.universalyogaapp.db.ClassInstanceEntity;
import java.util.List;

@Dao
public interface ClassInstanceDao {
    // Basic CRUD operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertClassInstance(ClassInstanceEntity classInstanceEntity);

    @Update
    void updateClassInstance(ClassInstanceEntity classInstanceEntity);

    @Delete
    void deleteClassInstance(ClassInstanceEntity classInstanceEntity);

    // Query operations
    @Query("SELECT * FROM class_instances WHERE parentCourseId = :parentCourseId")
    List<ClassInstanceEntity> getInstancesForCourse(String parentCourseId);

    @Query("SELECT * FROM class_instances WHERE cloudSyncStatus = 0")
    List<ClassInstanceEntity> getUnsyncedInstances();

    @Query("DELETE FROM class_instances WHERE cloudDatabaseId = :cloudDatabaseId")
    void deleteByCloudId(String cloudDatabaseId);

    @Query("SELECT * FROM class_instances WHERE cloudDatabaseId = :cloudDatabaseId LIMIT 1")
    ClassInstanceEntity getInstanceByCloudId(String cloudDatabaseId);

    @Query("UPDATE class_instances SET cloudSyncStatus = 1, cloudDatabaseId = :cloudDatabaseId WHERE localDatabaseId = :localDatabaseId")
    void markInstanceAsSynced(int localDatabaseId, String cloudDatabaseId);

    @Query("DELETE FROM class_instances")
    void deleteAllInstances();

    // Legacy methods for backward compatibility
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(ClassInstanceEntity instance);

    @Update
    void update(ClassInstanceEntity instance);

    @Delete
    void delete(ClassInstanceEntity instance);

    @Query("SELECT * FROM class_instances WHERE parentCourseId = :courseId")
    List<ClassInstanceEntity> getInstancesForCourseLegacy(String courseId);

    @Query("SELECT * FROM class_instances WHERE cloudSyncStatus = 0")
    List<ClassInstanceEntity> getUnsyncedInstancesLegacy();

    @Query("DELETE FROM class_instances WHERE cloudDatabaseId = :firebaseId")
    void deleteByFirebaseId(String firebaseId);

    @Query("SELECT * FROM class_instances WHERE cloudDatabaseId = :firebaseId LIMIT 1")
    ClassInstanceEntity getInstanceByFirebaseId(String firebaseId);

    @Query("UPDATE class_instances SET cloudSyncStatus = 1, cloudDatabaseId = :firebaseId WHERE localDatabaseId = :id")
    void markInstanceAsSyncedLegacy(int id, String firebaseId);
}