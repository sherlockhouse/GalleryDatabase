/*
 * Class name: KbStyle2SwitchPreference
 * 
 * Description: customer switch preference
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

import android.content.Context;
import android.content.res.Resources;
import android.preference.SwitchPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Checkable;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.freeme.gallery.R;
import com.freeme.gallery.app.Gallery;

public class GallerySwitchPreference extends SwitchPreference {

    private Resources mResources;

    /**
     * Construct a new SwitchPreference with default style options.
     *
     * @param context The Context that will style this preference
     */
    public GallerySwitchPreference(Context context) {
        this(context, null);
    }

    /**
     * Construct a new SwitchPreference with the given style options.
     *
     * @param context The Context that will style this preference
     * @param attrs   Style attributes that differ from the default
     */
    public GallerySwitchPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        mResources = context.getResources();
    }

    @Override
    protected void onClick() {
        // do nothing
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        View checkableView = view.findViewById(R.id.switchWidget);
        if (checkableView != null && checkableView instanceof Switch) {
            checkableView.setClickable(true);
            checkableView.setFocusable(false);
            boolean tt = isChecked();
            ((Checkable) checkableView).setChecked(tt);
            ((Switch) checkableView).setOnCheckedChangeListener(mListener);
        }
    }

    private final Listener mListener = new Listener();
    private class Listener implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (!callChangeListener(isChecked)) {
                // Listener didn't like it, change it back.
                // CompoundButton will make sure we don't recurse.
                buttonView.setChecked(!isChecked);
                return;
            }

            GallerySwitchPreference.this.setChecked(isChecked);
        }
    }
}
