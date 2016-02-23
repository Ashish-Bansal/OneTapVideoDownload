package com.phantom.onetapvideodownload.Video;

import android.content.Context;
import android.support.v4.util.Pair;
import android.support.v4.util.SparseArrayCompat;
import android.text.InputType;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.phantom.onetapvideodownload.DownloadOptionAdapter;
import com.phantom.onetapvideodownload.DownloadOptionIds;
import com.phantom.onetapvideodownload.Global;
import com.phantom.onetapvideodownload.MainActivity;
import com.phantom.onetapvideodownload.R;
import com.phantom.onetapvideodownload.downloader.DownloadOptionItem;

import java.util.ArrayList;
import java.util.List;

public class YoutubeVideo implements Video {
    private String mTitle, mParam;
    private long mDatabaseId = -1;
    public static List<Pair<Integer, String>> itagQualityMapping = new ArrayList<>();
    public static List<Pair<Integer, String>> itagExtensionMapping = new ArrayList<>();
    private Context mContext;

    static {
        itagQualityMapping.add(Pair.create(22, "MP4 - 720p"));
        itagQualityMapping.add(Pair.create(18, "MP4 - 360p"));
        itagQualityMapping.add(Pair.create(43, "WebM - 360p"));
        itagQualityMapping.add(Pair.create(36, "3GB - 240p"));
        itagQualityMapping.add(Pair.create(5, "FLV - 240p"));
        itagQualityMapping.add(Pair.create(17, "3GP - 144p"));
        itagQualityMapping.add(Pair.create(141, "M4A - 256 kbit/s (Audio)"));
        itagQualityMapping.add(Pair.create(140, "M4A - 128 kbit/s (Audio)"));
        itagQualityMapping.add(Pair.create(251, "WebM - 160 kbit/s"));
        itagQualityMapping.add(Pair.create(171, "WebM - 128 kbit/s"));
        itagQualityMapping.add(Pair.create(250, "WebM - 64 kbit/s"));
        itagQualityMapping.add(Pair.create(249, "WebM - 48 kbit/s"));


        itagExtensionMapping.add(Pair.create(22, "mp4"));
        itagExtensionMapping.add(Pair.create(18, "mp4"));
        itagExtensionMapping.add(Pair.create(43, "webm"));
        itagExtensionMapping.add(Pair.create(36, "3gp"));
        itagExtensionMapping.add(Pair.create(5, "flv"));
        itagExtensionMapping.add(Pair.create(17, "3gp"));
        itagExtensionMapping.add(Pair.create(141, "m4a"));
        itagExtensionMapping.add(Pair.create(140, "m4a"));
        itagExtensionMapping.add(Pair.create(251, "webm"));
        itagExtensionMapping.add(Pair.create(171, "webm"));
        itagExtensionMapping.add(Pair.create(250, "webm"));
        itagExtensionMapping.add(Pair.create(249, "webm"));
    }

    public class Format {
        public int itag;
        public String url;
        public boolean dashAudio;
    }

    private SparseArrayCompat<Format> mFormatList = new SparseArrayCompat<>();

    public YoutubeVideo(Context context, String title, String param) {
        mContext = context;
        mTitle = title;
        mParam = param;
    }

    public void addFormat(String videoUrl, int itag) {
        Format format = new Format();
        format.url = videoUrl;
        format.itag = itag;
        format.dashAudio = false;
        for (Pair p : itagQualityMapping) {
            if (p.first == itag && p.second.toString().contains("kbit")) {
                format.dashAudio = true;
            }
        }
        mFormatList.put(itag, format);
    }

    @Override
    public String getTitle() {
        return mTitle;
    }

    @Override
    public String getUrl() {
        return getBestVideoFormat().url;
    }

    @Override
    public long getDatabaseId() {
        return mDatabaseId;
    }

    @Override
    public void setDatabaseId(long databaseId) {
        mDatabaseId = databaseId;
    }

    public String getParam() {
        return mParam;
    }

    public static String getFormatDescription(Integer itag) {
        for (Pair<Integer, String> p : itagQualityMapping) {
            if (p.first.equals(itag)) {
                return p.second;
            }
        }

        return "";
    }

    public static Integer getItagForDescription(String description) {
        for (Pair<Integer, String> p : itagQualityMapping) {
            if (p.second.equals(description)) {
                return p.first;
            }
        }

        return -1;
    }

    public String getVideoUrl(int itag) {
        Format format = mFormatList.get(itag);
        if (format == null) {
            return "";
        }

        return format.url;
    }

    public boolean urlsForbidden() {
        return mFormatList.size() <= 0 && Global.isResourceAvailable(mFormatList.get(0).url);
    }

    public Format getBestVideoFormat() {
        for (Pair<Integer, String> p : itagQualityMapping) {
            if (p.second.contains("kbit")) {
                continue;
            }

            Format format = mFormatList.get(p.first);
            if (format != null) {
                return format;
            }
        }
        return null;
    }

    public Format getBestAudioFormat() {
        for (Pair<Integer, String> p : itagQualityMapping) {
            if (!p.second.contains("kbit")) {
                continue;
            }

            Format format = mFormatList.get(p.first);
            if (format != null) {
                return format;
            }
        }
        return null;
    }

    public ArrayList<Format> getAllFormats() {
        ArrayList<Format> formats = new ArrayList<>();
        for (Pair<Integer, String> p : itagQualityMapping) {
            Format format = mFormatList.get(p.first);
            if (format != null) {
                formats.add(format);
            }
        }
        return formats;
    }

    public static String getExtensionForItag(Integer itag) {
        for (Pair<Integer, String> p : itagExtensionMapping) {
            if (p.first.equals(itag)) {
                return p.second;
            }
        }

        return "";
    }

    @Override
    public void setContext(Context context) {
        mContext = context;
    }

    @Override
    public List<DownloadOptionItem> getOptions() {
        List<DownloadOptionItem> options = new ArrayList<>();
        options.add(new DownloadOptionItem(DownloadOptionIds.Filename,
                R.drawable.file,
                R.string.filename,
                Global.getValidatedFilename(getTitle()),
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final DownloadOptionAdapter downloadOptionAdapter =
                                MainActivity.getDownloadOptionAdapter();
                        final DownloadOptionItem filenameOptionItem = downloadOptionAdapter.getOptionItem(DownloadOptionIds.Filename);
                                new MaterialDialog.Builder(mContext)
                                .title(R.string.enter_filename)
                                .inputType(InputType.TYPE_CLASS_TEXT)
                                .input("", filenameOptionItem.getOptionValue(), new MaterialDialog.InputCallback() {
                                    @Override
                                    public void onInput(MaterialDialog dialog, CharSequence input) {
                                        filenameOptionItem.setOptionValue(Global.getValidatedFilename(input.toString()));
                                        downloadOptionAdapter.setOptionItem(filenameOptionItem);
                                    }
                                }).show();
                    }
                }
        ));

        options.add(new DownloadOptionItem(DownloadOptionIds.Format,
                R.drawable.quality,
                R.string.video_quality,
                getFormatDescription(getBestVideoFormat().itag),
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ArrayList<Format> formatArrayList = getAllFormats();
                        List<String> displayStrings = new ArrayList<>();
                        for(Format f : formatArrayList) {
                            int itag = f.itag;
                            String formatDescription = getFormatDescription(itag);
                            if (formatDescription.isEmpty()) {
                                continue;
                            }

                            displayStrings.add(formatDescription);
                        }

                        new MaterialDialog.Builder(mContext)
                                .title(R.string.video_quality)
                                .items(displayStrings)
                                .itemsCallback(new MaterialDialog.ListCallback() {
                                    @Override
                                    public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                        DownloadOptionAdapter downloadOptionAdapter =
                                                MainActivity.getDownloadOptionAdapter();
                                        DownloadOptionItem formatOptionItem = downloadOptionAdapter.getOptionItem(DownloadOptionIds.Format);
                                        formatOptionItem.setOptionValue(text.toString());
                                        downloadOptionAdapter.setOptionItem(formatOptionItem);
                                    }
                                })
                                .show();
                    }
                }
        ));

        return options;
    }
}
