package com.example.universalyogaapp.ui.course;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.universalyogaapp.model.YogaCourse;
import com.example.universalyogaapp.R;
import com.example.universalyogaapp.utils.YogaDateUtils;

import java.util.ArrayList;
import java.util.List;

public class YogaCourseAdapter extends RecyclerView.Adapter<YogaCourseAdapter.CourseViewHolder> {
    
    // Data management
    private List<YogaCourse> courseDataList = new ArrayList<>();
    private OnItemClickListener itemClickListener;

    // Interface for item click events
    public interface OnItemClickListener {
        void onItemClick(YogaCourse course);
    }

    /**
     * Set item click listener
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    /**
     * Update course data list
     */
    public void updateCourseList(List<YogaCourse> courses) {
        this.courseDataList = courses;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_yoga_course, parent, false);
        return new CourseViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        YogaCourse currentCourse = courseDataList.get(position);
        holder.bindCourseData(currentCourse);
        
        holder.itemView.setOnClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(currentCourse);
            }
        });
    }

    @Override
    public int getItemCount() {
        return courseDataList.size();
    }

    /**
     * ViewHolder class for course items
     */
    static class CourseViewHolder extends RecyclerView.ViewHolder {
        // UI Components
        private TextView courseNameField;
        private TextView courseDescriptionField;
        private TextView courseScheduleField;
        private TextView courseTimeField;
        private TextView courseTeacherField;

        public CourseViewHolder(@NonNull View itemView) {
            super(itemView);
            initializeViews(itemView);
        }

        /**
         * Initialize view references
         */
        private void initializeViews(View itemView) {
            courseNameField = itemView.findViewById(R.id.textViewCourseName);
            courseDescriptionField = itemView.findViewById(R.id.textViewCourseDescription);
            courseScheduleField = itemView.findViewById(R.id.textViewCourseSchedule);
            courseTimeField = itemView.findViewById(R.id.textViewCourseTime);
            courseTeacherField = itemView.findViewById(R.id.textViewCourseTeacher);
        }

        /**
         * Bind course data to views
         */
        public void bindCourseData(YogaCourse course) {
            courseNameField.setText(course.getName());
            courseDescriptionField.setText(course.getDescription());
            courseScheduleField.setText(formatScheduleDisplay(course.getSchedule()));
            courseTimeField.setText(course.getTime());
            courseTeacherField.setText(course.getTeacher());
        }

        /**
         * Format schedule for display (abbreviated)
         */
        private String formatScheduleDisplay(String schedule) {
            if (schedule == null || schedule.isEmpty()) {
                return "";
            }

            String[] days = schedule.split(",");
            StringBuilder formattedSchedule = new StringBuilder();
            
            for (String day : days) {
                String trimmedDay = day.trim();
                if (trimmedDay.length() >= 3) {
                    formattedSchedule.append(trimmedDay.substring(0, 3));
                } else {
                    formattedSchedule.append(trimmedDay);
                }
                formattedSchedule.append(", ");
            }
            
            // Remove trailing comma and space
            if (formattedSchedule.length() > 2) {
                formattedSchedule.setLength(formattedSchedule.length() - 2);
            }
            
            return formattedSchedule.toString();
        }
    }

    // Legacy methods for backward compatibility
    public void setCourseList(List<YogaCourse> courses) {
        updateCourseList(courses);
    }
} 