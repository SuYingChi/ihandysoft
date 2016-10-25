package com.ihs.keyboardutilslib.panelcontainer.lib;

/**
 * Created by Arthur on 16/10/21.
 */

public class PanelBean {
    private BasePanel panel;
    private boolean autoRelease;

    public PanelBean(BasePanel panel, boolean autoRelease) {
        this.panel = panel;
        this.autoRelease = autoRelease;
    }

    public BasePanel getPanel() {
        return panel;
    }

    public void setPanel(BasePanel panel) {
        this.panel = panel;
    }

    public boolean isAutoRelease() {
        return autoRelease;
    }

    public void setAutoRelease(boolean autoRelease) {
        this.autoRelease = autoRelease;
    }
}
