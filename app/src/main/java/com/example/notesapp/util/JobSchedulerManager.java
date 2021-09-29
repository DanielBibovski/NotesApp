package com.example.notesapp.util;

import android.content.Context;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.notesapp.model.Note;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public final class JobSchedulerManager {

    private JobSchedulerManager() {
    }

    public static void startWorker(Note note, Context context) {

        Data.Builder data = new Data.Builder();
        data.putInt(Constants.NOTE_ID, note.id);
        data.putString(Constants.NOTE_TEXT, note.noteText);

        OneTimeWorkRequest oneTimeWorkRequest = new OneTimeWorkRequest.Builder(ReminderNotificationWorker.class)
                .setInitialDelay(calculateTimeToStartWorker(note.reminder), TimeUnit.MILLISECONDS)
                .addTag(String.valueOf(note.id))
                .setInputData(data.build())
                .build();
        WorkManager.getInstance(context).enqueue(oneTimeWorkRequest);
    }

    private static long calculateTimeToStartWorker(long time) {
        return time - Calendar.getInstance().getTimeInMillis();
    }

    public static void stopWorker(int id, Context context) {
        WorkManager workManager = WorkManager.getInstance(context);
        workManager.cancelAllWorkByTag(String.valueOf(id));
    }
}
