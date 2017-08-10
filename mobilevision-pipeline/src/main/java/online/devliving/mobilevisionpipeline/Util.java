package online.devliving.mobilevisionpipeline;

import android.content.Context;
import android.content.res.Configuration;

/**
 * Created by Mehedi Hasan Khan <mehedi.mailing@gmail.com> on 8/10/17.
 */

public class Util {
    public static boolean isPortraitMode(Context context) {
        int orientation = context.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            return false;
        }
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            return true;
        }

        return false;
    }

    public interface FrameSizeProvider{
        int frameWidth();
        int frameHeight();
    }
}
