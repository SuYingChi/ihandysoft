# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Volumes/SSD/dong/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-dontwarn com.ihs.feature.**

-keepnames class com.honeycomb.launcher.model.DefaultAppFilter

-keepnames class com.honeycomb.launcher.dialog.LauncherFloatWindowManager

-keepnames class com.themelab.launcher.dialog.ThemeFloatWindowManager

-keep class com.honeycomb.launcher.desktop.allapps.AllAppsContainerView {
  public void setBackgroundAlpha(float);
}

-keep class com.honeycomb.launcher.customize.view.SuccessTickView{
  private void setTickPosition(float);
}

-keep class com.honeycomb.launcher.desktop.folder.SharedFolder {
  public void setBackgroundAlpha(float);
}

-keep class com.honeycomb.launcher.customize.wallpaper.WallpaperDrawer {
  public void setDarkMask(float);
}

-keepnames class com.honeycomb.launcher.weather.widget.WeatherClockWidget
-keepnames class com.honeycomb.launcher.desktop.widget.DesktopAdWidget

-keep class com.honeycomb.launcher.weather.HourlyForecastCurve {
  public void setProgress(float);
}

# Lucky

-keep class com.honeycomb.launcher.lucky.view.ChancesAnimationAdapter {
  public void set*(***);
}

-keep class com.honeycomb.launcher.lucky.MusicPlayer {
  public void setVolume(float);
}

-keep class com.honeycomb.launcher.lucky.view.FlyAwardBaseView {
  protected void setTranslationYProgress(float);
  protected void setFlipTranslationYProgress(float);
  protected void setFlipTranslationXProgress(float);
}

-keep class com.honeycomb.launcher.Icons { *; }


# ==== From AOSP Launcher 3 ====

-keep class com.honeycomb.launcher.desktop.DefaultFastScroller {
  public void setThumbWidth(int);
  public int getThumbWidth();
  public void setTrackWidth(int);
  public int getTrackWidth();
}

-keep class com.honeycomb.launcher.desktop.DefaultFastScrollerPopup {
  public void setAlpha(float);
  public float getAlpha();
}

-keep class com.honeycomb.launcher.desktop.BubbleTextView {
  public void setFastScrollFocus(float);
  public float getFastScrollFocus();
}

-keep class com.honeycomb.launcher.desktop.dragdrop.ButtonDropTarget {
  public int getTextColor();
}

-keep class com.honeycomb.launcher.desktop.CellLayout {
  public float getBackgroundAlpha();
  public void setBackgroundAlpha(float);
}

-keep class com.honeycomb.launcher.desktop.CellLayout$LayoutParams {
  public void setWidth(int);
  public int getWidth();
  public void setHeight(int);
  public int getHeight();
  public void setX(int);
  public int getX();
  public void setY(int);
  public int getY();
}

-keep class com.honeycomb.launcher.desktop.DragLayer$LayoutParams {
  public void setWidth(int);
  public int getWidth();
  public void setHeight(int);
  public int getHeight();
  public void setX(int);
  public int getX();
  public void setY(int);
  public int getY();
}

-keep class com.honeycomb.launcher.desktop.FastBitmapDrawable {
  public int getBrightness();
  public void setBrightness(int);
}

-keep class com.honeycomb.launcher.debug.MemoryDumpActivity {
  *;
}

-keep class com.honeycomb.launcher.desktop.PreloadIconDrawable {
  public float getAnimationProgress();
  public void setAnimationProgress(float);
}

-keep class com.honeycomb.launcher.desktop.Workspace {
  public float getBackgroundAlpha();
  public void setBackgroundAlpha(float);
}

-keep class com.honeycomb.launcher.battery.BatteryActivity {
    private void setBgColor(int);
}

# Customize
-keepclassmembers class android.support.design.internal.BottomNavigationMenuView {
    boolean mShiftingMode;
}