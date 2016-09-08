package com.phantom.onetapvideodownload.ui.downloadoptions;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.folderselector.FolderChooserDialog;
import com.phantom.onetapvideodownload.ui.MainActivity;
import com.phantom.onetapvideodownload.R;
import com.phantom.onetapvideodownload.downloader.DownloadOptionItem;
import com.phantom.utils.CheckPreferences;

import java.util.ArrayList;
import java.util.List;

public class DownloadOptionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final String FOLDER_CHOOSER_TAG = "VideoDownloadLocation";
    private List<DownloadOptionItem> mOptionList;
    private Context mContext;
    private String mDownloadLocation;

    public DownloadOptionAdapter(final Context context, List<DownloadOptionItem> optionList) {
        mContext = context;
        mDownloadLocation = CheckPreferences.getDownloadLocation(context);
        mOptionList = new ArrayList<>();
        addDefaultOptions();
        mOptionList.addAll(optionList);
    }

    private void addDefaultOptions() {
        mOptionList.add(downloadLocationOption());
    }

    private DownloadOptionItem downloadLocationOption() {
        return new DownloadOptionItem(DownloadOptionIds.DownloadLocation,
                R.drawable.directory,
                R.string.download_location,
                CheckPreferences.getDownloadLocation(mContext),
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MainActivity mainActivity = (MainActivity)mContext;
                        new FolderChooserDialog.Builder((MainActivity)mContext)
                                .chooseButton(R.string.md_choose_label)
                                .tag(FOLDER_CHOOSER_TAG)
                                .initialPath(mDownloadLocation)
                                .allowNewFolder(true, R.string.new_folder)
                                .show();
                    }
                }
        );
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public int getItemCount() {
        return mOptionList.size();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.dialog_download_file_item, parent, false);

        return new DownloadOptionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        DownloadOptionItem item = mOptionList.get(position);
        Integer resourceId = item.getIcon();
        Integer optionLabel = item.getOptionLabel();
        String optionValue = item.getOptionValue();
        DownloadOptionViewHolder vh = (DownloadOptionViewHolder) holder;
        vh.setOptionImage(resourceId);
        vh.setOptionLabel(optionLabel);
        vh.setOptionValue(optionValue);
        vh.setOptionOnClickListener(item.getOnClickListener());
    }

    public void setDownloadLocation(String directory) {
        mDownloadLocation = directory;
        DownloadOptionItem downloadOptionItem = getOptionItem(DownloadOptionIds.DownloadLocation);
        if (downloadOptionItem == null) {
            return;
        }

        downloadOptionItem.setOptionValue(directory);
        notifyDataSetChanged();
    }

    public DownloadOptionItem getOptionItem(DownloadOptionIds downloadOptionId) {
        for(DownloadOptionItem downloadOptionItem : mOptionList) {
            if (downloadOptionItem.getDownloadOptionId() == downloadOptionId) {
                return downloadOptionItem;
            }
        }

        return null;
    }

    public void setOptionItem(DownloadOptionItem downloadOptionItem) {
        for(int i=0; i < mOptionList.size(); i++) {
            if (downloadOptionItem.getDownloadOptionId() == mOptionList.get(i).getDownloadOptionId()) {
                mOptionList.set(i, downloadOptionItem);
                break;
            }
        }
        notifyDataSetChanged();
    }
}
