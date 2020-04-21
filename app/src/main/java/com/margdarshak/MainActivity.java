package com.margdarshak;

import android.os.Bundle;
import android.os.StrictMode;
import android.view.Gravity;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.navigation.NavigationView;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;
import com.margdarshak.util.CircleTransformation;
import com.squareup.picasso.Picasso;

import java.util.List;

public class MainActivity extends AppCompatActivity implements ActivityPermissionListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private AppBarConfiguration mAppBarConfiguration;
    private PermissionsManager permissionsManager;
    private LocationPermissionCallback locationPermissionCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Context init
        Mapbox.getInstance(getApplicationContext(), getString(R.string.mapbox_access_token));
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow,
                R.id.nav_tools, R.id.nav_share, R.id.nav_send, R.id.navigation_header_container)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        permissionsManager = new PermissionsManager(this);
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if(account !=  null) {
            updateUiWithUser(account);
        }
        navigationView.getHeaderView(0).setClickable(true);
        navigationView.getHeaderView(0).setOnClickListener(v -> {
            //loadLoginFragment(new LoginFragment());
            int navigateOnHeader;
            GoogleSignInAccount current_account = GoogleSignIn.getLastSignedInAccount(this);
            if(current_account != null) {
                navigateOnHeader = R.id.nav_profile;

                // Modify login screen
                // TODO

            } else {
                Toast.makeText(getApplicationContext(), "Not signed in", Toast.LENGTH_SHORT).show();
                navigateOnHeader = R.id.navigation_login;
            }
            navController.navigate(navigateOnHeader);
            drawer.closeDrawer(Gravity.LEFT);
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(this, R.string.user_location_permission_explanation, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            locationPermissionCallback.onGrant();
        } else {
            Toast.makeText(this, R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show();
            locationPermissionCallback.onDenial();
        }
    }

    @Override
    public void requestLocationPermission(LocationPermissionCallback locationPermissionCallback) {
        this.locationPermissionCallback = locationPermissionCallback;
        permissionsManager.requestLocationPermissions(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    public void updateUiWithUser(GoogleSignInAccount account) {
        String welcome = getString(R.string.welcome, account.getDisplayName());
        Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
        NavigationView navigationView = findViewById(R.id.nav_view);
        ((TextView) navigationView.getHeaderView(0).findViewById(R.id.username)).setText(account.getDisplayName());
        ((TextView) navigationView.getHeaderView(0).findViewById(R.id.usermail)).setText(account.getEmail());
        if(account.getPhotoUrl() != null) {
            Picasso.get().load(account.getPhotoUrl()).transform(new CircleTransformation()).into((ImageView) navigationView.getHeaderView(0).findViewById(R.id.userpicture));
        }
    }
}


