package com.example.notesapp.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.notesapp.R;
import com.example.notesapp.databinding.FragmentNotesBinding;
import com.example.notesapp.viewmodel.NotesViewModel;

public class NotesFragment extends Fragment {

    private FragmentNotesBinding binding;
    private NotesAdapter adapter;
    private NotesViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentNotesBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);
        viewModel = new ViewModelProvider(this).get(NotesViewModel.class);
        setupUi();
        setupObservers();
    }

    private void setupUi() {
        adapter = new NotesAdapter();
        binding.recyclerView.setAdapter(adapter);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        binding.retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.fetchNotes();
            }
        });
        viewModel.fetchNotes();

        binding.search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    viewModel.fetchNotes();
                } else {
                    viewModel.search(newText);
                }
                return false;
            }
        });
    }

    private void setupObservers() {
        viewModel.notes.observe(getViewLifecycleOwner(), listOfNotes -> {
            adapter.addAll(listOfNotes);
            binding.numberOfNotes.setText(getString(R.string.number_of_notes, listOfNotes.size()));
        });

        viewModel.error.observe(getViewLifecycleOwner(), error -> {
            if (error) {
                binding.errorContainer.setVisibility(View.VISIBLE);
            } else {
                binding.errorContainer.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.notes_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.addNote) {
            NavDirections directions = NotesFragmentDirections.actionAddNoteFragment();
            Navigation.findNavController(getView()).navigate(directions);
        }
        return super.onOptionsItemSelected(item);
    }
}