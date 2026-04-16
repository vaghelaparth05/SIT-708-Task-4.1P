package com.example.eventplanner.ui;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.eventplanner.R;
import com.example.eventplanner.data.Event;
import com.example.eventplanner.viewmodel.EventViewModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class EditEventDialogFragment extends DialogFragment {

    private static final String ARG_ID = "id";
    private static final String ARG_TITLE = "title";
    private static final String ARG_CATEGORY = "category";
    private static final String ARG_LOCATION = "location";
    private static final String ARG_DATE_TIME = "date_time";

    private EditText etTitle, etLocation;
    private Spinner spinnerCategory;
    private TextView tvSelectedDateTime;
    private Calendar selectedCalendar;

    public static EditEventDialogFragment newInstance(Event event) {
        EditEventDialogFragment fragment = new EditEventDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_ID, event.getId());
        args.putString(ARG_TITLE, event.getTitle());
        args.putString(ARG_CATEGORY, event.getCategory());
        args.putString(ARG_LOCATION, event.getLocation());
        args.putLong(ARG_DATE_TIME, event.getEventDateTime());
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        android.view.View view = inflater.inflate(R.layout.dialog_edit_event, null);

        etTitle = view.findViewById(R.id.etEditTitle);
        etLocation = view.findViewById(R.id.etEditLocation);
        spinnerCategory = view.findViewById(R.id.spinnerEditCategory);
        tvSelectedDateTime = view.findViewById(R.id.tvEditSelectedDateTime);
        Button btnPickDateTime = view.findViewById(R.id.btnEditPickDateTime);

        selectedCalendar = Calendar.getInstance();

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.event_categories,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);

        if (getArguments() != null) {
            int id = getArguments().getInt(ARG_ID);
            String title = getArguments().getString(ARG_TITLE);
            String category = getArguments().getString(ARG_CATEGORY);
            String location = getArguments().getString(ARG_LOCATION);
            long dateTime = getArguments().getLong(ARG_DATE_TIME);

            etTitle.setText(title);
            etLocation.setText(location);
            selectedCalendar.setTimeInMillis(dateTime);

            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
            tvSelectedDateTime.setText(sdf.format(selectedCalendar.getTime()));

            String[] categories = getResources().getStringArray(R.array.event_categories);
            for (int i = 0; i < categories.length; i++) {
                if (categories[i].equals(category)) {
                    spinnerCategory.setSelection(i);
                    break;
                }
            }

            btnPickDateTime.setOnClickListener(v -> showDateTimePicker());

            builder.setView(view)
                    .setTitle("Edit Event")
                    .setPositiveButton("Update", (dialog, which) -> {
                        String updatedTitle = etTitle.getText().toString().trim();
                        String updatedCategory = spinnerCategory.getSelectedItem().toString();
                        String updatedLocation = etLocation.getText().toString().trim();

                        if (TextUtils.isEmpty(updatedTitle)) {
                            Toast.makeText(requireContext(), "Title cannot be empty", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (selectedCalendar.getTimeInMillis() < System.currentTimeMillis()) {
                            Toast.makeText(requireContext(), "Past dates are not allowed", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Event updatedEvent = new Event(updatedTitle, updatedCategory, updatedLocation, selectedCalendar.getTimeInMillis());
                        updatedEvent.setId(id);

                        EventViewModel viewModel = new ViewModelProvider(requireActivity()).get(EventViewModel.class);
                        viewModel.update(updatedEvent);

                        Toast.makeText(requireContext(), "Event updated successfully", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        }

        return builder.create();
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
}