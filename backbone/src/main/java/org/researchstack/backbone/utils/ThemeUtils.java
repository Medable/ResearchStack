package org.researchstack.backbone.utils;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;

import java.util.Arrays;
import org.researchstack.backbone.R;

import java.lang.reflect.Method;

public class ThemeUtils {

    private ThemeUtils() {
    }

    public static int getTextColorPrimary(Context context) {
        TypedValue typedValue = new TypedValue();
        int[] attribute = new int[]{android.R.attr.textColorPrimary};
        TypedArray array = context.obtainStyledAttributes(typedValue.resourceId, attribute);
        int color = array.getColor(0, Color.BLACK);
        array.recycle();
        return color;
    }

    public static int getAccentColor(Context context) {
        TypedValue typedValue = new TypedValue();
        TypedArray a = context.obtainStyledAttributes(typedValue.data,
                new int[]{R.attr.colorAccent});
        int color = a.getColor(0, 0);
        a.recycle();
        return color;
    }

    public static int getPassCodeTheme(Context context) {
        TypedValue typedValue = new TypedValue();
        TypedArray a = context.obtainStyledAttributes(typedValue.data,
                new int[]{R.attr.passcodeTheme});
        int themeResId = a.getResourceId(0, 0);
        if (themeResId == 0) {
            throw new RuntimeException("Theme must define attribute passCodeTheme or extend from @style/Base.Theme.Backbone");
        }

        a.recycle();
        return themeResId;
    }

    /**
     * Helper method to get the theme resource id. Warning, accessing non-public methods is
     * a no-no and there is no guarantee this will work.
     *
     * @param context the context you want to extract the theme-resource-id from
     * @return The themeId associated w/ the context
     */
    public static int getTheme(Context context) {
        try {
            Class<?> wrapper = Context.class;
            Method method = wrapper.getMethod("getThemeResId");
            method.setAccessible(true);
            return (Integer) method.invoke(context);
        } catch (Exception e) {
            LogExt.e(ThemeUtils.class, e);
        }
        return 0;
    }

    public static Drawable getPrincipalColorButtonDrawable(Context context, int colorPrimary) {
        return getAdaptiveRippleDrawable(colorPrimary, ContextCompat.getColor(context, android.R.color.white));
    }

    private static Drawable getAdaptiveRippleDrawable(int normalColor, int pressedColor) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return new RippleDrawable(ColorStateList.valueOf(pressedColor),
                    getRippleMask(normalColor), null);
        } else {
            return getStateListDrawable(normalColor, pressedColor);
        }
    }

    private static Drawable getRippleMask(int color) {
        float[] outerRadii = new float[8];
        Arrays.fill(outerRadii, 10);

        RoundRectShape r = new RoundRectShape(outerRadii, null, null);
        ShapeDrawable shapeDrawable = new ShapeDrawable(r);
        shapeDrawable.getPaint().setColor(color);
        return shapeDrawable;
    }

    private static StateListDrawable getStateListDrawable(int normalColor, int pressedColor) {
        StateListDrawable states = new StateListDrawable();
        states.addState(new int[] {android.R.attr.state_pressed},
                new ColorDrawable(pressedColor));
        states.addState(new int[] {android.R.attr.state_focused},
                new ColorDrawable(pressedColor));
        states.addState(new int[] {android.R.attr.state_activated},
                new ColorDrawable(pressedColor));
        states.addState(new int[] {},
                new ColorDrawable(normalColor));
        return states;
    }

    public static ColorStateList getWhiteToSecondaryColorStateList(Context context, int textColor) {
        return new ColorStateList(
                new int[][] {
                        new int[] {android.R.attr.state_pressed},
                        new int[] {}
                },
                new int[] {
                        textColor,
                        ContextCompat.getColor(context, android.R.color.white)
                });
    }
}
