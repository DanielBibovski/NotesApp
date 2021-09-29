package com.example.notesapp.util;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.notesapp.model.Note;
import com.example.notesapp.model.NotesDatabase;

import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ReminderNotificationWorker extends Worker {

    private Disposable disposable;

    public ReminderNotificationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Data data = getInputData();
        int noteId = data.getInt(Constants.NOTE_ID, 0);
        String noteText = data.getString(Constants.NOTE_TEXT);
        NotificationUtil.sendNotification(getApplicationContext(), noteId, noteText);
        disposable = NotesDatabase.getInstance(getApplicationContext())
                .noteDao()
                .getNote(noteId)
                .subscribeOn(Schedulers.newThread())
                .subscribe(note -> {
                            note.reminder = 0;
                            updateNote(note);
                            disposable.dispose();
                        },
                        throwable -> {
                            Log.e("ReminderWorker", "Can't get note with id:" + noteId, throwable);
                            disposable.dispose();
                        });
        return Result.success();
    }

    private void updateNote(Note note) {
        NotesDatabase.getInstance(getApplicationContext())
                .noteDao()
                .update(note)
                .subscribeOn(Schedulers.newThread())
                .subscribe();
    }
}
