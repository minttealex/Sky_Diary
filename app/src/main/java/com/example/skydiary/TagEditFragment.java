package com.example.skydiary;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TagEditFragment extends androidx.fragment.app.DialogFragment {

    public interface TagEditListener {
        void onTagsSelected(List<String> selectedTags);
    }

    private TagEditListener listener;
    private List<String> allTags;
    private List<String> selectedTags;

    public static TagEditFragment newInstance(ArrayList<String> allTags, ArrayList<String> selectedTags) {
        TagEditFragment fragment = new TagEditFragment();
        Bundle args = new Bundle();
        args.putStringArrayList("allTags", allTags);
        args.putStringArrayList("selectedTags", selectedTags);
        fragment.setArguments(args);
        return fragment;
    }

    public void setTagEditListener(TagEditListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public android.app.Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        allTags = getArguments() != null ? getArguments().getStringArrayList("allTags") : new ArrayList<>();
        selectedTags = getArguments() != null ? getArguments().getStringArrayList("selectedTags") : new ArrayList<>();

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(getString(R.string.edit_tags));

        LinearLayout container = new LinearLayout(requireContext());
        container.setOrientation(LinearLayout.VERTICAL);
        int paddingPx = (int) (16 * getResources().getDisplayMetrics().density);
        container.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);

        ImageButton btnAddTag = new ImageButton(requireContext());
        btnAddTag.setImageResource(android.R.drawable.ic_input_add);
        btnAddTag.setBackgroundColor(0x00000000); // transparent background

        // Resolve colorPrimary attribute from current theme
        TypedValue typedValue = new TypedValue();
        requireContext().getTheme().resolveAttribute(
                androidx.appcompat.R.attr.colorPrimary, typedValue, true);
        int colorPrimary = typedValue.data;
        btnAddTag.setColorFilter(colorPrimary);

        int sizePx = (int) (32 * getResources().getDisplayMetrics().density);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(sizePx, sizePx);
        params.gravity = Gravity.END;
        btnAddTag.setLayoutParams(params);
        container.addView(btnAddTag);

        ScrollView scrollView = new ScrollView(requireContext());
        LinearLayout tagsLayout = new LinearLayout(requireContext());
        tagsLayout.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(tagsLayout);
        container.addView(scrollView, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (int) (200 * getResources().getDisplayMetrics().density)));

        android.widget.TextView noTagsText = new android.widget.TextView(requireContext());
        noTagsText.setText(getString(R.string.no_tags_yet));
        noTagsText.setVisibility(allTags.isEmpty() ? View.VISIBLE : View.GONE);
        tagsLayout.addView(noTagsText);

        List<CheckBox> checkBoxes = new ArrayList<>();

        // We'll keep track of all current tags here
        List<String> currentTags = new ArrayList<>(allTags);
        NoteStorage noteStorage = NoteStorage.getInstance(requireContext());

        final Runnable[] refreshTags = new Runnable[1];

        refreshTags[0] = () -> {
            tagsLayout.removeAllViews();
            if (currentTags.isEmpty()) {
                noTagsText.setVisibility(View.VISIBLE);
                tagsLayout.addView(noTagsText);
            } else {
                noTagsText.setVisibility(View.GONE);
                checkBoxes.clear();
                for (int i = 0; i < currentTags.size(); i++) {
                    String tag = currentTags.get(i);
                    CheckBox cb = new CheckBox(requireContext());
                    cb.setText(tag);
                    cb.setChecked(selectedTags.contains(tag));
                    int index = i;
                    cb.setOnLongClickListener(v -> {
                        new AlertDialog.Builder(requireContext())
                                .setTitle(getString(R.string.manage_tag))
                                .setItems(new CharSequence[]{getString(R.string.rename), getString(R.string.delete)}, (dialog, which) -> {
                                    if (which == 0) {
                                        EditText input = new EditText(requireContext());
                                        input.setInputType(InputType.TYPE_CLASS_TEXT);
                                        input.setText(tag);
                                        new AlertDialog.Builder(requireContext())
                                                .setTitle(getString(R.string.rename_tag))
                                                .setView(input)
                                                .setPositiveButton(getString(R.string.rename), (renameDialog, renameWhich) -> {
                                                    String newName = input.getText().toString().trim();
                                                    if (newName.isEmpty()) {
                                                        Toast.makeText(requireContext(), getString(R.string.tag_cannot_be_empty), Toast.LENGTH_SHORT).show();
                                                        return;
                                                    }
                                                    if (currentTags.contains(newName)) {
                                                        Toast.makeText(requireContext(), getString(R.string.tag_already_exists), Toast.LENGTH_SHORT).show();
                                                        return;
                                                    }
                                                    // Rename globally in NoteStorage
                                                    noteStorage.renameTag(tag, newName);
                                                    currentTags.set(index, newName);
                                                    if (selectedTags.contains(tag)) {
                                                        selectedTags.remove(tag);
                                                        selectedTags.add(newName);
                                                    }
                                                    refreshTags[0].run();
                                                })
                                                .setNegativeButton(getString(R.string.cancel), null)
                                                .show();
                                    } else if (which == 1) {
                                        new AlertDialog.Builder(requireContext())
                                                .setTitle(getString(R.string.delete_tag))
                                                .setMessage(getString(R.string.delete_tag_confirmation))
                                                .setPositiveButton(getString(R.string.delete), (delDialog, delWhich) -> {
                                                    noteStorage.deleteTag(tag);
                                                    currentTags.remove(index);
                                                    selectedTags.remove(tag);
                                                    refreshTags[0].run();
                                                })
                                                .setNegativeButton(getString(R.string.cancel), null)
                                                .show();
                                    }
                                })
                                .show();
                        return true;
                    });
                    tagsLayout.addView(cb);
                    checkBoxes.add(cb);
                }
            }
        };

        refreshTags[0].run();

        btnAddTag.setOnClickListener(v -> {
            EditText input = new EditText(requireContext());
            input.setHint(getString(R.string.enter_new_tag));
            new AlertDialog.Builder(requireContext())
                    .setTitle(getString(R.string.add_tag))
                    .setView(input)
                    .setPositiveButton(getString(R.string.add_tag), (dialog, which) -> {
                        String newTag = input.getText().toString().trim();
                        if (newTag.isEmpty()) {
                            Toast.makeText(requireContext(), getString(R.string.tag_cannot_be_empty), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (currentTags.contains(newTag)) {
                            Toast.makeText(requireContext(), getString(R.string.tag_already_exists), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        currentTags.add(newTag);
                        selectedTags.add(newTag);
                        noteStorage.addTag(newTag); // Persist globally
                        refreshTags[0].run();
                    })
                    .setNegativeButton(getString(R.string.cancel), null)
                    .show();
        });

        builder.setView(container);

        builder.setPositiveButton(getString(R.string.save_changes), (dialog, which) -> {
            if (listener != null) {
                listener.onTagsSelected(new ArrayList<>(selectedTags));
            }
        });

        builder.setNegativeButton(getString(R.string.cancel), null);

        return builder.create();
    }
}
