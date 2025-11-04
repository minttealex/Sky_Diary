package com.example.skydiary;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Note note);
    }

    private List<Note> notes;
    private final OnItemClickListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

    public NotesAdapter(List<Note> notes, OnItemClickListener listener) {
        this.notes = notes != null ? notes : new ArrayList<>();
        this.listener = listener;
    }

    public void updateNotes(List<Note> newNotes) {
        this.notes = newNotes != null ? new ArrayList<>(newNotes) : new ArrayList<>();

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = notes.get(position);
        holder.noteName.setText(note.getName());
        holder.noteDate.setText(dateFormat.format(note.getTimestamp()));

        if (note.getImages() != null && !note.getImages().isEmpty()) {
            holder.imageIndicator.setVisibility(View.VISIBLE);
            holder.imageCount.setText(String.valueOf(note.getImages().size()));
        } else {
            holder.imageIndicator.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(note);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView noteName;
        TextView noteDate;
        LinearLayout imageIndicator;
        TextView imageCount;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            noteName = itemView.findViewById(R.id.text_note_name);
            noteDate = itemView.findViewById(R.id.text_note_date);
            imageIndicator = itemView.findViewById(R.id.image_indicator);
            imageCount = itemView.findViewById(R.id.text_image_count);
        }
    }

    public void updateList(List<Note> newNotes) {
        this.notes.clear();
        if (newNotes != null) {
            this.notes.addAll(newNotes);
        }
        notifyDataSetChanged();
    }
}

