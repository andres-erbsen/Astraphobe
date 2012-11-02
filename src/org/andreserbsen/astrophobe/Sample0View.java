package org.andreserbsen.astrophobe;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.util.Log;

import org.andreserbsen.astrophobe.JumpDetector;;

class Sample0View extends SampleViewBase {
	
	private static final String TAG = "Sample0View";
	int mSize;
	int[] mRGBA;
	private Bitmap mBitmap;
    private int mViewMode;
    private JumpDetector mLight;
    
    public static final int     VIEW_MODE_RGBA = 0;
    public static final int     VIEW_MODE_GRAY = 1;
    
	
    public Sample0View(Context context) {
        super(context);
        mSize = 0;
        mViewMode = VIEW_MODE_GRAY;
    }

    @Override
    protected Bitmap processFrame(byte[] data) {
		Log.i(TAG, "processFrame(..data..)");
        int frameSize = getFrameWidth() * getFrameHeight();
	    long brightness = 0;
        
        int[] rgba = mRGBA;

        final int view_mode = mViewMode;
        if (view_mode == VIEW_MODE_GRAY) {
            for (int i = frameSize-1; i >= 0; --i) {
                int y = data[i] & 0xff;
                rgba[i] = 0xff000000 | (y << 16) | (y << 8) | y;
                brightness += y;
            }
        } else if (view_mode == VIEW_MODE_RGBA) {
            for (int i = 0; i < getFrameHeight(); i++)
                for (int j = 0; j < getFrameWidth(); j++) {
                	int index = i * getFrameWidth() + j;
                	int supply_index = frameSize + (i >> 1) * getFrameWidth() + (j & ~1);
                    int y = (0xff & ((int) data[index]));
                    int u = (0xff & ((int) data[supply_index + 0]));
                    int v = (0xff & ((int) data[supply_index + 1]));
                    y = y < 16 ? 16 : y;
                    
                    float y_conv = 1.164f * (y - 16);
                    int r = Math.round(y_conv + 1.596f * (v - 128));
                    int g = Math.round(y_conv - 0.813f * (v - 128) - 0.391f * (u - 128));
                    int b = Math.round(y_conv + 2.018f * (u - 128));

                    r = r < 0 ? 0 : (r > 255 ? 255 : r);
                    g = g < 0 ? 0 : (g > 255 ? 255 : g);
                    b = b < 0 ? 0 : (b > 255 ? 255 : b);

                    rgba[i * getFrameWidth() + j] = 0xff000000 + (b << 16) + (g << 8) + r;
		    brightness += (b + g + r)/3;
                }
        }
		Log.i(TAG, "brightness(" + brightness +")");
	    mLight.add(brightness);
        
        mBitmap.setPixels(rgba, 0/* offset */, getFrameWidth() /* stride */, 0, 0, getFrameWidth(), getFrameHeight());
        return mBitmap;
    }

	@Override
	protected void onPreviewStarted(int previewWidth, int previewHeight) {
		Log.i(TAG, "onPreviewStarted("+previewWidth+", "+previewHeight+")");
		/* Create a bitmap that will be used through to calculate the image to */
        mBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Bitmap.Config.ARGB_8888);
    	mRGBA = new int[previewWidth * previewHeight];
	    mLight = new JumpDetector(10,(float) -0.05) {  
		    protected void onJump() {
				Log.i(TAG, "onJump()");
				MediaPlayer mp = MediaPlayer.create(Sample0Base.getAppContext(), R.raw.flushsound);
				mp.setOnCompletionListener(new OnCompletionListener() {
					public void onCompletion(MediaPlayer mp) {
						Log.i(TAG, "onCompletion()");
						mp.release();
					}
				});
				mp.start();
		    }
		};
	}

	@Override
	protected void onPreviewStopped() {
		Log.i(TAG, "onPreviewStopped");
		if(mBitmap != null) {
			mBitmap.recycle();
			mBitmap = null;
		}
		
		if(mRGBA != null) {
			mRGBA = null;
		}
		if(mRGBA != null) {
			mRGBA = null;
		}
	}

	public void setViewMode(int viewMode) {
		Log.i(TAG, "setViewMode("+viewMode+")");
		mViewMode = viewMode;
	}
}
