package com.example.universalyogaapp.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.universalyogaapp.dao.YogaCourseDao;
import com.example.universalyogaapp.dao.YogaClassSessionDao;
import com.example.universalyogaapp.db.YogaClassSessionEntity;

@Database(entities = {YogaCourseEntity.class, YogaClassSessionEntity.class}, version = 5)
public abstract class YogaAppDatabase extends RoomDatabase {
    public abstract YogaCourseDao courseDao();
    public abstract YogaClassSessionDao classSessionDao();
} 