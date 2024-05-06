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
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.example.fantazoo.MainActivity;
import com.example.fantazoo.R;
import com.example.fantazoo.model.Animal;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class AnimalFragment extends Fragment {

    private ArrayAdapter<Animal> adapter;
    private List<Animal> animals = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_animal, container, false);
        ListView listView = view.findViewById(R.id.lstAnimals);
        Button btnAddAnimal = view.findViewById(R.id.btnAddAnimal);

        adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, animals);
        listView.setAdapter(adapter);

        btnAddAnimal.setOnClickListener(v -> showAddAnimalDialog());

        listView.setOnItemClickListener((adapterView, view1, i, l) -> showUpdateAnimalDialog(animals.get(i), i));

        listView.setOnItemLongClickListener((adapterView, view12, position, id) -> {
            Animal animal = animals.get(position);
            deleteAnimal(animal, position);
            return true;
        });

        fetchAnimals();

        return view;
    }

    // "http://10.0.2.2:8080/api/animal/delete/" + animal.getId()
    private void deleteAnimal(Animal animal, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Confirm Delete");
        builder.setMessage("Are you sure you want to delete this animal?");
        builder.setPositiveButton("Yes", (dialog, which) -> {
            StringRequest deleteRequest = new StringRequest(Request.Method.DELETE, "http://10.0.2.2:8080/api/animal/delete/" + animal.getId(),
                    response -> {
                        animals.remove(position);
                        adapter.notifyDataSetChanged();
                        Toast.makeText(getActivity(), "Animal deleted successfully", Toast.LENGTH_SHORT).show();
                    },
                    error -> {
                        Toast.makeText(getActivity(), "Failed to delete animal: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    });

            MainActivity.rq.add(deleteRequest);
        });
        builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    // "http://10.0.2.2:8080/api/animal/getall"
    private void fetchAnimals() {
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                "http://10.0.2.2:8080/api/animal/getall",
                null,
                response -> {
                    Type listType = new TypeToken<List<Animal>>() {
                    }.getType();
                    animals = new Gson().fromJson(response.toString(), listType);
                    adapter.clear();
                    adapter.addAll(animals);
                    adapter.notifyDataSetChanged();
                },
                error -> Toast.makeText(getActivity(), "Error fetching animals: " + error.getMessage(), Toast.LENGTH_SHORT).show()
        );

        MainActivity.rq.add(jsonArrayRequest);
    }

    private void updateAnimal(Animal animal, int position) {
        String json = new Gson().toJson(animal);

        StringRequest putRequest = new StringRequest(Request.Method.PUT, "http://10.0.2.2:8080/api/animal/update",
                response -> {
                    Animal updatedAnimal = new Gson().fromJson(response, Animal.class);
                    animals.set(position, updatedAnimal);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(getActivity(), "Animal updated successfully", Toast.LENGTH_SHORT).show();
                },
                error -> Toast.makeText(getActivity(), "Failed to update animal: " + error.getMessage(), Toast.LENGTH_SHORT).show()) {
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

    private void showUpdateAnimalDialog(Animal animal, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(String.format("Update Animal (Cage: %s)", animal.getCage().getName()));
        final EditText input = new EditText(getActivity());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(animal.getName());
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newName = input.getText().toString();
            if (!newName.isEmpty()) {
                animal.setName(newName);
                updateAnimal(animal, position);
            } else {
                Toast.makeText(getActivity(), "Name cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void showAddAnimalDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Add New Animal");

        final EditText input = new EditText(getActivity());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint("Enter animal name");
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String name = input.getText().toString();
            if (!name.isEmpty()) {
                createAnimal(name);
            } else {
                Toast.makeText(getActivity(), "Name cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void createAnimal(String name) {
        Animal newAnimal = new Animal();
        newAnimal.setName(name);
        String json = new Gson().toJson(newAnimal);

        StringRequest postRequest = new StringRequest(Request.Method.POST, "http://10.0.2.2:8080/api/animal/create",
                response -> {
                    Toast.makeText(getActivity(), "Animal created successfully", Toast.LENGTH_SHORT).show();
                    fetchAnimals();
                },
                error -> Toast.makeText(getActivity(), "Failed to create animal: " + error.getMessage(), Toast.LENGTH_SHORT).show()) {
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
