package com.example.universalyogaapp.ui.course;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.universalyogaapp.R;
import com.example.universalyogaapp.model.ClassInstance;
import java.util.List;

public class ClassInstanceAdapter extends RecyclerView.Adapter<ClassInstanceAdapter.ClassInstanceViewHolder> {
    
    // Data management
    private List<ClassInstance> classInstanceDataList;
    private final OnInstanceActionListener actionListener;

    // Interface for instance actions
    public interface OnInstanceActionListener {
        void onEdit(ClassInstance instance);
        void onDelete(ClassInstance instance);
    }

    /**
     * Constructor
     */
    public ClassInstanceAdapter(List<ClassInstance> instanceList, OnInstanceActionListener listener) {
        this.classInstanceDataList = instanceList;
        this.actionListener = listener;
    }

    /**
     * Update instance data list
     */
    public void updateInstanceList(List<ClassInstance> list) {
        this.classInstanceDataList = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ClassInstanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_class_instance, parent, false);
        return new ClassInstanceViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ClassInstanceViewHolder holder, int position) {
        ClassInstance currentInstance = classInstanceDataList.get(position);
        holder.bindInstanceData(currentInstance);
        
        holder.editButton.setOnClickListener(v -> actionListener.onEdit(currentInstance));
        holder.deleteButton.setOnClickListener(v -> actionListener.onDelete(currentInstance));
    }

    @Override
    public int getItemCount() {
        return classInstanceDataList != null ? classInstanceDataList.size() : 0;
    }

    /**
     * ViewHolder class for class instance items
     */
    public static class ClassInstanceViewHolder extends RecyclerView.ViewHolder {
        // UI Components
        private TextView dateField;
        private TextView teacherField;
        private TextView noteField;
        private Button editButton;
        private Button deleteButton;

        public ClassInstanceViewHolder(@NonNull View itemView) {
            super(itemView);
            initializeViews(itemView);
        }

        /**
         * Initialize view references
         */
        private void initializeViews(View itemView) {
            dateField = itemView.findViewById(R.id.textViewDate);
            teacherField = itemView.findViewById(R.id.textViewTeacher);
            noteField = itemView.findViewById(R.id.textViewNote);
            editButton = itemView.findViewById(R.id.buttonEditInstance);
            deleteButton = itemView.findViewById(R.id.buttonDeleteInstance);
        }

        /**
         * Bind instance data to views
         */
        public void bindInstanceData(ClassInstance instance) {
            dateField.setText(instance.getDate());
            teacherField.setText("Teacher: " + (instance.getTeacher() != null ? instance.getTeacher() : ""));
            noteField.setText("Note: " + (instance.getNote() != null ? instance.getNote() : ""));
        }
    }

    // Legacy methods for backward compatibility
    public void setInstanceList(List<ClassInstance> list) {
        updateInstanceList(list);
    }
}