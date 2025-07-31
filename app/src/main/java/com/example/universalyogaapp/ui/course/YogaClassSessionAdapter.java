package com.example.universalyogaapp.ui.course;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.universalyogaapp.R;
import com.example.universalyogaapp.model.YogaClassSession;
import java.util.List;

public class YogaClassSessionAdapter extends RecyclerView.Adapter<YogaClassSessionAdapter.SessionViewHolder> {
    
    // Data management
    private List<YogaClassSession> sessionDataList;
    private final OnSessionActionListener actionListener;

    // Interface for session actions
    public interface OnSessionActionListener {
        void onEdit(YogaClassSession session);
        void onDelete(YogaClassSession session);
    }

    /**
     * Constructor
     */
    public YogaClassSessionAdapter(List<YogaClassSession> sessionList, OnSessionActionListener listener) {
        this.sessionDataList = sessionList;
        this.actionListener = listener;
    }

    /**
     * Update session data list
     */
    public void updateSessionList(List<YogaClassSession> list) {
        this.sessionDataList = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SessionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_yoga_class_session, parent, false);
        return new SessionViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull SessionViewHolder holder, int position) {
        YogaClassSession currentSession = sessionDataList.get(position);
        holder.bindSessionData(currentSession);
        
        holder.editButton.setOnClickListener(v -> actionListener.onEdit(currentSession));
        holder.deleteButton.setOnClickListener(v -> actionListener.onDelete(currentSession));
    }

    @Override
    public int getItemCount() {
        return sessionDataList != null ? sessionDataList.size() : 0;
    }

    /**
     * ViewHolder class for class session items
     */
    public static class SessionViewHolder extends RecyclerView.ViewHolder {
        // UI Components
        private TextView dateField;
        private TextView teacherField;
        private TextView noteField;
        private Button editButton;
        private Button deleteButton;

        public SessionViewHolder(@NonNull View itemView) {
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
            editButton = itemView.findViewById(R.id.buttonEditSession);
            deleteButton = itemView.findViewById(R.id.buttonDeleteSession);
        }

        /**
         * Bind session data to views
         */
        public void bindSessionData(YogaClassSession session) {
            dateField.setText(session.getDate());
            teacherField.setText("Teacher: " + (session.getTeacher() != null ? session.getTeacher() : ""));
            noteField.setText("Note: " + (session.getNote() != null ? session.getNote() : ""));
        }
    }

    // Legacy methods for backward compatibility
    public void setSessionList(List<YogaClassSession> list) {
        updateSessionList(list);
    }
} 