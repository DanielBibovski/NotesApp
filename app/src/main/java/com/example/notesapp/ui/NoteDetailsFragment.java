package com.example.notesapp.ui;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.notesapp.R;
import com.example.notesapp.databinding.FragmentNoteDetailsBinding;
import com.example.notesapp.model.Note;
import com.example.notesapp.util.DateTimeUtil;
import com.example.notesapp.util.PermissionUtil;
import com.example.notesapp.viewmodel.NoteDetailsViewModel;

import java.util.Calendar;

public class NoteDetailsFragment extends Fragment implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    private int noteId;
    private FragmentNoteDetailsBinding binding;
    private NoteDetailsViewModel viewModel;
    private Calendar selectedDate;
    private static final int REQUEST_CODE_STORAGE_PERMISSION = 1;
    private static final int REQUEST_CODE_SELECT_IMAGE = 2;
    private static final int REQUEST_CODE_STORAGE_PERMISSION_TO_DISPLAY_IMAGE = 3;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentNoteDetailsBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);
        viewModel = new ViewModelProvider(this).get(NoteDetailsViewModel.class);
        noteId = NoteDetailsFragmentArgs.fromBundle(getArguments()).getNoteId();
        setupObservers();
        viewModel.getNote(noteId);
    }

    private void setupObservers() {
        viewModel.note.observe(getViewLifecycleOwner(), note -> {
            if (note != null) {
                binding.note.setText(note.noteText);
                showHideReminder(note.reminder);
                if (PermissionUtil.checkReadExternalStoragePermission(getContext())) {
                    displayImage(note.imageUrl);
                } else {
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_STORAGE_PERMISSION_TO_DISPLAY_IMAGE);
                }
                getActivity().invalidateOptionsMenu();
            } else {
                showErrorMessage();
            }
        });

        viewModel.error.observe(getViewLifecycleOwner(), error -> {
            if (error) {
                showErrorMessage();
            } else {
                displayImage(viewModel.note.getValue().imageUrl);
                getActivity().invalidateOptionsMenu();
            }
        });

        viewModel.noteDeletedSuccessfully.observe(getViewLifecycleOwner(), noteDeletedSuccessfully -> {
            if (noteDeletedSuccessfully) {
                Navigation.findNavController(getView()).navigateUp();
            }
        });

        viewModel.reminderValue.observe(getViewLifecycleOwner(), reminderValue -> {
            showHideReminder(reminderValue);
            getActivity().invalidateOptionsMenu();
        });
    }

    private void showErrorMessage() {
        Toast.makeText(getContext(), getString(R.string.generic_error), Toast.LENGTH_SHORT).show();
    }

    private void showHideReminder(long reminder) {
        if (reminder == 0) {
            binding.reminder.setVisibility(View.GONE);
        } else {
            binding.reminder.setVisibility(View.VISIBLE);
            binding.reminder.setText(DateTimeUtil.format(reminder));
        }
    }

    private void displayDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), R.style.Theme_AppCompat_Light_Dialog, this, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void displayTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), R.style.Theme_AppCompat_Light_Dialog, this, 0, 0, true);
        timePickerDialog.show();
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE);
        }
    }

    private void displayImage(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            binding.image.setVisibility(View.GONE);
        } else {
            binding.image.setVisibility(View.VISIBLE);
            binding.image.setImageBitmap(BitmapFactory.decodeFile(filePath));
        }
    }

    private String getPathFromUri(Uri uri) {
        String filePath;
        Cursor cursor = getContext().getApplicationContext().getContentResolver().query(uri, null, null, null, null);
        if (cursor == null) {
            filePath = uri.getPath();
        } else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex("_data");
            filePath = cursor.getString(index);
            cursor.close();
        }
        return filePath;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.note_details_menu, menu);
        Note note = viewModel.note.getValue();
        if (note != null) {
            if (!binding.note.isEnabled()) {
                menu.findItem(R.id.edit).setVisible(true);
                menu.findItem(R.id.done).setVisible(false);
            } else {
                menu.findItem(R.id.edit).setVisible(false);
                menu.findItem(R.id.done).setVisible(true);
            }

            menu.findItem(R.id.delete).setVisible(true);
            if (note.reminder == 0) {
                menu.findItem(R.id.addReminder).setVisible(true);
                menu.findItem(R.id.removeReminder).setVisible(false);
            } else {
                menu.findItem(R.id.addReminder).setVisible(false);
                menu.findItem(R.id.removeReminder).setVisible(true);
            }

            if (TextUtils.isEmpty(note.imageUrl)) {
                menu.findItem(R.id.addImage).setVisible(true);
                menu.findItem(R.id.removeImage).setVisible(false);
            } else {
                menu.findItem(R.id.addImage).setVisible(false);
                menu.findItem(R.id.removeImage).setVisible(true);
            }

        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.edit) {
            binding.note.setEnabled(true);
            getActivity().invalidateOptionsMenu();
        } else if (item.getItemId() == R.id.done) {
            binding.note.setEnabled(false);
            viewModel.updateNoteText(binding.note.getText().toString());
            getActivity().invalidateOptionsMenu();
        } else if (item.getItemId() == R.id.delete) {
            viewModel.deleteNote();
        } else if (item.getItemId() == R.id.addReminder) {
            displayDatePicker();
        } else if (item.getItemId() == R.id.removeReminder) {
            viewModel.updateReminder(0);
        } else if (item.getItemId() == R.id.addImage) {
            if (PermissionUtil.checkReadExternalStoragePermission(getContext())) {
                selectImage();
            } else {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_STORAGE_PERMISSION);
            }
        } else if (item.getItemId() == R.id.removeImage) {
            viewModel.updateImage("");
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectImage();
            } else {
                Toast.makeText(getContext(), getString(R.string.permission_denied), Toast.LENGTH_SHORT).show();
            }
        } else if(requestCode == REQUEST_CODE_STORAGE_PERMISSION_TO_DISPLAY_IMAGE && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                displayImage(viewModel.note.getValue().imageUrl);
            } else {
                Toast.makeText(getContext(), getString(R.string.permission_denied), Toast.LENGTH_SHORT).show();
                displayImage("");
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null) {
                    String filePath = getPathFromUri(selectedImageUri);
                    viewModel.updateImage(filePath);
                }
            }
        }
    }


    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        selectedDate = Calendar.getInstance();
        selectedDate.set(year, month, dayOfMonth);
        displayTimePicker();
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        selectedDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
        selectedDate.set(Calendar.MINUTE, minute);
        if (Calendar.getInstance().getTimeInMillis() > selectedDate.getTimeInMillis()) {
            Toast.makeText(getContext(), getString(R.string.invalid_date_message), Toast.LENGTH_SHORT).show();
        } else {
            viewModel.updateReminder(selectedDate.getTimeInMillis());
        }
    }
}