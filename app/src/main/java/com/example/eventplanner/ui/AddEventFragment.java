package com.example.eventplanner.ui;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.eventplanner.R;
import com.example.eventplanner.data.Event;
import com.example.eventplanner.viewmodel.EventViewModel;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddEventFragment extends Fragment {

    private EditText etTitle, etLocation;
    private Spinner spinnerCategory;
    private TextView tvSelectedDateTime;
    private Button btnPickDateTime, btnSaveEvent;

    private EventViewModel eventViewModel;
    private Calendar selectedCalendar;

    public AddEventFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_event, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etTitle = view.findViewById(R.id.etTitle);
        etLocation = view.findViewById(R.id.etLocation);
        spinnerCategory = view.findViewById(R.id.spinnerCategory);
        tvSelectedDateTime = view.findViewById(R.id.tvSelectedDateTime);
        btnPickDateTime = view.findViewById(R.id.btnPickDateTime);
        btnSaveEvent = view.findViewById(R.id.btnSaveEvent);

        eventViewModel = new ViewModelProvider(requireActivity()).get(EventViewModel.class);
        selectedCalendar = Calendar.getInstance();

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.event_categories,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);

        btnPickDateTime.setOnClickListener(v -> showDateTimePicker());

        btnSaveEvent.setOnClickListener(v -> saveEvent(view));
    }

    private void showDateTimePicker() {
        Calendar now = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (datePicker, year, month, dayOfMonth) -> {
                    selectedCalendar.set(Calendar.YEAR, year);
                    selectedCalendar.set(Calendar.MONTH, month);
                    selectedCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    TimePickerDialog timePickerDialog = new TimePickerDialog(
                            requireContext(),
                            (timePicker, hourOfDay, minute) -> {
                                selectedCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                selectedCalendar.set(Calendar.MINUTE, minute);
                                selectedCalendar.set(Calendar.SECOND, 0);
                                selectedCalendar.set(Calendar.MILLISECOND, 0);

                                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
                                tvSelectedDateTime.setText(sdf.format(selectedCalendar.getTime()));
                            },
                            now.get(Calendar.HOUR_OF_DAY),
                            now.get(Calendar.MINUTE),
                            false
                    );
                    timePickerDialog.show();
                },
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    private void saveEvent(View view) {
        String title = etTitle.getText().toString().trim();
        String category = spinnerCategory.getSelectedItem().toString();
        String location = etLocation.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            Snackbar.make(view, "Title cannot be empty", Snackbar.LENGTH_SHORT).show();
            return;
        }

        if (tvSelectedDateTime.getText().toString().equals("No date selected")) {
            Snackbar.make(view, "Please select a date and time", Snackbar.LENGTH_SHORT).show();
            return;
        }

        if (selectedCalendar.getTimeInMillis() < System.currentTimeMillis()) {
            Snackbar.make(view, "Past dates are not allowed", Snackbar.LENGTH_SHORT).show();
            return;
        }

        Event event = new Event(title, category, location, selectedCalendar.getTimeInMillis());
        eventViewModel.insert(event);

        etTitle.setText("");
        etLocation.setText("");
        spinnerCategory.setSelection(0);
        tvSelectedDateTime.setText("No date selected");
        selectedCalendar = Calendar.getInstance();

        Toast.makeText(requireContext(), "Event added successfully", Toast.LENGTH_SHORT).show();
    }
}