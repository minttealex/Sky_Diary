package com.example.skydiary;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.CalendarView;
import android.widget.ImageButton;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CalendarNotesFragment extends Fragment implements NotesAdapter.OnItemClickListener {

    private CalendarView calendarView;
    private TextView textMonthYear;
    private ImageButton buttonChangeDate;
    private RecyclerView recyclerNotes;
    private TextView emptyMessage;

    private NotesAdapter adapter;

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
        textMonthYear = view.findViewById(R.id.text_month_year);
        recyclerNotes = view.findViewById(R.id.recycler_notes_by_date);
        emptyMessage = view.findViewById(R.id.empty_message);

        FloatingActionButton fabChangeDate = view.findViewById(R.id.fab_change_date);

        fabChangeDate.setOnClickListener(v -> {
            Calendar current = Calendar.getInstance();
            current.setTimeInMillis(calendarView.getDate());
            DatePickerDialog picker = new DatePickerDialog(requireContext(), (dp, year, month, day) -> {
                Calendar chosen = Calendar.getInstance();
                chosen.set(year, month, day);
                calendarView.setDate(chosen.getTimeInMillis());
                updateMonthYearText(chosen);
                loadNotesForDate(year, month, day);
            }, current.get(Calendar.YEAR), current.get(Calendar.MONTH), current.get(Calendar.DAY_OF_MONTH));
            picker.show();
        });

        recyclerNotes.setLayoutManager(new LinearLayoutManager(requireContext()));

        Calendar today = Calendar.getInstance();
        today.setTimeInMillis(calendarView.getDate());
        updateMonthYearText(today);

        // When calendar date changes update list and month/year text display
        calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            Log.d("CalendarNotesFragment", "Date selected: " + year + "-" + (month + 1) + "-" + dayOfMonth);
            Calendar selected = Calendar.getInstance();
            selected.set(year, month, dayOfMonth);
            updateMonthYearText(selected);
            loadNotesForDate(year, month, dayOfMonth);
        });


        // Initial UI state
        emptyMessage.setText(getString(R.string.choose_date_to_see_notes));
        emptyMessage.setVisibility(View.VISIBLE);
        recyclerNotes.setVisibility(View.GONE);
    }

    private void updateMonthYearText(Calendar cal) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        textMonthYear.setText(sdf.format(cal.getTime()));
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

        if (notesForDay.isEmpty()) {
            emptyMessage.setText(getString(R.string.no_notes_added_on_day));
            emptyMessage.setVisibility(View.VISIBLE);
            recyclerNotes.setVisibility(View.GONE);
        } else {
            emptyMessage.setVisibility(View.GONE);
            recyclerNotes.setVisibility(View.VISIBLE);

            if (adapter == null) {
                adapter = new NotesAdapter(notesForDay, this);
                recyclerNotes.setAdapter(adapter);
            } else {
                adapter.updateNotes(notesForDay);
            }
        }
    }

    @Override
    public void onItemClick(Note note) {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, NoteDetailFragment.newInstance(note.getTimestamp()))
                .addToBackStack(null)
                .commit();
    }
}



