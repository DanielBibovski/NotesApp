package com.example.notesapp.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.notesapp.model.Note;
import com.example.notesapp.model.NotesDatabase;
import com.example.notesapp.util.JobSchedulerManager;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class NoteDetailsViewModel extends AndroidViewModel {

    public final MutableLiveData<Note> note;
    public final MutableLiveData<Boolean> error;
    public final MutableLiveData<Boolean> noteDeletedSuccessfully;
    public final MutableLiveData<Long> reminderValue;
    private final CompositeDisposable compositeDisposable;
    private static final String TAG = "NoteDetailsViewModel";

    public NoteDetailsViewModel(@NonNull Application application) {
        super(application);
        note = new MutableLiveData<>();
        error = new MutableLiveData<>();
        noteDeletedSuccessfully = new MutableLiveData<>();
        reminderValue = new MutableLiveData<>();
        compositeDisposable = new CompositeDisposable();
    }

    public void getNote(int noteId) {
        compositeDisposable.add(NotesDatabase.getInstance(getApplication().getApplicationContext())
                .noteDao()
                .getNote(noteId)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                            // getNote returns Single<Note>. We are subscribed to this Single and here we are getting the Note returned from this Single object (from  db)
                            // This lambda is called when we have a successful response.
                            note.setValue(result);
                        }, throwable -> {
                            onError(throwable, "getNote()");
                        }
                ));
    }

    private void updateNote() {
        compositeDisposable.add(updateNote(note.getValue())
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> {
                            error.setValue(false);
                        },
                        throwable -> {
                            onError(throwable, "updateNoteText");
                        }
                ));
    }

    public void updateNoteText(String noteText) {
        note.getValue().noteText = noteText;
        updateNote();
    }


    public void updateImage(String imageUrl) {
        note.getValue().imageUrl = imageUrl;
        updateNote();
    }

    public void updateReminder(long reminder) {
        Note note = this.note.getValue();
        note.reminder = reminder;
        compositeDisposable.add(updateNote(note)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> {
                            reminderValue.setValue(note.reminder);
                            if (note.reminder == 0) {
                                JobSchedulerManager.stopWorker(note.id, getApplication().getApplicationContext());
                            } else {
                                JobSchedulerManager.startWorker(note, getApplication().getApplicationContext());
                            }

                        },
                        throwable -> {
                            onError(throwable, "updateReminder");
                        }
                ));
    }

    private Completable updateNote(Note note) {
        return NotesDatabase.getInstance(getApplication().getApplicationContext())
                .noteDao()
                .update(note);
    }

    public void deleteNote() {
        int noteId = note.getValue().id;
        compositeDisposable.add(NotesDatabase.getInstance(getApplication().getApplicationContext())
                .noteDao()
                .delete(note.getValue())
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                            JobSchedulerManager.stopWorker(noteId, getApplication().getApplicationContext());
                            noteDeletedSuccessfully.setValue(true);
                        },
                        throwable -> {
                            onError(throwable, "deleteNote");
                        }));
    }

    private void onError(Throwable throwable, String message) {
        error.setValue(true);
        Log.d(TAG, message, throwable);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.dispose();
    }
}
