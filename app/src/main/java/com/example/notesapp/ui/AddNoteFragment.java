package com.example.notesapp.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.notesapp.R;
import com.example.notesapp.databinding.FragmentAddNoteBinding;
import com.example.notesapp.viewmodel.AddNoteViewModel;


public class AddNoteFragment extends Fragment {

    private FragmentAddNoteBinding binding;
    private AddNoteViewModel viewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentAddNoteBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);
        viewModel = new ViewModelProvider(this).get(AddNoteViewModel.class);
        setupObservers();
    }

    private void setupObservers() {
        viewModel.success.observe(getViewLifecycleOwner(), success -> {
            if (success) {
                Navigation.findNavController(getView()).navigateUp();
            } else {
                Toast.makeText(getContext(), getString(R.string.generic_error), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveNote() {
        String noteText = binding.note.getText().toString();
        if (!noteText.isEmpty()) {
            viewModel.addNote(noteText);
        } else {
            Navigation.findNavController(getView()).navigateUp();
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.add_note_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.done) {
            saveNote();
        }
        return super.onOptionsItemSelected(item);
    }
}