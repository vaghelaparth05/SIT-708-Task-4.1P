package com.example.eventplanner.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventplanner.MainActivity;
import com.example.eventplanner.R;
import com.example.eventplanner.data.Event;
import com.example.eventplanner.viewmodel.EventViewModel;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.snackbar.Snackbar;

public class EventListFragment extends Fragment {

    private EventViewModel eventViewModel;
    private EventAdapter adapter;
    private RecyclerView recyclerViewEvents;
    private TextView tvEmpty;
    private MaterialSwitch switchDarkMode;

    public EventListFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_event_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerViewEvents = view.findViewById(R.id.recyclerViewEvents);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        switchDarkMode = view.findViewById(R.id.switchDarkMode);

        SharedPreferences preferences = requireActivity().getSharedPreferences(
                MainActivity.PREFS_NAME,
                requireActivity().MODE_PRIVATE
        );

        boolean isDarkMode = preferences.getBoolean(MainActivity.KEY_DARK_MODE, false);
        switchDarkMode.setChecked(isDarkMode);

        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferences.edit().putBoolean(MainActivity.KEY_DARK_MODE, isChecked).apply();

            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });

        eventViewModel = new ViewModelProvider(requireActivity()).get(EventViewModel.class);

        adapter = new EventAdapter(new EventAdapter.OnEventActionListener() {
            @Override
            public void onEdit(Event event) {
                EditEventDialogFragment dialog = EditEventDialogFragment.newInstance(event);
                dialog.show(getParentFragmentManager(), "EditEventDialog");
            }

            @Override
            public void onDelete(Event event) {
                eventViewModel.delete(event);
                Snackbar.make(view, "Event deleted successfully", Snackbar.LENGTH_SHORT).show();
            }
        });

        recyclerViewEvents.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerViewEvents.setAdapter(adapter);

        eventViewModel.getUpcomingEvents().observe(getViewLifecycleOwner(), events -> {
            adapter.setEvents(events);

            if (events != null && !events.isEmpty()) {
                tvEmpty.setVisibility(View.GONE);
            } else {
                tvEmpty.setVisibility(View.VISIBLE);
            }
        });
    }
}