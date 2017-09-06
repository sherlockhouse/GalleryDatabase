/*
 * Class name: KbStyle2GallerySettings
 * 
 * Description: the settings menu of gallery
 *
 * Author: Theobald_wu, contact with wuqizhi@tydtech.com
 * 
 * Date: 2013/10   
 * 
 * Copyright (C) 2013 TYD Technology Co.,Ltd.
 * 
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.freeme.settings;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.MediaStore.Audio.Media;
import android.view.MenuItem;

import com.freeme.about.AboutAcitivity;
import com.freeme.gallery.R;
import com.freeme.utils.CustomJsonParser;

public class GallerySettings extends PreferenceActivity {
    public static final String BACK_MUSIC_ON_KEY        = "key_background_music_on";
    public static final String BACK_MUSIC_TITLE_KEY     = "key_background_music_title";
    public static final String BACK_MUSIC_PATH_KEY      = "key_background_music_path";
    public static final String SLIDESHOW_DURATION_KEY   = "key_slideshow_duration_time";
    public static final String SLIDESHOW_RAND_ORDER_KEY = "key_slideshow_rand_order";
    public static final String SLIDESHOW_REPEAT_KEY     = "key_slideshow_repeat";
    public static final String ABOUNT_KEY        = "key_about";

    public static final  boolean DEFAULT_SELECT_EXTERNAL_FIRST = true;
    public static final  String  DEFAULT_MUSIC                 = "KbStyle2GallerySettings.default_music";
    // +++
    public static final String MEDIASCANNER_IMAGE_SIZE_KEY = "scaned_image_size_setttings_key";
    //    public static final  int     DEFAULT_MUSIC_ID              = R.raw.family_love;
    private static final String  DEFAULT_MUSIC_NAME            = "爸爸去哪儿";
    private static final int REQUEST_PICK_MUSIC   = 0xfff;
    private static final int SELECT_MUSIC_DEFAULT = 0;
    private static final int SELECT_MUSIC_OTHER   = 1;
    private SwitchPreference mBackMusicPref;
    private ListPreference    mSlideshowDurPref;
    private SharedPreferences mSharedPrefs;
    private int mSelectedIndex;
    private Uri mSelectedUri = null;
    private Preference mMediaScannerImageSizeSetting;
    private Preference mAboutPrefs;
    // ---
    //*/ Added by Tyd Linguanrong for [tyd00528540] update image size, 2014-7-18
    private ContentObserver mImageSizeObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            updateMediaScannerImageSizeSummary();
        }
    };

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //*/ Modified by Tyd Linguanrong for Gallery new style, 2014-4-17
        ActionBar actionbar = getActionBar();
        actionbar.setDisplayShowTitleEnabled(true);
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setDisplayUseLogoEnabled(true);
//        mActionBar.setLogo(com.android.internal.R.drawable.ic_app_gallery);
        //*/

        setTitle(R.string.gallery_settings);
        addPreferencesFromResource(R.xml.gallery_settings);

        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        mBackMusicPref = (SwitchPreference) findPreference(BACK_MUSIC_ON_KEY);
//        boolean on = CustomJsonParser.getInstance().getCustomConfig()
//                .isSlideshow_background_music_on();
//        mBackMusicPref.setChecked(on);
        mSlideshowDurPref = (ListPreference) findPreference(SLIDESHOW_DURATION_KEY);
        mSlideshowDurPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                updateDurationSummary((String) newValue);
                return true;
            }
        });
        mAboutPrefs = findPreference(ABOUNT_KEY);

        // +++
        mMediaScannerImageSizeSetting = findPreference(MEDIASCANNER_IMAGE_SIZE_KEY);
        // ---

        init();

        /*/ Added by Tyd Linguanrong for [tyd00528540] update image size, 2014-7-18
        this.getContentResolver().registerContentObserver(
                Settings.System.getUriFor(Settings.System.TYD_SCAN_IMAGE_THRESHOLD),
                true, mImageSizeObserver);
        //*/
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //this.getContentResolver().unregisterContentObserver(mImageSizeObserver);
    }
    //*/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_PICK_MUSIC && resultCode == RESULT_OK) {
            Uri uri = data.getData();

            Cursor cursor = getContentResolver().query(uri,
                    new String[]{Media.TITLE, Media.DATA}, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    // pick new music
                    int titleIndex = cursor.getColumnIndex(Media.TITLE);
                    int pathIndex = cursor.getColumnIndex(Media.DATA);
                    String title = cursor.getString(titleIndex);
                    String path = cursor.getString(pathIndex);

                    //*/ Added by Tyd Linguanrong for Gallery new style, 2014-4-24
                    if (title == null) {
                        String where = "_data = '" + path + "'";
                        Cursor cursor_re = getContentResolver().query(Media.EXTERNAL_CONTENT_URI,
                                new String[]{Media.TITLE}, where, null, null);
                        try {
                            if (cursor_re != null && cursor_re.moveToFirst()) {
                                titleIndex = cursor_re.getColumnIndex(Media.TITLE);
                                title = cursor_re.getString(titleIndex);
                            }
                        } finally {
                            if (cursor_re != null) {
                                cursor_re.close();
                            }
                        }
                    }
                    //*/

                    mBackMusicPref.setSummary(title);
                    SharedPreferences.Editor editor = mSharedPrefs.edit();
                    editor.putString(BACK_MUSIC_PATH_KEY, path);
                    editor.putString(BACK_MUSIC_TITLE_KEY, title);
                    editor.commit();
                    mSelectedIndex = SELECT_MUSIC_OTHER;
                    mSelectedUri = uri;
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
    //*/

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
                                         Preference preference) {
        if (preference == mBackMusicPref) {
            if (!DEFAULT_SELECT_EXTERNAL_FIRST) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.select_music_title);
                builder.setSingleChoiceItems(R.array.select_music_list,
                        mSelectedIndex, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == SELECT_MUSIC_DEFAULT && which != mSelectedIndex) {
                                    // default
                                    mBackMusicPref.setSummary(DEFAULT_MUSIC_NAME);
                                    SharedPreferences.Editor editor = mSharedPrefs.edit();
                                    editor.putString(BACK_MUSIC_PATH_KEY, DEFAULT_MUSIC);
                                    editor.putString(BACK_MUSIC_TITLE_KEY, DEFAULT_MUSIC_NAME);
                                    editor.commit();
                                    mSelectedIndex = SELECT_MUSIC_DEFAULT;
                                    mSelectedUri = null;
                                } else if (which == SELECT_MUSIC_OTHER) {
                                    startMusicPicker();
                                }

                                dialog.dismiss();
                            }
                        });
                builder.setNegativeButton(android.R.string.cancel, null);
                builder.create().show();
            } else {
                startMusicPicker();
            }
        }else if(preference == mAboutPrefs){
            Intent intent = new Intent();
            intent.setClass(this, AboutAcitivity.class);
            startActivity(intent);
        }

        return true;
    }

    private void startMusicPicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, mSelectedUri);
        startActivityForResult(intent, REQUEST_PICK_MUSIC);
    }

    private void updateDurationSummary(String value) {
        CharSequence[] summaries = mSlideshowDurPref.getEntries();
        CharSequence[] values = mSlideshowDurPref.getEntryValues();
        /*Log.i("TYD_DEBUG", "updateDurationSummary(): value = " + value
                + ", summaries = " + summaries.length
                + ", values = " + values.length);*/

        for (int i = 0; i < values.length; i++) {
            if (values[i].equals(value)) {
                mSlideshowDurPref.setSummary(summaries[i]);
                break;
            }
        }
    }

    private void init() {
        mSelectedUri = getSelectedUri(this);

        // update music title summary
        String title = mSharedPrefs.getString(BACK_MUSIC_TITLE_KEY, DEFAULT_MUSIC_NAME);
        if (!(DEFAULT_SELECT_EXTERNAL_FIRST && DEFAULT_MUSIC_NAME.equals(title))
                && mSelectedUri != null) {
            mBackMusicPref.setSummary(title);
        }

        if (!DEFAULT_SELECT_EXTERNAL_FIRST) {
            if (DEFAULT_MUSIC_NAME.equals(title)) {
                mSelectedIndex = SELECT_MUSIC_DEFAULT;
            } else {
                mSelectedIndex = SELECT_MUSIC_OTHER;
            }
        }
    }

    public static Uri getSelectedUri(Context context) {
        Uri selectedUri = null;
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        String selectedPath = sharedPrefs.getString(BACK_MUSIC_PATH_KEY, DEFAULT_MUSIC);
        Cursor cursor = context.getContentResolver().query(Media.EXTERNAL_CONTENT_URI,
                new String[]{Media._ID, Media.TITLE, Media.DATA}, null, null, null);

        //*/ Added by Linguanrong for avoid exception, 2014-11-15
        if (cursor == null) {
            return null;
        }
        //*/

        try {
            if (DEFAULT_MUSIC.equals(selectedPath)) {
                if (DEFAULT_SELECT_EXTERNAL_FIRST && cursor.moveToFirst()) {
                    long id = cursor.getLong(cursor.getColumnIndex(Media._ID));
                    selectedUri = ContentUris.withAppendedId(Media.EXTERNAL_CONTENT_URI, id);

                    String title = cursor.getString(cursor.getColumnIndex(Media.TITLE));
                    String path = cursor.getString(cursor.getColumnIndex(Media.DATA));
                    SharedPreferences.Editor editor = sharedPrefs.edit();
                    editor.putString(BACK_MUSIC_PATH_KEY, path);
                    editor.putString(BACK_MUSIC_TITLE_KEY, title);
                    editor.commit();
                }
            } else {
                while (cursor.moveToNext()) {
                    int pathIndex = cursor.getColumnIndex(Media.DATA);
                    String path = cursor.getString(pathIndex);
                    if (selectedPath.equals(path)) {
                        long id = cursor.getLong(cursor.getColumnIndex(Media._ID));
                        selectedUri = ContentUris.withAppendedId(Media.EXTERNAL_CONTENT_URI, id);
                        break;
                    }
                }
            }
        } finally {
            cursor.close();
        }

        return selectedUri;
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateDurationSummary(mSlideshowDurPref.getValue());
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

//        if (hasFocus) {
//            updateMediaScannerImageSizeSummary();
//        }
    }

    //*/ Added by Tyd Linguanrong for Gallery new style, 2014-4-17
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // +++
    private void updateMediaScannerImageSizeSummary() {
        final Resources r = getResources();
        final int imageThresholdSize = 50;// (Settings.System.getInt(
//        		getContentResolver(),
//        		Settings.System.TYD_SCAN_IMAGE_THRESHOLD,
//        		r.getInteger(R.integer.pref_seekbar_value_min)) >>> 10);
        mMediaScannerImageSizeSetting.setSummary(
                r.getString(R.string.pref_scanner_min_image_size_summay,
                        imageThresholdSize));
    }
    // ---
}
