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
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.example.fantazoo.MainActivity;
import com.example.fantazoo.R;
import com.example.fantazoo.model.Zookeeper;
import com.example.fantazoo.model.Zookeeper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ZookeeperFragment extends Fragment {

    private ArrayAdapter<Zookeeper> adapter;
    private List<Zookeeper> zookeepers = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_zookeeper, container, false);
        ListView listView = view.findViewById(R.id.lstZookeepers);
        Button btnAddZookeeper = view.findViewById(R.id.btnAddZookeeper);

        adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, zookeepers);
        listView.setAdapter(adapter);

        btnAddZookeeper.setOnClickListener(v -> showAddZookeeperDialog());

        listView.setOnItemClickListener((adapterView, view1, i, l) -> showUpdateZookeeperDialog(zookeepers.get(i), i));

        listView.setOnItemLongClickListener((adapterView, view12, position, id) -> {
            Zookeeper zookeeper = zookeepers.get(position);
            deleteZookeeper(zookeeper, position);
            return true;
        });

        fetchZookeepers();

        return view;
    }

    // "http://10.0.2.2:8080/api/zookeeper/delete/" + zookeeper.getId()
    private void deleteZookeeper(Zookeeper zookeeper, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Confirm Delete");
        builder.setMessage("Are you sure you want to delete this zookeeper?");
        builder.setPositiveButton("Yes", (dialog, which) -> {
            StringRequest deleteRequest = new StringRequest(Request.Method.DELETE, "http://10.0.2.2:8080/api/zookeeper/delete/" + zookeeper.getId(),
                    response -> {
                        zookeepers.remove(position);
                        adapter.notifyDataSetChanged();
                        Toast.makeText(getActivity(), "Zookeeper deleted successfully", Toast.LENGTH_SHORT).show();
                    },
                    error -> {
                        Toast.makeText(getActivity(), "Failed to delete zookeeper: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    });

            MainActivity.rq.add(deleteRequest);
        });
        builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    // "http://10.0.2.2:8080/api/zookeeper/getall"
    private void fetchZookeepers() {
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                "http://10.0.2.2:8080/api/zookeeper/getall",
                null,
                response -> {
                    Type listType = new TypeToken<List<Zookeeper>>() {
                    }.getType();
                    zookeepers = new Gson().fromJson(response.toString(), listType);
                    adapter.clear();
                    adapter.addAll(zookeepers);
                    adapter.notifyDataSetChanged();
                },
                error -> Toast.makeText(getActivity(), "Error fetching zookeepers: " + error.getMessage(), Toast.LENGTH_SHORT).show()
        );

        MainActivity.rq.add(jsonArrayRequest);
    }

    private void updateZookeeper(Zookeeper zookeeper, int position) {
        String json = new Gson().toJson(zookeeper);

        StringRequest putRequest = new StringRequest(Request.Method.PUT, "http://10.0.2.2:8080/api/zookeeper/update",
                response -> {
                    Zookeeper updatedZookeeper = new Gson().fromJson(response, Zookeeper.class);
                    zookeepers.set(position, updatedZookeeper);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(getActivity(), "Zookeeper updated successfully", Toast.LENGTH_SHORT).show();
                },
                error -> Toast.makeText(getActivity(), "Failed to update zookeeper: " + error.getMessage(), Toast.LENGTH_SHORT).show()) {
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

    private void showUpdateZookeeperDialog(Zookeeper zookeeper, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(String.format("Update Zookeeper (Cage: %s)", zookeeper.getCage().getName()));
        final EditText input = new EditText(getActivity());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(zookeeper.getName());
        builder.setView(input);
        builder.setPositiveButton("Save", (dialog, which) -> {
            String newName = input.getText().toString();
            if (!newName.isEmpty()) {
                zookeeper.setName(newName);
                updateZookeeper(zookeeper, position);
            } else {
                Toast.makeText(getActivity(), "Name cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void showAddZookeeperDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Add New Zookeeper");

        final EditText input = new EditText(getActivity());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint("Enter zookeeper name");
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String name = input.getText().toString();
            if (!name.isEmpty()) {
                createZookeeper(name);
            } else {
                Toast.makeText(getActivity(), "Name cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void createZookeeper(String name) {
        Zookeeper newZookeeper = new Zookeeper();
        newZookeeper.setName(name);
        String json = new Gson().toJson(newZookeeper);

        StringRequest postRequest = new StringRequest(Request.Method.POST, "http://10.0.2.2:8080/api/zookeeper/create",
                response -> {
                    Toast.makeText(getActivity(), "Zookeeper created successfully", Toast.LENGTH_SHORT).show();
                    fetchZookeepers();
                },
                error -> Toast.makeText(getActivity(), "Failed to create zookeeper: " + error.getMessage(), Toast.LENGTH_SHORT).show()) {
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
