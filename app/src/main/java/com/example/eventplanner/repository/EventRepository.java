package com.example.eventplanner.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.eventplanner.data.AppDatabase;
import com.example.eventplanner.data.Event;
import com.example.eventplanner.data.EventDao;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EventRepository {

    private final EventDao eventDao;
    private final LiveData<List<Event>> upcomingEvents;
    private final ExecutorService executorService;

    public EventRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        eventDao = db.eventDao();
        upcomingEvents = eventDao.getUpcomingEvents(System.currentTimeMillis());
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<Event>> getUpcomingEvents() {
        return upcomingEvents;
    }

    public void insert(Event event) {
        executorService.execute(() -> eventDao.insert(event));
    }

    public void update(Event event) {
        executorService.execute(() -> eventDao.update(event));
    }

    public void delete(Event event) {
        executorService.execute(() -> eventDao.delete(event));
    }
}