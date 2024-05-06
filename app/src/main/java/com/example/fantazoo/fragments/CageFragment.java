package com.example.fantazoo.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.example.fantazoo.MainActivity;
import com.example.fantazoo.R;
import com.example.fantazoo.model.Animal;
import com.example.fantazoo.model.Cage;
import com.example.fantazoo.model.Zookeeper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CageFragment extends Fragment {

    private ArrayAdapter<Cage> adapter;
    private List<Cage> cages = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cage, container, false);
        ListView listView = view.findViewById(R.id.lstCage);
        Button btnAddCage = view.findViewById(R.id.btnAddCage);

        adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, cages);
        listView.setAdapter(adapter);

        btnAddCage.setOnClickListener(v -> showAddCageDialog());

        listView.setOnItemClickListener((adapterView, view1, i, l) -> showCageDialog(cages.get(i)));

        listView.setOnItemLongClickListener((adapterView, view12, position, id) -> {
            Cage cage = cages.get(position);
            deleteCage(cage, position);
            return true;
        });

        fetchCages();

        return view;
    }

    // "http://10.0.2.2:8080/api/cage/delete/" + cage.getId()
    private void deleteCage(Cage cage, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Confirm Delete");
        builder.setMessage("Are you sure you want to delete this cage?");
        builder.setPositiveButton("Yes", (dialog, which) -> {
            StringRequest deleteRequest = new StringRequest(Request.Method.DELETE, "http://10.0.2.2:8080/api/cage/delete/" + cage.getId(),
                    response -> {
                        cages.remove(position);
                        adapter.notifyDataSetChanged();
                        Toast.makeText(getActivity(), "Cage deleted successfully", Toast.LENGTH_SHORT).show();
                    },
                    error -> {
                        Toast.makeText(getActivity(), "Failed to delete cage: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    });

            MainActivity.rq.add(deleteRequest);
        });
        builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    // "http://10.0.2.2:8080/api/cage/getall"
    private void fetchCages() {
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                "http://10.0.2.2:8080/api/cage/getall",
                null,
                response -> {
                    Type listType = new TypeToken<List<Cage>>() {
                    }.getType();
                    cages = new Gson().fromJson(response.toString(), listType);
                    adapter.clear();
                    adapter.addAll(cages);
                    adapter.notifyDataSetChanged();
                },
                error -> Toast.makeText(getActivity(), "Error fetching cages: " + error.getMessage(), Toast.LENGTH_SHORT).show()
        );

        MainActivity.rq.add(jsonArrayRequest);
    }

    private void updateCage(Cage cage, int position) {
        String json = new Gson().toJson(cage);

        StringRequest putRequest = new StringRequest(Request.Method.PUT, "http://10.0.2.2:8080/api/cage/update",
                response -> {
                    Cage updatedCage = new Gson().fromJson(response, Cage.class);
                    cages.set(position, updatedCage);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(getActivity(), "Cage updated successfully", Toast.LENGTH_SHORT).show();
                },
                error -> Toast.makeText(getActivity(), "Failed to update cage: " + error.getMessage(), Toast.LENGTH_SHORT).show()) {
            @Override
            public byte[] getBody() {
                return json.getBytes(StandardCharsets.UTF_8);
            }

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
        };

        MainActivity.rq.add(putRequest);
    }

    private void showCageDialog(@Nullable Cage cage) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_cage, null);
        builder.setView(dialogView);
        Button addKeeperButton = dialogView.findViewById(R.id.addKeeperButton);
        Button addAnimalButton = dialogView.findViewById(R.id.addAnimalButton);
        addAnimalButton.setOnClickListener(v -> newAnimal(cage, dialogView));
        addKeeperButton.setOnClickListener(v -> newKeeper(cage, dialogView));
        TextView animalView = dialogView.findViewById(R.id.animalView);
        TextView keeperView = dialogView.findViewById(R.id.keeperView);
        animalView.setText(cage.getAnimalString());
        keeperView.setText(cage.getKeeperString());
        EditText editCageName = dialogView.findViewById(R.id.editCageName);
        editCageName.setText(cage.getName());
        // TODO: Jeg skal hente alle animals, zookeepers her - minus valgte
        List<Animal> animals = new ArrayList<>();
        animals.add(new Animal("Tiger"));
        animals.add(new Animal("Lion"));
        animals.add(new Animal("Elephant"));
        List<Zookeeper> zookeepers = new ArrayList<>();
        zookeepers.add(new Zookeeper("Mike"));
        zookeepers.add(new Zookeeper("John"));
        zookeepers.add(new Zookeeper("Joe"));

        Spinner spinnerAnimal = dialogView.findViewById(R.id.spinnerAnimal);
        ArrayAdapter<Animal> adapterAnimal = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, animals);
        adapterAnimal.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAnimal.setAdapter(adapterAnimal);

        Spinner spinnerZookeeper = dialogView.findViewById(R.id.spinnerZookeeper);
        ArrayAdapter<Zookeeper> adapterZookeeper = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, zookeepers);
        adapterZookeeper.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerZookeeper.setAdapter(adapterZookeeper);

        /* builder.setPositiveButton("Save", (dialog, which) -> {
            cage.setName(editCageName.getText() == null ? "" : editCageName.getText().toString());
            cage.getAnimals().add((Animal)spinnerAnimal.getSelectedItem());
            cage.getZookeepers().add((Zookeeper)spinnerZookeeper.getSelectedItem());

            if (cage.getId() != 0) {
                updateCage(cage, adapter.getPosition(cage));
            } else {
                createCage(cage);
            }
        }); */

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showAddCageDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Add New Cage");

        final EditText input = new EditText(getActivity());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint("Enter cage name");
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String name = input.getText().toString();
            if (!name.isEmpty()) {
                createCage(new Cage(name));
            } else {
                Toast.makeText(getActivity(), "Name cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void newAnimal(Cage cage, View dialogView) {
        Spinner spinnerAnimal = dialogView.findViewById(R.id.spinnerAnimal);
        String animal = spinnerAnimal.getSelectedItem().toString();
        cage.getAnimals().add(new Animal(animal));
        updateCage(cage, adapter.getPosition(cage));
    }

    private void newKeeper(Cage cage, View dialogView) {
        Spinner spinnerKeeper = dialogView.findViewById(R.id.spinnerAnimal);
        String zookeeper = spinnerKeeper.getSelectedItem().toString();
        cage.getAnimals().add(new Animal(zookeeper));
        updateCage(cage, adapter.getPosition(cage));
    }

    private void createCage(Cage cage) {
        String json = new Gson().toJson(cage);

        StringRequest postRequest = new StringRequest(Request.Method.POST, "http://10.0.2.2:8080/api/cage/create",
                response -> {
                    Toast.makeText(getActivity(), "Cage created successfully", Toast.LENGTH_SHORT).show();
                    fetchCages();
                },
                error -> Toast.makeText(getActivity(), "Failed to create cage: " + error.getMessage(), Toast.LENGTH_SHORT).show()) {
            @Override
            public byte[] getBody() {
                return json.getBytes(StandardCharsets.UTF_8);
            }

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
        };

        MainActivity.rq.add(postRequest);
    }
}
