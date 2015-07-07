package com.betomaluje.android.miband;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.betomaluje.android.miband.adapters.ApplicationsAdapter;
import com.betomaluje.android.miband.models.App;
import com.betomaluje.android.miband.sqlite.AppsSQLite;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by betomaluje on 7/6/15.
 */
public class AppsPreferencesActivity extends AppCompatActivity {

    private final String TAG = getClass().getSimpleName();

    private final int APP_DETAIL_CODE = 5211;

    private RecyclerView recycler;
    private LinearLayoutManager lManager;

    private ArrayList<App> apps;
    private ApplicationsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apps);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        if (!AppsSQLite.getInstance(AppsPreferencesActivity.this).doesTableExists()) {
            fillApps();
        }

        apps = AppsSQLite.getInstance(AppsPreferencesActivity.this).getApps();

        adapter = new ApplicationsAdapter(AppsPreferencesActivity.this, apps, itemClickListener);

        recycler = (RecyclerView) findViewById(R.id.recyclerView);
        recycler.setHasFixedSize(true);

        // Usar un administrador para LinearLayout
        lManager = new LinearLayoutManager(this);
        recycler.setLayoutManager(lManager);

        // Crear un nuevo adaptador
        recycler.setAdapter(adapter);
    }

    private View.OnClickListener itemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int position = recycler.getChildAdapterPosition(v);

            thumbNailScaleAnimation(v, apps.get(position), position);
        }
    };

    private void fillApps() {
        final PackageManager pm = getPackageManager();
        //get a list of installed apps.
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        AppsSQLite appsSQLite = AppsSQLite.getInstance(AppsPreferencesActivity.this);

        for (ApplicationInfo packageInfo : packages) {
            String name = pm.getApplicationLabel(packageInfo).toString();
            appsSQLite.saveApp(name, packageInfo.packageName, -524538, false, 500);
        }
    }

    private void thumbNailScaleAnimation(View view, App app, int position) {
        view.setDrawingCacheEnabled(true);
        view.setPressed(false);
        view.refreshDrawableState();
        Bitmap bitmap = view.getDrawingCache();
        ActivityOptionsCompat opts = ActivityOptionsCompat.makeThumbnailScaleUpAnimation(
                view, bitmap, 0, 0);
        // Request the activity be started, using the custom animation options.
        Intent intent = new Intent(AppsPreferencesActivity.this, AppDetailActivity.class);
        Bundle b = new Bundle();
        b.putParcelable(AppDetailActivity.extra, app);
        b.putInt(AppDetailActivity.extra_position, position);

        intent.putExtras(b);

        startActivityForResult(intent, APP_DETAIL_CODE, opts.toBundle());

        view.setDrawingCacheEnabled(false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == APP_DETAIL_CODE && resultCode == RESULT_OK) {

            Bundle b = data.getExtras();

            if (b != null) {
                App returned = b.getParcelable(AppDetailActivity.extra_returned);

                if (returned != null) {
                    App previous = apps.get(b.getInt(AppDetailActivity.extra_position, 0));
                    previous.setNotify(returned.isNotify());
                    previous.setColor(returned.getColor());
                    previous.setStartTime(returned.getStartTime());
                    previous.setEndTime(returned.getEndTime());

                    adapter.notifyDataSetChanged();
                }
            }
        }
    }
}
