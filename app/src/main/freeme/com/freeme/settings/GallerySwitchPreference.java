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
import android.widget.Switch;

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
    protected View onCreateView(ViewGroup parent) {
        View view = super.onCreateView(parent);
        int switchWidget = mResources.getIdentifier("android:id/switchWidget", null, null);
        View checkableView = view.findViewById(switchWidget);
        if (checkableView != null && checkableView instanceof Switch) {
            checkableView.setClickable(true);
        }
        return view;
    }
}
