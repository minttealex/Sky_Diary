package com.example.skydiary;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.CalendarView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CalendarNotesFragment extends Fragment implements NotesAdapter.OnItemClickListener {

    private CalendarView calendarView;
    private RecyclerView recyclerNotes;
    private TextView emptyMessage;

    private NotesAdapter adapter;
    private Calendar currentSelectedDate = Calendar.getInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calendar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        calendarView = view.findViewById(R.id.calendar_view);
        recyclerNotes = view.findViewById(R.id.recycler_notes_by_date);
        emptyMessage = view.findViewById(R.id.empty_message);

        FloatingActionButton fabChangeDate = view.findViewById(R.id.fab_change_date);

        adapter = new NotesAdapter(new ArrayList<>(), this);
        recyclerNotes.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerNotes.setAdapter(adapter);

        fabChangeDate.setOnClickListener(v -> {
            Calendar current = Calendar.getInstance();
            current.setTimeInMillis(calendarView.getDate());
            DatePickerDialog picker = new DatePickerDialog(requireContext(), (dp, year, month, day) -> {
                Calendar chosen = Calendar.getInstance();
                chosen.set(year, month, day);
                calendarView.setDate(chosen.getTimeInMillis());
                currentSelectedDate = chosen;
                loadNotesForDate(year, month, day);
            }, current.get(Calendar.YEAR), current.get(Calendar.MONTH), current.get(Calendar.DAY_OF_MONTH));
            picker.show();
        });

        Calendar today = Calendar.getInstance();
        today.setTimeInMillis(calendarView.getDate());
        currentSelectedDate = today;

        calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            Log.d("CalendarNotesFragment", "Date selected: " + year + "-" + (month + 1) + "-" + dayOfMonth);
            Calendar selected = Calendar.getInstance();
            selected.set(year, month, dayOfMonth);
            currentSelectedDate = selected;
            loadNotesForDate(year, month, dayOfMonth);
        });

        // Load initial data
        loadNotesForDate(
                currentSelectedDate.get(Calendar.YEAR),
                currentSelectedDate.get(Calendar.MONTH),
                currentSelectedDate.get(Calendar.DAY_OF_MONTH)
        );
    }

    @Override
    public void onResume() {
        super.onResume();
        if (currentSelectedDate != null) {
            Log.d("CalendarNotesFragment", "Refreshing calendar notes on resume");
            loadNotesForDate(
                    currentSelectedDate.get(Calendar.YEAR),
                    currentSelectedDate.get(Calendar.MONTH),
                    currentSelectedDate.get(Calendar.DAY_OF_MONTH)
            );
        }
    }

    private void loadNotesForDate(int year, int month, int dayOfMonth) {
        Calendar startCal = Calendar.getInstance();
        startCal.clear();
        startCal.set(year, month, dayOfMonth, 0, 0, 0);
        startCal.set(Calendar.MILLISECOND, 0);

        Calendar endCal = (Calendar) startCal.clone();
        endCal.add(Calendar.DATE, 1);

        List<Note> notesForDay = NoteStorage.getInstance(requireContext())
                .getNotesByDate(startCal.getTimeInMillis(), endCal.getTimeInMillis());

        Log.d("CalendarNotesFragment", "Found " + notesForDay.size() + " notes for date " + year + "-" + (month + 1) + "-" + dayOfMonth);

        if (notesForDay.isEmpty()) {
            emptyMessage.setText(getString(R.string.no_notes_added_on_day));
            emptyMessage.setVisibility(View.VISIBLE);
            recyclerNotes.setVisibility(View.GONE);
        } else {
            emptyMessage.setVisibility(View.GONE);
            recyclerNotes.setVisibility(View.VISIBLE);
            adapter.updateNotes(notesForDay);
        }
    }

    @Override
    public void onItemClick(Note note) {
        if (note != null && note.getId() != null) {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, NoteEditorFragment.newInstance(note.getId()))
                    .addToBackStack("calendar_to_detail")
                    .commit();
        } else {
            Log.e("CalendarNotesFragment", "Note or note ID is null");
        }
    }
}
