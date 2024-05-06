package com.example.fantazoo;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.example.fantazoo.fragments.AnimalFragment;
import com.example.fantazoo.fragments.CageFragment;
import com.example.fantazoo.fragments.ZookeeperFragment;

public class MainActivity extends AppCompatActivity {

    public static RequestQueue rq;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        rq = Volley.newRequestQueue(getApplicationContext());

        initGui();
        fragmentChanger(CageFragment.class);
    }

    void initGui() {
        findViewById(R.id.nav_cage).setOnClickListener(view -> fragmentChanger(CageFragment.class));
        findViewById(R.id.nav_animal).setOnClickListener(view -> fragmentChanger(AnimalFragment.class));
        findViewById(R.id.nav_zookeeper).setOnClickListener(view -> fragmentChanger(ZookeeperFragment.class));
    }

    private void fragmentChanger(Class c) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, c, null)
                .setReorderingAllowed(true)
                .addToBackStack(null)
                .commit();
    }
}