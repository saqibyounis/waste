package com.app.thestream.blacklist.adatptor;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.app.thestream.R;

import com.app.thestream.activities.MyApplication;
import com.app.thestream.blacklist.model.BlockedAppModel;
import com.app.thestream.blacklist.util.BlackListSelectionCallback;

import java.util.List;

public class BlackListAppsAdapter extends RecyclerView.Adapter<BlackListAppsAdapter.ViewHolder> {

    private List<BlockedAppModel> models;
    private BlackListSelectionCallback callback;

    public BlackListAppsAdapter(List<BlockedAppModel> models, BlackListSelectionCallback callback) {
        this.models = models;
        this.callback = callback;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.app_list_item, parent, false);
        return new ViewHolder(v);
    }


    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        int pos = holder.getAdapterPosition();
        BlockedAppModel appModel = models.get(pos);
        prepareModel(appModel);
        holder.tv_app_name.setText(appModel.getLable());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            holder.iv_app_icon.setBackground(appModel.getAppIcon());
        }
        holder.iv_delete.setTag(pos);
        holder.iv_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = (int) v.getTag();
                if (pos < models.size()) {
                    deleteItem(pos);
                }
            }
        });
    }

    private void prepareModel(BlockedAppModel appModel) {
        appModel.setAppIcon(getAppIconByPackageName(appModel.getPkgName()));
        appModel.setLable(getApplicationLabelByPackageName(appModel.getPkgName()));
    }

    private void deleteItem(int pos) {
        if (callback != null) {
            callback.onDeleteRequest(pos);
        }
    }

    @Override
    public int getItemCount() {
        return models.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView iv_delete, iv_app_icon;
        TextView tv_app_name;

        ViewHolder(View itemView) {
            super(itemView);
            iv_app_icon = itemView.findViewById(R.id.iv_app_icon);
            iv_delete = itemView.findViewById(R.id.iv_delete);
            tv_app_name = itemView.findViewById(R.id.tv_app_name);
        }

    }

    public Drawable getAppIconByPackageName(String packageName) {
        Drawable icon;
        try {
            icon = MyApplication.getmInstance().getPackageManager().getApplicationIcon(packageName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            // Get a default icon
            icon = ContextCompat.getDrawable( MyApplication.getmInstance(), R.drawable.ic_other_appname);
        }
        return icon;
    }

    // Custom method to get application label by package name
    public String getApplicationLabelByPackageName(String packageName) {
        PackageManager packageManager =MyApplication.getmInstance().getPackageManager();
        ApplicationInfo applicationInfo;
        String label = "Unknown";
        try {
            applicationInfo = packageManager.getApplicationInfo(packageName, 0);
            if (applicationInfo != null) {
                label = (String) packageManager.getApplicationLabel(applicationInfo);
            }

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return label;
    }
}