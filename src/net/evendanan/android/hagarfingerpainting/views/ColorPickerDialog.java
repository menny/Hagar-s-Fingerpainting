/*
 * Copyright (C) 2011 Menny Even Danan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
http://blog.evendanan.net/2011/08/Fingerpainting-app-for-Hagar-OR-Multitouch-sample-code
*/

package net.evendanan.android.hagarfingerpainting.views;

import android.os.Bundle;
import android.app.Dialog;
import android.content.Context;

public class ColorPickerDialog extends Dialog {

    public interface OnColorChangedListener {
        void colorChanged(int color, int index);

		int getCurrentColor(int pointerIndex);
    }

    private OnColorChangedListener mListener;
    private int mInitialColor;
    private int mPointerIndex;
    
    public ColorPickerDialog(Context context,
                             OnColorChangedListener listener,
                             int pointerIndex) {
        super(context);

        mListener = listener;
        mInitialColor = mListener.getCurrentColor(pointerIndex);
        mPointerIndex = pointerIndex;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OnColorChangedListener l = new OnColorChangedListener() {
            public void colorChanged(int color, int index) {
                mListener.colorChanged(color, mPointerIndex);
                dismiss();
            }
            
            @Override
            public int getCurrentColor(int pointerIndex) {
            	return mListener.getCurrentColor(pointerIndex);
            }
        };

        setContentView(new ColorPickerView(getContext(), l, mInitialColor));
        setTitle("Pick a Color");
    }
}
