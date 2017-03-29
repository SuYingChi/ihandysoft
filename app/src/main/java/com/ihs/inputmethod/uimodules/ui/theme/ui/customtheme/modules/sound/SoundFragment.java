package com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.modules.sound;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.base.BaseThemeFragment;
import com.ihs.inputmethod.uimodules.ui.theme.ui.customtheme.base.ThemePageItem;
import com.keyboard.core.themes.custom.KCCustomThemeManager;
import com.keyboard.core.themes.custom.elements.KCBaseElement;
import com.keyboard.core.themes.custom.elements.KCSoundElement;

import java.util.Arrays;
import java.util.List;

/**
 * Created by wenbinduan on 2016/12/12.
 */

public final class SoundFragment extends BaseThemeFragment {

	private int [] colors={
			0xffff5534,
			0xfffec107,
			0xff8bc24a,
			0xff00bcd5,
			0xff5a6fee,
			0xffa646cc,
	};

	@Override
	protected ThemePageItem initiateThemePageItem() {
		List<KCSoundElement> sounds= KCCustomThemeManager.getInstance().getSoundElements();
		int index=0;
		for(KCSoundElement sound:sounds){
			sound.setBackgroundColor(colors[index++%colors.length]);
		}
		return new ThemePageItem(Arrays.<ThemePageItem.CategoryItem<?>>asList(
				new ThemePageItem.CategoryItem<>(HSApplication.getContext().getString(R.string.custom_theme_title_sound), KCSoundElement.class, new SoundProvider(this), sounds)
		));
	}
}
