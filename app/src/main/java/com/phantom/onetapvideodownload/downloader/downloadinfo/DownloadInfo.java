package com.phantom.onetapvideodownload.downloader.downloadinfo;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.phantom.onetapvideodownload.R;
import com.phantom.onetapvideodownload.downloader.DownloadManager;
import com.phantom.onetapvideodownload.utils.Global;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class DownloadInfo {
    public enum Status {
        Stopped(0), Completed(1), Downloading(2), NetworkProblem(3), NetworkNotAvailable(4),
        WriteFailed(5);

        int status;
        Status(int s) {
            status = s;
        }

        int getStatus() {
            return status;
        }
    }

    public abstract String getUrl();
    public abstract String getFilename();
    public abstract String getDownloadLocation();
    public abstract long getDatabaseId();
    public abstract void setDatabaseId(long databaseId);
    public abstract Status getStatus();
    public abstract void setStatus(Status status);
    public abstract long getContentLength();
    public abstract void setContentLength(long contentLength);
    public abstract long getDownloadedLength();
    public abstract void setDownloadedLength(long downloadedLength);
    public abstract void addDownloadedLength(long additionValue);
    public abstract Integer getProgress();
    public abstract void writeToDatabase();
    public abstract void removeDatabaseEntry();
    public abstract Collection<String> getOptions();
    public abstract String getPackageName();
    public abstract void setPackageName(String packageName);

    List<String> getOptions(Context context, Status status) {
        List<String> options = new ArrayList<>();
        switch (status) {
            case Completed:
                options.add(context.getResources().getString(R.string.open));
                options.add(context.getResources().getString(R.string.share));
                options.add(context.getResources().getString(R.string.remove_from_list));
                options.add(context.getResources().getString(R.string.delete_from_storage));
                options.add(context.getResources().getString(R.string.details));
                break;
            case Stopped:
                options.add(context.getResources().getString(R.string.resume));
                options.add(context.getResources().getString(R.string.remove_from_list));
                options.add(context.getResources().getString(R.string.delete_from_storage));
                options.add(context.getResources().getString(R.string.details));
                break;
            case Downloading:
                options.add(context.getResources().getString(R.string.pause));
                options.add(context.getResources().getString(R.string.details));
                break;
        }

        return options;
    }

    public boolean handleOptionClicks(final Context context, int resourceId) {
        switch (resourceId) {
            case R.string.open:
                Global.startOpenIntent(context, getDownloadLocation());
                return true;
            case R.string.share:
                Global.startFileShareIntent(context, getDownloadLocation());
                return true;
            case R.string.delete_from_storage:
                confirmDeleteFromStorage(context);
                return true;
            case R.string.remove_from_list:
                confirmRemoveFromList(context);
                return true;
            case R.string.resume:
                context.startService(DownloadManager.getActionResumeDownload(getDatabaseId()));
                return true;
            case R.string.pause:
                context.startService(DownloadManager.getActionStopDownload(getDatabaseId()));
                return true;
            case R.string.details:
                MaterialDialog materialDialog = new MaterialDialog.Builder(context)
                        .title(R.string.details)
                        .customView(R.layout.dialog_download_details, true)
                        .positiveText(R.string.okay)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                dialog.dismiss();
                            }
                        })
                        .build();

                View materialDialogView = materialDialog.getCustomView();

                try {
                    TextView filename = (TextView)materialDialogView.findViewById(R.id.filename);
                    filename.setText(getFilename());

                    TextView totalSize = (TextView)materialDialogView.findViewById(R.id.total_size);
                    totalSize.setText(Global.getHumanReadableSize(getContentLength()));

                    TextView downloadedSize = (TextView)materialDialogView.findViewById(R.id.downloaded_size);
                    downloadedSize.setText(Global.getHumanReadableSize(getDownloadedLength()));

                    TextView downloadLocation = (TextView)materialDialogView.findViewById(R.id.download_location);
                    downloadLocation.setText(getDownloadLocation());
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }

                materialDialog.show();
                return true;
            default:
                return false;
        }
    }

    public int findIdByString(Context context, String string) {
        if (context.getResources().getString(R.string.open).equals(string)) {
            return R.string.open;
        } else if(context.getResources().getString(R.string.share).equals(string)) {
            return R.string.share;
        } else if(context.getResources().getString(R.string.resume).equals(string)) {
            return R.string.resume;
        } else if(context.getResources().getString(R.string.remove_from_list).equals(string)) {
            return R.string.remove_from_list;
        } else if(context.getResources().getString(R.string.delete_from_storage).equals(string)) {
            return R.string.delete_from_storage;
        } else if(context.getResources().getString(R.string.pause).equals(string)) {
            return R.string.pause;
        } else if(context.getResources().getString(R.string.details).equals(string)) {
            return R.string.details;
        } else {
            return -1;
        }
    }

    public void confirmDeleteFromStorage(final Context context) {
        new MaterialDialog.Builder(context)
                .title(R.string.delete_confirmation)
                .content(R.string.delete_confirmation_content)
                .positiveText(R.string.yes)
                .negativeText(R.string.no)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        context.startService(DownloadManager.getActionDeleteDownload(getDatabaseId()));
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    public void confirmRemoveFromList(final Context context) {
        new MaterialDialog.Builder(context)
                .title(R.string.remove_from_list_confirmation)
                .content(R.string.remove_from_list_confirmation_content)
                .positiveText(R.string.yes)
                .negativeText(R.string.no)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        context.startService(DownloadManager.getActionRemoveDownload(getDatabaseId()));
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    public void deleteDownloadFromStorage(Context context) {
        Global.deleteFile(context, getDownloadLocation());
    }

}
